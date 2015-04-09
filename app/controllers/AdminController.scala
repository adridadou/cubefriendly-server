package controllers

import java.io.File
import play.api.Play
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import Play.current

/**
 * Cubefriendly
 * Created by david on 05.04.15.
 */
object AdminController extends Controller{

  lazy val toProcessDirectory = Play.application.configuration.getString("process.directory").get

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
