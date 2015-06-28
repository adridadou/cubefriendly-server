package org.cubefriendly.rest

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.cubefriendly.manager.{CubeManager, CubeSearchResult, CubeSearchResultEntry}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, _}


class SourceServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with MockFactory with SourceService {
  override val manager = mock[CubeManager]
  override val logger = NoLogging

  override def testConfigSource = "akka.loglevel = WARNING"

  override def config = testConfig

  "SourceService" should "use cube manager to return the list of cubes available" in {
    val cube1 = CubeSearchResultEntry("test1")
    val cube2 = CubeSearchResultEntry("test2")
    val searchResult = CubeSearchResult(Seq(cube1, cube2))

    manager.list _ expects() returning searchResult

    Get("/source/list") ~> sourceRoutes ~> check {
      responseAs[CubeSearchResult].entries should contain theSameElementsAs searchResult.entries
    }


  }

  it should "use cube manager to delete a cube" in {
    manager.delete _ expects "myname" returning true
    manager.delete _ expects "myname2" returning false

    Get("/source/delete/myname") ~> sourceRoutes ~> check {
      status.intValue() shouldEqual 200
    }

    Get("/source/delete/myname2") ~> sourceRoutes ~> check {
      status.intValue() shouldEqual 404
    }
  }
}
