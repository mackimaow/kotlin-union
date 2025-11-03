# 

![GitHub release](https://img.shields.io/github/v/release/mackimaow/kotlin-union) ![Maven Central](https://img.shields.io/maven-central/v/io.github.mackimaow/kotlin-union) ![Coverage](https://img.shields.io/badge/Line%20Coverage-92.5%25-brightgreen) ![Branch Coverage](https://img.shields.io/badge/Branch%20Coverage-77.1%25-brightgreen)
![License](https://img.shields.io/github/license/mackimaow/kotlin-union)

# kotlin-union

This library adds Union Types to Kotlin that has **multiplatform support**; it may be used in Kotlin JVM and Kotlin Native. This library **supports representing external typescript unions** so kotlin code can easily interface with *npm* libraries in a type-correct way for KotlinJS.

Kotlin Unions is not a feature not part of the language currently. This library supports creating kotlin unions types to match union types described in typescript (that is, literals, and external JS objects). Although other unions-like structures are very nicely implemented through sealed interfaces and classes, they cannot support unwrapping into JavaScript objects, making them a poor candidate for external declarations in KotlinJS.

The implementation posed here is used to solve externally declared unions while also providing implementations for other multiplatform types (Kotlin JVM and Kotlin Native).

Union types would not be useful if they did not have nice control flow features along with them. That's why this implementation has operators such 'morph' that is a unique but useful control flow that is close to Kotlin's 'when' statement/expression.


# Motivation

Although kotlin-union can be used as general purpose union types for regular JVM project, a big use case is for KotlinJS. For example, say you have an external declaration for a typescript function or type that happens to use a union of types. For example, a function that takes in a color that can be either a number or a string literal:

**SomeTypeScriptLibrary.ts:**
```typescript
type Color = number | "red" | "green" | "blue"

function setColor(color: Color) {
	//  ... code body ...
}
function getColor(): Color {
	//  ... code body ...
}
```

At the current moment (1/_/2025), KotlinJS has no better way of express the union type in a type safe manner from an external typescript library than to use dynamic, which is not type safe.

**ColorExternal.kt:** (without kotlin-union)
```kotlin
// KotlinJS external declaration
external fun setColor(color: dynamic)
external fun getColor(): dynamic
```

Using kotlin-union, unions types can be created even with external declarations for them:

**ColorExternal.kt:** (with kotlin-union)
```kotlin
// KotlinJS external declaration
external fun setColor(color: Union<ColorCases>) // FIXED with kotlin-union
external fun getColor(): Union<ColorCases> // FIXED with kotlin-union
```

One just needs to specify the cases ```ColorCases``` within the union:

**Color.kt:**
```kotlin
// Union cases of typescript type:  number | "red" | "green" | "blue"
object ColorCases: MatchCases<ColorCases>() {
    val HEX by union(JsNumberCases)
    val RED by obj("red")
    val GREEN by obj("green")
    val BLUE by obj("blue")
}

// the typescript "number" type can be translated
// into kotlin as Union<JsNumberCases>:
object JsNumberCases: MatchCases<JsNumberCases>() {
    val INT by instance<Int>()
    val FLOAT by instance<Float>()
}
```

With union-kotlin unions can be created and protected by type-checking and union values can be appropriately syntax-highlighted by linters (unlike values of dynamic). For control-flow and creating Union types, see the usage section below. Also see the *general rules of thumb* section for correct usage of this library.

# Using In Your Projects

## Multiplatform

Through gradle (Make sure that you have `mavenCentral()` in the list of repositories):

```kotlin
repositories {
    mavenCentral()
}

val kotlinUnionVersion = "2.0.1"

// this is within commonMain for multiplatform projects
dependencies {
	implementation("io.github.mackimaow:kotlin-union:$kotlinUnionVersion")
}
```

Through maven:

```xml
<dependencies>
	<dependency>
	    <groupId>io.github.mackimaow</groupId>
	    <artifactId>kotlin-union</artifactId>
	    <version>2.0.1</version>
	</dependency>
</dependencies>
```

## Specific platform

If the project is platform specific, change the artifact id.

Artifact IDs:
- KotlinJS: `kotlin-union-js`
- KotlinJVM: `kotlin-union-jvm`
- KotlinNative: `kotlin-union-native`


## Import

Then import it into your project:

```kotlin
import io.github.mackimaow.kotlin.union.*

// ... code ...
```
# Usage for 2.0

## Creating Union Types

Creating Union types requires creating a corresponding UCases object (either by creating an object of type ```MatchingCases``` or ```DiscernCases```) and then adding cases by calling protected methods ```obj```, ```instance```, ```instanceWhen```, ```union```, ```unionWhen``` and then using *property delegation* to register it to that union-cases object.
Consider the follow example (as laid out in the motivation section):

```kotlin
// Union cases of typescript type:  number | "red" | "green" | "blue"
object ColorCases: MatchCases<ColorCases>() {
    val HEX by union(JsNumberCases)
    val RED by obj("red")
    val GREEN by obj("green")
    val BLUE by obj("blue")
}

// the typescript "number" type can be translated
// into kotlin as Union<JsNumberCases>:
object JsNumberCases: MatchCases<JsNumberCases>() {
    val INT by instance<Int>()
    val FLOAT by instance<Float>()
}
```

All the possible protected methods that add cases to the union-cases object are as follows:

| Case registering function | How the case accepts an object                 |
|---------------------------|------------------------------------------------|
| ```obj()```               | Checks by equality                             |
| ```instance()```          | Checks by type                                 |
| ```instanceWhen()```      | Checks by type and a specified condition       |
| ```union()```             | Checks by union type                           |
| ```unionWhen()```         | Checks by union type and a specified condition |

### Custom Cases

To create custom cases, create an extension function with UCaseSupplier. For example, say that we want to create that is shorthand for creating a js-number. We can do the following:

```kotlin
// custom case jsNumber():
fun <CS: UCases<CS>> UCaseSupplier<CS>.number() = union(JsNumberCases)
```

Now, whenever a cases object is created, the ```number()``` function is exposed via the ```case``` protected property to create a js-number case. For example, we could define ColorCases object alternatively as:

```kotlin
object ColorCases: MatchCases<ColorCases>() {
    val HEX by case.number() // using the custom case
    val RED by obj("red")
    val GREEN by obj("green")
    val BLUE by obj("blue")
}
```

### Enumeration Usage

One may also treat the union-cases object as an enumeration. Access ```cases``` or ```nameToCase``` properties to get all the cases in the union-cases object. For example:

```kotlin
val cases: List<UCase<ColorCases, *>> = ColorCases.cases
val nameToCase: Map<String, UCase<ColorCases, *>> = ColorCases.nameToCase
```

Each ```UCase``` has a name and ordinal:

```kotlin
val redCase: UCase<ColorCases, *> = ColorCases.RED
println(redCase.name) // "RED"  
println(redCase.ordinal) // 1
```

## Creating Union Values


Values are converted into unions by wrapping into a union object, although what actually happens within the library implementation is a little bit more complicated. The underlining data type of ```Union``` depends on what platform is being used:
+ Kotlin JS: there is no wrapper, values are dynamically cast to ```Union```. This is needed to support external union declarations.
+ Kotlin JVM / Native: there is a wrapper type that is a value class with an instance of a type in the union as the value. This is needed to avoid invalid cast Exceptions.

Regardless of the platform, the usage of the library *remains the same* and the wrapper type is a private implementation detail. To create a union value, use ```wrap()``` from either the ```MatchCases``` object or from a specific ```UCase```. You may also use the extension function ```Any?.wrapAs()``` to create union  values. For example, to create a union value of type ```ColorCases```:

```kotlin
// --- Creating unions by wrapping ---

// Wrapping directly on case assures the type of union:
val redColor: Union<ColorCases> = ColorCases.RED.wrap()

// Not wrapping directly gives you an Optional<Union<ColorCases>>.
// These will have a type of Optional.Some<ColorCases>:
val greenColor: Optional<Union<ColorCases>> = ColorCases.wrap("green")
val blueColor: Optional<Union<ColorCases>> = "blue".wrapAs(ColorCases)

// These will have a type of Optional.None because it doesn't match any case:
val yellowColor: Optional<Union<ColorCases>> = "yellow".wrapAs(ColorCases)

// Creating unions by double wrapping:
val intNumber: Union<JsNumberCases> = 0xFF0000.wrapAs(JsNumberCases.INT)
val intColor: Union<ColorCases> = intNumber.wrapAs(ColorCases.HEX)

// Creating a union directly (without double wrapping) from a float:
val floatColor: Optional<Union<ColorCases>> = 0.5f.wrapAs(ColorCases)
```

## Unwrapping Union Values

To unwrap a union value in a platform independent way, use ```Union.unwrap()```. This will return a type of ```Any?``` regardless of the union type.

```kotlin
// Suppose some function getColor() exists
val myColor: Union<ColorCases> = getColor()
val unwrappedColorValue: Any? = myColor.unwrap()
```
```unwrap()``` is useful when one would like to perform equality checks on the unwrapped value. If type-correct unwrapping is preferred, one must use the union-specific control-flow functions described in the section below.


## Union Type Checks

To check whether a type of instance ```obj``` is part of the union, **refrain** from using the ``is`` keyword because it does not work on all platforms. Rather use ```MatchCases.canWrap(obj)``` method:

```kotlin
val colorString = "red"
println(ColorCases.canWrap(colorString)) // true

val notAColorString = "strawberry"
println(ColorCases.canWrap(notAColorString)) // false
```

## Union Control Flow

### Kotlin Inspired Control Flow
Given a ```Union<ColorCases>```, one can use ```Union.alsoWhen``` to distinguish between types to perform type-safe computations. ```Union.alsoWhen``` is similar to kotlin's extension function ```Any?.also``` but with an additional case parameter to run a block specifically if the union is this case:

```kotlin
// for the sake of the example, getColor() returns a Union<ColorCases>
val color: Union<ColorCases> = getColor()

color.alsoWhen(ColorCases.HEX) { hexColor: Union<JsNumberCases> ->
    hexColor.alsoWhen(JsNumberCases.INT) { intColor: Int ->
        println("I'm an integer color: $intColor")
    }.alsoWhen(JsNumberCases.FLOAT) { floatColor: Float ->
        println("I'm a float color: $floatColor")
    }
}.alsoWhen(ColorCases.RED) { red: String ->
    println("I'm a $red color")
}.alsoWhen(ColorCases.GREEN) { green: String ->
    println("I'm a $green color")
}.alsoWhen(ColorCases.BLUE) { blue: String ->
    println("I'm a $blue color")
}
```

One can write this even more cleanly by using kotlin ```with``` statements:
```kotlin
val color: Union<ColorCases> = getColor()

with(ColorCases) {
    with(JsNumberCases) {
        color.alsoWhen(HEX) { hexColor ->
            hexColor.alsoWhen(INT) {
                println("I'm an integer color: $it")
            }.alsoWhen(FLOAT) {
                println("I'm a float color: $it")
            }
        }.alsoWhen(RED) {
            println("I'm a $it color")
        }.alsoWhen(GREEN) {
            println("I'm a $it color")
        }.alsoWhen(BLUE) {
            println("I'm a $it color")
        }       
    }
}
```

You may also use any of the other kotlin control flow extension functions that are fitted into the union type. Here is the table of all of them:

| Union Function               | Inspired Kotlin Function      |
|------------------------------|-------------------------------|
| ```Union.alsoWhen()```       | ```Any?.also()```             |
| ```Union.runWhen()```        | ```Any?.run()```              |
| ```Union.letWhen()```        | ```Any?.let()```              |
| ```Union.applyWhen()```      | ```Any?.apply()```            |
| ```Union.takeIfWhen()```     | ```Any?.takeIf()```           |
| ```Union.takeUnlessWhen()``` | ```Any?.takeUnless()```       |


### Exclusive Control-Flow for Union: *Morph*

Although the union functions declared above should be able to handle type-safe control flow for any use case, it can become more verbose than necessary. For this reason, the library provides a unique control flow function called ```morph()``` (and ```morphSelf()``` which returns a different value of the same union). This function's purpose is to translate the corresponding union type into another type in a less verbose way; it takes-in a lambda that has access to the current value of the union (via ```current```), and that lambda allows changes to this current value of the union by calls of ```changeWhen()```, ```changeByMorph()```, ```changeByMorphingCase()```. Besides these functions, the receiver has access to all the kotlin-inspired control flow functions for union as discussed above. For example: 

```kotlin
val myColor: Union<ColorCases> = getColor()

// morph color union to boolean
val isFavoriteColor: Boolean = myColor.morph {
    // I have access to the current value of the union, which
    // --at the moment-- is equal to myColor
    println(current == myColor) // true

    changeByMorph(ColorCases.HEX) {
        changeWhen(JsNumberCases.FLOAT) {
            val intHex: Int = (this * 255).toInt()
            JsNumberCases.INT.wrap(intHex)
        }
        runWhen(JsNumberCases.INT) {
            if (this < 256)
                ColorCases.BLUE.wrap()
            else
                return@morph false
        }.getOrThrow()
    }

    alsoWhen(ColorCases.GREEN) { green: String ->
        println("I don't like $green!")
    }

    runWhen(ColorCases.BLUE) {
        println("I love $this!")
        true
    }.orElse {
        false
    }
}
```

As said previously, the kotlin inspired functions can be used within ```morph```, but ```morph``` has additional values/function to use for control flow. Here is the table of all of them:

| Morph Function               | Description Kotlin Function                                                                          |
|------------------------------|------------------------------------------------------------------------------------------------------|
| ```current```                | The current union value within the morph block                                                       |
| ```changeWhen()```           | This function changes ```current``` given a certain union case                                       |
| ```changeByMorph()```        | Same as ```changeWhen()``` but can change a union case by a nested morph                             |
| ```changeByMorphingCase()``` | Same as ```changeByMorph()``` but changes a union case into another instance of that same union case |


## Problems With Generic Types

Due to type erasure in Kotlin, it's not possible to check whether a given instance is of a type with specific generic parameters at runtime. This needs to be considered when constructing type options for the union using this library. For example, a ```List<T>``` has the generic parameter ```T```, which cannot be known at runtime.

There *is* a way combat this:  if one defines a union with a ```List<T>``` union case specific with ```T```, a *matcher* (lambda) must be defined to distinguish this type. Even if a matcher is provided, the empty list of type ```List<T>``` is impossible to check the specific type parameter ```T``` at runtime. It is strongly encouraged (to avoid bugs) to separate the ambiguous types as their own case and treat them as such:

```kotlin

// Union of List<Float> | List<Int>
object NumbersCases: MatchCases<NumbersCases>() { 
    // add matcher to distinguish List<Float>
    val FLOATS by instance<List<Float>> { obj: Any? ->
        val possibleList = obj as? List<*>
        if (possibleList != null) {
            // make sure to shallow copy the list incase it's mutable 
            val list = possibleList.toList()
            if (list.isNotEmpty() && list.all { item -> item is Float }) {
                @Suppress("UNCHECKED_CAST")
                val floatList = list as List<Float>
                return@instance Optional.Some(floatList)
            }
        }
        return@instance Optional.None 
    }
    
    // add matcher to distinguish List<Int>
    val INTS by instance<List<Int>> { obj: Any? ->
        val possibleList = obj as? List<*>
        if (possibleList != null) {
		    // make sure to shallow copy the list incase it's mutable 
            val list = possibleList.toList()
            if (list.isNotEmpty() && list.all { item -> item is Int }) {
                @Suppress("UNCHECKED_CAST")
                val intList = list as List<Int>
                return@instance Optional.Some(intList)
            }
        }
        return@instance Optional.None
    }
    
    // add the ambiguous empty list case with its own matcher 
    val AMBIGUOUS_LIST by instance<List<*>> { obj: Any? ->
        val possibleList = obj as? List<*>
        if (possibleList != null) {
            // make sure to shallow copy the list incase it's mutable 
            val list = possibleList.toList()
            if (list.isEmpty())
                return@instance Optional.Some(list)
        }
        return@instance Optional.None 
    }
}
```

Every time one of the control-flow functions are used, the matcher lambda is called to check if the union is of that type. As can be seen within the list example above, this call to matcher can be **computationally expensive** as each check requires checking if all elements of the list are of a certain type. To combat this, one may alternatively create union cases by having the cases object extend ```DiscernCases``` instead of ```MatchCases```, where the lambda only needs to differentiate between other cases within union rather than match the type completely:

```kotlin

// Union of List<Float> | List<Int>
object NumbersCases: DiscernCases<NumbersCases>() { 
    // add differentiator to distinguish List<Float>
    val FLOATS by instance<List<Float>> { obj: Any? ->
        val list = obj as? List<*>
        if (list != null) {
            // only now have to check if the first element is a float
            if (list.isNotEmpty() && list[0] is Float) {
                @Suppress("UNCHECKED_CAST")
                val floatList = list as List<Float>
                return@instance Optional.Some(floatList)
            }
        }
        return@instance Optional.None 
    }
    
    // add differentiator to distinguish List<Int>
    val INTS by instance<List<Int>> { obj: Any? ->
        val list = obj as? List<*>
        if (list != null) {
            // only now have to check if the first element is a int
            if (list.isNotEmpty() && list[0] is Int) {
                @Suppress("UNCHECKED_CAST")
                val intList = list as List<Int>
                return@instance Optional.Some(intList)
            }
        }
        return@instance Optional.None
    }
    
    // add the ambiguous empty list case with its own differentiator 
    val AMBIGUOUS_LIST by instance<List<*>> { obj: Any? ->
        val possibleList = obj as? List<*>
        if (possibleList != null) {
		    // make sure to shallow copy the list incase it's mutable 
		    val list = possibleList.List()
            if (list.isEmpty())
                return@instance Optional.Some(list)
        }
        return@instance Optional.None 
    }
}
```
Although ```DiscernCases``` is more efficient than ```MatchCases```, you lose the ability to use ```canWrap()``` and ```wrap()``` since --if they were allowed-- certain cases would break type-correctness. Therefore, these methods are not implemented for ```DiscernCases``` objects. For similar reasons, the ```union()``` function (that specifies a union case) cannot take in a ```DiscernCases``` object as a parameter.


## General Rule of Thumb

1. Concerning ```instance<T>()``` and ```instanceWhen<T>()``` within either ```MatchingCases``` or ```DiscernCases```, if type argument T has generic arguments (e.g. ```instance<List<String>>()```,  ```instance<Map<String, Boolean>>()```, etc.), one **must** supply the matcher/differentiator lambda to distinguish the type to combat type-erasure. Otherwise, it's not type safe! Fortunately, if this is done incorrectly, the library will throw an exception during runtime immediately when that UCases object is created.
2. Use ```DiscernCases``` when the matcher check is expensive
3. Treat instances of ```Union``` as if they were wrapped in another object. Use ```Union.unwrap()``` to get the value that is wrapped in the union. Use ```canWrap()``` to check if a value can be wrapped into a union.
4. Don't use ```is``` to check if a value is a union type.
5. Remember to use property delegation to register cases to a union-cases object (i.e. _do_ this ```val COLOR by instance<Int>()```), not by setting (i.e. **DO NOT** do this ```val COLOR = instance<Int>()```)!


## Coding Conventions

To make the code more readable, it is recommended to use the following conventions:

1. Instantiate ```MatchingCases``` or ```DiscernCases``` as an object. Other ways are functional, but this is the most readable.
2. The name of the object that extends ```MatchingCases``` or ```DiscernCases``` should be prepended by ```Cases``` (e.g. ```ColorCases```, ```NumberCases```, etc.).
3. Each case property name of a cases object should be SCREAMING_SNAKE_CASE (e.g. ```HEX```, ```RED```, ```GREEN```, ```BLUE```, etc.).
4. Each case property of a cases object should be directly delegated via protected methods ```obj```, ```instance```, ```instanceWhen```, ```union```, ```unionWhen``` or by calling a custom extension function on UCaseSupplier (e.g. ```case.number()``` as described in previous sections).

## Usage for older versions
For usage only older versions, see the following links:

- [1.0.0](old-version-readmes/1.0.0.md)
