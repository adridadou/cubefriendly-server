package org.cubefriendly.manager

import com.typesafe.config.Config
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

/**
 * Cubefriendly
 * Created by david on 13.06.15.
 * This code is released under Apache 2 license
 */
class CubeManagerSpec extends FlatSpec with Matchers with MockFactory{
  "CubeManagerImpl" should "return the list of cubes available" in {
    val config = mock[Config]
    (config.getString _).expects("services.cubefriendly.cubes").returns("src/test/resources/cubes")

    val manager = new CubeManagerImpl(config)
    manager.list().entries should contain theSameElementsAs Seq(CubeSearchResultEntry("test1.cube"),CubeSearchResultEntry("test2.cube"))
  }
}
