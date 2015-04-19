package config

import controllers.WebModule
import play.api._
import play.api.mvc._
import scaldi.Injector
import scaldi.play.ScaldiSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

/**
 * Cubefriendly
 * Created by david on 05.04.15.
 */
object Global extends WithFilters(new AddCORSHeader())
with GlobalSettings with ScaldiSupport {
  override def applicationModule: Injector = new WebModule
}



class AddCORSHeader extends Filter {
  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = f(rh).map(_.withHeaders(
    "Access-Control-Allow-Methods" -> "POST, GET, OPTIONS, PUT, DELETE",
    "Access-Control-Max-Age" -> "3600",
    "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token, Cache-Control",
    "Access-Control-Allow-Credentials" -> "true",
    "Access-Control-Allow-Origin" -> "*"
  ))
}