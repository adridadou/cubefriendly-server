package org.cubefriendly.rest

import org.cubefriendly.manager.{CubeSearchResult, CubeSearchResultEntry}
import org.cubefriendly.processors.Language
import spray.json.{CollectionFormats, DefaultJsonProtocol}

/**
 * Cubefriendly
 * Created by davidroon on 27.06.15.
 * This code is released under Apache 2 license
 */
trait Protocols extends DefaultJsonProtocol with CollectionFormats {
  //Put here case class transformation
  implicit val languageFormat = jsonFormat1(Language.apply)
  implicit val cubeSearchResultFormatEntry = jsonFormat1(CubeSearchResultEntry)
  implicit val cubeSearchResultFormat = jsonFormat1(CubeSearchResult.apply)
  implicit val dimensionQueryFunctionFormat = jsonFormat3(DimensionQueryFunction.apply)
  implicit val dimensionQueryFormat = jsonFormat3(DimensionQuery.apply)
  implicit val transformFunctionFormat = jsonFormat2(TransformFunction.apply)
  implicit val cubeQueryFormat = jsonFormat5(CubeQuery.apply)
  implicit val valuesQueryFormat = jsonFormat6(ValuesQuery.apply)
}
