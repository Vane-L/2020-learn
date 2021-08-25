package com.scala.demo

/**
 * @Author: wenhongliang
 */
trait Speaker {
  def speak(): String // has no body, so it’s abstract
}

trait TailWagger {
  def startTail(): Unit = println("tail is wagging")

  def stopTail(): Unit = println("tail is stopped")
}

trait Runner {
  def startRunning(): Unit = println("I’m running")

  def stopRunning(): Unit = println("Stopped running")
}

class Dog(name: String) extends Speaker with TailWagger with Runner {
  def speak(): String = "Woof!"
}

class Cat extends Speaker with TailWagger with Runner {
  def speak(): String = "Meow"

  override def startRunning(): Unit = println("Yeah ... I don’t run")

  override def stopRunning(): Unit = println("No need to stop")
}

object Cat extends App {
  val cat = new Cat
  cat.startTail()
  cat.stopTail()
  cat.startRunning()
  cat.stopRunning()
}

