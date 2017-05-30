package com.perhac.blog

sealed trait Fruit extends Product with Serializable {
  val colour: String
}

final case class Apple(colour: String) extends Fruit

final case class Banana(colour: String) extends Fruit
