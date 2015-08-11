import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent._
import scala.concurrent.duration._

/**
 * @author nick
 * @since 15/08/11
 */
trait WorkingMemoryClient {

  import WorkingMemory._

  protected[this] implicit def workingMemory: ActorRef
  protected[this] implicit def timeout = Timeout(5.seconds)

  def read(key: String) = {

    val future = workingMemory ? Get(key)
    val result = Await.result(future, timeout.duration)

    result.asInstanceOf[Results]
  }

  def write(key: String, value: Any) = workingMemory ! Put(key, value)
}
