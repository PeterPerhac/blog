---
title:  Conditional flatMap - tweaking sequences of effects with boolean conditions
date:   2017-05-20 22:28:00
categories: scala
tags: scala cats
published: true
---

# Parallel Futures

There are many ways to defur a feline. Also, there is more than one way to execute a number of Futures in parallel. Let's have a look at some of our options:



{%highlight scala linenos %}

import cats.Applicative

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

object ParallelVsSerialExecution {

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

  def parallelFuturesWithTraverse()(implicit f: Applicative[Future]): Future[Unit] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    List("apple", "banana", "cherry") traverse findFruit map printer
  }

  def parallelFuturesWithSequence()(implicit f: Applicative[Future]): Future[Unit] = {
    import cats.instances.list._
    import cats.syntax.traverse._
    List(findFruit("apple"), findFruit("banana"), findFruit("cherry")).sequence map printer
  }

  def parallelFuturesWithCartesians()(implicit f: Applicative[Future]): Future[Unit] = {
    import cats.syntax.cartesian._
    findFruit("apple") |@| findFruit("banana") |@| findFruit("cherry") map tuplePrinter
  }

  def main(args: Array[String]): Unit = {
    import cats.instances.future._
    import cats.syntax.applicative._
    val program = for {
      _ <- sequentialFuturesWithMonads()
      _ <- println("\n===\n").pure
      _ <- parallelFuturesBasic()
      _ <- println("\n===\n").pure
      _ <- parallelFuturesWithTraverse()
      _ <- println("\n===\n").pure
      _ <- parallelFuturesWithSequence()
      _ <- println("\n===\n").pure
      _ <- parallelFuturesWithCartesians()
    } yield println("\n===\n")

    Await.result(program, Inf)
  }

  private def printer: (List[String]) => Unit = _.foreach(println)

  private def tuplePrinter: (String, String, String) => Unit = {
    case res@_ => res.productIterator.foreach(println)
  }

}

{%endhighlight %}


In the above code listing you can find several ways of performing multiple tasks.
In this article we will focus on performing asynchronous operations in parallel in a way that we can still continue with processing their results in a **predetermined order**.

The listing does, however, include one exmaple of performing calls sequentially - using the for-comprehension the underlying monadic `bind` / Scala `flatMap` operation. Using flatMap / for-comprehension the individual calls will be performed sequentially, one after another, which is what we would need if results of an earlier call were required as inputs for subsequent calls. Sometimes we can perform independent operations entirely in parallel and there are different ways to achieve this.


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
