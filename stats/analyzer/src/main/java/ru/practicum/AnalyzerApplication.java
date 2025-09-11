package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.runner.EventSimilarityRunner;
import ru.practicum.runner.UserActionRunner;

@EnableDiscoveryClient
@SpringBootApplication
public class AnalyzerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApplication.class, args);

        final UserActionRunner userActionProcessor = context.getBean(UserActionRunner.class);
        final EventSimilarityRunner eventSimilarityProcessor = context.getBean(EventSimilarityRunner.class);

        Thread EventsThread = new Thread(userActionProcessor);
        EventsThread.setName("UserActionThread");
        EventsThread.start();

        eventSimilarityProcessor.run();
    }
}