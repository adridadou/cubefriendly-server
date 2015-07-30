package org.cubefriendly.rest

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.cubefriendly.manager.{CubeManager, Dsd}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

/**
 * Cubefriendly
 * Created by davidroon on 29.06.15.
 * This code is released under Apache 2 license
 */
class CubeQueryServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with MockFactory with CubeQueryService{

  override val manager = mock[CubeManager]
  override val logger = NoLogging

  override def testConfigSource = "akka.loglevel = WARNING"

  override def config = testConfig

  "CubeQueryService" should "use CubeManager to retrieve the Dsd of a given cube" in {
    val dsd = Dsd("mytest",Vector())
    manager.dsd _ expects "mytest" returning Some(dsd)

    Get("/cube/dsd/mytest") ~> cubeQueryRoutes ~> check {
      status shouldEqual OK
      responseAs[Dsd] shouldEqual dsd
    }
  }


  "CubeQueryService" should "use CubeManager to retrieve the Dsd of a given cube but return 404 if not found" in {
    manager.dsd _ expects "mytest2" returning None

    Get("/cube/dsd/mytest2") ~> Route.seal(cubeQueryRoutes) ~> check {
      status shouldEqual NotFound
    }
  }

  "CubeQueryService" should "use CubeManager to query a cube" in {

    val query = CubeQuery("mytest")

    manager.query _ expects query returns Iterator((Vector("key"),Vector("value")))

    Post("/cube/query",query) ~> cubeQueryRoutes ~> check {
      status shouldEqual OK
      val entity = entityAs[CubeQueryResponse]
      entity.data shouldEqual Seq(CubeQueryRecord(Seq("key"),Seq("value")))
    }
  }

}
