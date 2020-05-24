package com.github.chrisgleissner.config.microprofile.jasypt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.properties.EncryptableProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String DECRYPTION_FAILURE_MESSAGE = "Could not decrypt property {}; falling back to unencrypted property";

    private final Properties properties;
    private final EncryptableProperties encryptableProperties;

    public JasyptConfigSource() {
        this.properties = loadProperties();
        this.encryptableProperties = new EncryptableProperties(properties, getEncryptor());
    }

    @Override public String getName() {
        return "jasypt-config";
    }

    @Override
    public int getOrdinal() {
        // Higher than Quarkus application.properties (250), but lower than Eclipse Microprofile EnvConfigSource (300)
        return 275;
    }

    protected String property(String propertyName, String defaultValue) {
        String envVarName = envVarName(propertyName);
        return Optional.ofNullable(System.getenv(envVarName))
                .orElseGet(() -> Optional.ofNullable(System.getProperty(propertyName)).orElse(defaultValue));
    }

    protected String envVarName(String propertyName) {
        return PATTERN.matcher(propertyName).replaceAll("_").toUpperCase();
    }

    protected StringEncryptor getEncryptor() {
        return createStringEncryptor();
    }

    protected StringEncryptor createStringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(property(JASYPT_PASSWORD, getDefaultPassword()));
        encryptor.setAlgorithm(property(JASYPT_ALGORITHM, getDefaultAlgorithm()));
        encryptor.setIvGenerator(new RandomIvGenerator());
        return encryptor;
    }

    private String getDefaultAlgorithm() {
        return "PBEWithHMACSHA512AndAES_256";
    }

    /**
     * Override this if a custom password resolution strategy is desired. The non-empty default password defined here
     * is not supposed to be used for encryption, but simplifies instantiating this class of no password is set, e.g. as part of
     * the Quarkus build.
     */
    protected String getDefaultPassword() {
        return "419419d231b";
    }

    protected String getCommaSeparatedPropertyFilenames() {
        return property(JASYPT_PROPERTIES, "classpath:application.properties,config/application.properties");
    }

    protected Properties loadProperties() {
        final List<String> propertyFilenames = Arrays.asList(getCommaSeparatedPropertyFilenames().split(","));
        Exception lastException = null;
        for (final String propertyFilename : propertyFilenames) {
            log.debug("Trying to load properties from {}", propertyFilename);
            try (final InputStream is = createInputStream(propertyFilename)) {
                return createProperties(propertyFilename, is);
            } catch (Exception e) {
                lastException = e;
            }
        }
        if (lastException == null) {
            throw new RuntimeException("Could not load properties from any location in " + propertyFilenames);
        } else {
            throw new RuntimeException("Could not load properties from any location in " + propertyFilenames, lastException);
        }
    }

    private Properties createProperties(String propertyFilename, InputStream is) throws IOException {
        final Properties properties = new Properties();
        properties.load(is);
        log.info("Loaded {} properties from {}", properties.size(), propertyFilename);
        return properties;
    }

    private InputStream createInputStream(String location) throws Exception {
        if (location.startsWith(CLASSPATH_PREFIX)) {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(location.substring(CLASSPATH_PREFIX.length()));
        } else {
            return new FileInputStream(new File(location));
        }
    }

    @Override public Map<String, String> getProperties() {
        final Map<String, String> propertyMap = new HashMap<>();
        for (final String name : encryptableProperties.stringPropertyNames()) {
            propertyMap.put(name, getValue(name));
        }
        return propertyMap;
    }

    @Override public String getValue(String key) {
        try {
            return encryptableProperties.getProperty(key);
        } catch (EncryptionOperationNotPossibleException e) {
            if (log.isDebugEnabled()) {
                log.debug(DECRYPTION_FAILURE_MESSAGE, key, e);
            } else {
                log.warn(DECRYPTION_FAILURE_MESSAGE, key);
            }
            return properties.getProperty(key);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Syntax: JasyptConfigSource <propertyToEncrypt>...");
            System.exit(1);
        }
        StringEncryptor stringEncryptor = new JasyptConfigSource().createStringEncryptor();
        for (String arg : args) {
            System.out.println(String.format("%s -> ENC(%s)", arg, stringEncryptor.encrypt(arg)));
        }
    }
}
