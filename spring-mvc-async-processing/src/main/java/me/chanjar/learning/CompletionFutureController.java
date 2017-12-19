package me.chanjar.learning;

import me.chanjar.learning.slowjob.SlowJob;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class CompletionFutureController {

  @RequestMapping("completable-future-hello")
  public CompletableFuture<String> hello() {

    return CompletableFuture
        .supplyAsync(() -> new SlowJob("CompletionFutureController").doWork());
  }

}
