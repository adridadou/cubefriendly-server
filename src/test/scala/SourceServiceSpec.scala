import akka.event.NoLogging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.cubefriendly.rest.SourceService
import org.scalatest._

class SourceServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with SourceService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  "SourceService" should "respond to single IP query" in {
    Get("/admin/source/list") ~> sourceRoutes ~> check {
      handled shouldBe false
    }
  }
}
