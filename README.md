# Laboratory Work N3 — Scala Best Practices and Idioms

## Prerequisites

- **Java 11+** (JDK)
- **sbt** (Scala Build Tool) 1.8+

## Run Instructions

```bash
# Compile the project
sbt compile

# Run all tasks
sbt run
```

## Task Descriptions

### TASK 1: Tail-recursive Fibonacci

Rewrites a mutable iterative Fibonacci implementation into an immutable, tail-recursive version using `@tailrec`, default parameters, and an accumulator (`Vector`). The function generates the first N Fibonacci numbers starting from 1. Result for n=10: `Vector(1, 2, 3, 5, 8, 13, 21, 34, 55, 89)`.

### TASK 2: Options and Collections (flatMap/map equivalent)

Demonstrates converting the mixed `Option`/collection for-comprehension into an equivalent chain of `flatMap` and `map` calls. The solution converts `Option` values to sequences where the comprehension is operating on collections. It also shows why omitting the conversion at the outer `Option.flatMap` fails: that method requires an `Option` result, but the callback returns a `Seq`.

### TASK 3: Inferred Return Type of safeDiv

States that the inferred return type of `safeDiv(n: Int, d: Int)` is **`Any`**. This occurs because Scala's Least Upper Bound (LUB) inference computes the common supertype of `None.type` (a reference subtype via `Option[Nothing]`) and `Int` (a value type). Since there is no common subtype between `AnyRef` and `AnyVal`, the LUB climbs to their shared supertype: `Any`.

### TASK 4: Type Inference Failure with flatten

Demonstrates three scenarios:
- **Part A**: Without explicit type annotation, `safeDiv` returns `Any`, so calling `.flatten` on the mapped result fails because there is no implicit `CanBuildFrom` or `AsTraversable` for `Any`.
- **Part B**: With an explicit `Option[Int]` return type annotation, compilation fails with a type mismatch — the else branch returns `Int` instead of `Option[Int]`.
- **Part C (fix)**: Wrapping the result in `Some(n / d)` resolves the issue. The mapped list can then be successfully flattened to extract all defined values.

### TASK 5: describeSign with match guards

Rewrites a nested if/else chain into a `match` expression with pattern guards (`case x if x == 0 => ...`). Produces identical output for all test inputs (-5, -1, 0, 1, 42).

### TASK 6: Asynchronous Future computation

Rewrites a synchronous example as an asynchronous computation using Scala `Future` and a for-comprehension. Three independent futures (producing 1, 2, 3) are composed, and their results are summed with a string prefix. Uses `Await.result` at the top level (allowed per assignment guidelines). Output: `"The answer is 6"`.
