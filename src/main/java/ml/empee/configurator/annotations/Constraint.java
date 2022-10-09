package ml.empee.configurator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Constraint {

  String min() default "";

  String max() default "";

  String pattern() default "";

  boolean notBlank() default false;

  int length() default Integer.MAX_VALUE;

}
