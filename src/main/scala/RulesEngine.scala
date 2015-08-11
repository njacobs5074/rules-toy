import akka.actor.{Actor, ActorLogging, Props}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

/**
 * @author nick
 * @since 15/08/11
 */
object RulesEngine {

  case class ExecuteRules()

}

class RulesEngine(implicit inj: Injector) extends Actor with ActorLogging with AkkaInjectable {

  import Rule._
  import RulesEngine._
  import WorkingMemory._

  val ruleDefs = inject[Seq[() => Boolean]]
  val workingMemory = injectActorRef[WorkingMemory]

  val rules = ruleDefs.map { ruleDef =>
    context.actorOf(Rule.props(ruleDef), name = Util.randomString("Rule-"))
  }

  override def receive = {

    case ExecuteRules =>
      rules.foreach { rule =>
        log.info(s"Executing $rule")
        rule ! ExecuteRule
      }

    case ruleExecuted: RuleExecuted =>
      log.info(s"Received $ruleExecuted from ${sender()}")
      workingMemory ! Put(sender().path.toString, Some(ruleExecuted.result))
  }

}