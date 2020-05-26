# microprofile-config-jasypt

[![Maven Central](https://img.shields.io/maven-central/v/com.github.chrisgleissner.config/microprofile-config-jasypt)](https://search.maven.org/artifact/com.github.chrisgleissner.config/microprofile-config-jasypt/)
[![Build Status](https://travis-ci.com/chrisgleissner/config.svg?branch=master)](https://travis-ci.com/chrisgleissner/config)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/config/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/config?branch=master)
[![Maintainability](https://api.codeclimate.com/v1/badges/68a242cd2d727a5af43d/maintainability)](https://codeclimate.com/github/chrisgleissner/config/maintainability)

Encrypted properties for [Quarkus](https://quarkus.io) and [Eclipse Microprofile Config](https://github.com/eclipse/microprofile-config).

## Eclipse MicroProfile Config with Jasypt Encryption

An [Eclipse Microprofile Config](https://github.com/eclipse/microprofile-config) library
for [Jasypt](http://www.jasypt.org)-encrypted properties. This means you can use secrets in publicly accessible 
property files and decrypt them transparently at runtime. 
* For an example on how to use this library with [Quarkus](https://quarkus.io) see below.
* This repo requires at least Java 8 and is automatically tested on OpenJDK 11.

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

Then use the entire `ENC(...)`-delimited string as your property value, e.g. in a `src/main/resources/application.properties`
file. 

The name of the property file is configurable, and it may be on the classpath or the filesystem. See the configuration 
section below for details.

### Decryption

Add this to your `pom.xml`:
```
<dependency>
    <groupId>com.github.chrisgleissner.config</groupId>
    <artifactId>microprofile-config-jasypt</artifactId>
    <version>1.0.3</version>
</dependency>
```

Then add a file at `src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource` with the content
```
com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource
```

Finally set the `JASYPT_PASSWORD` environment variable when starting your application. As per the previous example, set `JASYPT_PASSWORD=pwd`.

Any `ENC(...)`-delimited property in a `classpath:application.properties` file (configurable) gets decoded at run-time.

### Configuration

You can customize `microprofile-config-jasypt` via environment variables or system properties as per the following table.
 
Alternatively, you can subclass [`com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource`](https://github.com/chrisgleissner/config/blob/master/microprofile-config-jasypt/src/main/java/com/github/chrisgleissner/config/microprofile/jasypt/JasyptConfigSource.java),
override its methods, and specify the fully qualified name of your subclass in a 
`META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource` file on the classpath. 

| Environment variable | System property name  | Default value  | Description |
|----------------------|-----------------------|----------------|--------------| 
| `JASYPT_PASSWORD` | `jasypt.password` | none | Password used for encrypting property values |
| `JASYPT_ALGORITHM` | `jasypt.algorithm` | `PBEWithHMACSHA512AndAES_256` | [Encryption algorithm](http://www.jasypt.org/cli.html#Listing_algorithms) |
| `JASYPT_ITERATIONS` | `jasypt.iterations` | 1000 | Jasypt key obtention iterations  |
| `JASYPT_PROPERTIES` | `jasypt.properties` | `classpath:application.properties,config/application.properties` | Comma-separated property filenames, see below.  |

Property filenames specified via `JASYPT_PROPERTIES` are resolved against the classpath if using the `classpath:` prefix, 
otherwise against the filesystem relative to the current working directory.

## Encrypted Properties in Quarkus

The `microprofile-config-jasypt-quarkus-example` module contains a [Quarkus](https://quarkus.io)-based example: 
* Encrypted properties can be used both for normal and for profile-specific properties, eg. properties with the `%prod.` prefix.
* For demonstration purposes only, the `LogPropertiesBean` in this module logs all properties on startup. 

The `microprofile-config-jasypt-quarkus-override-example` module expands on this and shows how to override the default `JasyptConfigSource`.

### Decryption Example

To verify successful decryption, run the following from repository root
```
mvn clean install
(cd microprofile-config-jasypt-quarkus-example && JASYPT_PASSWORD=pwd java -jar target/*-runner.jar)
``` 
and observe the log contains decrypted passwords:
```
2020-05-24 11:52:53,598 INFO  [com.git.chr.con.mic.jas.qua.LogPropertiesBean] (main) ConfigSource(name=jasypt-config, ordinal=275):
{quarkus.datasource.password=sa, quarkus.log.console.color=true, quarkus.datasource.username=sa, quarkus.log.console.level=TRACE, quarkus.flyway.migrate-at-start=true, quarkus.hibernate-orm.database.generation=validate, config.password=sa, quarkus.datasource.db-kind=h2, quarkus.hibernate-orm.log.sql=false, quarkus.datasource.jdbc.url=jdbc:h2:mem:test, quarkus.log.console.enable=true, quarkus.http.port=8080}
``` 

### Failed Decryption Example

To verify a failed decryption, run the following from repository root whilst intentionally specifying a wrong `JASYPT_PASSWORD`
```
mvn clean install
(cd microprofile-config-jasypt-quarkus-example && JASYPT_PASSWORD=wrong-pwd java -jar target/*-runner.jar)
```
and observe the log contains encrypted passwords:
```
2020-05-24 11:53:19,318 INFO  [com.git.chr.con.mic.jas.qua.LogPropertiesBean] (main) ConfigSource(name=jasypt-config, ordinal=275):
{quarkus.datasource.password=ENC(MCK/0Y9BnM7WVAyNq4gxjcPpGkDvu379ymjnsN2GCtowKxiPJXFHiSK7jI4rYfop), quarkus.log.console.color=true, quarkus.datasource.username=sa, quarkus.log.console.level=TRACE, quarkus.flyway.migrate-at-start=true, quarkus.hibernate-orm.database.generation=validate, config.password=ENC(MCK/0Y9BnM7WVAyNq4gxjcPpGkDvu379ymjnsN2GCtowKxiPJXFHiSK7jI4rYfop), quarkus.datasource.db-kind=h2, quarkus.hibernate-orm.log.sql=false, quarkus.datasource.jdbc.url=jdbc:h2:mem:test, quarkus.log.console.enable=true, quarkus.http.port=8080}
```
