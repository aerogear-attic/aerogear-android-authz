# AeroGear Android Authz

[![Travis](https://img.shields.io/travis/aerogear/aerogear-android-authz.svg)](http://travis-ci.org/aerogear/aerogear-android-authz)
[![circle-ci](https://img.shields.io/circleci/project/github/aerogear/aerogear-android-authz/master.svg)](https://circleci.com/gh/aerogear/aerogear-android-authz)
[![License](https://img.shields.io/badge/-Apache%202.0-blue.svg)](https://opensource.org/s/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.jboss.aerogear/aerogear-android-authz.svg)](http://search.maven.org/#search%7Cga%7C1%7Caerogear-android-authz)
[![Javadocs](http://www.javadoc.io/badge/org.jboss.aerogear/aerogear-android-authz.svg?color=blue)](http://www.javadoc.io/doc/org.jboss.aerogear/aerogear-android-authz)

AeroGear's Android libraries were built as jar and aar packages using [Maven](http://maven.apache.org/) and the [android-maven-plugin](https://github.com/jayway/maven-android-plugin). The project follows the standard Maven layout so it can be imported directly into most IDEs as a Maven project.

## Authz

AeroGear Android Authz provides ability to integrate their Android application with RESTful services secured with OAuth 2 using Pipe.

|                 | Project Info  |
| --------------- | ------------- |
| License:        | Apache License, Version 2.0  |
| Build:          | Gradle  |
| Documentation:  | https://aerogear.org/android/ |
| Issue tracker:  | https://issues.jboss.org/browse/AGDROID  |
| Mailing lists:  | [aerogear-users](http://aerogear-users.1116366.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-users))  |
|                 | [aerogear-dev](http://aerogear-dev.1069024.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-dev))  |

## Usage

There are two supported ways of developing apps using AeroGear for Android: Android Studio and Maven.

### Android Studio

Add to your application's `build.gradle` file

```groovy
dependencies {
  compile 'org.jboss.aerogear:aerogear-android-authz:3.1.1'
}
```

### Maven

Include the following dependencies in your project's `pom.xml`

```xml
<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-android-authz</artifactId>
  <version>3.1.1</version>
  <type>aar</type>
</dependency>
```

## Documentation

For more details about that please consult [our documentation](http://aerogear.org/android/).

## Demo apps

Take a look in our demo apps

* [ShootAndShare](https://github.com/aerogear/aerogear-android-cookbook/blob/master/ShootAndShare)
* [GDrive](https://github.com/aerogear/aerogear-android-cookbook/blob/master/GDrive)

## Development

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AGDROID) with some steps to reproduce it.

