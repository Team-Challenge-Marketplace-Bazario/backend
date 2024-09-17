package io.teamchallenge.project.bazario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BazarioApplication {

    public static void main(String[] args) {

        Util.loadPropertiesIntoEnv(".env");

        SpringApplication.run(BazarioApplication.class, args);
    }
}
