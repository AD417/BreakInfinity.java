# BreakInfinity.java
A Java port of [break_infinity.js](https://github.com/Patashu/break_infinity.js) -
a solution for incremental games which want to deal with very large numbers
(bigger in magnitude than 1e308 and even `BigDecimal`'s 1e(2.14e9), up to as much as 1e(9e15) )
and want to prioritize speed over accuracy.

Disclaimer: this library is not accurate to the precision of a `double`; it merely 
allows users to store and do math with numbers larger than `double` quickly. 

## Installation
This library is available in the [Maven Central Repository](https://central.sonatype.com/artifact/io.github.ad417/BreakInfinity/overview), so you can include it using 
whatever tool you like the most.

Maven:
```xml
<dependency>
    <groupId>io.github.ad417</groupId>
    <artifactId>BreakInfinity</artifactId>
    <version>0.2.0</version>
</dependency>
```

Gradle / Groovy:
```groovy
dependencies {
    //...
    implementation 'io.github.ad417:BreakInfinity:0.2.0'
}
```

Gradle/Kotlin:
```kotlin
dependencies {
    //...
    implementation("io.github.ad417:BreakInfinity:0.2.0")
}
```

## Use
The library exports a single class BigDouble, constructor of which accepts a
`Number`, `String` or `BigDouble`.

```javascript
    BigDouble x = new BigDouble(123.4567);
    BigDouble y = new BigDouble("123456.7e-3");
    BigDouble z = new BigDouble(x);
    boolean equals = x.equals(y) && y.equals(z) && x.equals(z); // true
```

The methods that return a BigDouble can be chained.

```javascript
    BigDouble oneLine = x.dividedBy(y).plus(z).times(9).floor();
    BigDouble multiLine = x.times("1.23456780123456789e+9")
        .plus(9876.5432321)
        .dividedBy("4444562598.111772")
        .ceil();
````


## Credits
[Patashu](https://github.com/Patashu) - for an amazing library. \
[RazenPok](https://github.com/Razenpok) - for the C# port, which made porting to Java
significantly easier.