package controllers

import java.io.File

import config.AppConfig
import org.cubefriendly.processors.{CubeConfig, DataHeader, DataProcessor, DataProcessorProvider}
import org.mapdb.DB
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.Files.TemporaryFile

/**
 * Cubefriendly
 * Created by david on 19.04.15.
 */
class SourceProcessorRestSpec extends Specification with Mockito {

  "Source processor REST API" should {
    "load the source file" in {
      val service = mock[SourceManagementService]
      val provider = mock[DataProcessorProvider]
      val config = mock[AppConfig]
      val api = new SourceProcessorRestController(service, provider, config)
      //Given
      val source = TemporaryFile.apply("cubefriendly", "source")
      //When
      api.load(source)
      //Then
      there was one(service).moveToProcessDirectory(source)
    }

    "get the header of a source file" in {
      //Given
      val filename = "filename"
      val file = mock[File]
      val service = mock[SourceManagementService]
      val provider = mock[DataProcessorProvider]
      val processor = mock[DataProcessor]
      val config = mock[AppConfig]
      val api = new SourceProcessorRestController(service, provider, config)
      val header = mock[DataHeader]

      service.getSource(filename) returns file
      provider.forSource(file) returns processor
      processor.header() returns header
      //When
      val actual = api.header(filename)
      //Then
      there was one(service).getSource(filename)
      there was one(provider).forSource(file)
      there was one(processor).header()
      actual must be(header)
    }

    "import the source file" in {
      //Given
      val filename = "filename"
      val file = mock[File]
      val target = mock[File]
      val service = mock[SourceManagementService]
      val provider = mock[DataProcessorProvider]
      val processor = mock[DataProcessor]
      val config = mock[AppConfig]
      val api = new SourceProcessorRestController(service, provider, config)
      val db = mock[DB]
      val cubeConfig = CubeConfig(name = "name", metrics = Seq())
      service.getSource(filename) returns file
      service.getCube(cubeConfig.name) returns db
      provider.forSource(file) returns processor
      //When
      api.importFile(filename, cubeConfig)
      //Then
      there was one(service).getSource(filename)
      there was one(service).getCube(cubeConfig.name)
      there was one(provider).forSource(file)
      there was one(processor).process(cubeConfig, db)
    }
  }
}
