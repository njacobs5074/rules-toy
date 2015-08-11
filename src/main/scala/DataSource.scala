import akka.actor.{Actor, ActorLogging, Props}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

/**
 * @author nick
 * @since 15/08/11
 */
object DataSource {
  def props(name: String)(implicit inj: Injector) = Props(new DataSource(name))

  case class Acquire()

  case class DataReady(name: String, data: Any)

}

class DataSource(name: String)(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {

  import DataSource._
  import WorkingMemory._

  val workingMemory = injectActorRef[WorkingMemory]

  override def preStart() = log.info(s"$name started...")

  override def postStop() = log.info(s"$name stopped.")

  override def receive = {

    case Acquire =>
      log.info(s"Acquiring $name for ${sender().path}...")
      Util.randomSleep(3 * 1000)
      workingMemory ! Put(name, Util.rand.nextInt())
  }
}