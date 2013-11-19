package se.yobriefca

import sbt._
import Keys._
import scala.util.Try

object SbtTasks extends Plugin {

  val defaultTaskPackage = SettingKey[String]("defaultTaskPackage")

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    defaultTaskPackage := "tasks"
  )

  def taskRunner(taskClassname: String) = {
    (dependencyClasspath in Runtime, defaultTaskPackage) map { (deps, taskPackage) =>

      val depURLs     = deps.map(_.data.toURI.toURL).toArray
      val classLoader = new java.net.URLClassLoader(depURLs, null)
      val taskClass   = s"$taskPackage.$taskClassname"
      val task        = tryLoadTask(taskClass, classLoader) recover {
        case cnfe: ClassNotFoundException =>
          buildTaskNotFoundTask(taskClass)
      }

      task.get
    }
  }

  def installTask(taskName: String) = {
    TaskKey[Unit](taskName) <<= taskRunner(taskName.capitalize) map { _.run() }
  }

  private def buildTaskNotFoundTask(taskClassname: String) = new Runnable {
    def run() {
      println(s"Unable to find task $taskClassname")
    }
  }

  private def tryLoadTask(taskClass: String, classLoader: ClassLoader) =
    Try(classLoader
      .loadClass(taskClass)
      .newInstance()
      .asInstanceOf[Runnable])
}

