package ml.empee.configurator.annotations;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import ml.empee.configurator.Config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConfigField {

    @Getter
    private final Constraint constraints;
    private Method fieldSetter = null;

    @Getter(AccessLevel.PACKAGE)
    private Class<?> parameterClass = null;

    @Getter
    private final String configPath;

    @Getter
    private final boolean required;

    @Builder
    public ConfigField(Class<? extends Config> configClass, Field field, String configPath, boolean required) {        
        for(Method method : configClass.getDeclaredMethods()) {

            if(method.getName().equalsIgnoreCase("set" + field.getName())) {
                Class<?>[] parametersTypes = method.getParameterTypes();
                if(parametersTypes.length == 1) {
                    this.fieldSetter = method;
                    this.parameterClass = parametersTypes[0];
                    break;
                }
            }

        }

        if(fieldSetter == null || parameterClass == null) {
            throw new IllegalArgumentException("The field " + field.getName() + "of class " + configClass.getName() + " must have a setter method!");
        }

        this.constraints = field.getAnnotation(Constraint.class);
        this.configPath = configPath;
        this.required = required;
    }

    public void setField(Object instance, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        fieldSetter.setAccessible(true);
        fieldSetter.invoke(instance, value);
    }


}
