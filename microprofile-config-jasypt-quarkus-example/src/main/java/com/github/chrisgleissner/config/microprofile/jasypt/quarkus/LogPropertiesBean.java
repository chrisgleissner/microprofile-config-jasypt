package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigSource;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Comparator;
import java.util.stream.StreamSupport;

@ApplicationScoped @Slf4j
public class LogPropertiesBean {
    @ConfigProperty(name = "quarkus.datasource.password") String dbPassword;
    @ConfigProperty(name = "config.password") String configPassword;

    void logPropertiesForDebugging(@Observes StartupEvent ev) {
        log.info("quarkus.datasource.password={}", dbPassword);
        log.info("config.password={}", configPassword);
        StreamSupport.stream(ConfigProvider.getConfig().getConfigSources().spliterator(), false)
                .filter(cs -> !cs.getProperties().isEmpty())
                .sorted(Comparator.comparing(ConfigSource::getOrdinal, Comparator.reverseOrder())).forEach(cs ->
                log.info("ConfigSource(name={}, ordinal={}):\n{}\n", cs.getName(), cs.getOrdinal(), cs.getProperties()));
    }
}