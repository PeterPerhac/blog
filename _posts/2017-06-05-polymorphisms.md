---
title:  Polymorphism and higher kinds
description: TBD
date:   2017-06-05 18:05:00
category: scala
tags: scala learning
comments: false
published: false
---

We'll talk briefly about parametric, subtype and ad-hoc polymorphism, then focus on _ad-hoc polymorphism_ and its relationship with the _type class pattern_ and higher-kinded types like **Functor**, then move on to discuss higher-kinded types shipped with the _Cats_ library.



The following polymorphism discussion is based in part on this [Herding Cats](http://eed3si9n.com/herding-cats/polymorphism.html) article.

## 1. Parametric polymorphism

Using parametric polymorphism, a function or a data type can be written generically so that it can handle values identically without depending on their type.[[1]][1] When the type of a value contains one or more unconstrained type variables, the value may adopt any type that results from substituting those variables with concrete types.

{% highlight scala %}
  def fun[A](a:A): Unit = println(a)
{% endhighlight %}

## 2. Subtype polymorphism

We can constrain the type parameter by an **upper bound**: 

{% highlight scala %}
trait PlusInt[A] {
  def plus(i2: A): A
}
def plusBySubtype[A <: PlusIntf[A]](a1: A, a2: A): A =
  a1.plus(a2)
{% endhighlight %}

This requires the types used in `plusBySubtype` to _extend_ the trait. This means, it could **not** _be_ an `Int`.

## 3. Ad-hoc polymorphism

We can constrain the type parameter by a **context bound**: 

{% highlight scala %}
trait CanPlus[A] {
  def plus(a1: A, a2: A): A
}
def plus[A: CanPlus](a1: A, a2: A): A =
  implicitly[CanPlus[A]].plus(a1, a2)
{% endhighlight %}

This places a special kind of constraint on the type parameter. The actual type of `A` must be from a specific class of types (type class).

A context bound like the above will be de-sugared to:

{% highlight scala %}
def plus[A](a1: A, a2: A)(implicit cpa: CanPlus[A]): A =
  cpa.plus(a1, a2)
{% endhighlight %}

This means that in order to call the `plus` method, the compiler will need to be able to find evidence that behaviour/properties described by `CanPlus` have been implemented for the actual type of `A` (or an explicit instance must be provided by programmer).

## Type classes

What determines membership of a type `T` in a type class `TC` is the ability to provide an implementation of the trait `TC` for the specific type `T`.

For example, the `String` type could be a member of the `Party` type class, if the compiler could find an instance of `Party[String]` in the implicit scope. This would serve as _evidence_ that the type `String` indeed belongs to the class of types that know how to `Party`.

## Values, Types and Kinds

Kinds are to types what types are to values. Different types can be of the same kind. For example `String`, `Int`, `Boolean` and `Banana` are of the _same kind_ - namely, they are all **proper** / concrete types. Some types are parameterised with other proper types: `List`, `Option`, `Try` and `Future`, are all of this kind. They can also be called type constructors, as they require some type argument(s) in order to construct a proper type. These would be classified as _first-order types_ - types that take other types as parameter. **Higher-order types** are of a different kind: they take a first-(or higher)-order type in order to produce a proper type - i.e. types parameterised with other type constructors.




[1]: https://en.wikipedia.org/wiki/Parametric_polymorphism#CITEREFPierce2002

