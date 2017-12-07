package me.chanjar.learning;

public class SlowJob {

  private final String name;

  public SlowJob(String name) {
    this.name = name;
  }

  public String doWork() {
    try {
      Thread.sleep(1000L);
      return "Hi from " + name;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;

  }

}
