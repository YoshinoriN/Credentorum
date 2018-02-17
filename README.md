# :sparkles: Credentiam

[![Build Status](https://travis-ci.org/YoshinoriN/Credentiam.svg?branch=master)](https://travis-ci.org/YoshinoriN/Credentiam) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/7099cb31b4fb413c9bd2bcf1517d6c16)](https://www.codacy.com/app/YoshinoriN/Credentiam?utm_source=github.com&utm_medium=referral&utm_content=YoshinoriN/Credentiam&utm_campaign=badger) [![Known Vulnerabilities](https://snyk.io/test/github/yoshinorin/credentiam/badge.svg)](https://snyk.io/test/github/yoshinorin/credentiam?tab=dependencies)

**This project is under construction.**

ActiveDirectory search application. Powerd by Scala & Play Framework.

# Features

* View LDAP domains and each domain information.
* View LDAP organizations and each organaization information.
* View LDAP users and each user information.
* View LDAP computers and each computer information.
* Search object (user, computer, organization)

Maybe can use by OpenLDAP but some contents aren't display.

# Images

TODO

# Requirements

These requierments follow with [play framework](//www.playframework.com/documentation/2.6.x/Installing).

* sbt
* Java 1.8

# Installation

1. Download release from [release page](https://github.com/YoshinoriN/Credentiam/releases).
2. Extract zip.
3. Please change [settings](https://github.com/YoshinoriN/Credentiam/#settings).
4. Do below commands.

```
sh ./bin/credentiam -Dconfig.file=./conf/application.conf
```

# Settings

Configuration files are stored in `conf` directory.

## LDAP Settings

LDAP config file is `ldap.conf`.

Basically you have to change connection settings to your ldap server.

Each value's explanation are write in `ldap.conf`.

## Play Settings

Please change `play.http.secret.key` in `application.conf` to any value you like.

# Supported Language

* English
* Japanese

# Supported OS

TODO

# Packaging

## Universal

run `sbt universal:packageBin` in `src` directory.
