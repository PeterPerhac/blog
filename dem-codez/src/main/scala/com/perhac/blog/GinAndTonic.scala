package com.perhac.blog

import cats.instances.{FutureInstances, ListInstances}
import cats.syntax.{TraverseSyntax, CartesianSyntax}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

object GinAndTonic extends TraverseSyntax with CartesianSyntax with FutureInstances with ListInstances {

  case class GinAndTonic(glass:Any, gin: Any, tonic: Any)

  def glass() = Future {
    Thread.sleep(100)
    println(s"${System.currentTimeMillis()} Here's your glass")
    "glass"
  }

  def gin() = Future {
    Thread.sleep(50)
    println(s"${System.currentTimeMillis()} Here's your gin")
    "gin"
  }

  def tonic() = Future {
    println(s"${System.currentTimeMillis()} Here's your tonic")
    "tonic"
  }

  def fetchIt(what:String):Future[Any] = what match {
    case "glass" => glass()
    case "gin" => gin()
    case "tonic" => tonic()
  }

  def program1 = List(glass(), gin(), tonic()).sequence.collect{
    case glass :: gin :: tonic :: Nil => GinAndTonic(glass, gin, tonic)
  }

  def program2 = List("glass", "gin", "tonic").traverse(fetchIt).collect{
    case glass :: gin :: tonic :: Nil => GinAndTonic(glass, gin, tonic)
  }

  def program3 = glass() |@| gin() |@| tonic() map GinAndTonic

  def main(args: Array[String]): Unit = {
    Await.result(program1 map println, Inf)
    Await.result(program2 map println, Inf)
    Await.result(program3 map println, Inf)
  }

}

