package me.chanjar.learning;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@WebServlet(value = "/async-servlet-1", asyncSupported = true)
public class AsyncServlet1 extends HttpServlet {

  private volatile ExecutorService executorService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    AsyncContext asyncCtx = req.startAsync();
    executorService.submit(new SlowJob.SlowJobRunner(new SlowJob("async servlet1"), asyncCtx));

  }

  @Override
  public void init() throws ServletException {

    super.init();
    executorService = Executors.newFixedThreadPool(400);
  }

  @Override
  public void destroy() {

    super.destroy();
    executorService.shutdown();
    boolean successfulTerminated = false;
    try {
      successfulTerminated = executorService.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (!successfulTerminated) {
      executorService.shutdownNow();
    }
    executorService = null;
  }

}
