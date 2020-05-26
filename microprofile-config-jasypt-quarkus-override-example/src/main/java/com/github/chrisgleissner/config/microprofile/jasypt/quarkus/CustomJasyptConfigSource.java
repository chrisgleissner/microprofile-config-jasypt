package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource;

public class CustomJasyptConfigSource extends JasyptConfigSource {

    protected String getCommaSeparatedPropertyFilenames() {
        return property(JASYPT_PROPERTIES, "classpath:secure.properties");
    }
}
