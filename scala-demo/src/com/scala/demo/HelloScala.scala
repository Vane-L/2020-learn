package com.scala.demo

object Hello extends App {
  println("com.scala.demo.Hello World")
  val i: Int = 1
  val result = i match {
    case 1 => "one"
    case _ => "not know"
  }
  println(result)
  println(getClassAsString("hello"))
  println(getClassAsString(1))
  println(getClassAsString(1.5F))
  println(loop())

  val x = for (i <- 1 to 5) yield i * 2
  println(x)


  val fruits = List("apple", "banana", "lime", "orange")
  val fruitLengths = for {
    f <- fruits
    if f.length > 4
  } yield (f.length, f)
  println(fruitLengths)

  println(sum(1, 2))
  println(concatenate("adsd", "aaa"))

  def loop(): Unit = {
    // "x to y" syntax
    for (i <- 0 to 5) print(i + "-")
    // "x to y by" syntax
    for (i <- 0 to 10 by 2) print(i + "|")
  }

  def getClassAsString(x: Any): String = x match {
    case s: String => s + " is a String"
    case i: Int => "Int"
    case f: Float => "Float"
    case l: List[_] => "List"
    case _ => "Unknown"
  }

  def sum(a: Int, b: Int) = a + b

  def concatenate(s1: String, s2: String) = s1 + s2

}

