package me.chanjar.learning;

import me.chanjar.learning.slowjob.SlowJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.concurrent.ListenableFutureTask;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;

@RestController
public class ListenableFutureController {

  @Autowired
  @Qualifier("customExecutorService")
  private ExecutorService executorService;

  @RequestMapping("listenable-future-hello")
  public ListenableFutureTask<String> hello() {

    ListenableFutureTask<String> listenableFutureTask = new ListenableFutureTask<>(
        () -> new SlowJob("ListenableFutureController").doWork());
    executorService.submit(listenableFutureTask);
    return listenableFutureTask;
  }

}
