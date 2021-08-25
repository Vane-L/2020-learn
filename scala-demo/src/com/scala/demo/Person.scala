package com.scala.demo

/**
 * @Author: wenhongliang
 */
class Person(var firstName: String, var lastName: String) {
  def printFullName() = println(s"$firstName $lastName")
}

object Main extends App {
  val p = new Person("Julia", "Kern")
  println(p.firstName)
  p.lastName = "Manes"
  p.printFullName()
}