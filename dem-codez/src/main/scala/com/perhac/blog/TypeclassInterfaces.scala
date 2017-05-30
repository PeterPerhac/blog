package com.perhac.blog

import cats.Show

object TypeclassInterfaces {

  final case class Banana(colour: String)

  def main(args: Array[String]): Unit = {
    println(intefaceObject(Banana("green")))
    println(intefaceSyntax(Banana("yellow")))
  }

  implicit object BananaShow extends Show[Banana] {
    override def show(f: Banana): String = s"${f.colour} banana"
  }

  object Printer {
    def print[T](t: T)(implicit st: Show[T]) = st show t
  }

  def intefaceObject(banana: Banana): String = {
    Printer.print(banana)
  }

  def intefaceSyntax(banana: Banana): String = {
    import cats.syntax.show._
    banana.show
  }

}
