package org.cubefriendly.rest

import java.io.File

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.Config
import org.cubefriendly.manager.CubeManager
import org.cubefriendly.processors.DataProcessorProvider

import scala.concurrent.ExecutionContextExecutor

/**
 * Cubefriendly
 * Created by david on 24.05.15.
 * This code is released under Apache 2 license
 */

trait SourceService extends Protocols {

  implicit val system: ActorSystem
  implicit val materializer: Materializer
  implicit val manager: CubeManager
  implicit val provider: DataProcessorProvider

  val logger: LoggingAdapter
  val sourceRoutes = {
    logRequestResult("cubefriendly-microservice") {
      pathPrefix("source") {
        path("list") {
          complete {
            manager.list()
          }
        } ~ (path("upload") & post & entity(as[FormData])){
          formData => complete {
            formData.parts.runForeach(upload).map(u => ToResponseMarshallable(Map("result" -> "success")))
          }
        } ~ path("delete" / Rest) {cube =>
          complete(
            if (manager.delete(cube)) HttpResponse(200) else HttpResponse(404)
          )
        }
      }
    }
  }

  implicit def executor: ExecutionContextExecutor

  def config: Config

  private def upload(bodyPart:FormData.BodyPart):Unit = {
    val filename = bodyPart.filename.getOrElse("upload")
    manager.cubeFile(filename).foreach(_.delete())
    val dest = manager.cubeFile(filename).getOrElse(new File(manager.cubeFileName(filename)))
    val processor = provider.getProcessorByFilename(filename,dest)
    bodyPart.entity.dataBytes.runFold(processor)({ (processor, byteString) =>
      processor.process(byteString.decodeString(processor.defaultEncoding).toCharArray)
    }).map(_.complete().close())
  }
}
