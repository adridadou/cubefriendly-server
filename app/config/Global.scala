package config

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scala.language.postfixOps

import play.api._
import play.api.mvc._

/**
 * Cubefriendly
 * Created by david on 05.04.15.
 */
object Global extends WithFilters(new AddCORSHeader())
with GlobalSettings {

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