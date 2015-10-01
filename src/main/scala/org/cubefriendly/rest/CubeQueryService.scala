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
        }}
      } ~ pathPrefix("query") {
        (path("cube") & post & entity(as[CubeQuery])){ query =>
          extractRequestContext { ctx =>
            import spray.json._
            val transformedResponse = manager.query(query).map({ case (vector, metrics) =>
              ",[" + vector.toJson.compactPrint + "," + metrics.toJson.compactPrint + "]"
            })
            val firstElement = transformedResponse.next().substring(1)
            val responseSource = Source(() => transformedResponse.map(ByteString.apply))
            val jsonStream = Source.concat(Source.concat(Source.single(ByteString("[" + firstElement)), responseSource), Source.single(ByteString("]")))
            complete(HttpResponse(entity = Chunked.fromData(`application/json`, jsonStream)))
          }
        } ~(path("values") & post & entity(as[ValuesQuery])) {query =>
          complete(ToResponseMarshallable(manager.query(query)))
        }
      }
    }
  }

  implicit def executor: ExecutionContextExecutor

  def config: Config
}
case class DimensionQueryFunction(name:String, lang:Option[String], args:Map[String, String])
case class TransformFunction(func:String, args:Map[String, String])
case class CubeQuery(cube: String, dimensions:Map[String,DimensionQuery], eliminate:Option[Seq[String]],transform:Option[TransformFunction], lang:Option[String])
case class DimensionQuery(indexes:Option[Seq[Int]], values:Option[Seq[String]], functions:Option[Seq[DimensionQueryFunction]])

case class ValuesQuery(cube:String, dimension:String, func:String,params:Map[String, String], limit:Option[Int], lang:Option[String] = None)
