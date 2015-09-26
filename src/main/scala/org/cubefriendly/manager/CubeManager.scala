package org.cubefriendly.manager

import java.io.File
import java.util.Date

import com.typesafe.config.Config
import org.cubefriendly.data.{Cube, QueryBuilder}
import org.cubefriendly.processors.Language
import org.cubefriendly.reflection.DimensionValuesSelector
import org.cubefriendly.rest.{CubeQuery, DimensionQuery, DimensionQueryFunction, ValuesQuery}
import org.joda.time.DateTime
import scaldi.Module

import scala.collection.mutable

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
  def query(query:ValuesQuery) : Vector[String]
  def dimensions(cube: String, language: Option[Language]): Seq[String]
  def values(cube:String, dimension:String, lang:Option[Language]): Seq[String]
}

class CubeManagerImpl(config:Config) extends CubeManager{

  val cacheSize = 1000

  val scalaCache:collection.mutable.Map[String, Cube] = mutable.Map.empty
  val entries:mutable.Map[String, DateTime] = mutable.Map.empty

  private def openCube(name:String) : Option[Cube] = cubeFile(name).map(openCube)

  private def openCube(file:File) : Cube = {
    this.synchronized {
      val key = file.getAbsolutePath
      entries += key -> DateTime.now()
      scalaCache.get(key) match {
        case Some(cube) => cube
        case None =>
          scalaCache.put(key,Cube.open(file))
          if(entries.size > cacheSize) {
            val keyToRemove = entries.min(Ordering.by[(String,DateTime), Date]({case (_,value) => value.toDate}))._1
            scalaCache(keyToRemove).close()
            scalaCache.remove(keyToRemove)
          }
          scalaCache(key)
      }
    }

  }

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

  override def dimensions(name: String, optLang:Option[Language]): Seq[String] = cubeFile(name).map(openCube).map(cube => {
    optLang.map(cube.dimensions).getOrElse(cube.dimensions())
  }).getOrElse(Seq())

  override def values(name:String, dimension:String, optLang:Option[Language]): Seq[String] = cubeFile(name).map(openCube).map(cube => {
    optLang.map(cube.dimension(dimension,_)).getOrElse(cube.dimension(name)).values
  }).getOrElse(Seq())

  override def query(query: CubeQuery): Iterator[(Vector[String],Vector[String])] = {
    cubeFile(query.source).map(openCube).map(QueryBuilder.query).map(queryObject => {
      val values = query.dimensions.map({
        case (dimension,dimensionQuery) => dimension -> getSelectedElements(queryObject.cube,dimension,dimensionQuery)
      })
      queryObject.where(values)
      queryObject.run()
    }).getOrElse(Iterator.empty)
  }

  override def query(query: ValuesQuery): Vector[String] = {
    val lang = query.lang.map(Language.apply)
    openCube(query.cube).map( cube =>{
    val func = DimensionValuesSelector.funcs(query.func)
      lang match {
        case Some(language) => cube.searchDimension(query.dimension,language, func,query.params,query.limit)
        case None => cube.searchDimension(query.dimension, func,query.params, query.limit)
      }
    }
    ).getOrElse(Vector[String]())
  }

  private def getSelectedElements(cube:Cube, dimension:String, query:DimensionQuery) : Vector[String] = {
    val values = cube.dimension(dimension).values
    (query.values ++ query.indexes.map(values.apply) ++ query.functions.flatMap(getElementsFromFunction(cube,dimension,_))).toVector
  }

  private def getElementsFromFunction(cube:Cube, dimension:String, function:DimensionQueryFunction) : Vector[String] = {
    val func = DimensionValuesSelector.funcs(function.name)
    cube.searchDimension(dimension, func, function.args,None)
  }
}

case class CubeSearchResult(entries:Seq[CubeSearchResultEntry])
case class CubeSearchResultEntry(name:String)


class CubeManagerModule extends Module {
  bind[CubeManager] to injected[CubeManagerImpl]
}
