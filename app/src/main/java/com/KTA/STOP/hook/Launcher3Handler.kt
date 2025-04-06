package com.KTA.STOP.hook

import android.content.ComponentName
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.getObjectOrNull
import de.robv.android.xposed.XC_MethodHook
import rikka.hidden.compat.ActivityManagerApis

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

    private fun findDismissView(): TextView {
        val headerView = mCurrentView?.getObjectOrNull("mHeaderView") // Trả về Any? hoặc null
        val dismissView = headerView?.getObjectOrNull("mDismissView") // Trả về Any? hoặc null
        return dismissView as? TextView ?: throw IllegalStateException("Dismiss view not found or not a TextView")
    }

    private fun setupToBeKilled() {
        mCurrentView ?: return
        runCatching {
            findDismissView().text = "停止进程"
            mToBeKilled = mCurrentView
        }.onFailure {
            Log.e(TAG, "Failed to setup to be killed", it)
        }
    }

    private fun onBeginDrag(v: View) {
        mCurrentView = v
        mToBeKilled = null
        runCatching {
            origText = findDismissView().text
            handler.postDelayed(setViewHeaderRunnable, 700)
        }.onFailure {
            Log.e(TAG, "Failed in onBeginDrag", it)
        }
    }

    private fun onDragEnd(v: View) {
        handler.removeCallbacks(setViewHeaderRunnable)
        runCatching {
            if (origText != null) findDismissView().text = origText
        }.onFailure {
            Log.e(TAG, "Failed in onDragEnd", it)
        }
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
        val task = v.getObjectOrNull("mTask")
        val key = task?.getObjectOrNull("key")
        val user = key?.getObjectOrNull("userId") as? Int
        val topActivity = key?.getObjectOrNull("topActivity") as? ComponentName
        if (user != null && topActivity != null) {
            runCatching {
                ActivityManagerApis.forceStopPackage(topActivity.packageName, user)
            }.onSuccess {
                Toast.makeText(v.context, "killed ${topActivity.packageName}", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(v.context, "killed ${topActivity.packageName} failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "onChildDismissedEnd: ", it)
            }
        } else {
            Log.e(TAG, "Failed to get userId or topActivity")
        }
    }
}
