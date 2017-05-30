package com.perhac.blog

import cats.data.Kleisli
import cats.instances.FutureInstances
import cats.syntax.ApplicativeSyntax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

object Kleislis extends FutureInstances with ApplicativeSyntax {

  def main(args: Array[String]): Unit = {
    val program = Kleisli(getUsersFavouriteColour) andThen Kleisli(getFruitByColour) andThen printFruit
    Await.result(program run 1, Inf)
  }

  val fruit = Set(Apple("red"), Banana("yellow"))

  val getUsersFavouriteColour: Int => Future[String] =
    uid => (if (uid == 1) "red" else "green").pure

  val getFruitByColour: String => Future[Fruit] =
    colour => fruit.find(_.colour == colour).getOrElse(Banana(colour)).pure

  val printFruit: Fruit => Future[Unit] =
    fruit => println(s"Here's a ${fruit.colour} ${fruit.getClass.getSimpleName}").pure

}
