import cats.syntax.foldable._
import cats.instances.list._

final case class Cat(name:String, age: Int)

object FoldMapWithMonoid {

  implicit object intAdditionMonoid extends cats.Monoid[Int] {
    override def combine(i1:Int, i2: Int):Int = i1 * i2
    override val empty:Int = 1
  }

  def main(args:Array[String]):Unit = {
    println(List(1,2,3).combineAll)
    println(List("1", "2", "3").foldMap(_.toInt))

    val cats = List(Cat("Mitzi", 2), Cat("Fluffy", 5), Cat("Ginger", 9))

    println(cats.foldMap(_.age))
    println(cats.map(_.age).product)
  }

}
