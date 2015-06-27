package org.cubefriendly.rest

import java.io.File

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.config.Config
import org.cubefriendly.manager.{CubeManager, CubeSearchResult, CubeSearchResultEntry}
import org.cubefriendly.processors.CsvProcessor
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContextExecutor

/**
 * Cubefriendly
 * Created by david on 24.05.15.
 */

trait Protocols extends DefaultJsonProtocol {
  //Put here case class transformation
  implicit val messageFormat = jsonFormat1(MessageResult)

  implicit val cubeSearchResultFormatEntry = jsonFormat1(CubeSearchResultEntry)
  implicit val cubeSearchResultFormat =  jsonFormat1(CubeSearchResult.apply)
}

case class MessageResult(message:String)

trait SourceService extends Protocols {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  implicit val manager: CubeManager

  def config: Config
  val logger: LoggingAdapter

  val sourceRoutes = {
    logRequestResult("cubefriendly-microservice") {
      pathPrefix("admin" / "source") {
        path("list") {
          complete {
            manager.list()
          }
        } ~ (path("upload") & post & entity(as[FormData])){
            formData => complete {formData.parts.runForeach(upload).map(u => MessageResult("upload successful!"))}
        }
      }
    }
  }

  private def upload(bodyPart:FormData.BodyPart):Unit = {
    // read a upload file, but not execute this block
    val filename = bodyPart.filename.getOrElse("upload")
    manager.cubeFile(filename).foreach(_.delete())
    val dest = manager.cubeFile(filename).getOrElse(new File(manager.cubeFileName(filename)))

    bodyPart.entity.dataBytes.runFold(new CsvProcessor(dest))({ (processor, byteString) =>
      processor.process(byteString.decodeString("UTF-8").toCharArray)
    }).map(_.complete().close())
  }
}
