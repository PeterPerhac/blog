package com.perhac.blog

import cats.instances.{FutureInstances, ListInstances}
import cats.syntax.TraverseSyntax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

object TraversingAsap extends TraverseSyntax with FutureInstances with ListInstances {

  def f1() = Future {
    Thread.sleep(100)
    println(s"${System.currentTimeMillis()} I return Foo")
    Some("Foo")
  }

  def f2() = Future {
    Thread.sleep(50)
    println(s"${System.currentTimeMillis()} No. Not here")
    None
  }

  def f3() = Future {
    println(s"${System.currentTimeMillis()} I return Bar")
    Some("Bar")
  }

  val program = List(f1(), f2(), f3()).sequence map (_.flatten)

  def main(args: Array[String]): Unit = Await.result(program map println, Inf)

}
