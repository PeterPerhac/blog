---
title:  Concurrent evaluation of Futures
description: A look at a number of options to perform several Future evaluations concurrently with (and without) help of the cats library.
date:   2017-05-23 19:02:00
category: scala
tags: scala cats
published: true
---

In some cases execution of a Future depends on the outcome of an execution of a previous Future. We would use `flatMap` and `map` to sequence the evaluation of our dependent futures.

On the other hand, in cases when futures are _independent_ of each other, it is only reasonable to execute them concurrently and collect their results, one after another, when ready. The simplest of tricks to achieve this is to write a for-comprehension and pull out the creation of Futures outside of it.  A `Future` is submitted to an execution context as soon as it is created. If we create the futures prior to entering our for-comprehension, they are all running (or are at the very least _scheduled_ to run) and we utilise the for-comprehension only to collect their results (future values) in sequential manner. While this approach works, let's see some other ways to do this before we adopt it.

There are many ways to defur a feline. Also, there is more than one way to execute a number of Futures in parallel. Let's have a look at some of our options.




The below code blocks are excerpts from the sample program, full listing of which is included towards the _end of this post_. The program shows several ways of executing multiple futures in parallel. Let's go through them and briefly discuss each approach:

- `sequentialFuturesWithMonads`
Let's first have a look at an example that evaluates futures **serially**, one by one. I include this to make a point that if you need to run computations _in parallel_, this is how _not to do it_.

{% highlight scala %}
for {
  a <- findFruit("apple")
  b <- findFruit("banana")
  c <- findFruit("cherry")
} yield Seq(a, b, c).foreach(println)
{% endhighlight %}

This is the classic approach to sequencing asynchronous execution, where the outcome of one Future is used as input for the next. Doing a for-comprehension is making use of the monadic nature of the comprehended-over type. For-comprehensions rely on `flatMap` which takes care of binding the two futures together. Futures created inside a for-comprehension will be running **serially,** not in parallel.

______

- `parallelFuturesBasic`
{% highlight scala %}
val fa = findFruit("apple")
val fb = findFruit("banana")
val fc = findFruit("cherry")
for {
  a <- fa
  b <- fb
  c <- fc
} yield Seq(a, b, c).foreach(println)
{% endhighlight %}

This is the most simplistic way to execute futures in parallel. Moving their creation to _outside_ of the for-comprehension will cause their execution to proceed practically at the same time (at the time they are created). We keep track of the promised future values by keeping a handle on them (assignment to `val`) and then for-comprehending over the promises, collecting their resulting values as they become available. The good part of this solution is, you don't need to include no libraries (like _cats_) in your project to achieve your goal.

______

- `parallelFuturesWithSequence`
{% highlight scala %}
import cats.instances.list._
import cats.syntax.traverse._
List(findFruit("apple"), findFruit("banana"), findFruit("cherry")).sequence map printer
{% endhighlight %}

Here we're using the cats-provided `Traverse` type class and the `sequence` method that "swaps the wrappers". So, when we have a list of future values, we can use `sequence` to turn that into a future list of values. `map` will ensure that the processing of the eventual list of values only happens once the future completes successfully.

______

- `parallelFuturesWithTraverse`
{% highlight scala %}
import cats.instances.list._
import cats.syntax.traverse._
List("apple", "banana", "cherry") traverse findFruit map printer
{% endhighlight %}

If you look at the previous solution with `sequence` you might wonder if it really is necessary to repeat the name of the function `findFruit` three times over. If the function used for obtaining the future value inside the list is alays the same, we could use the `traverse` method (at the core of the Traverse type class) and "visit" each value in the list with the future-producing function. All of these produced futures together, will form an eventual list of results. Once that list is available, we'll map it to make use of the result.

______

- `parallelFuturesWithCartesians`

{% highlight scala %}
import cats.syntax.cartesian._
findFruit("apple") |@| findFruit("banana") |@| findFruit("cherry") map print3
{% endhighlight %}

[Cats library][1] provides a neat syntax for creating cross products - _cartesians_. Importing `cats.syntax.cartesian._` brings the `|@|` (scream operator) into the game. This is used for building up an instance of a `CartesianBuilder` of appropriate arity (i.e. 2..22) You can then call `apWith`, `map`, `contramap`, `imap` or `tupled` on it and in this way manipulate the final result of all concurrently executed futures. This works very nicely when you have a case class modeling the composite of all (completed) future values. Like so:

{% highlight scala %}
case class Magic(foo:Foo, bar: Bar, baz: Baz)
val magic = getFoo(1) |@| loadBar("42") |@| fetchBaz() map Magic
{% endhighlight %}


______

If you review the output of running the above program, you can see that while the futures in the for-comprehension are started one after another in roughly one-second intervals, all the rest of the methods are firing all futures off at the same time. Note also, that the printed output is always ordered the way we would expect: apple, banana, cherry.

{%highlight text %}

1495528097400 looking for fruit
1495528098407 looking for fruit
1495528099410 looking for fruit
I found your apple
I found your banana
I found your cherry

===

1495528100429 looking for fruit
1495528100429 looking for fruit
1495528100429 looking for fruit
I found your apple
I found your banana
I found your cherry

===

1495528101459 looking for fruit
1495528101461 looking for fruit
1495528101461 looking for fruit
I found your apple
I found your banana
I found your cherry

===

1495528102467 looking for fruit
1495528102467 looking for fruit
1495528102467 looking for fruit
I found your apple
I found your banana
I found your cherry

===

1495528103475 looking for fruit
1495528103475 looking for fruit
1495528103476 looking for fruit
I found your apple
I found your banana
I found your cherry

===
{%endhighlight %}

And just for completeness, here's copy-pastable code you could throw into a REPL to try this out. Remember to include the [cats library][1] on your classpath.

{%highlight scala %}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

object ParallelVsSerialExecution {

  import cats.instances.future._

  def findFruit(fruit: String) = Future {
    println(s"${System.currentTimeMillis()} looking for fruit")
    Thread.sleep(1000)
    s"I found your $fruit"
  }

  def sequentialFuturesWithMonads(): Future[Unit] =
    for {
      a <- findFruit("apple")
      b <- findFruit("banana")
      c <- findFruit("cherry")
    } yield Seq(a, b, c).foreach(println)


  def parallelFuturesBasic(): Future[Unit] = {
    val fa = findFruit("apple")
    val fb = findFruit("banana")
    val fc = findFruit("cherry")

    for {
      a <- fa
      b <- fb
      c <- fc
    } yield Seq(a, b, c).foreach(println)
  }

  def parallelFuturesWithSequence(): Future[Unit] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    List(findFruit("apple"), findFruit("banana"), findFruit("cherry")).sequence map printer
  }

  def parallelFuturesWithTraverse(): Future[Unit] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    List("apple", "banana", "cherry") traverse findFruit map printer
  }

  def parallelFuturesWithCartesians(): Future[Unit] = {
    import cats.syntax.cartesian._
    findFruit("apple") |@| findFruit("banana") |@| findFruit("cherry") map print3
  }

  def main(args: Array[String]): Unit = {
    import cats.syntax.applicative._
    val program = for {
      _ <- sequentialFuturesWithMonads()
      _ <- println("\n===\n").pure
      _ <- parallelFuturesBasic()
      _ <- println("\n===\n").pure
      _ <- parallelFuturesWithSequence()
      _ <- println("\n===\n").pure
      _ <- parallelFuturesWithTraverse()
      _ <- println("\n===\n").pure
      _ <- parallelFuturesWithCartesians()
    } yield println("\n===\n")

    Await.result(program, Inf)
  }

  private def printer: (List[String]) => Unit = _.foreach(println)

  private def print3: (String, String, String) => Unit = Seq(_, _, _).foreach(println)

}

{% endhighlight %}


[1]:http://typelevel.org/cats/
