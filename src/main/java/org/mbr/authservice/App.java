package org.mbr.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"org.mbr.authservice.repositories"})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
