package com.github.chrisgleissner.config.microprofile.jasypt;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JasyptConfigSourceTest {
    private static JasyptConfigSource jcs;

    @BeforeAll
    public static void setPassword() {
        System.setProperty("jasypt.password", "pwd");
        System.setProperty("jasypt.properties", "src/test/resources/jasypt.properties");
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
}