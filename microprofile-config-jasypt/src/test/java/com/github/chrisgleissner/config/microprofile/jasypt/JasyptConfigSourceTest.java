package com.github.chrisgleissner.config.microprofile.jasypt;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    void returnsNoPropertiesIfPropertyFileNotFound() {
        System.setProperty(JASYPT_PASSWORD, "pwd");
        System.setProperty(JASYPT_PROPERTIES, "doesNotExist.properties");
        assertThat(new JasyptConfigSource().getProperties()).isEmpty();
    }

    @Test
    void returnsPropertiesIfSomePropertyFilesAreNotFound() {
        System.setProperty(JASYPT_PASSWORD, "pwd");
        System.setProperty(JASYPT_PROPERTIES, "doesNotExist.properties,src/test/resources/application.properties");
        JasyptConfigSource jcs = createJasyptConfigSource();
        assertThat(jcs.getValue("a")).isEqualTo("1");
        assertThat(jcs.getValue("b")).isEqualTo("2");
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

    @Test
    void securePropertiesAndApplicationPropertiesAreResolvedSeperately() {
        System.clearProperty(JASYPT_PROPERTIES);
        List<ConfigSource> jasyptConfigSources = StreamSupport.stream(ConfigProvider.getConfig().getConfigSources().spliterator(), false)
                .filter(cs -> cs.getName().startsWith("JasyptProperties[source=classpath:application.properties]"))
                .collect(Collectors.toList());
        assertThat(jasyptConfigSources).hasSize(1);
        ConfigSource jcs = jasyptConfigSources.get(0);
        assertThat(jcs.getOrdinal()).isEqualTo(275);
        assertThat(jcs.toString()).isEqualTo(jcs.getName());
        assertThat(jcs.getProperties().size()).isEqualTo(2);
    }
}