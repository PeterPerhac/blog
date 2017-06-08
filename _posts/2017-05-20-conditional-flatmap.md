---
title:  Conditional flatMap - tweaking sequences of effects with boolean conditions
description: How to perform a step in a for-comprehension only under a certain condition with a conditional flatMap
date:   2017-05-20 22:28:00
category: scala
tags: scala cats
published: true

---

I found myself in a situation when several Futures had to execute in a predefined sequence and one of those Futures was to be executed _only uder a certain condition_. I immediately reached for the cats-provided flatMap syntax (`cats.syntax.flatMap._`) and my pet `ifM`:

{% highlight scala %}
  _ <- condition.pure.ifM(service.call(params), ().pure)
{% endhighlight %}

However, the above doesn't look good. It's hard to see what's going on. So I tinkered with cats and made the more pleasing to the eye:

{% highlight scala %}
  _ <- service.call(params) onlyIf condition
{% endhighlight %}

After all,

>  In the original language design great care was taken to ensure that the syntax would allow programmers to create natural looking DSLs.
> 
> www.scala-lang.org



### Original solution

{% highlight scala %}
import scala.concurrent.ExecutionContext.Implicits.global

import cats.instances.future._
import cats.syntax.applicative._
import cats.syntax.flatMap._

for {
  vatEligibility <- viewModel[VatServiceEligibility]
  _ <- s4l.saveForm(vatEligibility.setAnswer(question, data.answer))
  exit = data.answer == question.exitAnswer
  _ <- exit.pure.ifM(keystore.cache(INELIGIBILITY_REASON, question.name), ().pure)
} yield ...
{% endhighlight %}

Let's focus on this line:

{% highlight scala %}
exit.pure.ifM(keystore.cache(INELIGIBILITY_REASON, question.name), ().pure)
{% endhighlight %}

This lifts the boolean condition `exit` into the `Future` context using `.pure` syntax. `pure` method is defined on the `Applicative` type class and if we import `cats.syntax.applicative._` we can lift _any_ value into an effect `F`, provided there is an instance of `Applicative[F]` available in the implicit scope.

We can make further use of the [cats library][1] to enrich any `Future[Boolean]` (indeed, a boolean in any monadic context) with the `ifM` method. `ifM` is introduced by the import of `cats.syntax.flatMap._` and allows for flatMapping different expressions, depending on what's in the box (i.e. the boolean value in the context, on which we added the `ifM` etension method).

Just like `flatMap` takes a function `A => F[B]`, `ifM` takes **two** functions of this shape, but only flatMaps _one of them_. The first paramter to `ifM` is called `ifTrue` and the second one is `ifFalse` and which one gets flatmapped is obvious. I made the conditional service call in the `ifTrue` part, leaving the `ifFalse` as a successfully completed `Future` of `Unit`.

But when I slept on it and re-visited the line the next day, I thought I would much rather have it written like this:

{% highlight scala %}
  _ <- keystore.cache(INELIGIBILITY_REASON, question.name) onlyIf exit
{% endhighlight %}

### Natural looking DSL

Remember the part about natural looking DSLs? With the help of an implicit class and some cat-herding skills, I was able to make my dream come true:

{% highlight scala %}
implicit class CustomApplicativeOps[F[_] : Applicative, A](fa: => F[A]) {
  def onlyIf(condition: Boolean)(implicit F: Applicative[F]): F[Unit] =
    if (condition) F.map(fa)(_ => ()) else F.pure(())
}
{% endhighlight %}


### Final solution
With this implicit class imported into scope, the final for-comprehension looks like this:

{% highlight scala %}
import scala.concurrent.ExecutionContext.Implicits.global

import cats.instances.future._
import cats.syntax.applicative._
import cats.syntax.flatMap._

for {
  vatEligibility <- viewModel[VatServiceEligibility]
  _ <- s4l.saveForm(vatEligibility.setAnswer(question, data.answer))
  exit = data.answer == question.exitAnswer
  _ <- keystore.cache(INELIGIBILITY_REASON, question.name) onlyIf exit
} yield ...
{% endhighlight %}

And that's all, folks.

PS: cats will provide `whenA` syntax from version 1.0. Check it out when it comes out. It can be used to achieve similar sort of thing.

[1]:http://typelevel.org/cats/
