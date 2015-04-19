package controllers

import java.io.File

import config.AppConfig
import org.cubefriendly.data.Cube
import org.cubefriendly.processors.{CubeConfig, DataHeader, DataProcessorProvider}
import org.mapdb.{DB, DBMaker}
import play.api.libs.Files.TemporaryFile
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import scaldi.Module


/**
 * Cubefriendly
 * Created by david on 19.04.15.
 */
class SourceProcessorRestController(service: SourceManagementService, provider: DataProcessorProvider, config: AppConfig) extends Controller {

  implicit val configRead: Reads[CubeConfig] = ((JsPath \ "name").read[String] and (JsPath \ "dimensions").read[Seq[String]])(CubeConfig.apply _)
  implicit val headerWrites: Writes[DataHeader] = new Writes[DataHeader] {
    override def writes(header: DataHeader): JsValue = {
      Json.obj(
        "dimensions" -> Json.toJson(header.dimensions)
      )
    }
  }

  def importFileForm(filename: String) = Action { implicit request =>
    (for (
      data <- request.body.asFormUrlEncoded;
      config <- getConfig(data)
    ) yield {
        val cube = importFile(filename, config)
        Ok("cube " + cube.name + " has been successfully created")
      }).headOption.getOrElse(BadRequest("could not import cube"))
  }

  def importFile(filename: String, config: CubeConfig): Cube = {
    val source = service.getSource(filename)
    val db = service.getCube(config.name)
    provider.forSource(source).process(config, db)
  }

  private def getConfig(data: Map[String, Seq[String]]): Option[CubeConfig] = {
    data.get("config").map(_.headOption.map(Json.parse(_).as[CubeConfig])).flatten
  }

  def headerAction(filename: String) = Action { implicit request =>
    Ok(Json.toJson(header(filename)))
  }

  def header(filename: String) = provider.forSource(service.getSource(filename)).header()

  def listPending() = Action {
    Ok(Json.toJson(new File(config.processDirectory).list().map(name => Json.obj("file" -> name))))
  }

  def loadForm = Action(parse.multipartFormData) { request =>
    request.body.file("source").map { source =>
      load(source.ref)
      Ok("File uploaded")
    }.getOrElse(BadRequest("Missing file"))
  }

  def load(file: TemporaryFile): Unit = {
    service.moveToProcessDirectory(file)
  }
}

class SourceManagementService(config: AppConfig) {
  def moveToProcessDirectory(file: TemporaryFile): Unit = {
    file.moveTo(new File(config.processDirectory + "/" + file.file.getName), replace = true)
  }

  def getSource(filename: String) = new File(config.processDirectory + "/" + filename)

  def getCube(name: String): DB = DBMaker.newFileDB(new File(config.cubesDirectory + "/" + name + ".cube")).make()

}

class SourceProcessorModule extends Module {
  binding to injected[SourceManagementService]
}