package kg.attractor.java.server;

import lesson45.Utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class Cookie {
  private final String name;
  private final String value;
  private int maxAge;
  private boolean httpOnly;

  public Cookie(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public static Cookie make(String name, String value) {
    return new Cookie(name, value);
  }

  public static Map<String, String> parse(String cookieString) {
    return Utils.parseUrlEncoded(cookieString);
  }

  public Cookie setMaxAge(int maxAge) {
    this.maxAge = maxAge;
    return this;
  }

  public Cookie setHttpOnly(boolean httpOnly) {
    this.httpOnly = httpOnly;
    return this;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public int getMaxAge() {
    return maxAge;
  }

  public boolean isHttpOnly() {
    return httpOnly;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    String encodedName = URLEncoder.encode(getName(), StandardCharsets.UTF_8);
    String encodedValue = URLEncoder.encode(getValue(), StandardCharsets.UTF_8);

    sb.append(String.format("%s=%s", encodedName, encodedValue));

    if (getMaxAge() > 0) {
      sb.append(String.format("; Max-Age=%s", getMaxAge()));
    }

    if (isHttpOnly()) {
      sb.append("; HttpOnly");
    }

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Cookie cookie)) return false;
    return Objects.equals(name, cookie.name) && Objects.equals(value, cookie.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }
}