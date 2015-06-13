package org.cubefriendly

import com.typesafe.config.{ConfigFactory, Config}
import scaldi.Module

/**
 * Cubefriendly
 * Created by david on 13.06.15.
 */
class AppModule extends Module {
  bind[Config] to ConfigFactory.load()
}
