package com.KTA.STOP.hook

import android.app.IActivityManager
import android.content.ComponentName
import android.os.Handler
import android.os.Looper
import android.os.ServiceManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.XC_MethodHook

class Launcher3Handler : BaseHook() {
    companion object {
        private const val TAG = "MyInjector-Launcher3Handler"
    }

    private val taskViewClass by lazy { "com.android.quickstep.views.TaskView" }
    private var hookBeginDrag: XC_MethodHook.Unhook? = null
    private var hookDragEnd: XC_MethodHook.Unhook? = null
    private var hookDismiss: XC_MethodHook.Unhook? = null
    private var hookCancel: XC_MethodHook.Unhook? = null

    override fun init() {
        Log.d(TAG, "init: hooked Launcher3")
        runCatching {
            // Hook onBeginDrag
            hookBeginDrag = findMethod(taskViewClass) {
                name == "onTouchEvent"
            }.hookAfter { param ->
                val view = param.args[0] as View
                onBeginDrag(view)
            }

            // Hook onDragEnd
            hookDragEnd = findMethod(taskViewClass) {
                name == "onTouchEvent"
            }.hookAfter { param ->
                val view = param.args[0] as View
                onDragEnd(view)
            }

            // Hook onChildDismissedEnd
            hookDismiss = findMethod(taskViewClass) {
                name == "dismissTask"
            }.hookAfter { param ->
                val view = param.thisObject as View
                onChildDismissedEnd(view)
            }

            // Hook onDragCancelled
            hookCancel = findMethod(taskViewClass) {
                name == "onTouchEvent"
            }.hookAfter { param ->
                val view = param.args[0] as View
                onDragCancelled(view)
            }
        }.onFailure {
            Log.e(TAG, "Fatal error occurred, disable hooks", it)
            unload()
        }
    }

    private fun unload() {
        hookBeginDrag?.unhook()
        hookDragEnd?.unhook()
        hookDismiss?.unhook()
        hookCancel?.unhook()
        hookBeginDrag = null
        hookDragEnd = null
        hookDismiss = null
        hookCancel = null
    }

    private var handler = Handler(Looper.getMainLooper())
    private var mCurrentView: View? = null
    private var origText: CharSequence? = null
    private var setViewHeaderRunnable = Runnable { setupToBeKilled() }
    private var mToBeKilled: View? = null
    private val ams by lazy { IActivityManager.Stub.asInterface(ServiceManager.getService("activity")) }

    private fun findDismissView(): TextView {
        val headerView = mCurrentView?.getObjectField("mHeaderView")
        return headerView?.getObjectField("mDismissView") as TextView
    }

    private fun setupToBeKilled() {
        mCurrentView ?: return
        findDismissView().text = "停止进程"
        mToBeKilled = mCurrentView
    }

    private fun onBeginDrag(v: View) {
        mCurrentView = v
        mToBeKilled = null
        origText = findDismissView().text
        handler.postDelayed(setViewHeaderRunnable, 700)
    }

    private fun onDragEnd(v: View) {
        handler.removeCallbacks(setViewHeaderRunnable)
        if (origText != null) findDismissView().text = origText
        mCurrentView = null
        origText = null
    }

    private fun onDragCancelled(v: View) {
        Log.d(TAG, "onDragCancelled: cancelled $mToBeKilled")
        mToBeKilled = null
    }

    private fun onChildDismissedEnd(v: View) {
        if (mToBeKilled != v) {
            Log.e(TAG, "onChildDismissedEnd: not target: $v $mToBeKilled")
            return
        }
        mToBeKilled = null
        val task = v.getObjectField("mTask")
        val key = task.getObjectField("key")
        val user = key.getObjectField("userId") as Int
        val topActivity = key.getObjectField("topActivity") as ComponentName
        runCatching { ams.forceStopPackage(topActivity.packageName, user) }
            .onSuccess {
                Toast.makeText(v.context, "killed ${topActivity.packageName}", Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(v.context, "killed ${topActivity.packageName} failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "onChildDismissedEnd: ", it)
            }
    }

    private fun Any.getObjectField(fieldName: String): Any? {
        return com.github.kyuubiran.ezxhelper.utils.getObjectOrNull(this, fieldName)
    }
}
