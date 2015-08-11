import akka.actor._
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scaldi.{Injector, Module}
import scaldi.akka.AkkaInjectable

import scala.util.{Failure, Random, Success, Try}

/**
 * @author nick
 * @since 15/07/12
 */

object Util {
  val rand = new Random()

  def sleep(ms: Long) = Thread.sleep(ms)

  def randomSleep(max: Int) = sleep(rand.nextInt(max))

  def randomString(prefix: String, suffix: String = "-") = {
    prefix + Math.abs(rand.nextInt()) + suffix
  }
}


abstract class RuleBase(implicit inj: Injector) extends WorkingMemoryClient with AkkaInjectable {

  implicit val system = inject[ActorSystem]

  override protected[this] def workingMemory = injectActorRef[WorkingMemory]
  protected[this] val logger = Logger(LoggerFactory.getLogger(getClass))

  def logic: () => Boolean
}

class Rule1(implicit inj: Injector) extends RuleBase {

  override def logic = () => {
    logger.info( """Rule1: Reading "A"""")
    val x = read("A")
    if (x.value.get.asInstanceOf[Int] <= 0) {
      logger.info( """Rule1: Reading "B""")
      write("B", x)
      false
    } else true
  }
}

class Rule2(implicit inj: Injector) extends RuleBase {

  override def logic = () => {
    logger.info( """Rule2: Writing "A""")
    write("A", Random.nextInt())
    true
  }
}

class Rule3(implicit inj: Injector) extends RuleBase {

  override def logic = () => {
    logger.info( """Rule3: Reading "B""")
    Try(read("B").value.get.asInstanceOf[Int] > 0) match {
      case Success(value) =>
        logger.info( s"""Rule3: "B" = $value""")
        true
      case Failure(e) =>
        logger.warn(e.getMessage)
        false
    }

  }
}

class AkkaModule extends Module {
  bind [ActorSystem] to ActorSystem("toy-rules-runner") destroyWith (_.shutdown())
}

class AppModule extends Module {
  binding toProvider new WorkingMemory
  binding toProvider new RulesEngine
  bind [Rule1] to new Rule1
  bind [Rule2] to new Rule2
  bind [Rule3] to new Rule3
}

class RulesModule extends Module with AkkaInjectable {

  lazy val rule1 = inject[Rule1]
  lazy val rule2 = inject[Rule2]
  lazy val rule3 = inject[Rule3]

  binding identifiedBy "ruleDefs" to Seq(
    rule1.logic, rule2.logic, rule3.logic
  )
}

object ToyRulesRunner extends App with AkkaInjectable {

  implicit val appModule = new AppModule :: new RulesModule :: new AkkaModule
  implicit val system = inject[ActorSystem]

  val rulesEngine = injectActorRef[RulesEngine]
  rulesEngine ! RulesEngine.ExecuteRules
}