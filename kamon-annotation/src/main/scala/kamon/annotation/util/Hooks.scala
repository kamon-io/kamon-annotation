package kamon.annotation.util

import kamon.context.Context
import kamon.trace.Tracer

object Hooks {
  def key(): Context.Key[Tracer.PreStartHook] =
    kamon.trace.Hooks.PreStart.Key

  def updateOperationName(operationName:String): Tracer.PreStartHook =
    kamon.trace.Hooks.PreStart.updateOperationName(operationName)
}
