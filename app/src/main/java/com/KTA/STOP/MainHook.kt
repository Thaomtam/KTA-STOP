package com.KTA.STOP

import android.util.Log
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Entry : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        lateinit var modulePath: String
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelperInit.initHandleLoadPackage(lpparam)
        
        Log.d("KTASTOP", "handleLoadPackage: ${lpparam.packageName} ${lpparam.processName}")
        val handler = when (lpparam.packageName) {
            "com.android.launcher3" -> Launcher3Handler()
            else -> return
        }
        handler.handleLoadPackage(lpparam)
    }
}
