package com.perhac.blog


trait Printable[A] {
  self =>

  def format(value: A): String

  def contramap[B](func: B => A): Printable[B] = new Printable[B] {
    override def format(value: B): String = self.format(func(value))
  }
}


object Printable {

  implicit object intPrintable extends Printable[Int] {
    override def format(value: Int): String = value.toString
  }

}

object PrintableContramap {

  def format[A](value: A)(implicit p: Printable[A]): String = p.format(value)

  implicit def optionPrintable[T](implicit p: Printable[T]): Printable[Option[T]] = p.contramap[Option[T]](_.get)

  def main(args: Array[String]): Unit = {
    println(format(42))

    println(format(Option(123)))
//    println(format(Option("some")))
  }

}
