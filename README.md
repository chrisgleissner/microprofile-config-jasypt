# config

This project contains configuration-related utilities.

## microprofile-config-jasypt

Eclipse Config Microprofile implementation that supports Jasypt-encrypted property file values. 

Configuration:

| Environment variable | System property name  | Default value  | Description |
|----------------------|-----------------------|----------------|--------------| 
| JASYPT_PASSWORD | jasypt.password | none | Password used for encrypting property values |
| JASYPT_ALGORITHM | jasypt.algorithm | PBEWithHMACSHA512AndAES_256 | Encryption algorithm |
| JASYPT_PROPERTIES | jasypt.properties | config/application.properties | Property filename |
