```/**
  * This class is used to allow conditional executions in for-comprehensions
  * e.g.
  * <pre>
  * for {
  * foo <- something()
  * _ <- somethingElse() onlyIf condition
  * } yield (foo)
  * </pre>
  *
  * Here somethingElse() only executes if condition is true
  *
  * Also, note that this can be removed once we upgrade cats library to 1.0
  * new version of cats includes .whenA syntax for doing this kind of stuff
  */
```



https://github.com/hmrc/vat-registration-frontend/blob/master/app/common/ConditionalFlatMap.scala







```
package common

import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConditionalFlatMapSpec extends UnitSpec with ScalaFutures {

  private trait TestDummy {
    def methodToCall(s: String): Future[Unit]
  }

  private class Setup {
    val mockDummy = Mockito.mock(classOf[TestDummy])
    when(mockDummy.methodToCall("testString")).thenReturn(Future.successful(()))
  }

  import ConditionalFlatMap._
  import cats.instances.future._

  "an arbitrary boolean condition is true" should {

    "invoke a method on a service in a for-comprehension" in new Setup() {
      val arbitraryCondition = true
      val futureFoo = for {
        foo <- Future.successful("testString")
        _ <- mockDummy.methodToCall(foo) onlyIf arbitraryCondition
      } yield foo

      whenReady(futureFoo) { result =>
        result shouldBe "testString"
        verify(mockDummy, times(1)).methodToCall("testString")
      }
    }

  }


  "an arbitrary boolean condition is false" should {

    "NOT invoke a method on a service in a for-comprehension" in new Setup() {
      val arbitraryCondition = false
      val futureFoo = for {
        foo <- Future.successful("testString")
        _ <- mockDummy.methodToCall(foo) onlyIf arbitraryCondition
      } yield foo

      whenReady(futureFoo) { result =>
        result shouldBe "testString"
        verify(mockDummy, never()).methodToCall("testString")
      }
    }

  }

}
```


https://github.com/hmrc/vat-registration-frontend/blob/master/test/common/ConditionalFlatMapSpec.scala

