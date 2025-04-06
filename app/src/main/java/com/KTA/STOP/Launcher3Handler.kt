package com.KTA.STOP

import android.app.ActivityManager // Keep for potential future use if needed, but IActivityManager is used directly
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ServiceManager
import android.util.Log
import android.view.MotionEvent // Import explicitly for clarity
import android.view.View
import android.view.ViewGroup // Import for findTextViewInViewGroup
import android.widget.TextView
import android.widget.Toast
import com.github.kyuubiran.ezxhelper.utils.findMethod
// Removed findMethodOrNull as it wasn't used
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File // Keep, although not directly used in this logic, maybe for future expansion
import java.lang.ref.WeakReference // Use WeakReference for Context to avoid leaks

class Launcher3Handler : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "MyInjector-Launcher3Handler"
        // Use a more specific prefs name if this module handles other things too
        private const val PREFS_NAME = "myinjector_launcher3_prefs"
        private const val KEY_EXCEPTION_LIST = "exception_list"
        private const val KEY_ENABLE_FORCE_STOP = "enableForceStop" // Define key constant
        private const val LONG_PRESS_TIMEOUT_MS = 500L // Configurable long press duration

        // Default protected system packages
        // Added common Google packages and potentially sensitive apps
        private val DEFAULT_SYSTEM_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.android.launcher", // Common alternative launcher package name
            "com.android.launcher3", // Target package itself
            "com.google.android.apps.nexuslauncher", // Pixel Launcher
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
            "com.zing.zalo", // Example third-party app (keep if needed)
            "com.google.android.gms", // Google Play Services
            "com.google.android.gsf", // Google Services Framework
            "com.android.vending", // Google Play Store
            "com.android.inputmethod.latin", // AOSP Keyboard
            "com.google.android.inputmethod.latin" // Gboard
            // Add more critical system/input method packages as needed
        )
    }

    // Use WeakReference to Context to avoid potential memory leaks
    private var contextRef: WeakReference<Context>? = null
    private var prefs: SharedPreferences? = null
    private var exceptionList: MutableSet<String> = mutableSetOf()
    private var enableForceStop: Boolean = true

    // Main thread handler for timed operations (long press)
    private val handler = Handler(Looper.getMainLooper())
    // References to views involved in touch/kill process
    private var currentTouchedView: View? = null
    private var viewMarkedForKill: View? = null
    // Runnable for detecting long press completion
    private var longPressRunnable: Runnable? = null

    // Lazily initialize IActivityManager using reflection
    // Cache the result for efficiency
    private val iActivityManager: Any? by lazy {
        try {
            val amServiceBinder = ServiceManager.getService(Context.ACTIVITY_SERVICE) as IBinder?
            if (amServiceBinder == null) {
                Log.e(TAG, "Failed to get ActivityService binder.")
                return@lazy null
            }
            // Find the IActivityManager$Stub class (works across Android versions)
            val stubClass = XposedHelpers.findClass("android.app.IActivityManager\$Stub", null)
            val asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder::class.java)
            asInterfaceMethod.invoke(null, amServiceBinder)
        } catch (e: Throwable) { // Catch Throwable for broader error capture
            Log.e(TAG, "Failed to get IActivityManager instance via reflection", e)
            null
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Check target package name (consider variations if needed)
        if (lpparam.packageName != "com.android.launcher3") {
             // Log if you want to know about other packages being loaded by Xposed
             // Log.v(TAG, "Ignoring package: ${lpparam.packageName}")
             return
        }

        Log.i(TAG, "Hooking into Launcher3 package: ${lpparam.packageName}")

        // Centralized error handling for hooks
        runCatching {
            // Find classes using the provided ClassLoader
            val recentsViewClass = XposedHelpers.findClass("com.android.quickstep.views.RecentsView", lpparam.classLoader)
            val taskViewClass = XposedHelpers.findClass("com.android.quickstep.views.TaskView", lpparam.classLoader)

            // --- Hook Task Dismissal ---
            findMethod(recentsViewClass) {
                // Be slightly more specific if multiple methods have similar names/params
                name == "dismissTask" && parameterTypes.size == 1 // Assuming one argument (TaskView or similar)
            }.hookBefore { param ->
                val dismissedTaskView = param.args[0] as? View
                if (dismissedTaskView == null) {
                    Log.w(TAG, "dismissTask hook: Argument is not a View or is null")
                    return@hookBefore
                }
                // Ensure preferences are initialized (only needs to happen once)
                initPreferencesIfNeeded(dismissedTaskView.context)
                // Handle the dismissal logic
                onTaskDismissed(dismissedTaskView)
            }
            Log.d(TAG, "Hooked RecentsView.dismissTask")

            // --- Hook Task Touch Events ---
            findMethod(taskViewClass) {
                name == "onTouchEvent" && parameterTypes.contentEquals(arrayOf(MotionEvent::class.java))
            }.hookBefore { param ->
                val taskView = param.thisObject as View
                val motionEvent = param.args[0] as MotionEvent

                // Ensure preferences are initialized
                initPreferencesIfNeeded(taskView.context)

                // Process touch actions
                when (motionEvent.actionMasked) { // Use actionMasked for pointer events
                    MotionEvent.ACTION_DOWN -> onTaskTouchDown(taskView)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onTaskTouchUp(taskView)
                }
            }
            Log.d(TAG, "Hooked TaskView.onTouchEvent")

        }.onFailure { error ->
            Log.e(TAG, "Failed to set up hooks in Launcher3", error)
            // Maybe show a persistent error notification if hooks fail critically?
        }
    }

    /**
     * Initializes SharedPreferences and loads settings if not already done.
     * Uses WeakReference to context.
     */
    @Synchronized // Ensure thread safety during initialization
    private fun initPreferencesIfNeeded(context: Context) {
        // Check if already initialized or if context is invalid
        if (prefs != null && contextRef?.get() != null) {
            return
        }

        Log.d(TAG, "Initializing preferences...")
        val appContext = context.applicationContext // Use application context
        contextRef = WeakReference(appContext)
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Load initial settings
        loadSettings()

        // Register listener for changes (only register once)
        prefs?.registerOnSharedPreferenceChangeListener(settingsChangeListener)
        Log.i(TAG, "Preferences initialized and listener registered.")
    }

    /**
     * Listener for changes in SharedPreferences.
     */
    private val settingsChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        Log.d(TAG, "SharedPreferences changed: key='$key'")
        when (key) {
            KEY_ENABLE_FORCE_STOP -> {
                enableForceStop = sharedPreferences.getBoolean(key, true)
                Log.i(TAG, "Force stop feature toggled: $enableForceStop")
                // If feature disabled, cancel any pending long press
                if (!enableForceStop) {
                    cancelLongPress()
                }
            }
            KEY_EXCEPTION_LIST -> {
                loadExceptionList() // Reload the exception list
                Log.i(TAG, "Exception list reloaded. New size: ${exceptionList.size}")
            }
        }
    }

    /**
     * Loads settings (exception list and enable flag) from SharedPreferences.
     */
    private fun loadSettings() {
        prefs?.let { p ->
            enableForceStop = p.getBoolean(KEY_ENABLE_FORCE_STOP, true)
            loadExceptionList() // Separate loading the list
            Log.d(TAG, "Initial settings loaded: enableForceStop=$enableForceStop, exceptionList size=${exceptionList.size}")
        } ?: run {
            Log.e(TAG, "Cannot load settings, SharedPreferences is null.")
            // Set defaults if prefs are somehow null after init attempt
            enableForceStop = true
            exceptionList = DEFAULT_SYSTEM_PACKAGES.toMutableSet()
        }
    }


    /**
     * Loads the exception list from SharedPreferences and adds default system apps.
     */
    private fun loadExceptionList() {
        val savedList = prefs?.getStringSet(KEY_EXCEPTION_LIST, emptySet()) ?: emptySet()
        // Create a new mutable set from the loaded and default lists
        exceptionList = mutableSetOf<String>().apply {
            addAll(savedList)
            addAll(DEFAULT_SYSTEM_PACKAGES) // Always include defaults
        }
        // Log.v(TAG, "Loaded exception list: $exceptionList") // Verbose logging if needed
    }

    /**
     * Checks if a package name is in the combined (user + default) exception list.
     */
    private fun isPackageInExceptionList(packageName: String?): Boolean {
        if (packageName.isNullOrEmpty()) return false // Cannot exclude null/empty
        return exceptionList.contains(packageName)
    }

    // --- Touch Event Handling ---

    /**
     * Called when a TaskView touch begins (ACTION_DOWN).
     * Schedules the long press check.
     */
    private fun onTaskTouchDown(view: View) {
        // If force stop is disabled, do nothing
        if (!enableForceStop) return

        // Cancel any previously scheduled long press runnable
        cancelLongPress()

        // Store the currently touched view
        currentTouchedView = view
        // Clear the "to be killed" flag from previous interactions
        viewMarkedForKill = null
        // Reset any visual cues on previously marked views if necessary (optional)

        // Create and schedule the long press runnable
        longPressRunnable = Runnable { setupTaskForKill() }
        handler.postDelayed(longPressRunnable!!, LONG_PRESS_TIMEOUT_MS)
        // Log.v(TAG, "ACTION_DOWN on view. Scheduled long press check.")
    }

    /**
     * Called when a TaskView touch ends (ACTION_UP or ACTION_CANCEL).
     * Cancels the pending long press check.
     */
    private fun onTaskTouchUp(view: View) {
        // Cancel the scheduled long press check regardless of feature state
        cancelLongPress()
        // Log.v(TAG, "ACTION_UP/CANCEL on view. Cancelled long press check.")

        // Optional: Reset visual state if the view wasn't marked for killing
        // This is less critical as the view might be dismissed anyway
        // if (viewMarkedForKill != view) {
        //     resetDismissViewText(view) // Example: Reset text if changed
        // }

        // Clear the reference to the touched view
        currentTouchedView = null
    }

    /**
     * Cancels the pending long press runnable and cleans up references.
     */
    private fun cancelLongPress() {
        longPressRunnable?.let { handler.removeCallbacks(it) }
        longPressRunnable = null
        // currentTouchedView = null // Keep currentTouchedView until UP/CANCEL
    }

    // --- Core Logic ---

    /**
     * Called by the longPressRunnable after the timeout.
     * Marks the current task view for potential force-stopping upon dismissal.
     */
    private fun setupTaskForKill() {
        // Clear the runnable reference as it has executed
        longPressRunnable = null

        // Check if the feature is still enabled and if we have a view
        if (!enableForceStop || currentTouchedView == null) {
            Log.d(TAG, "setupTaskForKill: Aborted (feature disabled or view is null)")
            return
        }

        val view = currentTouchedView!! // We know it's not null here
        val context = view.context ?: contextRef?.get() // Get context safely

        // Extract package info
        val taskInfo = getTaskInfo(view)

        // Check if the package is protected
        if (taskInfo != null && isPackageInExceptionList(taskInfo.packageName)) {
            Log.d(TAG, "setupTaskForKill: Package '${taskInfo.packageName}' is protected.")
            context?.let {
                Toast.makeText(it, "Ứng dụng \"${taskInfo.packageName}\" được bảo vệ", Toast.LENGTH_SHORT).show()
            }
            // Don't mark for kill if protected
            currentTouchedView = null // Clear reference as long press failed validation
            return
        }

        // Mark this view for killing
        viewMarkedForKill = view
        Log.i(TAG, "setupTaskForKill: Marked view for package '${taskInfo?.packageName ?: "unknown"}' for kill.")

        // Provide visual feedback
        context?.let {
            Toast.makeText(it, "Vuốt lên để dừng ứng dụng", Toast.LENGTH_SHORT).show()
            // Optional: Try changing the dismiss button text (might be fragile)
            changeDismissViewText(view, "Dừng ứng dụng")
        }
    }

    /**
     * Called when a task view is about to be dismissed (swiped away).
     * Checks if the view was marked for killing and performs the force-stop if conditions met.
     */
    private fun onTaskDismissed(view: View) {
        // Check if the feature is enabled and if this is the view we marked
        if (!enableForceStop || viewMarkedForKill != view) {
            // Log.v(TAG, "onTaskDismissed: Ignoring dismissal (feature disabled or view not marked)")
            // Reset the kill flag if it wasn't the marked view being dismissed (edge case safety)
            if (viewMarkedForKill == view) viewMarkedForKill = null
            return
        }

        Log.d(TAG, "onTaskDismissed: Intercepted dismissal for marked view.")
        // Reset the flag immediately
        val viewToKill = viewMarkedForKill
        viewMarkedForKill = null

        val context = view.context ?: contextRef?.get()

        // Get task info again (or cache it if reliable)
        val taskInfo = getTaskInfo(viewToKill)

        if (taskInfo == null) {
            Log.e(TAG, "onTaskDismissed: Failed to get TaskInfo for the view to kill.")
            context?.let { Toast.makeText(it, "Lỗi: Không thể lấy thông tin ứng dụng", Toast.LENGTH_SHORT).show() }
            return
        }

        // Final check against exception list (safety net)
        if (isPackageInExceptionList(taskInfo.packageName)) {
            Log.w(TAG, "onTaskDismissed: Package '${taskInfo.packageName}' is protected (final check). Aborting kill.")
            context?.let { Toast.makeText(it, "Ứng dụng \"${taskInfo.packageName}\" được bảo vệ", Toast.LENGTH_SHORT).show() }
            return
        }

        // Perform the force stop
        try {
            val am = iActivityManager
            if (am == null) {
                 Log.e(TAG, "onTaskDismissed: IActivityManager is null, cannot force stop.")
                 context?.let { Toast.makeText(it, "Lỗi: Không thể truy cập Activity Manager", Toast.LENGTH_SHORT).show() }
                 return
            }
            Log.i(TAG, "Attempting to force stop package: ${taskInfo.packageName}, userId: ${taskInfo.userId}")
            // Call forceStopPackage using reflection
            XposedHelpers.callMethod(am, "forceStopPackage", taskInfo.packageName, taskInfo.userId)

            // Success feedback
            Log.i(TAG, "Successfully force stopped package: ${taskInfo.packageName}")
            context?.let {
                Toast.makeText(it, "Đã dừng ứng dụng: ${taskInfo.packageName}", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Throwable) {
            Log.e(TAG, "Error force stopping package: ${taskInfo.packageName}", e)
            context?.let {
                Toast.makeText(it, "Lỗi khi dừng ứng dụng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } finally {
            // Optional: Reset dismiss text if it was changed (though view is disappearing)
            // resetDismissViewText(viewToKill)
        }
    }

    // --- Helper Functions ---

    /**
     * Data class to hold extracted task information.
     */
    private data class TaskInfo(val packageName: String?, val userId: Int, val componentName: ComponentName?)

    /**
     * Extracts TaskInfo (package name, user ID, component name) from a TaskView.
     * Handles potential reflection errors.
     */
    private fun getTaskInfo(taskView: View?): TaskInfo? {
        if (taskView == null) return null
        return try {
            // Get the Task object associated with the TaskView
            // Method name might vary ('getTask', 'getTaskInfo', etc.) - Requires inspection of Launcher3 source/decompiled code
            val taskObject = XposedHelpers.callMethod(taskView, "getTask") ?: return null

            // Get the Task.TaskKey object
            // Field/method name might be 'key', 'mKey', 'getTaskKey'
            val taskKeyObject = XposedHelpers.getObjectField(taskObject, "key") ?: XposedHelpers.callMethod(taskObject, "getKey") ?: return null

            // Extract ComponentName and userId from the TaskKey
            // Field/method names might be 'componentName'/'getComponent', 'userId'/'getUserId'
            val componentName = XposedHelpers.getObjectField(taskKeyObject, "componentName") as? ComponentName
                                ?: XposedHelpers.callMethod(taskKeyObject, "getComponent") as? ComponentName

            val userId = (XposedHelpers.getObjectField(taskKeyObject, "userId") as? Int)
                         ?: (XposedHelpers.callMethod(taskKeyObject, "getUserId") as? Int)
                         ?: 0 // Default to 0 if not found

            val packageName = componentName?.packageName

            if (packageName == null) {
                 Log.w(TAG, "getTaskInfo: Could not determine package name from ComponentName: $componentName")
            }

            TaskInfo(packageName, userId, componentName)
        } catch (e: Throwable) {
            Log.e(TAG, "Error getting task info via reflection", e)
            null
        }
    }

    // --- Optional Visual Feedback Helpers ---

    /**
     * Attempts to find a TextView within the TaskView's hierarchy, potentially the dismiss button/label.
     * This is highly dependent on the Launcher's layout and might break easily.
     */
    private fun findDismissView(view: View): TextView? {
        // Example: Look for a known ID (if available via resources or inspection)
        // val dismissButtonId = view.context.resources.getIdentifier("dismiss_button", "id", view.context.packageName)
        // if (dismissButtonId != 0) return view.findViewById(dismissButtonId)

        // Example: Look for a specific view field (e.g., 'mFooter', 'mDismissButton')
        try {
            val footerView = XposedHelpers.getObjectField(view, "mFooter") as? ViewGroup
            if (footerView != null) {
                // Search recursively within the footer
                return findTextViewInViewGroup(footerView)
            }
            // Add other potential field names if known
        } catch (e: NoSuchFieldException) {
            // Field doesn't exist, try another method or give up
        } catch (e: Exception) {
            Log.w(TAG, "Error accessing potential dismiss view field", e)
        }

        // Fallback: Recursive search from the TaskView root (less efficient)
        // return findTextViewInViewGroup(view) // Be careful with performance

        return null // Not found
    }

    /**
     * Recursively searches for the first TextView within a ViewGroup.
     */
    private fun findTextViewInViewGroup(viewGroup: View): TextView? {
        if (viewGroup is TextView) {
            return viewGroup
        }
        if (viewGroup is ViewGroup) {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                val found = findTextViewInViewGroup(child) // Recurse
                if (found != null) {
                    return found // Return the first one found
                }
            }
        }
        return null // Not found in this branch
    }

    /**
     * Attempts to change the text of the dismiss view.
     */
    private fun changeDismissViewText(taskView: View, text: String) {
        try {
            findDismissView(taskView)?.let { textView ->
                // Store original text if needed for reset? Could use view tags.
                // textView.setTag(R.id.tag_original_text, textView.text) // Need a resource ID for tag key
                textView.text = text
                Log.d(TAG, "Changed dismiss view text to '$text'")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to change dismiss view text", e)
        }
    }

    /**
     * Attempts to reset the text of the dismiss view (if original text was stored).
     */
    // private fun resetDismissViewText(taskView: View) {
    //     try {
    //         findDismissView(taskView)?.let { textView ->
    //             val originalText = textView.getTag(R.id.tag_original_text) as? CharSequence
    //             if (originalText != null) {
    //                 textView.text = originalText
    //                 Log.d(TAG, "Reset dismiss view text")
    //             }
    //         }
    //     } catch (e: Exception) {
    //         Log.w(TAG, "Failed to reset dismiss view text", e)
    //     }
    // }
}