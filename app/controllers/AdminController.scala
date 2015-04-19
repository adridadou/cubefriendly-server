package controllers

import java.io.File

import org.cubefriendly.processors.CsvProcessor
import play.api.Play
import play.api.Play.current
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Cubefriendly
 * Created by david on 05.04.15.
 */
object AdminController extends Controller{

  lazy val toProcessDirectory = Play.application.configuration.getString("process.directory").get

  def getCsvHeaderFile(filename: String) = Action {
    val csvFile = new File(toProcessDirectory + "/" + filename)
    val header = CsvProcessor(csvFile).header
    Ok(Json.obj("separator" -> header.separator, "dimensions" -> Json.toJson(header.dimensions)))
  }

  def loadOpt = Action {
    Ok("ok")
  }

  def listPending() = Action {
    Ok(Json.toJson(new File(toProcessDirectory).list().map(name =>Json.obj("file" -> name))))
  }

  def load = Action(parse.multipartFormData) { request =>
    request.body.file("source").map { source =>
      val filename = source.filename
      val directory = new File(toProcessDirectory)
      directory.mkdir()
      source.ref.moveTo(new File(toProcessDirectory + "/" + filename),replace = true)
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.Application.index()).flashing(
        "error" -> "Missing file"
      )
    }
  }

}
