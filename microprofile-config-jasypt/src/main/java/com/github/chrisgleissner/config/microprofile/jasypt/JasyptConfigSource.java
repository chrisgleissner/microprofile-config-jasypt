package com.github.chrisgleissner.config.microprofile.jasypt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.properties.EncryptableProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Eclipse MicroProfile ConfigSource that supports Jasypt-encoded properties.
 */
@Slf4j
public class JasyptConfigSource implements ConfigSource {
    public static final String JASYPT_PASSWORD = "jasypt.password";
    public static final String JASYPT_ALGORITHM = "jasypt.algorithm";
    public static final String JASYPT_PROPERTIES = "jasypt.properties";
    private static final Pattern PATTERN = Pattern.compile("[^a-zA-Z0-9_]");
    private final EncryptableProperties encryptableProperties;

    public JasyptConfigSource() {
        this.encryptableProperties = new EncryptableProperties(loadProperties(), getEncryptor());
    }

    private static String property(String propertyName, String defaultValue) {
        String envVarName = envVarName(propertyName);
        return Optional.ofNullable(System.getenv(envVarName))
                .orElseGet(() -> Optional.ofNullable(System.getProperty(propertyName)).orElse(defaultValue));
    }

    private static String property(String propertyName) {
        return Optional.ofNullable(property(propertyName, null))
                .orElseThrow(() -> new RuntimeException(String.format("Please specify an environment variable '%s' " +
                        "or a system property '%s'", envVarName(propertyName), propertyName)));
    }

    private static String envVarName(String propertyName) {
        return PATTERN.matcher(propertyName).replaceAll("_").toUpperCase();
    }

    protected StringEncryptor getEncryptor() {
        return createStringEncryptor();
    }

    private static StringEncryptor createStringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(property(JASYPT_PASSWORD));
        encryptor.setAlgorithm(property(JASYPT_ALGORITHM, "PBEWithHMACSHA512AndAES_256"));
        encryptor.setIvGenerator(new RandomIvGenerator());
        return encryptor;
    }

    protected Properties loadProperties() {
        final String propertyFilename = property(JASYPT_PROPERTIES, "config/application.properties");
        log.info("Loading properties from {}", propertyFilename);
        final Properties properties = new Properties();
        try (final FileInputStream fis = new FileInputStream(new File(propertyFilename))) {
            properties.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not open " + propertyFilename, e);
        } catch (IOException e) {
            throw new RuntimeException("Could not access " + propertyFilename, e);
        }
        log.info("Loading {} properties from {}", properties.size(), propertyFilename);
        return properties;
    }

    @Override public Map<String, String> getProperties() {
        final Map<String, String> propertyMap = new HashMap<>();
        for (final String name: encryptableProperties.stringPropertyNames()) {
            propertyMap.put(name, encryptableProperties.getProperty(name));
        }
        return propertyMap;
    }

    @Override public String getValue(String key) {
        return encryptableProperties.getProperty(key);
    }

    @Override public String getName() {
        return "jasypt-config";
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Syntax: JasyptConfigSource <propertyToEncrypt>...");
            System.exit(1);
        }
        StringEncryptor stringEncryptor = createStringEncryptor();
        for (String arg : args) {
            System.out.println(String.format("%s -> ENC(%s)", arg, stringEncryptor.encrypt(arg)));
        }
    }
}
