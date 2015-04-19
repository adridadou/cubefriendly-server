package controllers

import java.io.File

import config.AppConfig
import org.cubefriendly.processors.{CubeConfig, DataProcessorProvider}
import org.mapdb.{DB, DBMaker}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.Controller
import scaldi.Module


/**
 * Cubefriendly
 * Created by david on 19.04.15.
 */
class SourceProcessorRestController(service: SourceManagementService, provider: DataProcessorProvider) extends Controller {
  def importFile(filename: String, config: CubeConfig) = {
    val source = service.getSource(filename)
    val db = service.getCube(config.name)
    provider.forSource(source).process(config, db)
  }


  def header(filename: String) = provider.forSource(service.getSource(filename)).header()

  def load(file: TemporaryFile) = {
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