package name.remal.github_actions.common.lifecycle_processor;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static javax.lang.model.SourceVersion.latest;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.tools.Diagnostic.Kind.ERROR;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.github_actions.common.lifecycle.MainLifecycle;
import name.remal.github_actions.common.lifecycle.PostMainLifecycle;
import name.remal.github_actions.common.lifecycle.PreMainLifecycle;

@AutoService(Processor.class)
public class LifecycleProcessor extends AbstractProcessor {

    private static final Set<Class<? extends Annotation>> SUPPORTED_ANNOTATIONS = Set.of(
        PreMainLifecycle.class,
        MainLifecycle.class,
        PostMainLifecycle.class
    );

    private boolean processed;

    @Override
    @SneakyThrows
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (processed) {
            return false;
        } else {
            processed = true;
        }

        for (val annotationClass : SUPPORTED_ANNOTATIONS) {
            val mainMethodBuilder = methodBuilder("main")
                .addModifiers(PUBLIC, STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addException(Throwable.class);

            val annotatedMethods = getAnnotatedLifecycleElements(annotationClass, roundEnv);
            for (val annotatedMethod : annotatedMethods) {
                mainMethodBuilder.addStatement(
                    "$T.$N()",
                    annotatedMethod.getEnclosingElement(),
                    annotatedMethod.getSimpleName()
                );
            }

            val javaClass = classBuilder(annotationClass.getSimpleName())
                .addAnnotation(AnnotationSpec.builder(Generated.class)
                    .addMember("value", "$S", LifecycleProcessor.class.getName())
                    .build()
                )
                .addMethod(mainMethodBuilder.build())
                .addStaticBlock(CodeBlock.of(
                    "$N.$N();",
                    "name.remal.github_actions.common.logging.LoggingConfigurer",
                    "configureLogging"
                ))
                .build();

            val javaFile = JavaFile.builder("lifecycle", javaClass)
                .skipJavaLangImports(true)
                .indent("    ")
                .build();

            javaFile.writeTo(processingEnv.getFiler());
        }

        return false;
    }

    @SneakyThrows
    private List<ExecutableElement> getAnnotatedLifecycleElements(
        Class<? extends Annotation> annotationClass,
        RoundEnvironment roundEnv
    ) {
        return roundEnv.getElementsAnnotatedWith(annotationClass).stream()
            .filter(element -> {
                if (element.getKind() != METHOD) {
                    processingEnv.getMessager().printMessage(
                        ERROR,
                        "Only methods can be annotated with " + annotationClass,
                        element
                    );
                    return false;
                }
                return true;
            })
            .map(ExecutableElement.class::cast)
            .filter(element -> {
                val enclosingElement = element.getEnclosingElement();
                if ((enclosingElement.getKind() != CLASS && enclosingElement.getKind() != INTERFACE)
                    || enclosingElement.getEnclosingElement().getKind() != PACKAGE
                    || !enclosingElement.getModifiers().contains(PUBLIC)
                ) {
                    processingEnv.getMessager().printMessage(
                        ERROR,
                        "Methods annotated with " + annotationClass + " must be defined in top-level public classes",
                        element
                    );
                    return false;
                }
                return true;
            })
            .filter(element -> {
                if (!element.getModifiers().contains(PUBLIC)
                    || !element.getModifiers().contains(STATIC)
                ) {
                    processingEnv.getMessager().printMessage(
                        ERROR,
                        "Methods methods annotated with " + annotationClass + " must be public static",
                        element
                    );
                    return false;
                }
                return true;
            })
            .filter(element -> {
                if (element.getReturnType().getKind() != VOID) {
                    processingEnv.getMessager().printMessage(
                        ERROR,
                        "Methods methods annotated with " + annotationClass + " must return void",
                        element
                    );
                    return false;
                }
                return true;
            })
            .filter(element -> {
                if (!element.getParameters().isEmpty()) {
                    processingEnv.getMessager().printMessage(
                        ERROR,
                        "Methods methods annotated with " + annotationClass + " must not have parameters",
                        element
                    );
                    return false;
                }
                return true;
            })
            .sorted(comparing(element -> getOrder(element.getAnnotation(annotationClass))))
            .toList();
    }

    @SneakyThrows
    private static int getOrder(Annotation annotation) {
        return (Integer) annotation.annotationType().getMethod("order").invoke(annotation);
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return SUPPORTED_ANNOTATIONS.stream()
            .map(Class::getName)
            .collect(toUnmodifiableSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latest();
    }

}
