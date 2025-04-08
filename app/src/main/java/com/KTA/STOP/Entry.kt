package com.KTA.STOP

import android.util.Log
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Entry : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        private const val TAG = "KTASTOP_Entry"
        lateinit var modulePath: String

        private const val LAUNCHER3_PACKAGE = "com.android.launcher3"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        Log.i(TAG, "Initializing in Zygote. Module path: $modulePath")
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelperInit.initHandleLoadPackage(lpparam)

        try {
            when (lpparam.packageName) {
                LAUNCHER3_PACKAGE -> {
                    Log.i(TAG, "Detected target package $LAUNCHER3_PACKAGE. Initializing ForceStopAndDisableHandler.")
                    val handler = ForceStopAndDisableHandler() // Thay Launcher3Handler bằng ForceStopAndDisableHandler
                    handler.handleLoadPackage(lpparam)
                }
                else -> {
                    return
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error while handling package ${lpparam.packageName}", e)
        }
    }
}