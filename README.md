[![Travis CI](https://img.shields.io/travis/tvinke/bulletproof.svg)](https://travis-ci.org/tvinke/bulletproof)
# Bulletproof
*Groovy AST transformations, such as @Value and @NonNull, which makes constructing an instance more bullet-proof.*

![bulletproof logo](https://raw.githubusercontent.com/tvinke/bulletproof/master/logo.png)

Groovy has the `Immutable` annotation which allows to create immutable classes, which is a prerequisite for creating [value objects](https://en.wikipedia.org/wiki/Value_object). Unfortunately, when a class has been annotated with `Immutable` it's no longer possible to add your own constructor to verify if provided parameters are not `null`, making our value objects really bullet-proof.

Bulletproof helps to fill this gap by adding a few AST transformations.
 * The `NonNull` annotation which modifies every constructor to perform null-checks. Add this to an `Immutable` class and no `null` slips past your constructor.
 * The `Value` meta-annotation which puts both `NonNull` and `Immutable` on your class as a convenience to do above step with one annotation.

## Contents
 * [Annotations](#annotations)
   * [@NonNull](#nonnull)
   * [@Value](#value)
 * [Future Changes](#future-changes)

## Annotations

Consult the [bulletproof 0.1 Groovydocs](http://tvinke.github.io/bulletproof/v0.1/groovydoc/) for complete API information.

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

### Value

The `Value` meta-annotation combines the `Immutable` and `NonNull` annotations, which is used to assist in the creation of [value objects](https://en.wikipedia.org/wiki/Value_object).

```groovy
@tvinke.bulletproof.transform.Value
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