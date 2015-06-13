package org.cubefriendly.rest

import java.io.File

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.stream.FlowMaterializer
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
  implicit val materializer: FlowMaterializer
  implicit val manager: CubeManager

  def config: Config
  val logger: LoggingAdapter

  private def cubeDirectory:String = config.getString("services.cubefriendly.cubes")
  private def cubeFileName(name:String) = cubeDirectory + "/" + name + ".cube"

  private def cubeFile(name:String) : Option[File] = {
    val directory = new File(cubeDirectory)
    if(directory.exists()) {
      val cube = new File(cubeFileName(name))
      if(cube.exists() && cube.isFile) {
        Some(cube)
      }else {
        None
      }
    }else {
      directory.mkdirs()
      None
    }
  }

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
    cubeFile(filename).foreach(_.delete())
    val dest = cubeFile(filename).getOrElse(new File(cubeFileName(filename)))

    bodyPart.entity.dataBytes.runFold(new CsvProcessor(dest))({ (processor, byteString) =>
      processor.process(byteString.decodeString("UTF-8").toCharArray)
    }).map(_.complete().close())
  }

  val optionsSupport = {
    options {complete("")}
  }

  val corsHeaders = List(RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, DELETE"),
    RawHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cache-Control") )
}
