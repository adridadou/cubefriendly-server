package org.cubefriendly.manager

import scaldi.Module

/**
 * Cubefriendly
 * Created by david on 13.06.15.
 */
trait CubeManager {
  def list():CubeSearchResult
}

class CubeManagerImpl extends CubeManager{
  override def list(): CubeSearchResult = CubeSearchResult(Seq(CubeSearchResultEntry("test1"),CubeSearchResultEntry("test2")))
}

case class CubeSearchResult(entries:Seq[CubeSearchResultEntry])
case class CubeSearchResultEntry(name:String)


class CubeManagerModule extends Module {
  bind[CubeManager] to new CubeManagerImpl
}