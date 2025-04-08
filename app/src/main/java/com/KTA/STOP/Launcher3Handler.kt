package com.KTA.STOP

import android.content.ComponentName
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import rikka.hidden.compat.ActivityManagerApis // Sử dụng đúng lớp từ HiddenApi

class Launcher3Handler : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "MyInjector-Launcher3Handler"
        
        private val EXCEPTION_LIST = setOf(
            "android",
            "com.android.systemui",
            "com.android.launcher3",
            "com.android.settings",
            "com.android.phone",
            "com.android.shell",
            "com.android.providers.settings",
            "com.android.providers.media",
            "com.android.providers.downloads",
            "com.android.providers.contacts",
            "com.android.providers.calendar",
            "com.android.providers.telephony",
            "com.android.bluetooth",
            "com.android.nfc",
            "com.android.inputmethod.latin",
            "com.google.android.gms"
        )
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.launcher3") return

        Log.d(TAG, "handleLoadPackage: hooked Launcher3")
        runCatching {
            val recentsViewClass = lpparam.classLoader.loadClass("com.android.quickstep.views.RecentsView")

            findMethod(recentsViewClass) {
                name == "dismissTask"
            }.hookAfter { param ->
                onTaskDismissed(param.args[0] as View)
            }

            val taskViewClass = lpparam.classLoader.loadClass("com.android.quickstep.views.TaskView")

            findMethod(taskViewClass) {
                name == "onTouchEvent"
            }.hookAfter { param ->
                val motionEvent = param.args[0]
                val action = XposedHelpers.callMethod(motionEvent, "getAction") as Int
                if (action == 0) { // ACTION_DOWN
                    onTaskTouchDown(param.thisObject as View)
                } else if (action == 1) { // ACTION_UP
                    onTaskTouchUp(param.thisObject as View)
                }
            }
        }.onFailure {
            Log.e(TAG, "handleLoadPackage: ", it)
        }
    }

    private var handler = Handler(Looper.getMainLooper())
    private var mCurrentView: View? = null
    private var mToBeKilled: View? = null
    private var longPressRunnable = Runnable { setupTaskForKill() }

    private fun setupTaskForKill() {
        mCurrentView?.let { view ->
            mToBeKilled = view
        }
    }

    private fun onTaskTouchDown(view: View) {
        mCurrentView = view
        mToBeKilled = null
        handler.postDelayed(longPressRunnable, 100) // 100ms
    }

    private fun onTaskTouchUp(view: View) {
        handler.removeCallbacks(longPressRunnable)
        mCurrentView = null
    }

    private fun onTaskDismissed(view: View) {
        if (mToBeKilled != view) {
            return
        }

        mToBeKilled = null
        runCatching {
            val task = XposedHelpers.callMethod(view, "getTask")
            val taskKey = XposedHelpers.getObjectField(task, "key")
            val componentName = XposedHelpers.getObjectField(taskKey, "componentName") as ComponentName
            val userId = XposedHelpers.getObjectField(taskKey, "userId") as Int
            val packageName = componentName.packageName

            // Kiểm tra nếu package nằm trong danh sách ngoại lệ
            if (EXCEPTION_LIST.contains(packageName)) {
                Log.d(TAG, "Package $packageName is in exception list, skipping force stop")
                return@runCatching
            }

            // Sử dụng forceStopPackage từ ActivityManagerApis của HiddenApi
            ActivityManagerApis.forceStopPackage(packageName, userId)
            Log.d(TAG, "Force stopped package: $packageName")
        }.onFailure {
            Log.e(TAG, "Error force stopping package", it)
        }
    }
}