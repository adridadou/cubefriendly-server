package controllers

import config.{AppConfig, PlayAppConfig}
import scaldi.Module

/**
 * Cubefriendly
 * Created by david on 19.04.15.
 */
class WebModule extends Module {
  binding to new AdminRestController
  binding to injected[SourceProcessorRestController]
  bind[AppConfig] to new PlayAppConfig
}
