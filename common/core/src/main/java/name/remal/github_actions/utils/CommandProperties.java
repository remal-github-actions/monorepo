package name.remal.github_actions.utils;

import static java.beans.Introspector.getBeanInfo;
import static java.util.Collections.unmodifiableMap;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.val;

public interface CommandProperties {

    @SneakyThrows
    default Map<String, Object> asMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        val beanInfo = getBeanInfo(this.getClass());
        for (val prop : beanInfo.getPropertyDescriptors()) {
            val readMethod = prop.getReadMethod();
            if (readMethod != null) {
                val value = readMethod.invoke(this);
                if (value != null) {
                    result.put(prop.getName(), value);
                }
            }
        }
        return unmodifiableMap(result);
    }


    static CommandProperties fromMap(Map<String, Object> map) {
        return new CommandProperties() {
            @Override
            public Map<String, Object> asMap() {
                return map;
            }
        };
    }

    static CommandProperties fromMap(
        String key1, Object value1
    ) {
        return fromMap(Map.of(key1, value1));
    }

}
