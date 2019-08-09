package com.example.preferenceroom_compiler_kotlin;

import androidx.annotation.NonNull;

import com.google.common.base.CaseFormat;

@SuppressWarnings("WeakerAccess")
public class StringUtils {
  public static String toUpperCamel(String text) {
    if (isNullOrEmpty(text)) return text;
    if(text.contains("_")) {
      text = text.toLowerCase();
      return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, text);
    }
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, text);
  }

  public static String getErrorMessagePrefix() {
    return "\n==================== <ERROR LOG> ====================\n";
  }

  public static String toLowerCamelCase(@NonNull String text) {
    if (isNullOrEmpty(text)) return text;
    if(text.contains("_")) {
      text = text.toLowerCase();
      return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, text);
    }
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, text);
  }

  public static boolean isNullOrEmpty(String string) {
    return string == null || string.equals("");
  }
}
