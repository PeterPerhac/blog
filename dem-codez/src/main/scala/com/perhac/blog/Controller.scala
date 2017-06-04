import cats.instances.FutureInstances
import cats.syntax.ApplicativeSyntax
import scala.concurrent.Future

trait BaseController extends ApplicativeSyntax with FutureInstances  {
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
}

object MyController extends BaseController {
  val act: String => Future[String] = _.toUpperCase.pure
}
