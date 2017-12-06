package me.chanjar.learning;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.io.PrintWriter;

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

  public static class SlowJobRunner implements Runnable {

    private final SlowJob job;

    private final AsyncContext asyncContext;

    public SlowJobRunner(SlowJob job, AsyncContext asyncContext) {
      this.job = job;
      this.asyncContext = asyncContext;
    }

    @Override
    public void run() {

      try {
        String result = job.doWork();
        PrintWriter writer = asyncContext.getResponse().getWriter();
        writer.println(result);
        writer.flush();
        asyncContext.complete();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

  }
}
