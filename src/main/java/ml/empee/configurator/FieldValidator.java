package ml.empee.configurator;

import java.lang.reflect.Field;
import java.util.List;
import ml.empee.configurator.annotations.Max;
import ml.empee.configurator.annotations.Min;
import ml.empee.configurator.annotations.Path;
import ml.empee.configurator.annotations.RegEx;
import ml.empee.configurator.annotations.Required;
import ml.empee.configurator.exceptions.MisconfigurationException;

public final class FieldValidator {

  private final String filePath;
  private final Field field;
  private final String fieldPath;
  private final Object value;

  public static void validate(String filePath, Field field, Object value) {
    FieldValidator validator = new FieldValidator(
        filePath, field, value
    );

    validator.validateField();
  }
  private FieldValidator(String filePath, Field field, Object value) {
    this.filePath = filePath;
    this.field = field;
    this.fieldPath = field.getAnnotation(Path.class).value();
    this.value = value;
  }

  public void validateField() {
    if (value == null) {
      if(field.isAnnotationPresent(Required.class)) {
        throw MisconfigurationException.builder()
            .filePath(filePath)
            .configPath(fieldPath)
            .message("Required field is missing")
            .build();
      }
    } else {
      if(value instanceof String) {
        validateString();
      } else if (value instanceof Number) {
        validateNumber();
      } else if(value instanceof List) {
        validateList();
      }
    }
  }

  private void validateString() {
    RegEx regex = field.getAnnotation(RegEx.class);
    if(regex == null || ((String) value).matches(regex.value())) {
      return;
    }

    throw MisconfigurationException.builder()
        .filePath(filePath)
        .configPath(fieldPath)
        .message("The value '" + value + "' must match the regex " + regex.value())
        .build();
  }

  private void validateNumber() {
    Min min = field.getAnnotation(Min.class);
    if(min != null && ((Number) value).doubleValue() < min.value()) {
      throw MisconfigurationException.builder()
          .filePath(filePath)
          .configPath(fieldPath)
          .message("The value '" + value + "' must be greater than " + min.value())
          .build();
    }

    Max max = field.getAnnotation(Max.class);
    if(max != null && ((Number) value).doubleValue() > max.value()) {
      throw MisconfigurationException.builder()
          .filePath(filePath)
          .configPath(fieldPath)
          .message("The value '" + value + "' must be lower than " + max.value())
          .build();
    }
  }

  private void validateList() {
    for(Object element : (List<?>) value) {
      FieldValidator.validate(filePath, field, element);
    }
  }

}
