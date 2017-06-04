import cats.MonoidK
import cats.SemigroupK

object KindMonoid {

  def main(args:Array[String]):Unit = {
    import cats.instances.list._
    val thing1 = List("gin", "tonic")
    val thing2 = List("rum", "coke")
    val combined = SemigroupK[List].combineK(thing1 , thing2)
    println(combined)
  }
}
