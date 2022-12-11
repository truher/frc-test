package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;

import com.google.common.base.Stopwatch;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;

@SuppressWarnings("PMD.MoreThanOneLogger") // there's only one logger used, the others are for setting up file logging
public class Shuffleboard extends Application {

  private static final Logger logger = Logger.getLogger(Shuffleboard.class.getName());


  private Runnable onOtherAppStart = () -> {};

  private final Stopwatch startupTimer = Stopwatch.createStarted();


  @Override
  public void init() throws Exception {
    try {
      JUnique.acquireLock(getClass().getCanonicalName(), message -> {
        onOtherAppStart.run();
        return null;
      });
    } catch (AlreadyLockedException alreadyLockedException) {
      JUnique.sendMessage(getClass().getCanonicalName(), "alreadyRunning");
      throw alreadyLockedException;
    }



    // Search for and load themes from the custom theme directory before loading application preferences
    // This avoids an issue with attempting to load a theme at startup that hasn't yet been registered
    logger.finer("Registering custom user themes from external dir");

 

    logger.info("Build time: " + getBuildTime());

    // Before we load components that only work with Java 8, check to make sure
    // the application is running on Java 8. If we are running on an invalid
    // version, show an alert and exit before we get into trouble.



  
    //PluginLoader.getDefault().load(new Plugin());

   
    PluginLoader.getDefault().load(new NetworkTablesPlugin());


    // notifyPreloader(new ShuffleboardPreloader.StateNotification("Loading custom plugins", 0.625));
    // PluginLoader.getDefault().loadAllJarsFromDir(Storage.getPluginPath());


    //PluginCache.getDefault().loadCache(PluginLoader.getDefault());
    Stopwatch fxmlLoadTimer = Stopwatch.createStarted();

   

    long fxmlLoadTime = fxmlLoadTimer.elapsed(TimeUnit.MILLISECONDS);

    logger.log(fxmlLoadTime >= 500 ? Level.WARNING : Level.INFO, "Took " + fxmlLoadTime + "ms to load the main FXML");

    
    Thread.sleep(20); // small wait to let the status be visible - the preloader doesn't get notifications for a bit
  }

  @Override
  public void start(Stage primaryStage) {
    // Set up the application thread to log exceptions instead of using printStackTrace()
    // Must be called in start() because init() is run on the main thread, not the FX application thread
    Thread.currentThread().setUncaughtExceptionHandler(Shuffleboard::uncaughtException);
    onOtherAppStart = () -> Platform.runLater(primaryStage::toFront);

  



    primaryStage.setTitle("Shuffleboard");
    primaryStage.setMinWidth(640);
    primaryStage.setMinHeight(480);
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());


    primaryStage.show();

    long startupTime = startupTimer.elapsed(TimeUnit.MILLISECONDS);
    logger.log(startupTime > 5000 ? Level.WARNING : Level.INFO, "Took " + startupTime + "ms to start Shuffleboard");
  }

  @Override
  public void stop() throws Exception {
    logger.info("Running shutdown hooks");
   
    logger.info("Shutting down");
  }

  /**
   * Logs an uncaught exception on a thread. This is in a method instead of directly in a lambda to make the log a bit
   * cleaner ({@code edu.wpi.first.shuffleboard.app.Shuffleboard uncaughtException} vs
   * {@code edu.wpi.first.shuffleboard.app.Shuffleboard start$lambda$2$}).
   *
   * @param thread    the thread on which the exception was thrown
   * @param throwable the uncaught exception
   */
  private static void uncaughtException(Thread thread, Throwable throwable) {
    logger.log(Level.WARNING, "Uncaught exception on " + thread.getName(), throwable);
  }

  /**
   * Gets the time at which the application JAR was built, or the instant this was first called if shuffleboard is not
   * running from a JAR.
   */
  public static Instant getBuildTime() {
    return ApplicationManifest.getBuildTime();
  }



  /**
   * Gets the location from which shuffleboard is running. If running from a JAR, this will be the location of the JAR;
   * otherwise, it will likely be the root build directory of the `app` project.
   */
  public static String getRunningLocation() {
    try {
      return new File(Shuffleboard.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
    } catch (URISyntaxException e) {
      throw new AssertionError("Local file URL somehow had invalid syntax!", e);
    }
  }

}