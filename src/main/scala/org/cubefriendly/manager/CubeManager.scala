package org.cubefriendly.manager

import java.io.File

import com.typesafe.config.Config
import scaldi.Module

/**
 * Cubefriendly
 * Created by david on 13.06.15.
 */
trait CubeManager {
  def list():CubeSearchResult
  def delete(name:String) :Unit
  def cubeFile(name:String):Option[File]
  def cubeFileName(name:String) :String
}

class CubeManagerImpl(config:Config) extends CubeManager{

  private val cubeDirectory:File = new File(config.getString("services.cubefriendly.cubes"))
  override def cubeFileName(name:String):String = cubeDirectory + "/" + name + ".cube"

  override def list(): CubeSearchResult = {
    val cubes = Option(cubeDirectory.list()).map(_.toSeq).getOrElse(Seq())
    val entries = cubes.map(f => CubeSearchResultEntry(f))
    CubeSearchResult(entries)
  }

  override def cubeFile(name:String):Option[File] = {
    if(cubeDirectory.exists()) {
      val cube = new File(cubeFileName(name))
      if(cube.exists() && cube.isFile) {
        Some(cube)
      }else {
        None
      }
    }else {
      cubeDirectory.mkdirs()
      None
    }
  }

  override def delete(name: String): Unit = {
    cubeFile(name).foreach(_.delete())
  }
}

case class CubeSearchResult(entries:Seq[CubeSearchResultEntry])
case class CubeSearchResultEntry(name:String)


class CubeManagerModule extends Module {
  bind[CubeManager] to injected[CubeManagerImpl]
}
