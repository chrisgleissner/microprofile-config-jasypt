package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SystemProperty(key = JasyptConfigSource.JASYPT_PASSWORD, value = "pwd")
@QuarkusTest
class PropertyDecryptionTest {
    @ConfigProperty(name = "config.password") String configPassword;

    private static void assertConfigSource(String name, int configSourceCount, int totalPropertyCount) {
        List<ConfigSource> configSources = StreamSupport.stream(ConfigProvider.getConfig().getConfigSources().spliterator(), false)
                .filter(cs -> cs.getName().equals(name)).collect(Collectors.toList());
        assertThat(configSources.size()).isEqualTo(configSourceCount);
        assertThat(configSources.stream().mapToInt(cs -> cs.getProperties().size()).sum()).isEqualTo(totalPropertyCount);
    }

    @Test
    void applicationPropertyCountIsCorrect() {
        assertConfigSource("PropertiesConfigSource[source=application.properties]", 2, 11);
    }

    @Test
    void jasyptPropertyCountIsCorrect() {
        assertConfigSource("JasyptProperties[source=classpath:secure.properties]", 1, 4);
    }

    @Test
    void applicationPropertyIsResolvedViaConfigProvider() {
        Config config = ConfigProvider.getConfig();
        assertThat(config.getValue("quarkus.flyway.migrate-at-start", String.class)).isEqualTo("true");
    }
    
    @Test
    void jasyptPropertyIsResolvedViaConfigPropertyAnnotation() {
        assertThat(configPassword).isEqualTo("test-pwd");
    }

    @Test
    void jasyptPropertyIsResolvedViaConfigProvider() {
        Config config = ConfigProvider.getConfig();
        assertThat(config.getValue("config.password", String.class)).isEqualTo("test-pwd");
    }

    @Test
    void applicationPropertyCanReferenceJasyptProperty() {
        Config config = ConfigProvider.getConfig();
        assertThat(config.getValue("config.reference.password", String.class)).isEqualTo("test-pwd");
    }
}