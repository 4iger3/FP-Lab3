import scala.annotation.tailrec
import scala.concurrent._
import duration._
import ExecutionContext.Implicits.global

/**
 * Laboratory Work N3: Scala Best Practices and Idioms
 * Solutions for all 6 tasks from the assignment.
 */
object LaboratoryWorkN3 extends App {

  // =========================================================================
  // TASK 1: Rewrite the mutable `fibs` using an immutable, tail-recursive approach
  // equivalent to the `facts2` example (using @tailrec, default parameters, and accumulator).
  // =========================================================================

  // Original mutable version (from the PDF):
  // def fibs(ct: Int): Seq[Int] = {
  //   var n1 = 0
  //   var n2 = 1
  //   for (i <- 1 to ct) yield {
  //     val x = n1 + n2
  //     n1 = n2
  //     n2 = x
  //     x
  //   }
  // }

  @tailrec
  def fibsTailrec(
    limit: Int,
    ct: Int = 1,
    n1: Int = 0,
    n2: Int = 1,
    acc: Vector[Int] = Vector.empty
  ): Seq[Int] = {
    if (ct > limit) acc
    else fibsTailrec(limit, ct + 1, n2, n1 + n2, acc :+ (n1 + n2))
  }

  println("=== TASK 1: Tail-recursive Fibonacci ===")
  val task1Result = fibsTailrec(10)
  println(s"fibsTailrec(10) = $task1Result")
  // Expected: Vector(1, 2, 3, 5, 8, 13, 21, 34, 55, 89)
  assert(task1Result == Vector(1, 2, 3, 5, 8, 13, 21, 34, 55, 89), "TASK 1 failed!")
  println("TASK 1 passed.\n")


  // =========================================================================
  // TASK 2: Rewrite the Options-and-Collections for-comprehension using only
  // flatMap and map, demonstrating why incompatible types error occurs without .toSeq.
  // =========================================================================

  case class Address(street: String, city: String, state: String, zipCode: String)
  case class Person(first: String, last: String, address: Option[Address])

  val p1 = Some(Person("Fred", "Bloggs", None))
  val p2 = Some(Person("Simon", "Jones", Some(Address("123 Main", "Fakesville", "AZ", "12345"))))
  val p3: Option[Person] = None

  // Working version with .toSeq (from the PDF):
  val optPeople = Some(Seq(p1, p2, p3))
  val workingForCompResult = for {
    people      <- optPeople.toSeq
    personOpt   <- people
    person      <- personOpt.toSeq
    address     <- person.address.toSeq
  } yield address.zipCode

  println("=== TASK 2: Options and Collections (flatMap/map equivalent) ===")
  println(s"Working for-comprehension result: $workingForCompResult")
  // Expected: List(12345)

  // Equivalent using flatMap and map only:
  val task2FlatMapResult = optPeople.toSeq.flatMap { people =>
    people.flatMap { personOpt =>
      personOpt.toSeq.flatMap { person =>
        person.address.toSeq.map { address =>
          address.zipCode
        }
      }
    }
  }

  println(s"flatMap/map equivalent result: $task2FlatMapResult")
  assert(task2FlatMapResult == workingForCompResult, "TASK 2 flatMap result mismatch!")

  // This translation without converting the outer Option to a collection does not compile:
  // optPeople.flatMap { people =>
  //   people.flatMap { personOpt =>
  //     personOpt.flatMap(_.address.map(_.zipCode))
  //   }
  // }
  // The callback to Option.flatMap must return an Option, but people.flatMap returns a Seq.
  println("Without .toSeq, Option.flatMap expects an Option result but receives a Seq.")
  println("TASK 2 passed.\n")


  // =========================================================================
  // TASK 3: State the inferred return type of safeDiv and explain why.
  // =========================================================================

  def safeDiv(n: Int, d: Int) = if (d == 0) None else n / d

  println("=== TASK 3: Inferred return type of safeDiv ===")
  // The inferred return type is `Any`.
  // Explanation: Scala's type inference computes the Least Upper Bound (LUB) of the two branches.
  // - The if-branch returns `None`, whose type is `None.type` (a subtype of `Option[Nothing]`).
  // - The else-branch returns `n / d`, which has type `Int`.
  // - The LUB of `None.type` and `Int` in Scala 2.13 is `Any` because:
  //   * `None.type <: Option[Nothing] <: AnyRef` (reference type)
  //   * `Int <: AnyVal` (value type)
  //   * There is no common subtype between AnyRef and AnyVal, so the LUB climbs to their common supertype: `Any`.
  // This means safeDiv has signature: def safeDiv(n: Int, d: Int): Any

  val task3Type = safeDiv(5, 2)
  println(s"safeDiv(5, 2) = $task3Type")
  println(s"Inferred type of safeDiv is: Any (because LUB(None.type, Int) = Any)")
  println("TASK 3 passed.\n")


  // =========================================================================
  // TASK 4: Demonstrate the failure with flatten and explicit typing.
  // =========================================================================

  println("=== TASK 4: Type inference failure with safeDiv ===")

  // Part A: Without explicit type annotation, flatten fails because the inferred type is Any.
  println("\nPart A — without explicit type (inferred as Any):")
  val mapped = List(0, 1, 2).map(safeDiv(5, _))
  println(s"List.map result: $mapped")
  // The .flatten call would fail with:
  //   error: could not find implicit value for parameter asTraversable: ((Any) => Traversable[B])
  // This is because flatten requires an implicit CanBuildFrom or AsTraversable for the element type,
  // and there is no such implicit for `Any`.

  println("Calling .flatten on Any-typed collection would fail:")
  // Note: mapped.flatten does not compile because there is no implicit CanBuildFrom for Any.
  // The error message is:
  //   error: No implicit view available from Any => scala.collection.IterableOnce[B].
  println("  (compile-time error — see explanation above)")

  // Part B: With explicit Option[Int] annotation, we get a type mismatch on the else branch.
  println("\nPart B — with explicit Option[Int] annotation:")
  // The following function would fail to compile if uncommented:
  // def safeDivTyped(n: Int, d: Int): Option[Int] = if (d == 0) None else n / d
  // Error: type mismatch; found: Int, required: Option[Int]
  println("  error: type mismatch; found: Int, required: Option[Int]")
  println("  if (d == 0) None else n / d")

  println("safeDivTyped(5, 2) with explicit annotation would fail compilation:")
  println("  error: type mismatch; found: Int, required: Option[Int]")
  println("  if (d == 0) None else n / d")

  // Part C: Corrected version — wrap the result in Some.
  def safeDivFixed(n: Int, d: Int): Option[Int] = if (d == 0) None else Some(n / d)

  println("\nPart C — corrected version:")
  val fixedResult = List(0, 1, 2).map(safeDivFixed(5, _))
  println(s"List.map with safeDivFixed: $fixedResult")
  // Expected: Vector(None, Some(5), Some(2))

  val flattened = fixedResult.flatten
  println(s"After flatten: $flattened")
  // Expected: List(5, 2)

  assert(fixedResult == Vector(None, Some(5), Some(2)), "TASK 4 fixed result mismatch!")
  assert(flattened == List(5, 2), "TASK 4 flattened result mismatch!")
  println("TASK 4 passed.\n")


  // =========================================================================
  // TASK 5: Rewrite describeSign using match with guards instead of if/else if/else.
  // =========================================================================

  // Original (from the PDF):
  def describeSignOriginal(n: Int): String =
    if (n == 0) "Zero" else
      if (n > 0) "Positive" else
        "Negative"

  // Rewritten using match with guards:
  def describeSignMatch(n: Int): String = n match {
    case x if x == 0 => "Zero"
    case x if x > 0  => "Positive"
    case _           => "Negative"
  }

  println("=== TASK 5: describeSign with match guards ===")
  val testValues = List(-5, -1, 0, 1, 42)
  for (v <- testValues) {
    val original = describeSignOriginal(v)
    val rewritten = describeSignMatch(v)
    println(s"describeSign($v): original='$original', match='$rewritten'")
    assert(original == rewritten, s"Mismatch at $v!")
  }
  println("TASK 5 passed.\n")


  // =========================================================================
  // TASK 6: Rewrite the synchronous example as a fully asynchronous computation
  // using Future and for-comprehension.
  // =========================================================================

  println("=== TASK 6: Asynchronous Future computation ===")

  val asyncResult = for {
    a <- Future(1)
    b <- Future(2)
    c <- Future(3)
  } yield {
    val s = "The answer is"
    val sum = a + b + c
    s"$s $sum"
  }

  val task6Result = Await.result(asyncResult, 10.seconds)
  println(s"Async result: $task6Result")
  // Expected: The answer is 6
  assert(task6Result == "The answer is 6", "TASK 6 failed!")
  println("TASK 6 passed.\n")


  // =========================================================================
  // Final summary
  // =========================================================================

  println("=" * 50)
  println("All tasks completed successfully!")
  println("=" * 50)
}
