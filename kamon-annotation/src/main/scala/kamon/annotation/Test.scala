package kamon.annotation

import kamon.Kamon
import kamon.annotation.api.Trace

object Test extends App {
  @Trace
  def hello: Unit = {
    Kamon.currentSpan //Esto decis??
    println("HEEEEEELOOOOOOOOO")
  }
  hello
}
