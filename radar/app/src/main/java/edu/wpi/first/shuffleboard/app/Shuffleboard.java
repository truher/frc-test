package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import java.io.File;
import java.net.URISyntaxException;
import java.time.Instant;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Shuffleboard extends Application {

  private Runnable onOtherAppStart = () -> {
  };

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

    new NetworkTablesPlugin().onLoad();

  }

  @Override
  public void start(Stage primaryStage) {
    // Set up the application thread to log exceptions instead of using
    // printStackTrace()
    // Must be called in start() because init() is run on the main thread, not the
    // FX application thread
    Thread.currentThread().setUncaughtExceptionHandler(Shuffleboard::uncaughtException);
    onOtherAppStart = () -> Platform.runLater(primaryStage::toFront);

    primaryStage.setTitle("Shuffleboard");
    primaryStage.setMinWidth(640);
    primaryStage.setMinHeight(480);
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());

    primaryStage.show();


  }

  @Override
  public void stop() throws Exception {

  }

  /**
   * Logs an uncaught exception on a thread. This is in a method instead of
   * directly in a lambda to make the log a bit
   * cleaner
   * ({@code edu.wpi.first.shuffleboard.app.Shuffleboard uncaughtException} vs
   * {@code edu.wpi.first.shuffleboard.app.Shuffleboard start$lambda$2$}).
   *
   * @param thread    the thread on which the exception was thrown
   * @param throwable the uncaught exception
   */
  private static void uncaughtException(Thread thread, Throwable throwable) {
  
  }

  /**
   * Gets the time at which the application JAR was built, or the instant this was
   * first called if shuffleboard is not
   * running from a JAR.
   */
  public static Instant getBuildTime() {
    return ApplicationManifest.getBuildTime();
  }

  /**
   * Gets the location from which shuffleboard is running. If running from a JAR,
   * this will be the location of the JAR;
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