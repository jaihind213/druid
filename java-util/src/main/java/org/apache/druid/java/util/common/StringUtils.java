/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.java.util.common;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * As of OpenJDK / Oracle JDK 8, the JVM is optimized around String charset variable instead of Charset passing, that
 * is exploited in {@link #toUtf8(String)} and {@link #fromUtf8(byte[])}.
 */
public class StringUtils
{
  public static final byte[] EMPTY_BYTES = new byte[0];
  @Deprecated // Charset parameters to String are currently slower than the charset's string name
  public static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
  public static final String UTF8_STRING = StandardCharsets.UTF_8.toString();

  // should be used only for estimation
  // returns the same result with StringUtils.fromUtf8(value).length for valid string values
  // does not check validity of format and returns over-estimated result for invalid string (see UT)
  public static int estimatedBinaryLengthAsUTF8(String value)
  {
    int length = 0;
    for (int i = 0; i < value.length(); i++) {
      char var10 = value.charAt(i);
      if (var10 < 0x80) {
        length += 1;
      } else if (var10 < 0x800) {
        length += 2;
      } else if (Character.isSurrogate(var10)) {
        length += 4;
        i++;
      } else {
        length += 3;
      }
    }
    return length;
  }

  public static byte[] toUtf8WithNullToEmpty(final String string)
  {
    return string == null ? EMPTY_BYTES : toUtf8(string);
  }

  public static String fromUtf8(final byte[] bytes)
  {
    try {
      return new String(bytes, UTF8_STRING);
    }
    catch (UnsupportedEncodingException e) {
      // Should never happen
      throw Throwables.propagate(e);
    }
  }

  public static String fromUtf8(final ByteBuffer buffer, final int numBytes)
  {
    final byte[] bytes = new byte[numBytes];
    buffer.get(bytes);
    return fromUtf8(bytes);
  }

  public static String fromUtf8(final ByteBuffer buffer)
  {
    return StringUtils.fromUtf8(buffer, buffer.remaining());
  }

  public static byte[] toUtf8(final String string)
  {
    try {
      return string.getBytes(UTF8_STRING);
    }
    catch (UnsupportedEncodingException e) {
      // Should never happen
      throw Throwables.propagate(e);
    }
  }

  @Nullable
  public static byte[] toUtf8Nullable(@Nullable final String string)
  {
    if (string == null) {
      return null;
    }
    return toUtf8(string);
  }

  /**
   * Equivalent of String.format(Locale.ENGLISH, message, formatArgs).
   */
  public static String format(String message, Object... formatArgs)
  {
    return String.format(Locale.ENGLISH, message, formatArgs);
  }

  /**
   * Formats the string as {@link #format(String, Object...)}, but instead of failing on illegal format, returns the
   * concatenated format string and format arguments. Should be used for unimportant formatting like logging,
   * exception messages, typically not directly.
   */
  public static String nonStrictFormat(String message, Object... formatArgs)
  {
    if (formatArgs == null || formatArgs.length == 0) {
      return message;
    }
    try {
      return String.format(Locale.ENGLISH, message, formatArgs);
    }
    catch (IllegalFormatException e) {
      StringBuilder bob = new StringBuilder(message);
      for (Object formatArg : formatArgs) {
        bob.append("; ").append(formatArg);
      }
      return bob.toString();
    }
  }

  public static String toLowerCase(String s)
  {
    return s.toLowerCase(Locale.ENGLISH);
  }

  public static String toUpperCase(String s)
  {
    return s.toUpperCase(Locale.ENGLISH);
  }

  public static String removeChar(String s, char c)
  {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == c) {
        return removeChar(s, c, i);
      }
    }
    return s;
  }

  public static String urlEncode(String s)
  {
    try {
      return URLEncoder.encode(s, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private static String removeChar(String s, char c, int firstOccurranceIndex)
  {
    StringBuilder sb = new StringBuilder(s.length() - 1);
    sb.append(s, 0, firstOccurranceIndex);
    for (int i = firstOccurranceIndex + 1; i < s.length(); i++) {
      char charOfString = s.charAt(i);
      if (charOfString != c) {
        sb.append(charOfString);
      }
    }
    return sb.toString();
  }

  /**
   * Returns the given string if it is non-null; the empty string otherwise.
   * This method should only be used at places where null to empty conversion is
   * irrelevant to null handling of the data.
   *
   * @param string the string to test and possibly return
   *
   * @return {@code string} itself if it is non-null; {@code ""} if it is null
   */
  public static String nullToEmptyNonDruidDataString(@Nullable String string)
  {
    //CHECKSTYLE.OFF: Regexp
    return Strings.nullToEmpty(string);
    //CHECKSTYLE.ON: Regexp
  }

  /**
   * Returns the given string if it is nonempty; {@code null} otherwise.
   * This method should only be used at places where null to empty conversion is
   * irrelevant to null handling of the data.
   *
   * @param string the string to test and possibly return
   *
   * @return {@code string} itself if it is nonempty; {@code null} if it is
   * empty or null
   */
  @Nullable
  public static String emptyToNullNonDruidDataString(@Nullable String string)
  {
    //CHECKSTYLE.OFF: Regexp
    return Strings.emptyToNull(string);
    //CHECKSTYLE.ON: Regexp
  }
}
