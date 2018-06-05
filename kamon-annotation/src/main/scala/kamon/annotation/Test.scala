package kamon.annotation

import kamon.Kamon
import kamon.annotation.api._

object Test extends App {
  val hello = new Hello(100)
  hello.hello()
  hello.hello()
  hello.hello()
  hello.hello()
  hello.hello()
  hello.hello()
  hello.hello()
  AnnotatedObject.trace()
}

case class Hello(id:Long) {
//  @Trace(operationName="${'trace:' += this.id}")
  @Trace(operationName="${'trace:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
  @Timer(name="${'trace:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
  @Count(name="${'trace:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
//  @Count(name="${'trace:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
//  @Count(name="${'trace:' += this.id}")
  @RangeSampler(name="${'trace:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
  @Histogram(name="${'trace:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
  @Gauge(name="${'gauge:' += this.id}", tags = "${'slow-service':'service', 'env':'prod'}")
// / @Trace
  def hello(name:String = ""): Long = {
    println(Kamon.currentSpan)
    println("HEEEEEELOOOOOOOOO")
    100
  }
}


object AnnotatedObject {
  //
  val Id = "1000"

  //
  @Gauge(name="${'pepe:' += AnnotatedObject$.MODULE$.Id}", tags = "${'slow-service':'service', 'env':'prod'}")
  @Trace(operationName="trace")
  def trace(): Long = {
    100L
  }
}


