package org.cubefriendly.rest

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity.Chunked
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.config.Config
import org.cubefriendly.manager.CubeManager
import org.cubefriendly.processors.Language

import scala.concurrent.ExecutionContextExecutor

/**
 * Cubefriendly
 * Created by davidroon on 27.06.15.
 * This code is released under Apache 2 license
 */
trait CubeQueryService extends Protocols {
  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit val manager: CubeManager
  val logger: LoggingAdapter
  val cubeQueryRoutes = rejectEmptyResponse {
    logRequestResult("cubefriendly-microservice") {
      pathPrefix("cube") {
        path("dimensions" / Segment) { name => parameter('lang.?) { lang =>
          complete(ToResponseMarshallable(manager.dimensions(name, lang.map(Language.apply))))
        }} ~ path("values" / Segment / Segment){ (cube,dimension) =>
          parameter('lang.?){ lang =>
            complete(ToResponseMarshallable(manager.values(cube,dimension,lang.map(Language.apply))))
        }} ~ path("query") {
          (path("cube") & post & entity(as[CubeQuery])){ query =>
            extractRequestContext { ctx =>
              import spray.json._
              val transformedResponse = manager.query(query).map({ case (vector, metrics) =>
                "[" + vector.toJson.compactPrint + "," + metrics.toJson.compactPrint + "]"
              })
              val responseSource = Source(() => new IteratorDecorator(transformedResponse, "[", "]").map(ByteString.apply))
              complete(HttpResponse(entity = Chunked.fromData(`application/json`, responseSource)))
            }
          } ~(path("values") & post & entity(as[ValuesQuery])) {query =>
            complete(ToResponseMarshallable(manager.query(query)))
          }
        }
      }
    }
  }

  implicit def executor: ExecutionContextExecutor

  def config: Config
}
case class DimensionQueryFunction(name:String, args:Seq[String])
case class CubeQuery(source: String, dimensions:Map[String,DimensionQuery] = Map(), lang:Option[Language] = None)
case class DimensionQuery(indexes:Seq[Int] = Seq(), values:Seq[String] = Seq(), functions:Seq[DimensionQueryFunction] = Seq())


case class ValuesQuery(cubes:Vector[String], dimension:String, func:String,params:Vector[String], lang:Option[Language])

class IteratorDecorator(val iterator:Iterator[String], firstValue:String, lastValue:String) extends Iterator[String] {
  private var isFirst = true
  private var afterLast = false
  override def hasNext: Boolean = iterator.hasNext || !afterLast

  override def next(): String = {
    if(isFirst) {
      isFirst = false
      firstValue
    } else if (iterator.hasNext){
      iterator.next()
    }else {
      afterLast = true
      lastValue
    }
  }
}
