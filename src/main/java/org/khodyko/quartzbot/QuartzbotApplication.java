package org.khodyko.quartzbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories(value = "org.khodyko.quartzbot.repository")
@SpringBootApplication(scanBasePackages = "org.khodyko.quartzbot")
@EntityScan("org.khodyko.quartzbot.model")
@EnableScheduling
public class QuartzbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuartzbotApplication.class, args);
    }

}
