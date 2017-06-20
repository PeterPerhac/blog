This is not particularly interesting, but we can create monoids that can combine multiple Maps into one, or collect a number of validations into a single final validation result, etc.

With relevant `Monoid` instance in scope, we can make use cats' `FoldableSyntax` to add `foldMap` operation onto foldable types. If we import the type class instance of `Foldable[List]` any `List` will be eligible for wrapping up in an implicit class that will give it the `foldMap` method. Here's an example of `foldMap` in action:

```tut:invisible
import cats.Monoid
implicit object intAdditionMonoid extends Monoid[Int] {
  def combine(i1: Int, i2: Int): Int = i1 + i2
  def empty: Int = 0
}
```
```tut:silent
case class Cat(name:String, age: Int)
val catz = List(Cat("Mitzi", 2), Cat("Fluffy", 5), Cat("Ginger", 3))

import cats.syntax.foldable._
import cats.instances.list._
```

Let's sum up the cats' age:

```tut:book
catz.map(_.age).sum
catz.map(_.age).fold(0)(_+_)
catz.foldLeft(0)((total, cat) => total + cat.age)
catz.foldMap(_.age)
```

The first two ways of adding up the cats' age **take two steps to achieve the desired result**. Both create an intermediate, _throw-away_ list of integers. `.sum` method simply delegates to a `foldLeft` and the `fold` in the second line delegates to `foldLeft` too.

If we wanted to avoid creating an intermediate collection of cats' ages, we could do a `foldLeft` as in the third example. We can't use a `fold` anymore, as the accumulator type is not a supertype of `Cat`, as `fold` would require. The resulting code is not too bad, in that it performs the summation of cats' ages in a single step, but, for what it does, it is unnecessarily verbose.

With `foldMap` the _mapping_ and _folding_ happens in a **single step**. The code is easy to read and understand - it looks like a simple `map` operation, but as long as there's a `Monoid` instance for the type mapped-into, we can fold down the cats into a single value in one go.



