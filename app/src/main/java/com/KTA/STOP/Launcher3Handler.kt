package com.KTA.STOP // Ensure this matches your actual package structure

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
import java.lang.ref.WeakReference // Use WeakReference for Context to avoid leaks

// Import HiddenApiBypass - Make sure this import is correct based on your project setup
import org.lsposed.hiddenapibypass.HiddenApiBypass

class Launcher3Handler : IXposedHookLoadPackage {
    companion object {
        private const val TAG = "KTASTOP-Launcher3Handler"
        private const val PREFS_NAME = "KTASTOP_launcher3_prefs"
        private const val KEY_EXCEPTION_LIST = "exception_list"
        private const val KEY_ENABLE_FORCE_STOP = "enableForceStop"
        private const val LONG_PRESS_TIMEOUT_MS = 300L // Adjusted slightly, configure as needed

        // Default protected system packages
        private val DEFAULT_SYSTEM_PACKAGES = setOf(
            "android",
			"com.zing.zalo",
            "com.android.systemui",
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
            "com.google.android.gms", // Google Play Services
            "com.google.android.gsf", // Google Services Framework
            "com.android.vending", // Google Play Store
            "com.android.inputmethod.latin", // AOSP Keyboard
            "com.google.android.inputmethod.latin", // Gboard
            // Add launchers to prevent self-kill issues
            "com.android.launcher3", // Target package itself
            "com.google.android.apps.nexuslauncher" // Pixel Launcher
            // Add more critical system/input method packages as needed
        )
    }

    private var contextRef: WeakReference<Context>? = null
    private var prefs: SharedPreferences? = null
    private var exceptionList: MutableSet<String> = mutableSetOf()
    private var enableForceStop: Boolean = true
    private val handler = Handler(Looper.getMainLooper())
    private var currentTouchedView: View? = null
    private var viewMarkedForKill: View? = null
    private var longPressRunnable: Runnable? = null

    // Lazily initialize IActivityManager using reflection & HiddenApiBypass
    private val iActivityManager: Any? by lazy {
        try {
            // Ensure HiddenApiBypass allows access if needed for getting the service/stub
            // (Usually ServiceManager is okay, but Stub might be restricted)
            Log.d(TAG, "Attempting to get IActivityManager...")
            val amServiceBinder = ServiceManager.getService(Context.ACTIVITY_SERVICE) as IBinder?
            if (amServiceBinder == null) {
                Log.e(TAG, "Failed to get ActivityService binder.")
                return@lazy null
            }
            val stubClass = XposedHelpers.findClass("android.app.IActivityManager\$Stub", null) // Use context classloader if available? Usually null works for system classes.
            val asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder::class.java)
            val instance = asInterfaceMethod.invoke(null, amServiceBinder)
            Log.i(TAG, "Successfully obtained IActivityManager instance.")
            instance
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to get IActivityManager instance via reflection", e)
            null
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Target specific launcher packages
        // Add other launcher package names if needed (e.g., Pixel Launcher)
        val targetPackages = setOf("com.android.launcher3", "com.google.android.apps.nexuslauncher")
        if (lpparam.packageName !in targetPackages) {
            return
        }

        Log.i(TAG, "Hooking into Launcher package: ${lpparam.packageName}")

        // --- Add Hidden API Exemptions ---
        // Call this early, before accessing restricted APIs.
        // We primarily need access to IActivityManager methods.
        // Add exemptions generously for related classes if experiencing issues.
        try {
            Log.d(TAG, "Attempting to bypass Hidden API restrictions...")
            val exempted = HiddenApiBypass.addHiddenApiExemptions(
                "Landroid/app/IActivityManager;", // Main interface for forceStopPackage
                "Landroid/app/ActivityManager;", // Related concrete class, internals might be used
                "Landroid/app/IActivityManager\$Stub;", // For the asInterface method
                "Landroid/os/ServiceManager;" // For getService
                // Add more if specific errors point to other restricted classes/methods
                // Examples from user query (adjust if actually needed):
                // "Landroid/content/pm/ApplicationInfo;",
                // "Ldalvik/system",
                // "Lx" // Be careful with wildcard prefixes like this
            )
            if (exempted) {
                Log.i(TAG, "Successfully added Hidden API exemptions.")
            } else {
                Log.w(TAG, "Failed to add Hidden API exemptions (or bypass not needed/supported).")
                // Continue anyway, it might work on older Android or without strict enforcement
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error occurred during HiddenApiBypass setup", e)
            // Show a persistent warning? This might prevent the core feature from working.
            // Toast might not be possible here yet if context isn't available.
        }
        // --- End Hidden API Exemptions ---


        // Centralized error handling for hooks
        runCatching {
            // Find classes using the provided ClassLoader
            // Class names might differ slightly in variants (e.g., Pixel Launcher)
            // Consider making these class names configurable or adding checks if issues arise.
            val recentsViewClass = XposedHelpers.findClass("com.android.quickstep.views.RecentsView", lpparam.classLoader)
            val taskViewClass = XposedHelpers.findClass("com.android.quickstep.views.TaskView", lpparam.classLoader)

            // --- Hook Task Dismissal ---
            findMethod(recentsViewClass) {
                name == "dismissTask" // Check parameters if ambiguous
                // Example: parameterTypes.size == 1 && parameterTypes[0].isAssignableFrom(taskViewClass)
            }.hookBefore { param ->
                val dismissedTaskView = param.args[0] as? View
                if (dismissedTaskView == null) {
                    Log.w(TAG, "dismissTask hook: Argument is not a View or is null")
                    return@hookBefore
                }
                initPreferencesIfNeeded(dismissedTaskView.context) // Ensure prefs init
                onTaskDismissed(dismissedTaskView)
            }
            Log.d(TAG, "Hooked RecentsView.dismissTask")

            // --- Hook Task Touch Events ---
            findMethod(taskViewClass) {
                name == "onTouchEvent" && parameterTypes.contentEquals(arrayOf(MotionEvent::class.java))
            }.hookBefore { param ->
                val taskView = param.thisObject as View
                val motionEvent = param.args[0] as MotionEvent
                initPreferencesIfNeeded(taskView.context) // Ensure prefs init
                when (motionEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN -> onTaskTouchDown(taskView)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onTaskTouchUp(taskView)
                }
            }
            Log.d(TAG, "Hooked TaskView.onTouchEvent")

        }.onFailure { error ->
            Log.e(TAG, "Failed to set up hooks in ${lpparam.packageName}", error)
            // Consider a more visible error notification if hooks fail
        }
    }

    @Synchronized
    private fun initPreferencesIfNeeded(context: Context) {
        if (prefs != null && contextRef?.get() != null) {
            return
        }
        Log.d(TAG, "Initializing preferences...")
        // Use application context to avoid leaking Activity/View contexts
        val appContext = context.applicationContext
        contextRef = WeakReference(appContext)
        // Make SharedPreferences world-readable if a separate settings UI needs to access it
        // MODE_WORLD_READABLE is deprecated, use ContentProvider or other IPC if needed.
        // For internal use, MODE_PRIVATE is fine.
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        loadSettings() // Load initial values

        // Register listener only once
        prefs?.registerOnSharedPreferenceChangeListener(settingsChangeListener)
        Log.i(TAG, "Preferences initialized and listener registered.")
    }

    private val settingsChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        Log.d(TAG, "SharedPreferences changed: key='$key'")
        when (key) {
            KEY_ENABLE_FORCE_STOP -> {
                enableForceStop = sharedPreferences.getBoolean(key, true)
                Log.i(TAG, "Force stop feature toggled via settings: $enableForceStop")
                if (!enableForceStop) {
                    cancelLongPress() // Cancel any pending action if disabled
                }
            }
            KEY_EXCEPTION_LIST -> {
                loadExceptionList() // Reload the list
                Log.i(TAG, "Exception list reloaded via settings. New size: ${exceptionList.size}")
            }
        }
    }

    private fun loadSettings() {
        prefs?.let { p ->
            enableForceStop = p.getBoolean(KEY_ENABLE_FORCE_STOP, true)
            loadExceptionList() // Load/reload the exception list
            Log.d(TAG, "Settings loaded: enableForceStop=$enableForceStop, exceptionList size=${exceptionList.size}")
        } ?: run {
            Log.e(TAG, "Cannot load settings, SharedPreferences is null.")
            // Fallback to defaults if prefs fail unexpectedly
            enableForceStop = true
            exceptionList = DEFAULT_SYSTEM_PACKAGES.toMutableSet()
        }
    }

    private fun loadExceptionList() {
        val savedList = prefs?.getStringSet(KEY_EXCEPTION_LIST, emptySet()) ?: emptySet()
        // Always combine saved list with the default system packages
        exceptionList = mutableSetOf<String>().apply {
            addAll(savedList)
            addAll(DEFAULT_SYSTEM_PACKAGES)
        }
        // Log.v(TAG, "Loaded exception list: $exceptionList") // Verbose
    }

    private fun isPackageInExceptionList(packageName: String?): Boolean {
        if (packageName.isNullOrEmpty()) return false
        // Perform case-insensitive check? Package names are generally case-sensitive, but good practice? Let's keep it sensitive.
        return exceptionList.contains(packageName)
    }

    // --- Touch Event Handling ---

    private fun onTaskTouchDown(view: View) {
        if (!enableForceStop) return
        cancelLongPress() // Cancel previous one if any
        currentTouchedView = view
        viewMarkedForKill = null // Clear previous kill mark

        longPressRunnable = Runnable { setupTaskForKill() }
        handler.postDelayed(longPressRunnable!!, LONG_PRESS_TIMEOUT_MS)
        // Log.v(TAG, "ACTION_DOWN on view. Scheduled long press check.")
    }

    private fun onTaskTouchUp(view: View) {
        cancelLongPress()
        // Log.v(TAG, "ACTION_UP/CANCEL on view. Cancelled long press check.")
        // Optional: Reset visual state if needed, but view is often removed or recycled quickly.
        currentTouchedView = null // Clear reference on touch release
    }

    private fun cancelLongPress() {
        longPressRunnable?.let { handler.removeCallbacks(it) }
        longPressRunnable = null
        // Don't clear currentTouchedView here, wait for ACTION_UP/CANCEL
    }

    // --- Core Logic ---

    private fun setupTaskForKill() {
        longPressRunnable = null // Runnable has executed
        if (!enableForceStop || currentTouchedView == null) {
            Log.d(TAG, "setupTaskForKill: Aborted (feature disabled or view gone)")
            currentTouchedView = null // Ensure clean state
            return
        }

        val view = currentTouchedView!!
        val context = view.context ?: contextRef?.get()
        val taskInfo = getTaskInfo(view)

        if (taskInfo == null) {
             Log.w(TAG, "setupTaskForKill: Could not get TaskInfo for the touched view.")
             context?.let { Toast.makeText(it, "Lỗi: Không thể lấy thông tin ứng dụng", Toast.LENGTH_SHORT).show() }
             currentTouchedView = null // Abort if info unavailable
             return
        }

        // Check protection list
        if (isPackageInExceptionList(taskInfo.packageName)) {
            Log.d(TAG, "setupTaskForKill: Package '${taskInfo.packageName}' is protected.")
            context?.let {
                // Provide more context in the toast
                val appLabel = getAppLabel(context, taskInfo.packageName)
                Toast.makeText(it, "Không thể dừng: \"$appLabel\" (${taskInfo.packageName}) được bảo vệ", Toast.LENGTH_SHORT).show()
            }
            currentTouchedView = null // Clear reference as action is blocked
            return
        }

        // Mark for kill and provide feedback
        viewMarkedForKill = view
        Log.i(TAG, "setupTaskForKill: Marked view for package '${taskInfo.packageName}' for potential kill.")
        context?.let {
            val appLabel = getAppLabel(context, taskInfo.packageName)
            Toast.makeText(it, "Vuốt lên để dừng \"$appLabel\"", Toast.LENGTH_SHORT).show()
            changeDismissViewText(view, "Force Stop") // Optional visual cue (fragile)
        }
        // Don't clear currentTouchedView here, wait for ACTION_UP/CANCEL
    }

    private fun onTaskDismissed(view: View) {
        // Check if the feature is enabled AND if this specific view was the one marked
        if (!enableForceStop || viewMarkedForKill != view) {
            // If a marked view exists but *this* dismissed view isn't it, clear the mark (edge case safety)
            if (viewMarkedForKill != null && viewMarkedForKill != view) {
                 Log.w(TAG, "onTaskDismissed: Dismissed view was not the one marked for kill. Clearing mark.")
                 viewMarkedForKill = null
            }
            return // Ignore dismissal
        }

        Log.d(TAG, "onTaskDismissed: Intercepted dismissal for marked view.")
        val viewToKill = viewMarkedForKill // Local copy for clarity
        viewMarkedForKill = null // Reset the flag *immediately*

        val context = view.context ?: contextRef?.get()
        val taskInfo = getTaskInfo(viewToKill) // Get info again

        if (taskInfo == null) {
            Log.e(TAG, "onTaskDismissed: Failed to get TaskInfo for the view to kill.")
            context?.let { Toast.makeText(it, "Lỗi: Không thể lấy thông tin ứng dụng để dừng", Toast.LENGTH_SHORT).show() }
            return
        }

        // Final protection check (safety net)
        if (isPackageInExceptionList(taskInfo.packageName)) {
            Log.w(TAG, "onTaskDismissed: Package '${taskInfo.packageName}' is protected (final check). Aborting kill.")
             context?.let {
                val appLabel = getAppLabel(context, taskInfo.packageName)
                Toast.makeText(it, "Đã hủy dừng: \"$appLabel\" được bảo vệ", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // --- Perform Force Stop ---
        val am = iActivityManager // Use the cached instance
        if (am == null) {
            Log.e(TAG, "onTaskDismissed: IActivityManager is null, cannot force stop.")
            context?.let { Toast.makeText(it, "Lỗi: Không thể truy cập Activity Manager", Toast.LENGTH_SHORT).show() }
            return
        }

        try {
            Log.i(TAG, "Attempting to force stop package: ${taskInfo.packageName}, userId: ${taskInfo.userId}")

            // Call forceStopPackage using reflection - HiddenApiBypass should allow this
            XposedHelpers.callMethod(am, "forceStopPackage", taskInfo.packageName, taskInfo.userId)

            Log.i(TAG, "Successfully requested force stop for package: ${taskInfo.packageName}")
            context?.let {
                val appLabel = getAppLabel(context, taskInfo.packageName)
                Toast.makeText(it, "Đã dừng ứng dụng: $appLabel", Toast.LENGTH_SHORT).show()
            }

        } catch (e: SecurityException) {
             Log.e(TAG, "SecurityException force stopping package: ${taskInfo.packageName}. Check permissions or Hidden API bypass.", e)
             context?.let { Toast.makeText(it, "Lỗi quyền khi dừng: ${taskInfo.packageName}", Toast.LENGTH_SHORT).show() }
        } catch (e: Throwable) { // Catch broader errors
            Log.e(TAG, "Error force stopping package: ${taskInfo.packageName}", e)
            context?.let { Toast.makeText(it, "Lỗi khi dừng ứng dụng: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    // --- Helper Functions ---

    private data class TaskInfo(val packageName: String?, val userId: Int, val componentName: ComponentName?)

    private fun getTaskInfo(taskView: View?): TaskInfo? {
        if (taskView == null) return null
        return try {
            // Reflection targets - these might need adjustment for different Launcher3 versions/variants
            val taskObject = XposedHelpers.callMethod(taskView, "getTask") ?: return null
            // Try common names for the key field/method
            val taskKeyObject = XposedHelpers.getObjectField(taskObject, "key")
                ?: XposedHelpers.callMethod(taskObject, "getKey")
                ?: return null
            // Try common names for componentName field/method
            val componentName = (XposedHelpers.getObjectField(taskKeyObject, "componentName") as? ComponentName)
                ?: (XposedHelpers.callMethod(taskKeyObject, "getComponent") as? ComponentName)
            // Try common names for userId field/method
            val userId = (XposedHelpers.getObjectField(taskKeyObject, "userId") as? Int)
                ?: (XposedHelpers.callMethod(taskKeyObject, "getUserId") as? Int)
                ?: 0 // Default to user 0 if not found

            val packageName = componentName?.packageName

            if (packageName.isNullOrEmpty()) {
                 Log.w(TAG, "getTaskInfo: Could not determine package name. ComponentName: $componentName")
                 // Return null if package name is essential and missing
                 return null
            }

            TaskInfo(packageName, userId, componentName)
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "Error getting task info: Method not found. Launcher version mismatch?", e)
            null
        } catch (e: NoSuchFieldException) {
            Log.e(TAG, "Error getting task info: Field not found. Launcher version mismatch?", e)
            null
        } catch (e: Throwable) { // Catch any other reflection errors
            Log.e(TAG, "Generic error getting task info via reflection", e)
            null
        }
    }

    /**
     * Helper to get application label for better Toasts.
     */
    private fun getAppLabel(context: Context, packageName: String?): String {
        if (packageName.isNullOrEmpty()) return "Unknown App"
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0) // No flags needed for label
            pm.getApplicationLabel(appInfo)?.toString() ?: packageName // Fallback to package name
        } catch (e: Exception) {
            Log.w(TAG, "Couldn't get app label for $packageName", e)
            packageName // Fallback to package name on error
        }
    }


    // --- Optional Visual Feedback Helpers (Fragile - Use with Caution) ---

    private fun findDismissView(view: View): TextView? {
        // This is highly dependent on the specific Launcher's layout structure.
        // It might be a Button, an ImageView with content description, or a TextView.
        // Inspect the layout with a tool if possible.
        // Example: Search for a view with specific text or content description.
        // Example: Search within a known container ID (e.g., "dismiss_view", "task_footer")

        // Generic recursive search for *any* TextView (less reliable)
        // return findTextViewInViewGroup(view)

        // Specific field access (if known from decompilation)
        try {
            // Common field names for footer/action views: mActionsView, mFooter, mDismissButton
             val potentialContainer = XposedHelpers.getObjectField(view, "mActionsView") as? ViewGroup
                 ?: XposedHelpers.getObjectField(view, "mFooter") as? ViewGroup

             if (potentialContainer != null) {
                  // Search within the likely container first
                  return findTextViewInViewGroup(potentialContainer) // Find first TextView inside
             }
             // If no known container, maybe search the whole TaskView (less efficient)
             // return findTextViewInViewGroup(view)

        } catch (e: NoSuchFieldException) {
             // Field doesn't exist on this version
        } catch (e: Exception) {
            Log.w(TAG, "Error accessing potential dismiss view field", e)
        }
        Log.w(TAG, "findDismissView: Could not reliably find a view to change text on.")
        return null // Not found or error
    }

    private fun findTextViewInViewGroup(view: View): TextView? {
        if (view is TextView) {
            // Maybe add checks here? e.g., if (view.isClickable() || !view.text.isNullOrEmpty())
            return view
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val found = findTextViewInViewGroup(child)
                if (found != null) {
                    return found
                }
            }
        }
        return null
    }

    private fun changeDismissViewText(taskView: View, text: String) {
        // Be cautious: This might break easily with launcher updates.
        try {
            findDismissView(taskView)?.let { textView ->
                // Consider saving original text if you want to restore it later
                // textView.setTag(R.id.some_tag_id_for_original_text, textView.text)
                textView.text = text
                Log.d(TAG, "Attempted to change dismiss view text to '$text'")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to change dismiss view text", e)
        }
    }

    // Resetting text is likely unnecessary as the view is dismissed, but shown for completeness
    /*
    private fun resetDismissViewText(taskView: View) {
        try {
            findDismissView(taskView)?.let { textView ->
                // Assuming you stored the original text in a tag
                // val originalText = textView.getTag(R.id.some_tag_id_for_original_text) as? CharSequence
                // if (originalText != null) {
                //     textView.text = originalText
                //     Log.d(TAG, "Attempted to reset dismiss view text")
                // } else {
                     // Fallback: Try setting to a default like "Dismiss" or empty string?
                // }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to reset dismiss view text", e)
        }
    }
    */
}