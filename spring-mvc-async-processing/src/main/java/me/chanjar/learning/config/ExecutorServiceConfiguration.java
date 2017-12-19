package me.chanjar.learning.config;

import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ExecutorServiceConfiguration {

  @Bean
  public ExecutorService customExecutorService() {
    return Executors.newFixedThreadPool(400, new ThreadFactory() {
      private final AtomicInteger threadNumber = new AtomicInteger(1);

      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, "my-executor-service-" + threadNumber.getAndIncrement());
      }
    });

  }

  @Bean
  public Lifecycle customExecutorServiceLifeCycle() {

    final ExecutorService executorService = customExecutorService();
    return new Lifecycle() {

      @Override
      public void start() {

      }

      @Override
      public void stop() {

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

      }

      @Override
      public boolean isRunning() {
        return !executorService.isTerminated();
      }

    };
  }
}
