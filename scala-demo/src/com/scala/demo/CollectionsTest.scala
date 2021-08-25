package com.scala.demo

/**
 * @Author: wenhongliang
 */
class CollectionsTest {

}

object CollectionsTest {
  def main(args: Array[String]): Unit = {
    val nums = List.range(1, 10)
    val letters = ('a' to 'f').toList

    val doubles = nums.map(_ * 2)

    val names = List("joel", "ed", "chris", "maurice")
    val capNames = names.map(_.capitalize)

    val add = nums.foldLeft(1)(_ + _)
    val mul = nums.foldLeft(1)(_ * _)

    val t = (11, "Eleven")

    println(nums)
    println(letters)
    println(doubles)
    println(names)
    println(capNames)
    println(add)
    println(mul)
    println(t)
  }
}


