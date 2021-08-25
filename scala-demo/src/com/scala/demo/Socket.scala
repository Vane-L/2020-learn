package com.scala.demo

/**
 * @Author: wenhongliang
 */

/**
 * Supplying default constructor parameters has at least two benefits:
 *
 * You provide preferred, default values for your parameters
 * You let consumers of your class override those values for their own needs
 */

class Socket(var timeout: Int = 2000, var linger: Int = 3000) {
  override def toString = s"com.scala.demo.Socket(timeout: $timeout, linger: $linger)"
}

object Socket {
  def double(a: Int): Double = a * 3

  def double(a: Int, b: Double): Double = {
    val sum = a * 10 + b
    sum
  }

  def main(args: Array[String]): Unit = {
    println(new Socket())
    println(new Socket(1000, 6000))
    println(new Socket(1000))

    println(double(100))
    println(double(10))

    println(double(1, 2))
    println(double(10, 20))
  }
}
