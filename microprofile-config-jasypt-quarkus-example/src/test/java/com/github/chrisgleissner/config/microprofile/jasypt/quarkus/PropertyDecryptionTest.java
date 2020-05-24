package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThat;

@SystemProperty(key = JasyptConfigSource.JASYPT_PASSWORD, value = "pwd")
@QuarkusTest
class PropertyDecryptionTest {
    @ConfigProperty(name = "config.password") String configPassword;

    @Test
    void decryptionWorks() {
        assertThat(configPassword).isEqualTo("test-pwd");
    }
}