package com.ericsson.cifwk.tdm;

import org.springframework.boot.SpringApplication;

public final class ApplicationLauncher {

    private ApplicationLauncher() {
    }

    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }
}
