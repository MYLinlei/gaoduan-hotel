package com.sky.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LocalConfigurationImportTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void defaultDevProfileImportsOptionalMachineLocalConfiguration() {
        contextRunner.run(context -> {
            assertThat(context.getEnvironment().getProperty("spring.profiles.active")).isEqualTo("dev");
            assertThat(context.getEnvironment().getProperty("sky.datasource.password"))
                    .isEqualTo("test-local-password");
        });
    }
}
