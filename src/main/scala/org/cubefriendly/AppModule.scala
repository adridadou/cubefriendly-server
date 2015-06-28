package org.cubefriendly

import com.typesafe.config.{Config, ConfigFactory}
import scaldi.Module

/**
 * Cubefriendly
 * Created by david on 13.06.15.
 * This code is released under Apache 2 license
 */
class AppModule extends Module {
  bind[Config] to ConfigFactory.load()
}
