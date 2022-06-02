package name.remal.github_actions.utils;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static lombok.AccessLevel.PRIVATE;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import javax.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Contract;

@NoArgsConstructor(access = PRIVATE)
public abstract class Validation {

    @SuppressWarnings("resource")
    private static final Validator VALIDATOR = buildDefaultValidatorFactory().getValidator();

    @Contract("_->param1")
    public static <T> T validate(T object) {
        return validate(object, null);
    }

    @Contract("_,_->param1")
    public static <T> T validate(T object, @Nullable String description) {
        val constraintViolations = VALIDATOR.validate(object);
        if (!constraintViolations.isEmpty()) {
            if (description == null || description.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            } else {
                throw new ValidationException(
                    description,
                    new ConstraintViolationException(constraintViolations)
                );
            }
        }
        return object;
    }

}
