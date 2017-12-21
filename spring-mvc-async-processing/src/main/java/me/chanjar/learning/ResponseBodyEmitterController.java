package me.chanjar.learning;

import me.chanjar.learning.slowjob.SlowJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.ExecutorService;

@RestController
public class ResponseBodyEmitterController {

  @Autowired
  @Qualifier("customExecutorService")
  private ExecutorService executorService;

  @RequestMapping("response-body-emitter-hello")
  public ResponseBodyEmitter hello() {

    ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    executorService.submit(() -> {
      try {
        for (int i = 0; i < 5; i++) {

          String hello = new SlowJob("ResponseBodyEmitterController").doWork();
          emitter.send("Count: " + (i + 1));
          emitter.send("\n");
          emitter.send(hello);
          emitter.send("\n\n");
        }
        emitter.complete();
      } catch (Exception e) {
        emitter.completeWithError(e);
      }

    });

    return emitter;
  }
}
