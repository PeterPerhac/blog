package com.perhac.blog

import cats.MonoidK
import cats.SemigroupK

object KindMonoid {

  def main(args:Array[String]):Unit = {
    import cats.instances.list._
    import cats.syntax.semigroup._
    val thing1 = List("gin", "tonic")
    val thing2 = List("rum", "coke")
    val thing3 = List(1,2,3,4,5)
    val combined = thing1 |+| thing2
    val combined2 = SemigroupK[List].combineK(combined, thing3)
    println(combined2)
  }
}
