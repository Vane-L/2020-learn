package com.scala.demo

/**
 * @Author: wenhongliang
 */

// An object is similar to a class, but you specifically use it when you want a single instance of that class
object Helllo {
  def main(args: Array[String]): Unit = {
    println("com.scala.demo.Hello world!")
  }
}


// What happens here is that the App trait has its own main method, so you don’t need to write one.
object Hello2 extends App {
  println("com.scala.demo.Hello world!2")
}


object HelloYou extends App {
  if (args.size == 0)
    println("com.scala.demo.Hello, you")
  else
    println("com.scala.demo.Hello  " + args(0))
}