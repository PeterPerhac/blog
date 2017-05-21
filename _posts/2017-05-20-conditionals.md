---
title:  "Conditional flatMap - tweaking sequences of effects with boolean conditions"
date:   2017-05-20 22:28:00
categories: ['scala']
tags: ['scala', 'cats']
published: true
---

# Conditional flatMap

I found myself in a situation when several Futures had to execute in a prededined sequence and towards the end, one more future was to be executed under a certain condition. I immediately reached for the cats-provided FlatMap syntax and my pet `ifM` but later decided to sprinkle some Scala implicit magic and make the code a bit more readable. I want to tell you all about it.



**Code**

{% highlight scala %}

object ConditionalFlatMap {
    implicit class CustomApplicativeOps[F[_] : Applicative, A](fa: => F[A]) {
        def onlyIf(condition: Boolean)(implicit F: Applicative[F]): F[Unit] =
            if (condition) F.map(fa)(_ => ()) else F.pure(())
    }
}

{% endhighlight %}

One thing to note: cats will provide `whenA` syntax from version 1.0. Curretnly we are using 0.9 version and the `onlyIf` syntax I came up with provides the same functionality.

