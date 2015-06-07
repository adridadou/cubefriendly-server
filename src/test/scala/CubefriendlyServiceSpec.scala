import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.cubefriendly.rest.CubefriendlyService
import org.scalatest._

class CubefriendlyServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with CubefriendlyService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  "CubefriendlyService" should "respond to single IP query" in {

  }
}
