package com.perhac.blog

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration._
import scala.concurrent.{Await, Future}

object FutureOptions {

  def findById(id: Long): Future[Option[Banana]] =
    Future.successful(Some(Banana("yellow")))

  val program = for {
    optBanana <- findById(123L)
  } yield optBanana.map(_.colour)

  def main(args: Array[String]): Unit =
    Await.result(program.map(_.fold(())(println)), Inf)

}
