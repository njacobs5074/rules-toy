import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * @author nick
 * @since 15/07/23
 */
class ToyRulesRunnerSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  import DataSource._

  def this() = this(ActorSystem("ToyRulesRunnerSpec"))

  override def afterAll(): Unit = {
    system.shutdown()
    system.awaitTermination(10 seconds)
  }

  it should "be able to acquire a DataSource" in {
    implicit val testModule = new AppModule :: new RulesModule :: new AkkaModule
    val dataSource = system.actorOf(DataSource.props("TestDS"))
    dataSource ! Acquire
    expectMsgType[DataReady].name.toString should be("TestDS")
  }
}
