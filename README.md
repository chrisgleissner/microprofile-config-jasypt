# config

[![Maven Central](https://img.shields.io/maven-central/v/com.github.chrisgleissner.config/microprofile-config-jasypt)](https://search.maven.org/artifact/com.github.chrisgleissner.config/microprofile-config-jasypt/)
[![Build Status](https://travis-ci.com/chrisgleissner/config.svg?branch=master)](https://travis-ci.com/chrisgleissner/config)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/config/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/config?branch=master)
[![Maintainability](https://api.codeclimate.com/v1/badges/68a242cd2d727a5af43d/maintainability)](https://codeclimate.com/github/chrisgleissner/config/maintainability)

Encrypted properties for [Quarkus](https://quarkus.io) and [Eclipse Microprofile Config](https://github.com/eclipse/microprofile-config). This repo requires at least Java 8 and is automatically tested on OpenJDK 11.

## Eclipse MicroProfile Config with Jasypt Encryption

The `microprofile-config-jasypt` module contains an [Eclipse Microprofile Config](https://github.com/eclipse/microprofile-config) implementation 
that supports [Jasypt](http://www.jasypt.org)-encrypted property values. This allows to place secrets in publicly accessible 
property files and resolve them from any application that supports Microprofile Config. 

For an example on how to use this library in [Quarkus](https://quarkus.io) see below.

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

Add
```
<dependency>
    <groupId>com.github.chrisgleissner.config</groupId>
    <artifactId>microprofile-config-jasypt</artifactId>
    <version>1.0.1</version>
</dependency>
```
to your `pom.xml` and set the `JASYPT_PASSWORD` environment variable. As per the previous example, set `JASYPT_PASSWORD=pwd`.

Any `ENC(...)`-delimited properties in a `classpath:application.properties` file (configurable) will be decoded at run-time.

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

## Encrypted Properties in Quarkus

The `microprofile-config-jasypt-quarkus-example` module contains a [Quarkus](https://quarkus.io)-based example: 
* Encrypted properties can be used both for normal and for profile-specific properties, eg. properties with the `%prod.` prefix.
* For demonstration purposes only, the `LogPropertiesBean` in this module logs all properties on startup. 

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
