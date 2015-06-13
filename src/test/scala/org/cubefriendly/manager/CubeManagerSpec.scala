package org.cubefriendly.manager

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

/**
 * Cubefriendly
 * Created by david on 13.06.15.
 */
class CubeManagerSpec extends FlatSpec with Matchers with MockFactory{
  "CubeManagerImpl" should "return the list of cubes available" in {
    val manager = new CubeManagerImpl()
    manager.list().entries should contain theSameElementsAs Seq(CubeSearchResultEntry("test1"),CubeSearchResultEntry("test2"))
  }
}
