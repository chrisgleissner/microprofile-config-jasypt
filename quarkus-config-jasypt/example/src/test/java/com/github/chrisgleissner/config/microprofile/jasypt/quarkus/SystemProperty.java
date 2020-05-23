package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(SystemPropertyExtension.class)
public @interface SystemProperty {
    String key();
    String value();
}