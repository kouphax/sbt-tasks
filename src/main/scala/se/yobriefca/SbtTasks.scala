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
    TaskKey[Unit](taskName, taskDescription) <<= taskRunner(taskName.capitalize) map (_.run)
  }

  private def taskRunner(taskClassName: String) = {
    (dependencyClasspath in Runtime, defaultTaskPackage) map
      (loadTaskSafe(_, _, taskClassName))
  }

  private def loadTaskSafe(dependencies: Keys.Classpath, taskPackage:String, className: String) = {
    val dependenciesUrls = dependencies.map(_.data.toURI.toURL).toArray
    val classLoader = new URLClassLoader(dependenciesUrls, null)
    val taskClass = s"$taskPackage.$className"
    val task = tryLoadTask(taskClass, classLoader) recover {
      case cnfe:ClassNotFoundException => taskNotFoundTask(taskClass)
    }

    task.get
  }

  private def taskNotFoundTask(taskClassname: String) = new Runnable {
    def run() = println(s"Unable to find task $taskClassname")
  }

  private def tryLoadTask(taskClass: String, classLoader: ClassLoader) =
    Try(classLoader
      .loadClass(taskClass)
      .newInstance()
      .asInstanceOf[Runnable])
}

