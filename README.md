# config

[![Maven Central](https://img.shields.io/maven-central/v/com.github.chrisgleissner.config/microprofile-config-jasypt)](https://search.maven.org/artifact/com.github.chrisgleissner.config/microprofile-config-jasypt/)
[![Build Status](https://travis-ci.com/chrisgleissner/config.svg?branch=master)](https://travis-ci.com/chrisgleissner/config)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/config/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/config?branch=master)

This project contains configuration-related utilities. They require at least Java 8 and have been build
on OpenJDK 11 and 14.

## microprofile-config-jasypt

This module contains an [Eclipse Microprofile Config](https://github.com/eclipse/microprofile-config) implementation 
that supports [Jasypt](http://www.jasypt.org)-encrypted property values. This allows to place secrets in publicly accessible 
property files and resolve them from any application that supports Microprofile Config.

### Encryption

First, encrypt a property. For example, either of the following two commands encrypts a property `foo` using a password `pwd`: 

```
./microprofile-config-jasypt/encrypt.sh pwd foo
mvn -f microprofile-config-jasypt/pom.xml validate -Pencrypt -Djasypt.password=pwd -Dproperty=foo
```

This will print the encrypted property:

```
foo -> ENC(eu82k78q/boBye5P574UwNdafDuy9VRy19tdlmM9IeYXWkVIdChdZybEx41rRbdv)
```

Then use the entire `ENC`-delimited string (including the leading `ENC(` and trailing `)`) as a property value.

### Decryption

Add this dependency to your project:
```
<dependency>
    <groupId>com.github.chrisgleissner.config</groupId>
    <artifactId>microprofile-config-jasypt</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then set the encryption password in the `JASYPT_PASSWORD` environment variable. A comma-separated list of property filenames
 may be supplied by the `JASYPT_PROPERTIES` environment variable which has sensible defaults for Quarkus.

Any `ENC`-delimited properties in this property file will now be decoded at run-time.

### Configuration

You can customize `microprofile-config-jasypt` via environment variables or system properties as per the following table.
 
Alternatively, you can subclass [`com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource`](https://github.com/chrisgleissner/config/blob/master/microprofile-config-jasypt/src/main/java/com/github/chrisgleissner/config/microprofile/jasypt/JasyptConfigSource.java),
override its methods, and specify the fully qualified name of your subclass in a 
`META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource` file on the classpath. 

| Environment variable | System property name  | Default value  | Description |
|----------------------|-----------------------|----------------|--------------| 
| `JASYPT_PASSWORD` | `jasypt.password` | none | Password used for encrypting property values |
| `JASYPT_ALGORITHM` | `jasypt.algorithm` | `PBEWithHMACSHA512AndAES_256` | [Encryption algorithm](http://www.jasypt.org/cli.html#Listing_algorithms) |
| `JASYPT_PROPERTIES` | `jasypt.properties` | `classpath:application.properties,config/application.properties` | Comma-separated property filenames, see below.  |

Property filenames specified via `JASYPT_PROPERTIES` are resolved against the classpath if using the `classpath:` prefix, 
otherwise against the filesystem relative to the current working directory.

## quarkus-jasypt-config

This module contains a Quarkus extension for `microprofile-config-jasypt`. Add this dependency to your project:
```
<dependency>
    <groupId>com.github.chrisgleissner.config</groupId>
    <artifactId>quarkus-config-jasypt</artifactId>
    <version>1.0.0</version>
</dependency>
```

For configuration details, see above.

