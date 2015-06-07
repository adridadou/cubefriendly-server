import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.ConfigFactory
import org.cubefriendly.processors.DataProcessorProvider
import org.cubefriendly.rest.CubefriendlyService

/**
 * Cubefriendly
 * Created by david on 24.05.15.
 */
object CubefriendlyServer extends App with CubefriendlyService {

  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()

  override val logger = Logging(system, getClass)
  Http().bindAndHandle(corsRoutes, config.getString("http.interface"), config.getInt("http.port"))
}
