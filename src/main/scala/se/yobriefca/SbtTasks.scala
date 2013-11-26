package se.yobriefca

import sbt._
import Keys._
import scala.util.Try
import java.net.URLClassLoader

object SbtTasks extends Plugin {

  val defaultTaskPackage = SettingKey[String]("defaultTaskPackage")

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    defaultTaskPackage := "tasks"
  )

  def installTask(taskName: String, taskDescription: String = "") = {
    TaskKey[Unit](taskName, taskDescription) <<= taskRunner(taskName.capitalize) map { task =>
      import scala.Console._
      println(s"[${GREEN}run-task$RESET] $taskName")
      task.run()
    }
  }

  def installTaskWithArgs(taskName: String, taskDescription: String = "") = {
    InputKey[Unit](taskName, taskDescription) := {

      val dc = (dependencyClasspath in Runtime).result.value.toEither.right.get
      val dp = defaultTaskPackage.toTask.result.value.toEither.right.get
      val args = Def.spaceDelimited("<arg>").parsed.toArray

      loadTaskSafe(dc, dp, taskName.capitalize, Some(args)).run()
    }
  }

  private def taskRunner(taskClassName: String) = {
    (dependencyClasspath in Runtime, defaultTaskPackage) map
      (loadTaskSafe(_, _, taskClassName))
  }

  private def loadTaskSafe(dependencies: Keys.Classpath, taskPackage:String, className: String, args: Option[Array[String]] = None) = {
    val dependenciesUrls = dependencies.map(_.data.toURI.toURL).toArray
    val classLoader = new URLClassLoader(dependenciesUrls, null)
    val taskClass = s"$taskPackage.$className"
    val task = tryLoadTask(taskClass, classLoader, args) recover {
      case _ : ClassNotFoundException => taskNotFoundTask(taskClass)
      case _ : InstantiationException => taskCantBeInstantiated(taskClass, args.isDefined)
      case _ : NoSuchMethodException  => taskCantBeInstantiated(taskClass, true)
    }

    task.get
  }

  private def taskNotFoundTask(taskClassname: String) = new Runnable {
    def run() = {
      import scala.Console._
      println(s"[${RED}task-error$RESET] Unable to find task $taskClassname")
    }
  }

  private def taskCantBeInstantiated(taskClassname: String, expectsArgs: Boolean) = new Runnable {
    import scala.Console._
    def run() = if(expectsArgs) {
      println(s"[${RED}task-error$RESET] Unable to find task $taskClassname that accepts arguments")
    } else {
      println(s"[${RED}task-error$RESET] Unable to find task $taskClassname that doesn't accepts arguments")
    }
  }

  private def tryLoadTask[T](taskClass: String, classLoader: ClassLoader, args: Option[Array[String]]) = {
    Try(args.map { arguments =>
      constructTaskWithArgs(taskClass, classLoader, arguments)
    } getOrElse {
      constructClassWithoutArgs(taskClass, classLoader)
    })
  }

  private def constructTaskWithArgs(taskClass: String, classLoader: ClassLoader, args: Array[String]) = {
    classLoader
      .loadClass(taskClass)
      .getConstructor(classOf[Array[String]])
      .newInstance(args)
      .asInstanceOf[Runnable]
  }

  private def constructClassWithoutArgs(taskClass: String, classLoader: ClassLoader) = {
    classLoader
      .loadClass(taskClass)
      .newInstance()
      .asInstanceOf[Runnable]
  }
}

