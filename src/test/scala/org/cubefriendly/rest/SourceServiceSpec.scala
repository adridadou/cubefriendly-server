package org.cubefriendly.rest

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.cubefriendly.manager.{CubeManager, CubeSearchResult, CubeSearchResultEntry}
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class SourceServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with MockFactory with SourceService {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val manager = stub[CubeManager]
  override val logger = NoLogging

  "SourceService" should "use cube manager to return the list of cubes available" in {
    val cube1 = CubeSearchResultEntry("test1")
    val cube2 = CubeSearchResultEntry("test2")
    val searchResult = CubeSearchResult(Seq(cube1, cube2))

    manager.list _ when() returns searchResult

    Get("/admin/source/list") ~> sourceRoutes ~> check {
      responseAs[CubeSearchResult].entries should contain theSameElementsAs searchResult.entries
    }
  }
}
