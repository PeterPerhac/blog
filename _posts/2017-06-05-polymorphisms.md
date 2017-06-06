---
title:  Polymorphism and higher kinds
description: TBD
date:   2017-06-05 18:05:00
category: scala
tags: scala learning
published: false
comments: false
---

We'll talk briefly about parametric, subtype and ad-hoc polymorphism, then focus on _ad-hoc polymorphism_ and its relationship with the _type class pattern_ and higher-kinded types like **Functor**, then move on to discuss higher-kinded types shipped with the _Cats_ library.



The following polymorphism discussion is based in part on this [Herding Cats](http://eed3si9n.com/herding-cats/polymorphism.html) article.

## 1. Parametric polymorphism

the type of a value contains one or more unconstrained type variables, so that the value may adopt any type that results from substituting those variables with concrete types.

{% highlight scala %}
  def fun[A](a:A): Unit = println(a)
{% endhighlight %}

## 2. Subtype polymorphism

constraining type parameter with an **upper bound**: 

{% highlight scala %}
trait PlusInt[A] {
  def plus(i2: A): A
}
def plusBySubtype[A <: PlusIntf[A]](a1: A, a2: A): A =
  a1.plus(a2)
{% endhighlight %}

This requires the types used in `plusBySubtype` to _extend_ the trait.

## 3. Ad-hoc polymorphism

constraining type parameter with a **context bound**: 

{% highlight scala %}
trait CanPlus[A] {
  def plus(a1: A, a2: A): A
}
def plus[A: CanPlus](a1: A, a2: A): A =
  implicitly[CanPlus[A]].plus(a1, a2)
{% endhighlight %}

This places a special kind ofconstraint on the type parameter. A context bound like the above will be de-sugared to:

{% highlight scala %}
def plus[A](a1: A, a2: A)(implicit cpa: CanPlus[A]): A =
  cpa.plus(a1, a2)
{% endhighlight %}

This means that in order to call the `plus` method, the compiler will need to be able to find evidence that behaviour/properties described by `CanPlus` have been implemented for the actual type of `A` (or an explicit instance must be provided by programmer).


