package edu.wpi.first.shuffleboard.app.plugin;

import java.util.stream.Stream;

/**
 * Helper class for {@link PluginLoader}.
 */
final class PluginLoaderHelper {

  private PluginLoaderHelper() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  public static Stream<Class<?>> tryLoadClass(String name, ClassLoader classLoader) {
    try {
      return Stream.of(Class.forName(name, false, classLoader));
    } catch (ClassNotFoundException e) {
  
      return Stream.empty();
    }
  }




}
