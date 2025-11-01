package org.example.hackaton1_;

import org.springframework.boot.SpringApplication;

public class TestHackaton1Application {

    public static void main(String[] args) {
        SpringApplication.from(Hackaton1Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
