package io.teamchallenge.project.bazario;

import io.teamchallenge.project.bazario.helpers.EnvHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BazarioApplication {

    public static void main(String[] args) {

        EnvHelper.loadPropertiesIntoEnv(".env");

        SpringApplication.run(BazarioApplication.class, args);
    }
}
