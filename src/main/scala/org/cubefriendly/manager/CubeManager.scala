package org.cubefriendly.manager

import java.io.File

import com.typesafe.config.Config
import org.cubefriendly.data.{Cube, Dimension, QueryBuilder}
import org.cubefriendly.reflection.DimensionValuesSelector
import org.cubefriendly.rest.{CubeQuery, DimensionQuery, DimensionQueryFunction}
import scaldi.Module

/**
 * Cubefriendly
 * Created by david on 13.06.15.
 */
trait CubeManager {
  def list():CubeSearchResult

  def delete(name: String): Boolean
  def cubeFile(name:String):Option[File]
  def cubeFileName(name:String) :String
  def query(query:CubeQuery) : Iterator[(Vector[String], Vector[String])]

  def dsd(name: String): Option[Dsd]
}

class CubeManagerImpl(config:Config) extends CubeManager{

  private val cubeDirectory:File = new File(config.getString("services.cubefriendly.cubes"))

  override def list(): CubeSearchResult = {
    val cubes = Option(cubeDirectory.list()).map(_.toSeq).getOrElse(Seq())
    val entries = cubes.map(f => CubeSearchResultEntry(f))
    CubeSearchResult(entries)
  }

  override def delete(name: String): Boolean = {
    cubeFile(name).exists(_.delete())
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

  override def cubeFileName(name: String): String = cubeDirectory + "/" + name + ".cube"

  override def dsd(name: String): Option[Dsd] = cubeFile(name).map(Cube.open).map(cube => {
    val dimensions = cube.dimensions().map(cube.dimension)
    Dsd(cube.name(), dimensions)
  })

  override def query(query: CubeQuery): Iterator[(Vector[String],Vector[String])] = {
    cubeFile(query.source).map(Cube.open).map(QueryBuilder.query).map(queryObject => {
      val values = query.dimensions.map({
        case (dimension,dimensionQuery) => dimension -> getSelectedElements(queryObject.cube,dimension,dimensionQuery)
      })
      queryObject.where(values)
      queryObject.run()
    }).getOrElse(Iterator.empty)
  }

  private def getSelectedElements(cube:Cube, dimension:String, query:DimensionQuery) : Vector[String] = {
    val values = cube.dimension(dimension).values
    (query.values ++ query.indexes.map(values.apply) ++ query.functions.flatMap(getElementsFromFunction(cube,dimension,_))).toVector
  }

  private def getElementsFromFunction(cube:Cube, dimension:String, function:DimensionQueryFunction) : Vector[String] = {
    DimensionValuesSelector.funcs(function.name).select(function.args :_*)

  }
}

case class CubeSearchResult(entries:Seq[CubeSearchResultEntry])
case class CubeSearchResultEntry(name:String)


class CubeManagerModule extends Module {
  bind[CubeManager] to injected[CubeManagerImpl]
}

case class Dsd(name: String, dimensions: Vector[Dimension])
