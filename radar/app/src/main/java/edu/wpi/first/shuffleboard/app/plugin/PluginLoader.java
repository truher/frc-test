package edu.wpi.first.shuffleboard.app.plugin;


import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class PluginLoader {



  private static final PluginLoader defaultLoader =
      new PluginLoader(

);

  private final ObservableSet<Plugin> loadedPlugins = FXCollections.observableSet(new LinkedHashSet<>());
  private final Set<Class<? extends Plugin>> knownPluginClasses = new HashSet<>();
 
  private final ObservableList<ClassLoader> classLoaders = FXCollections.observableArrayList();
 

  



  /**
   * Creates a new plugin loader object. For app use, use {@link #getDefault() the default instance}; this should only
   * be used for tests.
   *
   * @param dataTypes       the data type registry to use for registering data types from plugins
   * @param sourceTypes     the source type registry to use for registering source types from plugins
   * @param components      the component registry to use for registering components from plugins
   * @param themes          the theme registry to use for registering themes from plugins
   * @param tabInfoRegistry the registry for tab information provided by plugins
   * @param converters      the registry for custom recording file converters
   * @param propertyParsers the registry for custom property parsers
   *
   * @throws NullPointerException if any of the parameters is {@code null}
   */
  public PluginLoader(
            
             ) {




  }

  /**
   * Gets the default plugin loader instance. This should be used as the global instance for use in the application.
   */
  public static PluginLoader getDefault() {
    return defaultLoader;
  }

  /**
   * Loads all jars found in the given directory. This does not load jars in nested directories. Jars will be loaded in
   * alphabetical order.
   *
   * @param directory the directory to load plugins from
   *
   * @throws IllegalArgumentException if the path is not a directory
   * @throws IOException              if the directory could not be read from
   * @see #loadPluginJar
   */
  public void loadAllJarsFromDir(Path directory) throws IOException {
    if (!Files.isDirectory(directory)) {
      throw new IllegalArgumentException("The given path is not a directory: " + directory);
    }
    Files.list(directory)
        .filter(p -> p.toString().endsWith(".jar"))
        .map(Path::toUri)
        .sorted() // sort alphabetically to make load order deterministic
        .forEach(jar -> {
          try {
            loadPluginJar(jar);
          } catch (IOException e) {
         
          }
        });
  }

  /**
   * Loads a plugin jar and loads all plugin classes within. Plugins will be loaded in the following order:
   *
   * <ol>
   * <li>By the amount of dependencies</li>
   * <li>By dependency graph; if one plugin requires another, the requirement will be loaded first</li>
   * <li>By class name</li>
   * </ol>
   *
   * @param jarUri a URI representing  jar file to load plugins from
   *
   * @throws IOException if the jar file denoted by the URI could not be found or read
   * @see #load(Plugin)
   */
  public void loadPluginJar(URI jarUri) throws IOException {

    URL url = jarUri.toURL();
    PrivilegedAction<URLClassLoader> getClassLoader = () -> {
      return new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
    };
    URLClassLoader classLoader = AccessController.doPrivileged(getClassLoader);
    try (JarFile jarFile = new JarFile(new File(jarUri))) {
      List<? extends Class<? extends Plugin>> pluginClasses = jarFile.stream()
          .filter(e -> e.getName().endsWith(".class"))
          .map(e -> e.getName().replace('/', '.'))
          .map(n -> n.substring(0, n.length() - 6)) // ".class".length() == 6
          .flatMap(className -> PluginLoaderHelper.tryLoadClass(className, classLoader))
          .filter(Plugin.class::isAssignableFrom)
          .map(c -> (Class<? extends Plugin>) c)
          .filter(c -> {
           
        
              return true;
            
          })
          .collect(Collectors.toList());
      knownPluginClasses.addAll(pluginClasses);


      if (!pluginClasses.isEmpty()) {
        classLoaders.add(classLoader);
      }
    }
  }



  /**
   * Attempts to load a plugin class. This class may or not be a plugin; only a plugin class will be loaded. A plugin
   * loaded with this method will be loaded after unloading a plugin that shares the same
   * {@link Plugin#idString() ID string}, if one exists.
   *
   * @param clazz the class to attempt to load
   *
   * @return true if the class is a plugin class and was successfully loaded; false otherwise
   * @throws Exception
   */
  public boolean loadPluginClass(Class<? extends Plugin> clazz) throws Exception {
    if (Plugin.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
      try {
       
        if (Modifier.isPublic(clazz.getConstructor().getModifiers())) {
          Plugin plugin = clazz.newInstance();
          load(plugin);
          return true;
        }
      } catch (ReflectiveOperationException  e) {
    
      }
    }
    return false;
  }



  /**
   * Loads a plugin.
   *
   * @param plugin the plugin to load
   *
   * @return true if the plugin was loaded, false if it wasn't. This could happen if the plugin were already loaded,
   *         or if the plugin requires other plugins that are not loaded.
   * @throws Exception
   *
   * @throws IllegalArgumentException if a plugin already exists with the same ID
   */
  public boolean load(Plugin plugin) throws Exception {
    if (loadedPlugins.contains(plugin)) {
      // Already loaded
      return false;
    }



    knownPluginClasses.add(plugin.getClass());



      plugin.onLoad();
 
    

    loadedPlugins.add(plugin);
   

    return true;
  }




}
