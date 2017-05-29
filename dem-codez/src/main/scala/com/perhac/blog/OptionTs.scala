package com.perhac.blog

import cats.data.OptionT
import cats.instances.FutureInstances

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration._
import scala.concurrent.{Await, Future}

object OptionTs extends FutureInstances {

  def findById(id: Long): OptionT[Future, Banana] =
    OptionT.some(Banana("yellow"))

  val program = for {
    banana <- findById(123L)
  } yield banana.colour

  def main(args: Array[String]): Unit =
    Await.result(program.fold(())(println), Inf)

}
