package ai.foodscan.aggregate.db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@EnableScheduling
public class AggregateDbApplication {
    public AggregateDbApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(AggregateDbApplication.class, args);
    }
}