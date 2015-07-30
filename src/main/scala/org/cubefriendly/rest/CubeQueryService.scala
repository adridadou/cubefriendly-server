package org.cubefriendly.rest

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.Config
import org.cubefriendly.manager.CubeManager

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
        path("dsd" / Rest) { name =>
          complete(ToResponseMarshallable(manager.dsd(name)))
        } ~ (path("query") & post & entity(as[CubeQuery])) { query =>
            val response = manager.query(query)
            val records = response.map({case (vector,metrics) => CubeQueryRecord(vector, metrics)}).toSeq
            complete(ToResponseMarshallable(CubeQueryResponse(records)))
        }
      }
    }
  }

  implicit def executor: ExecutionContextExecutor

  def config: Config
}

case class CubeQuery(name: String)

case class CubeQueryRecord(vector:Seq[String], metrics:Seq[String])
case class CubeQueryResponse(data:Seq[CubeQueryRecord])


