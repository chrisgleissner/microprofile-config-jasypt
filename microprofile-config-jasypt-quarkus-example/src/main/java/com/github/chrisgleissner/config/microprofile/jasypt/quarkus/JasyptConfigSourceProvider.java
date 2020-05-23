package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.util.List;

public class JasyptConfigSourceProvider implements ConfigSourceProvider {
    @Override public Iterable<ConfigSource> getConfigSources(ClassLoader classLoader) {
        return List.of(new JasyptConfigSource() {
            protected String getDefaultPassword() {
                return "pwd";
            }
        });
    }
}
