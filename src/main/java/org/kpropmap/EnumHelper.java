package org.kpropmap;

/**
 * To create Enum's through reflection. Can't do this directly in Kotlin for some reason. See
 * https://discuss.kotlinlang.org/t/confusing-type-error/506/4.
 */
public class EnumHelper {
  public static <T extends Enum<?>> T makeEnum(Class<T> cls, String str) {
    for (T each : cls.getEnumConstants()) {
      if (each.name().equalsIgnoreCase(str)) {
        return each;
      }
    }
    throw new IllegalArgumentException("No case insensitive enum constant [" + cls.getName() + "." + str + "]");
  }
}
