package com.github.chrisgleissner.config.microprofile.jasypt;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource.JASYPT_PASSWORD;
import static com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource.JASYPT_PROPERTIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JasyptConfigSourceTest {
    private JasyptConfigSource jcs;

    @BeforeEach
    public void setUp() {
        System.setProperty(JASYPT_PASSWORD, "pwd");
        System.setProperty(JASYPT_PROPERTIES, "src/test/resources/jasypt.properties");
        jcs = new JasyptConfigSource();
    }

    @Test
    void getValue() {
        assertThat(jcs.getValue("a")).isEqualTo("1");
        assertThat(jcs.getValue("b")).isEqualTo("2");
    }

    @Test
    void getProperties() {
        Map<String, String> properties = jcs.getProperties();
        assertThat(properties.keySet()).containsExactlyInAnyOrder("a", "b");
        assertThat(properties).containsEntry("a", "1");
        assertThat(properties).containsEntry("b", "2");
    }

    @Test
    void getConfig() {
        Config config = ConfigProvider.getConfig();
        assertThat(config.getValue("a", String.class)).isEqualTo("1");
        assertThat(config.getValue("b", String.class)).isEqualTo("2");
    }

    @Test
    void failsIfNoPasswordSet() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
            System.clearProperty(JASYPT_PASSWORD);
            jcs = new JasyptConfigSource();
        }).withMessage("Please specify an environment variable 'JASYPT_PASSWORD' or a system property 'jasypt.password'");
    }

    @Test
    void failsIfNoPropertyFileNotFound() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
            System.clearProperty(JASYPT_PROPERTIES);
            jcs = new JasyptConfigSource();
        }).withMessage("Could not open config/application.properties");
    }

    @Test
    void encodingViaMainMethod() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            JasyptConfigSource.main(new String[]{"a", "b"});
            assertThat(outContent.toString()).matches(Pattern.compile("a -> ENC(.+)\nb -> ENC(.+)\n"));
       } finally {
            System.setOut(originalOut);
        }
    }
}