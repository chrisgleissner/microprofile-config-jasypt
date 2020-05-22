# config

[![Build Status](https://travis-ci.com/chrisgleissner/config.svg?branch=master)](https://travis-ci.com/chrisgleissner/config)

This project contains configuration-related utilities. They require at least Java 8 and have been build
on OpenJDK 8, 11, and 14.

## microprofile-config-jasypt

[Eclipse Microprofile Config](https://github.com/eclipse/microprofile-config) implementation 
that supports [Jasypt](http://www.jasypt.org)-encrypted property values.

### Encryption of properties

For example, to encrypt a property `foo` using a password `pwd`, either run

```
./encrypt.sh pwd foo
```

or

```
mvn validate -Pencrypt -Djasypt.password=pwd -Dproperty=foo
```

which will print

```
foo -> ENC(8xM/21CIHYDNS8PbglamVPXKdr5pMQciYiElnQ0ZghlQnGB1fVsF9xwyNuDVfHWF)
```

Then use the entire `ENC`-delimited string (including the leading `ENC(` and trailing `)`) as a property value.

### Use of encrypted properties

Add a dependency on this project and set the encryption password in the `JASYPT_PASSWORD` environment 
variable. 

Any `ENC`-delimited property values will now be dynamically decoded.

### Configuration

| Environment variable | System property name  | Default value  | Description |
|----------------------|-----------------------|----------------|--------------| 
| `JASYPT_PASSWORD` | `jasypt.password` | none | Password used for encrypting property values |
| `JASYPT_ALGORITHM` | `jasypt.algorithm` | `PBEWithHMACSHA512AndAES_256` | Encryption algorithm |
| `JASYPT_PROPERTIES` | `jasypt.properties` | `config/application.properties` | Property filename |
