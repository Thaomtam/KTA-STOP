package com.KTA.STOP.hook

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.ServiceManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.github.kyuubiran.ezxhelper.utils.*

object Launcher3Hook : BaseHook() {
    private const val TAG = "Launcher3Hook"
    private const val PREFS_NAME = "launcher3_handler_prefs"
    private const val KEY_EXCEPTION_LIST = "exception_list"

    private val DEFAULT_SYSTEM_PACKAGES = setOf(
        "android", "com.android.systemui", "com.android.launcher3", "com.android.settings",
        "com.android.phone", "com.android.shell", "com.android.providers.settings",
        "com.android.providers.media", "com.android.providers.downloads",
        "com.android.providers.contacts", "com.android.providers.calendar",
        "com.android.providers.telephony", "com.android.bluetooth", "com.android.nfc",
        "com.zing.zalo", "com.google.android.gms", "com.android.inputmethod.latin",
        "com.google.android.inputmethod.latin"
    )

    private lateinit var prefs: SharedPreferences
    private var exceptionList: MutableSet<String> = mutableSetOf()
    private var enableForceStop: Boolean = true

    private val handler = Handler(Looper.getMainLooper())
    private var mCurrentView: View? = null
    private var longPressRunnable = Runnable { setupTaskForKill() }
    private var mToBeKilled: View? = null
    private val ams by lazy {
        val am = ServiceManager.getService("activity")
        findClass("android.app.IActivityManager\$Stub").getDeclaredMethod("asInterface", android.os.IBinder::class.java)
            .invoke(null, am)
    }

    override fun init() {
        // Hook dismissTask trong RecentsView với hookAfter
        findMethod("com.android.quickstep.views.RecentsView") {
            name == "dismissTask"
        }.hookAfter {
            val taskView = it.args[0] as View
            if (!::prefs.isInitialized) initPreferences(taskView)
            onTaskDismissed(taskView)
        }

        // Hook onTouchEvent trong TaskView với hookBefore (giữ nguyên vì cần phát hiện trước)
        findMethod("com.android.quickstep.views.TaskView") {
            name == "onTouchEvent"
        }.hookBefore {
            val motionEvent = it.args[0]
            val action = invokeMethod(motionEvent, "getAction") as Int
            if (!::prefs.isInitialized) initPreferences(it.thisObject as View)

            when (action) {
                0 -> onTaskTouchDown(it.thisObject as View) // ACTION_DOWN
                1 -> onTaskTouchUp(it.thisObject as View)   // ACTION_UP
            }
        }

        Log.i("$TAG: Initialized hooks for Launcher3")
    }

    private fun initPreferences(view: View) {
        val context = view.context
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadExceptionList()
        enableForceStop = prefs.getBoolean("enableForceStop", true)

        prefs.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "enableForceStop" -> {
                    enableForceStop = sharedPreferences.getBoolean(key, true)
                    Log.d(TAG, "enableForceStop changed to $enableForceStop")
                }
                KEY_EXCEPTION_LIST -> {
                    loadExceptionList()
                    Log.d(TAG, "Exception list reloaded")
                }
            }
        }
    }

    private fun loadExceptionList() {
        val savedList = prefs.getStringSet(KEY_EXCEPTION_LIST, null) ?: mutableSetOf()
        exceptionList = savedList.toMutableSet()
        exceptionList.addAll(DEFAULT_SYSTEM_PACKAGES)
        Log.d(TAG, "Loaded exception list: $exceptionList")
    }

    private fun isPackageInExceptionList(packageName: String): Boolean {
        return exceptionList.contains(packageName)
    }

    private fun setupTaskForKill() {
        if (!enableForceStop) return

        mCurrentView?.let { view ->
            val taskKey = getTaskKey(view) ?: return@let
            val componentName = getComponentName(taskKey) ?: return@let
            if (isPackageInExceptionList(componentName.packageName)) {
                Toast.makeText(view.context, "Ứng dụng ${componentName.packageName} được bảo vệ", Toast.LENGTH_SHORT).show()
                return@let
            }

            mToBeKilled = view
            Toast.makeText(view.context, "Vuốt để dừng tiến trình", Toast.LENGTH_SHORT).show()

            runCatching {
                val dismissView = findDismissView(view)
                dismissView?.text = "Dừng tiến trình"
            }.onFailure {
                Log.e(TAG, "Error changing dismiss text", it)
            }
        }
    }

    private fun findDismissView(view: View): TextView? {
        return try {
            val footer = getObjectField(view, "mFooter") as? View
            footer?.let { findTextViewInViewGroup(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding dismiss view", e)
            null
        }
    }

    private fun findTextViewInViewGroup(viewGroup: View): TextView? {
        if (viewGroup is TextView) return viewGroup
        return try {
            val childCount = invokeMethod(viewGroup, "getChildCount") as Int
            for (i in 0 until childCount) {
                val child = invokeMethod(viewGroup, "getChildAt", i) as View
                if (child is TextView) return child
                findTextViewInViewGroup(child)?.let { return it }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error traversing view hierarchy", e)
            null
        }
    }

    private fun onTaskTouchDown(view: View) {
        mCurrentView = view
        mToBeKilled = null
        handler.postDelayed(longPressRunnable, 500)
    }

    private fun onTaskTouchUp(view: View) {
        handler.removeCallbacks(longPressRunnable)
        if (mToBeKilled != view) {
            runCatching {
                val dismissView = findDismissView(view)
                if (dismissView?.text == "Dừng tiến trình") {
                    dismissView.text = "Đóng"
                }
            }.onFailure {
                Log.e(TAG, "Error resetting dismiss text", it)
            }
        }
    }

    private fun onTaskDismissed(view: View) {
        if (!enableForceStop || mToBeKilled != view) return
        mToBeKilled = null

        val taskKey = getTaskKey(view) ?: return
        val componentName = getComponentName(taskKey) ?: return
        val userId = getUserId(taskKey)

        if (isPackageInExceptionList(componentName.packageName)) {
            Toast.makeText(view.context, "Ứng dụng ${componentName.packageName} được bảo vệ", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Package in exception list, not force stopping: ${componentName.packageName}")
            return
        }

        runCatching {
            invokeMethod(ams, "forceStopPackage", componentName.packageName, userId)
            Toast.makeText(view.context, "Đã dừng tiến trình: ${componentName.packageName}", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Force stopped package: ${componentName.packageName}")
        }.onFailure {
            Log.e(TAG, "Error force stopping package", it)
            Toast.makeText(view.context, "Lỗi khi dừng tiến trình: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTaskKey(taskView: View): Any? {
        return try {
            getObjectField(taskView, "mTask")?.let { task ->
                getObjectField(task, "key") ?: invokeMethod(task, "getKey")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task key", e)
            null
        }
    }

    private fun getComponentName(taskKey: Any): ComponentName? {
        return try {
            getObjectField(taskKey, "componentName") as? ComponentName
                ?: invokeMethod(taskKey, "getComponent") as? ComponentName
        } catch (e: Exception) {
            Log.e(TAG, "Error getting component name", e)
            null
        }
    }

    private fun getUserId(taskKey: Any): Int {
        return try {
            (getObjectField(taskKey, "userId") as? Int)
                ?: (invokeMethod(taskKey, "getUserId") as? Int) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user ID", e)
            0
        }
    }
}