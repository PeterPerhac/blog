package com.perhac.blog

import cats.instances.FutureInstances
import cats.syntax.CartesianSyntax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

final case class Profile(iid: String, name: String, age: Int)

object SequentialAndParallelFutures extends CartesianSyntax with FutureInstances {

  def main(args: Array[String]): Unit = {
    val program = for {
      id <- getProfileId("reteP")
      p <- getProfile(id)
      (prefs, balance) <- (getPreferences(p.iid) |@| getBalance(p.iid)).tupled
    } yield println(s"${System.currentTimeMillis()} ${p.name}'s balance is $balance. Preferences: $prefs")
    Await.result(program, Inf)
  }

  val getProfileId: String => Future[String] = in => Future {
    println(s"${System.currentTimeMillis()} getProfileId")
    Thread.sleep(1000)
    in
  }

  val getProfile: String => Future[Profile] = pid => Future {
    println(s"${System.currentTimeMillis()} getProfile")
    Thread.sleep(1000)
    Profile(pid.reverse + pid, pid.reverse, pid.length)
  }

  val getPreferences: String => Future[String] = iid => Future {
    println(s"${System.currentTimeMillis()} getPreferences")
    Thread.sleep(1000)
    iid
  }

  val getBalance: String => Future[Long] = iid => Future {
    println(s"${System.currentTimeMillis()} getBalance")
    Thread.sleep(1000)
    System.currentTimeMillis
  }

}
