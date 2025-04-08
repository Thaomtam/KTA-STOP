package com.KTA.STOP;

import android.content.ComponentName;
import android.content.Context; // Import rõ ràng
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import com.github.kyuubiran.ezxhelper.utils.findMethod;
import com.github.kyuubiran.ezxhelper.utils.hookAfter;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import rikka.hidden.compat.ActivityManagerApis; // Sử dụng thư viện Rikka để gọi API ẩn

public class ForceStopAndDisableHandler implements IXposedHookLoadPackage {
    companion object {
        private const val TAG = "KTASTOP_ForceStopDisable" // Đổi tên TAG một chút

        // Danh sách các ứng dụng ngoại lệ không bị xử lý
        private val EXCEPTION_LIST = setOf(
            "android",
            "com.zing.zalo",
            "com.android.systemui",
            "com.android.launcher3", // Chính trình khởi chạy
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
            "com.android.inputmethod.latin", // Bàn phím
            "com.google.android.gms" // Dịch vụ Google Play
            // Thêm các gói quan trọng khác nếu cần
        )
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Chỉ hook vào Launcher3
        if (lpparam.packageName != "com.android.launcher3") return

        Log.d(TAG, "handleLoadPackage: Đã hook vào Launcher3")
        runCatching {
            // Hook phương thức loại bỏ tác vụ
            val recentsViewClass = lpparam.classLoader.loadClass("com.android.quickstep.views.RecentsView")
            findMethod(recentsViewClass) {
                name == "dismissTask"
            }.hookAfter { param ->
                onTaskDismissed(param.args[0] as View) // Gọi hàm xử lý
            }

            // Hook phương thức xử lý chạm trên TaskView để phát hiện nhấn giữ
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
            Log.e(TAG, "handleLoadPackage: Lỗi khi hook Launcher3", it)
        }
    }

    // Handler và biến để quản lý logic nhấn giữ
    private var handler = Handler(Looper.getMainLooper())
    private var mCurrentView: View? = null // View đang chạm
    private var mToBeKilled: View? = null // View được đánh dấu để xử lý
    private var longPressRunnable = Runnable { setupTaskForKill() } // Runnable thực thi khi nhấn giữ

    // Đánh dấu view để xử lý nếu nhấn giữ đủ lâu
    private fun setupTaskForKill() {
        mCurrentView?.let { view ->
            Log.d(TAG, "setupTaskForKill: Đã nhấn giữ đủ lâu, đánh dấu task.")
            mToBeKilled = view
        }
    }

    // Khi bắt đầu chạm vào TaskView
    private fun onTaskTouchDown(view: View) {
        mCurrentView = view
        mToBeKilled = null
        handler.postDelayed(longPressRunnable, 100) // Bắt đầu đếm 100ms
    }

    // Khi nhấc ngón tay khỏi TaskView
    private fun onTaskTouchUp(view: View) {
        handler.removeCallbacks(longPressRunnable) // Hủy đếm nếu chưa đủ 100ms
        mCurrentView = null
    }

    // Hàm chính: Xử lý khi tác vụ bị loại bỏ (dismiss)
    private fun onTaskDismissed(view: View) {
        // Chỉ xử lý nếu view này đã được đánh dấu từ trước (do nhấn giữ)
        if (mToBeKilled != view) {
            return
        }

        Log.d(TAG, "onTaskDismissed: Xử lý tác vụ đã nhấn giữ và loại bỏ.")
        mToBeKilled = null // Reset trạng thái

        // Bọc toàn bộ logic trong runCatching để bắt lỗi tổng quát
        runCatching {
            // Trích xuất thông tin gói từ View
            val task = XposedHelpers.callMethod(view, "getTask")
            val taskKey = XposedHelpers.getObjectField(task, "key")
            val componentName = XposedHelpers.getObjectField(taskKey, "componentName") as ComponentName
            val userId = XposedHelpers.getObjectField(taskKey, "userId") as Int
            val packageName = componentName.packageName

            // Kiểm tra danh sách ngoại lệ
            if (EXCEPTION_LIST.contains(packageName)) {
                Log.d(TAG, "Gói $packageName nằm trong danh sách ngoại lệ, bỏ qua.")
                return@runCatching
            }

            Log.d(TAG, "Chuẩn bị buộc dừng và vô hiệu hóa gói: $packageName (UserId: $userId)")

            // 1. Buộc dừng ứng dụng (Force Stop) sử dụng Rikka Hidden API Compat
            try {
                ActivityManagerApis.forceStopPackage(packageName, userId)
                Log.i(TAG, "Đã buộc dừng (force stopped) gói: $packageName")
            } catch (e: SecurityException) {
                Log.e(TAG, "Không có quyền buộc dừng gói $packageName (UserId: $userId). Cần quyền FORCE_STOP_PACKAGES.", e)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi buộc dừng gói $packageName (UserId: $userId)", e)
            }

            // Lấy Context để sử dụng PackageManager
            val context = XposedHelpers.callStaticMethod(
                XposedHelpers.findClass("android.app.ActivityThread", null),
                "currentApplication"
            ) as Context
            val pm = context.packageManager

            // 2. Vô hiệu hóa ứng dụng để ngăn tự khởi chạy dịch vụ/thông báo
            // Sử dụng COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED:
            // - Vô hiệu hóa hoàn toàn (dịch vụ, receiver không chạy).
            // - Tự động kích hoạt lại khi người dùng mở ứng dụng thủ công.
            try {
                pm.setApplicationEnabledSetting(
                    packageName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED,
                    0 // Flags (0 là mặc định)
                )
                Log.i(TAG, "Đã vô hiệu hóa (disabled until used) gói $packageName để ngăn tự khởi chạy.")
            } catch (e: SecurityException) {
                Log.e(TAG, "Không có quyền vô hiệu hóa gói $packageName. Cần quyền CHANGE_COMPONENT_ENABLED_STATE.", e)
            } catch (e: IllegalArgumentException) {
                 Log.e(TAG, "Không thể vô hiệu hóa gói $packageName, có thể là gói hệ thống quan trọng hoặc không tồn tại.", e)
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi khi vô hiệu hóa gói $packageName", e)
            }

        }.onFailure {
            // Ghi log nếu có lỗi không mong muốn trong quá trình xử lý
            Log.e(TAG, "Lỗi trong quá trình xử lý onTaskDismissed", it)
        }
    }
}
