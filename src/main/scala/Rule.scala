import akka.actor.{Actor, ActorLogging, Props}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

/**
 * @author nick
 * @since 15/08/11
 */
object Rule {
  def props(logic: () => Boolean)(implicit inj: Injector) = Props(new Rule(logic))

  case class ExecuteRule()

  case class RuleExecuted(result: Any)

}

class Rule(logic: () => Boolean)(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {

  import Rule._

  val rulesEngine = injectActorRef[RulesEngine]

  override def receive = {
    case ExecuteRule =>
      log.info(s"Executing $logic for ${rulesEngine.path.toString}...")
      rulesEngine ! RuleExecuted(logic())
  }
}