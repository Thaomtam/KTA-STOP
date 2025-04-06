package com.KTA.STOP.hook

abstract class BaseHook {
    var isInit: Boolean = false
    abstract fun init()
}