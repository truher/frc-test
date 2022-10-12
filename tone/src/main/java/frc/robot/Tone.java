package frc.robot;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Targeting tone example.
 * 
 * Listens for network tables changes on specific keys and plays missile-seeking
 * and missile-lock tones.
 * 
 * To try this out, fire up a networktables server, e.g. glass or outlineviewer,
 * in server mode (use a blank "Listen Address"), and add two booleans:
 * /tone/lock and /tone/search. Run this app ("Simulate Robot" will do it), and
 * twiddle the networktable values. The audio will play accordingly.
 */
public final class Tone {
  private Clip searchClip;
  private Clip lockClip;
  private boolean initialized = false;

  /**
   * Reads the wav files. To avoid checked exceptions here, there's an
   * "initialized" flag.
   */
  public Tone() {
    try {
      // audio files go in src/main/resources, not deploy, this is a desktop app.
      searchClip = AudioSystem.getClip();
      lockClip = AudioSystem.getClip();
      searchClip.open(
          AudioSystem.getAudioInputStream(
              new BufferedInputStream(getClass().getResourceAsStream("/search.wav"))));
      lockClip
          .open(AudioSystem.getAudioInputStream(
              new BufferedInputStream(getClass().getResourceAsStream("/lock.wav"))));
      initialized = true;
    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
      e.printStackTrace();
    }
  }

  /**
   * Loops forever, listening for network tables changes and playing sounds.
   */
  public void run() {
    if (!initialized) {
      System.out.println("initialization failed, exiting");
      return;
    }
    System.out.println("running");

    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("tone");
    
    table.getEntry("search").addListener((event) -> play(event, searchClip),
        EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);
    table.getEntry("lock").addListener((event) -> play(event, lockClip),
        EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);

    inst.startClient("localhost");

    while (true) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /** Plays a clip or stops it, depending on the event. */
  private static void play(EntryNotification event, Clip clip) {
    if (event.getEntry().getBoolean(false)) {
      System.out.println(event.name + " on");
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    } else {
      System.out.println(event.name + " off");
      clip.stop();
    }
  }

  public static void main(String... args) {
    new Tone().run();
  }
}
