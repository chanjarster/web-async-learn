package me.chanjar.learning.slowjob;

public class SlowJob {

  private final String name;

  public SlowJob(String name) {
    this.name = name;
  }

  public String doWork() {
    try {
      Thread.sleep(1000L);

      return "Hi from " + name + ". \nCurrent Thread: " + Thread.currentThread().getName();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;

  }

}
