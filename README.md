# config

[![Build Status](https://travis-ci.com/chrisgleissner/config.svg?branch=master)](https://travis-ci.com/chrisgleissner/config)

This project contains configuration-related utilities. They require at least Java 8 and have been build
on OpenJDK 8, 11, and 14.

## microprofile-config-jasypt

[Eclipse Microprofile Config](https://github.com/eclipse/microprofile-config) implementation that supports [Jasypt](http://www.jasypt.org)-encrypted property file values.

To use it, add a dependency on this project and ensure the `JASYPT_PASSWORD` property is configured.  

Configuration:

| Environment variable | System property name  | Default value  | Description |
|----------------------|-----------------------|----------------|--------------| 
| JASYPT_PASSWORD | jasypt.password | none | Password used for encrypting property values |
| JASYPT_ALGORITHM | jasypt.algorithm | PBEWithHMACSHA512AndAES_256 | Encryption algorithm |
| JASYPT_PROPERTIES | jasypt.properties | config/application.properties | Property filename |
