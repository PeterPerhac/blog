---
title:  Concurrent evaluation of Futures - run it in parallel if you can
date:   2017-05-20 22:28:00
categories: scala
tags: scala cats
published: true
---

# Parallel Futures

In some cases execution of a Future depends on the outcome of an execution of a previous Future. We would use `flatMap` and `map` to sequence the evaluation of our dependent futures.

On the other hand, in cases when futures are _independent_ of each other, it is only reasonable to execute them concurrently and collect their results, one after another, when ready. The simplest of tricks to achieve this is to write a for-comprehension and pull out the creation of Futures outside of it.  A `Future` is submitted to an execution context as soon as it is created. If we create the futures prior to entering our for-comprehension, they are all running (or are at the very least _scheduled_ to run) and we utilise the for-comprehension only to collect their results (future values) in sequential manner. While this approach works, let's see some other ways to do this before we adopt it.

There are many ways to defur a feline. Also, there is more than one way to execute a number of Futures in parallel. Let's have a look at some of our options:



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
    findFruit("apple") |@| findFruit("banana") |@| findFruit("cherry") map productPrinter
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

  private def productPrinter: (String, String, String) => Unit = Seq(_, _, _).foreach(println)

}

{% endhighlight %}


In the above code listing you can find several ways of executing multiple futures in parallel and one way to run them sequentially. Let's go through them briefly one by one:

- **sequential** execution of Futures taking the classic approach - via for-comprehension - is demonstrated in `sequentialFuturesWithMonads`
- **parallel** execution taking the classic approach - via for-comprehension with extracted futures (`parallelFuturesBasic`)
- **parallel** execution using cats-provided `Traverse` type class and `sequence` method (`parallelFuturesWithSequence`)
- **parallel** execution using cats-provided `Traverse` type class (`parallelFuturesWithTraverse`)
- **parallel** execution using cats-provided `CartesianBuilder` and the `|@|` syntax (the "scream" operator) (`parallelFuturesWithCartesians`)


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
