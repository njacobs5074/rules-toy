import akka.actor.{ActorRef, Props, ActorLogging, Actor}

import scala.collection.mutable

/**
 * @author nick
 * @since 15/08/11
 */
object WorkingMemory {
  def props() = Props(classOf[WorkingMemory])

  case class Put(key: String, value: Any)

  case class Remove(key: String)

  case class Get(key: String)

  case class Results(key: String, value: Option[Any])

  case class Clear()

  case class Update(key: String)

}

class WorkingMemory extends Actor with ActorLogging {

  import WorkingMemory._

  var memory = mutable.HashMap[String, Any]()
  var waitingReaders = mutable.HashMap[String, mutable.MutableList[ActorRef]]()

  def receive = {

    case dataToAdd: Put =>
      log.info(s"Adding ${dataToAdd.key} -> ${dataToAdd.value}")
      memory.put(dataToAdd.key, dataToAdd.value)
      if (waitingReaders.contains(dataToAdd.key)) {
        waitingReaders.get(dataToAdd.key).get.foreach { actor => actor ! Results(dataToAdd.key, memory.get(dataToAdd.key)) }
        waitingReaders.remove(dataToAdd.key)
      }

    case dataToRemove: Remove =>
      log.info(s"Removing $dataToRemove")
      memory.remove(dataToRemove.key)

    case dataToFetch: Get =>
      val result = memory.get(dataToFetch.key)
      if (result.isDefined) {
        log.info(s"Returning ${dataToFetch.key} -> $result to ${sender().path}")
        sender() ! Results(dataToFetch.key, memory.get(dataToFetch.key))
      }
      else {
        val actorsList = waitingReaders.getOrElseUpdate(dataToFetch.key, mutable.MutableList[ActorRef]())
        actorsList += sender()
      }

    case Clear =>
      log.info(s"Clearing working memory")
      memory.clear()
      waitingReaders.clear()
  }
}

