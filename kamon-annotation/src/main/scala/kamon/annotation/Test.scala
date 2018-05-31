package kamon.annotation

import kamon.Kamon
import kamon.annotation.api.Trace

object Test extends App {
  new Hello(100).hello
}


case class Hello(id:Long) {
//  @Trace(operationName="${'trace:' += this.id}")
  @Trace(operationName="${'trace:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
// / @Trace
  def hello: Unit = {
    println(Kamon.currentSpan)
    println("HEEEEEELOOOOOOOOO")
  }
}