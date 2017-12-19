package me.chanjar.learning;

import me.chanjar.learning.slowjob.SlowJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutorService;

@RestController
public class DeferredResultController {

  @Autowired
  @Qualifier("customExecutorService")
  private ExecutorService executorService;

  @RequestMapping("deferred-result-hello")
  public DeferredResult<String> hello() {
    DeferredResult<String> deferredResult = new DeferredResult<>();
    executorService.submit(() -> {
      try {
        deferredResult.setResult(new SlowJob("DeferredResultController").doWork());
      } catch (Exception e) {
        deferredResult.setErrorResult(e);
      }

    });
    return deferredResult;
  }

}
