import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import org.cubefriendly.manager.{CubeManagerModule, CubeManager}
import org.cubefriendly.processors.DataProcessorProvider
import org.cubefriendly.rest.SourceService
import scaldi.{Injectable, Injector}

/**
 * Cubefriendly
 * Created by david on 24.05.15.
 */
object CubefriendlyServer extends App with SourceService with Injectable{

  implicit val appModule = new CubeManagerModule
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()
  override implicit val manager = inject[CubeManager]

  override val config = ConfigFactory.load()

  override val logger = Logging(system, getClass)

  val corsRoutes = {
    respondWithHeaders(corsHeaders) {sourceRoutes ~ optionsSupport}
  }

  Http().bindAndHandle(corsRoutes, config.getString("http.interface"), config.getInt("http.port"))
}
