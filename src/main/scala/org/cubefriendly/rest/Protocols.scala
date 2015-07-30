package org.cubefriendly.rest

import org.cubefriendly.data.Dimension
import org.cubefriendly.manager.{CubeSearchResult, CubeSearchResultEntry, Dsd}
import spray.json.{CollectionFormats, DefaultJsonProtocol}

/**
 * Cubefriendly
 * Created by davidroon on 27.06.15.
 * This code is released under Apache 2 license
 */
trait Protocols extends DefaultJsonProtocol with CollectionFormats {
  //Put here case class transformation
  implicit val cubeSearchResultFormatEntry = jsonFormat1(CubeSearchResultEntry)
  implicit val cubeSearchResultFormat = jsonFormat1(CubeSearchResult.apply)
  implicit val dimensionFormat = jsonFormat2(Dimension.apply)
  implicit val dsdFormat = jsonFormat2(Dsd.apply)
  implicit val cubeQueryFormat = jsonFormat1(CubeQuery.apply)
  implicit val cubeQueryRecordFormat = jsonFormat2(CubeQueryRecord.apply)
  implicit val cubeQueryResponseFormat = jsonFormat1(CubeQueryResponse.apply)
}
