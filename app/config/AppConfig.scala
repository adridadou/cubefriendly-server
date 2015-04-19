package config

import play.api.Play
import play.api.Play.current

/**
 * Cubefriendly
 * Created by david on 19.04.15.
 */
trait AppConfig {
  def cubesDirectory: String

  def processDirectory: String
}

class PlayAppConfig extends AppConfig {
  lazy val processDirectory = Play.application.configuration.getString("process.directory").get
  lazy val cubesDirectory = Play.application.configuration.getString("cubes.directory").get
}
