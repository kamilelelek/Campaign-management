package org.example.campaigntask;

import org.springframework.boot.SpringApplication;

public class TestCampaignTaskApplication {

    public static void main(String[] args) {
        SpringApplication.from(CampaignTaskApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
