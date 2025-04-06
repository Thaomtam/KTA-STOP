package com.KTA.STOP.hook

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
import de.robv.android.xposed.XposedHelpers

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
                val motionEvent = param.args[0] as android.view.MotionEvent
                if (motionEvent.action == android.view.MotionEvent.ACTION_DOWN) {
                    val view = param.thisObject as View
                    onBeginDrag(view)
                }
            }

            // Hook onDragEnd
            hookDragEnd = findMethod(taskViewClass) {
                name == "onTouchEvent"
            }.hookAfter { param ->
                val motionEvent = param.args[0] as android.view.MotionEvent
                if (motionEvent.action == android.view.MotionEvent.ACTION_UP) {
                    val view = param.thisObject as View
                    onDragEnd(view)
                }
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
                val motionEvent = param.args[0] as android.view.MotionEvent
                if (motionEvent.action == android.view.MotionEvent.ACTION_CANCEL) {
                    val view = param.thisObject as View
                    onDragCancelled(view)
                }
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
    private val ams by lazy { 
        val am = ServiceManager.getService("activity")
        XposedHelpers.findClass("android.app.IActivityManager\$Stub", null)
            .getDeclaredMethod("asInterface", android.os.IBinder::class.java)
            .invoke(null, am)
    }

    private fun findDismissView(view: View): TextView? {
        return try {
            val footer = XposedHelpers.getObjectField(view, "mFooter") as? View
            footer?.let { findTextViewInViewGroup(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding dismiss view", e)
            null
        }
    }

    private fun findTextViewInViewGroup(viewGroup: View): TextView? {
        if (viewGroup is TextView) return viewGroup
        try {
            val childCount = XposedHelpers.callMethod(viewGroup, "getChildCount") as Int
            for (i in 0 until childCount) {
                val child = XposedHelpers.callMethod(viewGroup, "getChildAt", i) as View
                if (child is TextView) return child
                val result = findTextViewInViewGroup(child)
                if (result != null) return result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error traversing view hierarchy", e)
        }
        return null
    }

    private fun setupToBeKilled() {
        mCurrentView?.let { view ->
            runCatching {
                val dismissView = findDismissView(view)
                if (dismissView != null) {
                    dismissView.text = "停止进程"
                    mToBeKilled = view
                }
            }.onFailure {
                Log.e(TAG, "Failed to setup to be killed", it)
            }
        }
    }

    private fun onBeginDrag(v: View) {
        mCurrentView = v
        mToBeKilled = null
        runCatching {
            val dismissView = findDismissView(v)
            if (dismissView != null) {
                origText = dismissView.text
                handler.postDelayed(setViewHeaderRunnable, 700)
            }
        }.onFailure {
            Log.e(TAG, "Failed in onBeginDrag", it)
        }
    }

    private fun onDragEnd(v: View) {
        handler.removeCallbacks(setViewHeaderRunnable)
        runCatching {
            val dismissView = findDismissView(v)
            if (dismissView != null && origText != null) {
                dismissView.text = origText
            }
        }.onFailure {
            Log.e(TAG, "Failed in onDragEnd", it)
        }
        mCurrentView = null
        origText = null
    }

    private fun onDragCancelled(v: View) {
        Log.d(TAG, "onDragCancelled: cancelled $mToBeKilled")
        handler.removeCallbacks(setViewHeaderRunnable)
        mToBeKilled = null
    }

    private fun onChildDismissedEnd(v: View) {
        if (mToBeKilled != v) {
            Log.e(TAG, "onChildDismissedEnd: not target: $v $mToBeKilled")
            return
        }
        mToBeKilled = null
        runCatching {
            val task = XposedHelpers.getObjectField(v, "mTask")
            val key = XposedHelpers.getObjectField(task, "key")
            val user = XposedHelpers.getObjectField(key, "userId") as? Int ?: 0
            val topActivity = XposedHelpers.getObjectField(key, "topActivity") as? ComponentName
            if (topActivity != null) {
                XposedHelpers.callMethod(ams, "forceStopPackage", topActivity.packageName, user)
                Toast.makeText(v.context, "killed ${topActivity.packageName}", Toast.LENGTH_SHORT).show()
            }
        }.onFailure {
            Toast.makeText(v.context, "killed failed", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "onChildDismissedEnd: ", it)
        }
    }
}
