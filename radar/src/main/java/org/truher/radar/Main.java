package org.truher.radar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.truher.radar.detector.Producer;
import org.truher.radar.net.Publisher;
import org.truher.radar.net.Server;
import org.truher.radar.net.Subscriber;
import org.truher.radar.view.Renderer;

public final class Main {
  public static class Publish {
    private final Server server;
    private final Publisher targetPublisher;
    private final Producer producer;

    public Publish() {
      server = new Server();
      targetPublisher = new Publisher();
      producer = new Producer(targetPublisher);
      Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
    }

    public void run() {
      server.start();
      targetPublisher.start();
      producer.start();
    }

    private void shutdown() {
      server.close();
      targetPublisher.close();
    }
  }

  public static class Render {
    private final List<Subscriber> subscribers;

    public Render(String[] args) {
      subscribers = new ArrayList<Subscriber>();
      for (int i = 0; i < args.length; ++i) {
        String topicName = args[i];
        System.out.printf("subscribing to topic %s\n", topicName);
        subscribers.add(
            new Subscriber(topicName, new Renderer(topicName, i)));
      }
      Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
    }

    public void run() {
      System.out.println("starting subscribers");
      for (Subscriber s : subscribers) {
        s.start();
      }
    }

    private void shutdown() {
      System.out.println("closing subscribers");
      for (Subscriber s : subscribers) {
        s.close();
      }
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("""
        Radar: NT4 example dashboard app.

        Usage: java -jar Radar-winx64.jar             NT server, publish fake data to 'targets' and 'map'
           or: java -jar Radar-winx64.jar [topic ..]  NT client, one display per topic
        """);

    if (args.length == 0) {
      System.out.println("running publisher");
      Publish p = new Publish();
      p.run();
      while (true) {
        Thread.sleep(1000);
      }
    } else {
      System.out.println("running renderers");
      Render r = new Render(args);
      r.run();
      while (true) {
        Thread.sleep(1000);
      }
    }
  }

}
