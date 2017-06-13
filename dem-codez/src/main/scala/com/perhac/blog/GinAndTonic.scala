package com.perhac.blog

import cats.instances.{FutureInstances, ListInstances}
import cats.syntax.{TraverseSyntax, CartesianSyntax}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

sealed trait Prerequisite
trait Gin extends Prerequisite
trait Tonic extends Prerequisite
trait Glass extends Prerequisite
case object Gin extends Gin
case object Tonic extends Tonic
case object Glass extends Glass

object GinAndTonic extends TraverseSyntax with CartesianSyntax with FutureInstances with ListInstances {

  case class GinAndTonic(glass:Glass, gin: Gin, tonic: Tonic)

  def glass() = Future {
    Thread.sleep(100)
    println(s"${System.currentTimeMillis()} Here's your glass")
    Glass
  }

  def gin(succeed:Boolean=true) = Future {
    Thread.sleep(50)
    if (!succeed) throw new RuntimeException("Can't find Gin!")
    println(s"${System.currentTimeMillis()} Here's your gin")
    Gin
  }

  def tonic() = Future {
    println(s"${System.currentTimeMillis()} Here's your tonic")
    Tonic
  }

  def fetchIt(what:String):Future[Prerequisite] = what match {
    case "glass" => glass()
    case "gin" => gin(false)
    case "tonic" => tonic()
    case _ => Future {
      throw new RuntimeException("Oh noes, I have no clue what to do")
    }
  }

  def program1 = List(glass(), gin(), tonic()).sequence.collect{
    case glass :: gin :: tonic :: Nil => GinAndTonic(glass.asInstanceOf[Glass], gin.asInstanceOf[Gin], tonic.asInstanceOf[Tonic])
  }

  def program2 = List("glass", "gin", "tonic").traverse(fetchIt).collect{
    case glass :: gin :: tonic :: Nil => GinAndTonic(glass.asInstanceOf[Glass], gin.asInstanceOf[Gin], tonic.asInstanceOf[Tonic])
  }

  def program3 = List("grass", "gun", "tonic").traverse(fetchIt).collect{
    case glass :: gin :: tonic :: Nil => GinAndTonic(glass.asInstanceOf[Glass], gin.asInstanceOf[Gin], tonic.asInstanceOf[Tonic])
  }

  def program4 = glass() |@| gin() |@| tonic() map GinAndTonic

  def main(args: Array[String]): Unit = {
    Await.ready(program1 map println, Inf)
    Await.ready(program2.failed map println, Inf)
    Await.ready(program3.failed map println, Inf)
    Await.ready(program4 map println, Inf)
  }

}

