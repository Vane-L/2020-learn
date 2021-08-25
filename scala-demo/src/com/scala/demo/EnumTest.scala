package com.scala.demo

/**
 * @Author: wenhongliang
 */
class EnumTest {

  sealed trait DayOfWeek

  case object Monday extends DayOfWeek

  case object TuesDay extends DayOfWeek

  case object Wednesday extends DayOfWeek

  case object Thursday extends DayOfWeek

  case object Friday extends DayOfWeek

  case object Saturday extends DayOfWeek

  case object Sunday extends DayOfWeek

}
