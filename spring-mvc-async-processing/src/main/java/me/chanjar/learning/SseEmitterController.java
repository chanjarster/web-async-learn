package me.chanjar.learning;

import me.chanjar.learning.slowjob.SlowJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;

@RestController
public class SseEmitterController {

  @Autowired
  @Qualifier("customExecutorService")
  private ExecutorService executorService;

  @RequestMapping("sse-emitter-hello")
  public ResponseBodyEmitter hello() {

    SseEmitter emitter = new SseEmitter();
    executorService.submit(() -> {
      try {
        for (int i = 0; i < 5; i++) {

          String hello = new SlowJob("SseEmitterController").doWork();
          StringBuilder sb = new StringBuilder();
          sb.append("Count: " + (i + 1)).append(". ").append(hello.replace("\n", ""));
          emitter.send(sb.toString());
        }
        emitter.complete();
      } catch (Exception e) {
        emitter.completeWithError(e);
      }

    });

    return emitter;
  }
}
