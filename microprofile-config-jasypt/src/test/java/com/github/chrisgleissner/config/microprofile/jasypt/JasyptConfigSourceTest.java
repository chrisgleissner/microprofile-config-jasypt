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

    private JasyptConfigSource createJasyptConfigSource() {
        System.setProperty(JASYPT_PASSWORD, "pwd");
        System.setProperty(JASYPT_PROPERTIES, "src/test/resources/application.properties");
        return new JasyptConfigSource();
    }

    @Test
    void getValue() {
        JasyptConfigSource jcs = createJasyptConfigSource();
        assertThat(jcs.getValue("a")).isEqualTo("1");
        assertThat(jcs.getValue("b")).isEqualTo("2");
    }

    @Test
    void getProperties() {
        JasyptConfigSource jcs = createJasyptConfigSource();
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
    void returnsEncryptedValueIfNoPasswordSet() {
        System.setProperty(JASYPT_PROPERTIES, "src/test/resources/application.properties");
        System.clearProperty(JASYPT_PASSWORD);
        assertThat(new JasyptConfigSource().getValue("b")).startsWith("ENC(");
    }

    @Test
    void failsIfPropertyFileNotFound() {
        System.setProperty(JASYPT_PASSWORD, "pwd");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
            System.setProperty(JASYPT_PROPERTIES, "doesNotExist.properties");
            new JasyptConfigSource();
        }).withMessage("Could not load properties from any location in [doesNotExist.properties]");
    }

    @Test
    void resolvesPropertiesFromDefaultLocationsIfJasyptPasswordPropertyNotSet() {
        System.setProperty(JASYPT_PASSWORD, "pwd");
        System.clearProperty(JASYPT_PROPERTIES);
        JasyptConfigSource jcs = new JasyptConfigSource();
        assertThat(jcs.getValue("a")).isEqualTo("1");
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