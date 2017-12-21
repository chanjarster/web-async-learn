package me.chanjar.learning.slowjob;

public class SlowJob {

  private final String name;

  public SlowJob(String name) {
    this.name = name;
  }

  public String doWork() {
    try {
      Thread.sleep(1000L);

      return "Hi from " + name + ". Current Thread: " + Thread.currentThread().getName() + "\n";
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;

  }

}
