package org.cubefriendly.rest

import akka.event.NoLogging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.cubefriendly.manager.CubeManager
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

}
