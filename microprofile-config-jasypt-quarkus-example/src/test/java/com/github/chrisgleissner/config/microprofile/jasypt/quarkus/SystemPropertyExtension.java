package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class SystemPropertyExtension implements AfterAllCallback, BeforeAllCallback {

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        SystemProperty annotation = getAnnotation(extensionContext);
        System.clearProperty(annotation.key());
        log.info("Cleared system property {}", annotation.key());
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        SystemProperty annotation = getAnnotation(extensionContext);
        System.setProperty(annotation.key(), annotation.value());
        log.info("Set system property {}={}", annotation.key(), annotation.value());
    }

    private SystemProperty getAnnotation(ExtensionContext extensionContext) {
        return extensionContext.getTestClass().orElseThrow().getAnnotation(SystemProperty.class);
    }
}