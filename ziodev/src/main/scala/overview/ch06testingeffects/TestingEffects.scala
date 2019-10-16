package overview.ch06testingeffects

import zio.{DefaultRuntime, IO, Task, UIO, ZIO}

import scala.util.chaining._

import util._

/*
  type UIO[+A]       = ZIO[Any, Nothing, A]
  type URIO[-R, +A]  = ZIO[R, Nothing, A]
  type Task[+A]      = ZIO[Any, Throwable, A]
  type RIO[-R, +A]   = ZIO[R, Throwable, A]
  type IO[+E, +A]    = ZIO[Any, E, A]
 */
object TestingEffects extends App {

  // ------------------------------------------------------------
  prtTitleObjectName(this)

  val runtime = new DefaultRuntime {}

  def putStrLn(line: String): UIO[Unit] = ZIO.effectTotal(println(line))

  // ------------------------------------------------------------
  prtSubTitle("Environments: #provide, #access, #accessM")

  val zioEnv: ZIO[Int, Nothing, Int] = for {
    env <- ZIO.environment[Int]
    _   <- putStrLn(s"The value of the environment is: $env")
  } yield env
  val zio: IO[Nothing, Int] = zioEnv.provide(42)
  runtime.unsafeRun(zio) tap println

  final case class Config(server: String, port: Int)

  val configString: ZIO[Config, Nothing, String] =
    for {
      server <- ZIO.access[Config](_.server)
      port   <- ZIO.access[Config](_.port)
    } yield s"Server: $server, port: $port"
  val conf: String = runtime.unsafeRun(configString.provide(Config("localhost", 9999))) tap println

  trait DatabaseOps {
    def getTableNames: Task[List[String]]
    def getColumnNames(table: String): Task[List[String]]
  }

  val tablesAndColumns: ZIO[DatabaseOps, Throwable, (List[String], List[String])] =
    for {
      tables  <- ZIO.accessM[DatabaseOps](_.getTableNames)
      columns <- ZIO.accessM[DatabaseOps](_.getColumnNames("user_table"))
    } yield (tables, columns)

  val zioDB = tablesAndColumns.provide(new DatabaseOps {
    override def getTableNames: Task[List[String]] = Task.succeed(List("user_table", "user_table2"))

    override def getColumnNames(table: String): Task[List[String]] =
      Task.succeed(List("col00", "col01", "col02"))
  })
  val tableAndColumns: (List[String], List[String]) = runtime.unsafeRun(zioDB) tap println

  // ------------------------------------------------------------
  prtSubTitle("Providing Environments: #provide")

  // Effects that require an environment cannot be run without first providing their environment to them.
  //
  // The simplest way to provide an effect the environment that it requires is to use the ZIO#provide method.
  //
  // Once you provide an effect with the environment it requires,
  // then you get back an effect whose environment type is Any,
  // indicating its requirements have been fully satisfied.

  val square: ZIO[Int, Nothing, Int] = for {
    env <- ZIO.environment[Int]
  } yield env * env

  val result: UIO[Int] = square.provide(42)
  runtime.unsafeRun(result) tap println

  // ------------------------------------------------------------
  prtSubTitle(s"""${dash(10, "=")} Environmental Effects ${dash(10, "=")}""")

  // ------------------------------------------------------------
  prtSubTitle("Define the Service")

  trait UserID
  trait EmailAddress

  trait UserProfile {
    def name: String
    def email: EmailAddress
  }

  object Database {

    trait Service {
      def lookup(id: UserID): Task[UserProfile]
      def update(id: UserID, profile: UserProfile): Task[Unit]
    }
  }

  trait Database {
    def database: Database.Service
  }

  // ------------------------------------------------------------
  prtSubTitle("Provide Helpers")

  object db {

    def lookup(id: UserID): ZIO[Database, Throwable, UserProfile] =
      ZIO.accessM(_.database.lookup(id))

    def update(id: UserID, profile: UserProfile): ZIO[Database, Throwable, Unit] =
      ZIO.accessM(_.database.update(id, profile))
  }

  // ------------------------------------------------------------
  prtSubTitle("Use the Service")

  val userId = new UserID {}

  val lookedupProfile: ZIO[Database, Throwable, UserProfile] =
    for {
      profile <- db.lookup(userId)
    } yield profile

  // ------------------------------------------------------------
  prtSubTitle("Implement Live Service")

  trait DatabaseLive extends Database {

    def database: Database.Service =
      new Database.Service {
        def lookup(id: UserID): Task[UserProfile]                = ???
        def update(id: UserID, profile: UserProfile): Task[Unit] = ???
      }
  }
  object DatabaseLive extends DatabaseLive

  // ------------------------------------------------------------
  prtSubTitle("Run the Database Effect")

  def main: ZIO[Database, Throwable, Unit] = ???

  def main2: ZIO[Any, Throwable, Unit] =
    main.provide(DatabaseLive)

  // ------------------------------------------------------------
  prtSubTitle("Implement Test Service")

  class TestService extends Database.Service {
    private var map: Map[UserID, UserProfile] = Map()

    def setTestData(map0: Map[UserID, UserProfile]): Task[Unit] =
      Task { map = map0 }

    def getTestData: Task[Map[UserID, UserProfile]] =
      Task(map)

    def lookup(id: UserID): Task[UserProfile] =
      Task(map(id))

    def update(id: UserID, profile: UserProfile): Task[Unit] =
      Task.effect { map = map + (id -> profile) }
  }

  trait TestDatabase extends Database {
    val database: TestService = new TestService
  }
  object TestDatabase extends TestDatabase

  // ------------------------------------------------------------
  prtSubTitle("Test Database Code")

  def code: ZIO[Database, Throwable, Unit] = ???

  def code2: ZIO[Any, Throwable, Unit] =
    code.provide(TestDatabase)

  prtLine()
}
