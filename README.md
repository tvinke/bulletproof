[![Travis CI](https://img.shields.io/travis/tvinke/bulletproof.svg)](https://travis-ci.org/tvinke/bulletproof)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.tvinke/bulletproof.svg)](http://repo1.maven.org/maven2/com/github/tvinke/bulletproof/0.2/bulletproof-0.2.jar)
[![GitHub release](https://img.shields.io/github/tag/tvinke/bulletproof.svg)](https://github.com/tvinke/bulletproof/releases/tag/v0.2)
# Bulletproof
*Groovy AST transformations, such as @ValueObject and @NonNull, which makes constructing an instance more bullet-proof.*

![bulletproof logo](https://raw.githubusercontent.com/tvinke/bulletproof/master/logo.png)

Groovy has the `Immutable` annotation which allows to create immutable classes, which is a prerequisite for creating [value objects](https://en.wikipedia.org/wiki/Value_object). Unfortunately, when a class has been annotated with `Immutable` it's no longer possible to add your own constructor to verify if provided parameters are not `null`, making our value objects really bullet-proof.

Bulletproof helps to fill this gap by adding a few AST transformations.
 * The `NonNull` annotation which modifies every constructor to perform null-checks. Add this to an `Immutable` class and no `null` slips past your constructor.
 * The `ValueObject` meta-annotation which puts both `NonNull` and `Immutable` on your class as a convenience to do above step with one annotation.

## Contents
 * [Prerequisites](#prerequisites)
 * [Installation](#installation)
 * [Annotations](#annotations)
   * [@NonNull](#nonnull)
   * [@ValueObject](#value-object)
 * [Future Changes](#future-changes)

## Prerequisites

Bulletproof requires Java 7 or later.

## Installation

Add the bulletproof jar to the classpath in your preferred way and you're set.

### Grape
```groovy
@Grab('com.github.tvinke:bulletproof:0.2') 
```

### Gradle
```groovy
compile group: 'com.github.tvinke', name: 'bulletproof', version: '0.2'
```

### Maven
```xml
 <dependency>
    <groupId>com.github.tvinke</groupId>
    <artifactId>bulletproof</artifactId>
    <version>0.2</version>
</dependency>
```

## Annotations

Consult the [bulletproof 0.2 Groovydocs](http://tvinke.github.io/bulletproof/v0.2/groovydoc/) for complete API information.

### NonNull

The `NonNull` annotation on the class-level triggers an AST transformation which modifies every constructor to perform a null-check.

```groovy
@groovy.transform.Immutable
@tvinke.bulletproof.transform.NonNull
class Person {
    String name
}
new Person() // throws IllegalArgumentException: "Name can not be null"
```

### Value Object

The `ValueObject` meta-annotation combines the `Immutable` and `NonNull` annotations, which is used to assist in the creation of [value objects](https://en.wikipedia.org/wiki/Value_object).

```groovy
@tvinke.bulletproof.transform.ValueObject
class Money {
    BigDecimal amount
}

new Money(amount: null) // throws IllegalArgumentException because of NonNull

def money = new Money(2.95)
money.amount = 3.0 // throws ReadOnlyPropertyException because of Immutable
```

## Future Changes

 * Consider allowing a _mutable_ object (e.g. default Map constructor, or with `TupleConstructor`) also to have null-checks performed. In this case probably also through the setters.
 * In above case, then probably you need you want to specify which properties need to be null-checked instead of just everything.