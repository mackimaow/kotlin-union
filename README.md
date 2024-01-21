# kotlin-union

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mackimaow/kotlin-union)](https://search.maven.org/artifact/io.github.mackimaow/kotlin-union/1.0.0/jar)

Adds an implementation of Union Type to Kotlin that supports external declarations for typescript unions in KotlinJS. It can also be used in Kotlin JVM and Kotlin Native.

Kotlin Unions, a feature not part of the language, is created using existing Kotlin features. It supports unions for types described in typescript (that is, literals, and external JS objects). Although other Unions are implemented through sealed inline classes, they cannot support unwrapping into JavaScript objects, making them a poor candidate for external declarations in KotlinJS.

This implementation is used to solve externally declared unions while also providing implementations for other multiplatform types (Kotlin JVM and Kotlin Native). Lastly, Union types would not be useful if they did not have nice control flow features along with them. That's why this implementation has operators such 'map' that provides control flow that is akin to Kotlin's 'when' statement/expression.

# Using In Your Projects

## Multiplatform

Through gradle (Make sure that you have `mavenCentral()` in the list of repositories):

```kotlin
val kotlinUnionVersion = "1.0.0"

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
	    <version>1.0.0</version>
	</dependency>
</dependencies>
```

## Specific platform

If the project is platform specific, one must change artifact id.

Artifact IDs:
- KotlinJS: `kotlin-union-js`
- KotlinJVM: `kotlin-union-jvm`
- KotlinNative: `kotlin-union-native`


## Import

You may then import it into your project:

```kotlin
import io.github.mackimaow.kotlin.union.*

// ... code ...
```

# Usage
Say you need to mock the typescript union for color, which can be either the hex representation as a number or the literals "red", "green", or "blue":
```typescript
type Color = number | "red" | "green" | "blue"

function setColor(color: Color) {
	//  ... code body ...
}
function getColor(): Color {
	//  ... code body ...
}
```
Using kotlin-union, one can create a this type and even have a external declaration for it:

**Color.kt:**
```kotlin
// Union of Int | Float | "red" | "green" | "blue"
object Color: UnionOptions<Color>({Color}) {
	val INT = option<Int>() // mocking number
	val FLOAT = option<Float>() // mocking number
	val RED = literal("red")
	val GREEN = literal("green")
	val BLUE = literal("blue")
}
```
And then we can provide the following external declaration to
access the JavaScript implementation:

**ColorExternal.kt:** (if needed by KotlinJS)
```kotlin
// KotlinJS external declaration
external fun setColor(color: Union<Color>)
external fun getColor(): Union<Color>
```

## Union Control Flow

Given a ```Union<Color>```, one can use ```Union.map```, ```Union.trans```, or ```Union.alter```  to distinguish between types to perform type-safe computations.

#### Using map

Here is an example of using map to convert a union object into another
type:
```kotlin
// using map with default value
val myColor: Union<Color> = getColor()
  
val isFavoriteColor = 
	myColor.map(false) { // map color to boolean (default as false)
		
		// if myColor is a float, then
		//     continue as a Int
		change(Color.FLOAT) { floatNumber ->
			val intNumber: Int = floatNumber.toInt()
			Color.INT.wrap(intNumber) // change to int
		}
		
		// if myColor is a Int, then
		// 	   if it's hex value is below 256
		//  		continue as the literal "blue"
		//     else
		//          return default (false)
		change(Color.INT) { colorAsHex ->  
			if(colorAsHex < 256)
				// basically a blue color   
				Color.BLUE.wrap()   
			else  
				Break // goes to default (false)  
		}
		
		// if myColor is the literal "green", then
		//     print("I don't like green")
		//     and continue  
		execute(Color.GREEN) { greenString ->  
			println("I don't like green!")  
		}
		
		// if myColor is the literal "blue", then
		//      println("I love blue!")
		//		return true
		accept(Color.BLUE) { blueString ->  
			println("I love blue!")  
			true  
		}
		// anything else not accepted becomes default (false)
	}
```

One may also use an otherwise block to create a default using map:
```kotlin
// using map with using otherwise
val color: Union<Color> = getColor()
  
val isFavoriteColor = 
	color.map {
		change(Color.FLOAT) { floatHex ->
			val intHex: Int = floatHex.toInt()
			Color.INT.wrap(intHex)
		}
		change(Color.INT) { colorAsHex ->  
			if(colorAsHex < 256)
				Color.BLUE.wrap()   
			else  
				Break
		}  
		execute(Color.GREEN) { greenString ->  
			println("I don't like green!")  
		}  
		accept(Color.BLUE) { blueString ->  
			println("I love blue!")  
			true  
		}
		// anything else not accepted goes into the
		// otherwise block
		otherwise { color ->
			 println("I don't like the color $color")
			 false
		}
	}
```

#### Using transform
We can use a ```trans``` expression (short for transform) to transform the Union to another instance of that Union:

```kotlin
val color: Union<Color> = getColor()

val colorAsHexWithAlpha: Union<Color> =  
	color.trans {
		change(Color.FLOAT) { floatHex ->
			val intHex: Int = floatHex.toInt()
			Color.INT.wrap(intHex)
		}
		execute(Color.INT) {  
			println("I don't need to 'change' clause, I'm already as a hex")
		}
		change(Color.RED) { redString ->  
			Color.INT.wrap(0xFF0000)  
		}  
		change(Color.GREEN) { greenString ->  
			Color.INT.wrap(0x00FF00)  
		}  
		change(Color.BLUE) { blueString ->  
			Color.INT.wrap(0x0000FF)  
		}  
		accept(Color.INT) { colorAsHex ->  
			Color.INT.wrap(colorAsHex or 0xFF000000u.toInt())  
		}
		// otherwise block not needed but can be added
	}
```
If there are instances in the union which did not get accepted, the default return of a ```trans``` is the original Union instance itself.

#### Using alter
We can use a ```alter``` expression to perform some computation to the Union instance and then return the original union instance:
```kotlin
val color: Union<Color> = getColor()

// println my opinions on 'color'  
color.alter() {
	change(Color.FLOAT) { floatHex ->
		val intHex: Int = floatHex.toInt()
		Color.INT.wrap(intHex)
	}
	change(Color.INT) { colorAsHex ->  
		if(colorAsHex < 256)  
			Color.BLUE.wrap()
		else  
			Break  
        }  
	execute(Color.GREEN) { greenString ->  
		println("I don't like green!")  
	}  
	accept(Color.BLUE) { blueString ->  
		println("I love blue!")  
	}  
	otherwise { color ->  
		println("'$color' is not blue so I don't like it")  
	}  
}
```

## Problems With Generic Types

Due to type erasure in Kotlin, it's not possible to check whether a given instance is of a type with specific generic parameters at runtime. This needs to be considered when constructing type options for the union using this library. For example, a ```List<T>``` has the generic parameter ```T```, which cannot be known at runtime.

There *is* a way combat this:  if one defines a union with a ```List<T>``` union option specific with ```T```, a *discriminator* (a predicate lambda) must be defined to distinguish this type. Even if one tries to supply a discriminator, the empty list of type ```List<T>``` is impossible to check the specific type parameter ```T``` at runtime. It is strongly encouraged (to avoid bugs) to add the ambiguous types as their own option and treat them as such:

```kotlin
// Union of List<Float> | List<Int> | List<String>
object Colors: UnionOptions<Colors>({Colors}) {
	// add discriminator predicate to distinguish List<Float>
	val FLOATS = option<List<Float>> { obj: Any -> 
		obj is List<*> && obj.isNotEmpty() && obj[0] is Float
	}
	
	// add discriminator predicate to distinguish List<Int>
	val INTS = option<List<Int>> { obj: Any ->
		obj is List<*> && obj.isNotEmpty() && obj[0] is Int
	}
	
	// add discriminator predicate to distinguish List<String>
	val STRINGS = option<List<String>>{ obj: Any ->
		obj is List<*> && obj.isNotEmpty() && obj[0] is String
	}
	
	// add the ambiguous empty list case with its own discriminator
	val AMBIGUOUS_LIST = option<List<*>> { obj: Any ->
		obj is List<*> && obj.isEmpty()
	}
}
```

## Union as a Type Wrapper 

The underlining data type of ```Union``` depends on what platform is being used:
+ Kotlin JS: there is no wrapper, values are dynamically casted to ```Union```. This is needed to support external union declarations.
+ Kotlin JVM / Native: there is a wrapper type that is a value class with an instance of a type in the union as the value. This is needed to avoid invalid cast Exceptions.

Regardless of the platform, the usage of the library *remains the same* and the wrapper type is an private implementation detail. This means that in order to wrap a specific instance of a type within the union, one has to use either ```UnionOptions.wrap(obj)``` or ```UnionOptions.<specific option type>.wrap(obj)```:
```kotlin
val color1 = Color.wrap(0xFF0000)!!
val color2 = Color.INT.wrap(0xFF0000)
println(color1 == color2) // true
```
Similarly for literals:
```kotlin
val color1 = Color.wrap("red")!!
val color2 = Color.RED.wrap()
println(color1 == color2) // true
```
### Type Checks

To check whether an type of instance ```obj``` is part of the union, **refrain** from using the ``is`` keyword because it does not work on all platforms. Rather use ```UnionOptions.canBeWrapped(obj)``` method:

```kotlin
val colorString = "red"
println(Color.canBeWrapped(colorString)) // true

val notAColorString = "strawberry"
println(Color.canBeWrapped(notAColorString)) // false
``` 
