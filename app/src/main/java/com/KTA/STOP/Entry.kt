package com.KTA.STOP // <-- Quan trọng: Giữ nguyên package này giống với Launcher3Handler.kt

import android.util.Log
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
// Không cần import Launcher3Handler ở đây nữa vì nó nằm cùng package "com.KTA.STOP"
// import com.KTA.STOP.Launcher3Handler // <--- KHÔNG CẦN DÒNG NÀY
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

class Entry : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        // Sử dụng TAG rõ ràng hơn cho việc debug
        private const val TAG = "KTASTOP_Entry"
        lateinit var modulePath: String

        // Định nghĩa package mục tiêu ở đây để dễ quản lý
        private const val LAUNCHER3_PACKAGE = "com.android.launcher3"
        // Bạn có thể thêm các package launcher khác ở đây nếu muốn hỗ trợ sau này
        // private const val PIXEL_LAUNCHER_PACKAGE = "com.google.android.apps.nexuslauncher"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        Log.i(TAG, "Initializing in Zygote. Module path: $modulePath") // Dùng Log.i cho các bước khởi tạo
        EzXHelperInit.initZygote(startupParam)
        // Thường không cần làm gì nhiều ở đây trừ khi bạn hook trực tiếp process Zygote
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Khởi tạo EzXHelper cho context package cụ thể đang được load
        EzXHelperInit.initHandleLoadPackage(lpparam)

        // Log package nào đang được xử lý bởi logic module của bạn
        // Dùng Log.d cho thông tin debug, Log.i cho các bước thông tin
        // Log.d(TAG, "Handling package: ${lpparam.packageName}, Process: ${lpparam.processName}") // Bỏ comment nếu cần log mọi package

        // Điều hướng đến handler phù hợp dựa trên tên package
        try {
            when (lpparam.packageName) {
                LAUNCHER3_PACKAGE -> {
                    Log.i(TAG, "Detected target package $LAUNCHER3_PACKAGE. Initializing Launcher3Handler.")
                    // Tạo một instance của handler và gọi handleLoadPackage của nó
                    // Vì Entry và Launcher3Handler cùng package "com.KTA.STOP",
                    // bạn có thể gọi trực tiếp mà không cần import.
                    val handler = Launcher3Handler() // <-- Tạo instance trực tiếp
                    handler.handleLoadPackage(lpparam)
                }
                // Ví dụ: Thêm hỗ trợ cho Pixel Launcher (nếu hook tương thích hoặc tạo handler mới)
                // PIXEL_LAUNCHER_PACKAGE -> {
                //    Log.i(TAG, "Detected target package $PIXEL_LAUNCHER_PACKAGE. Initializing handler.")
                //    Launcher3Handler().handleLoadPackage(lpparam) // Hoặc dùng PixelLauncherHandler() riêng
                // }

                // Nếu không phải package mục tiêu, bỏ qua
                else -> {
                    // Log.v(TAG, "Ignoring package: ${lpparam.packageName}") // Log chi tiết nếu cần
                    return // Thoát sớm đối với các package không phải mục tiêu
                }
            }
        } catch (e: Throwable) {
            // Bắt các lỗi tiềm ẩn trong quá trình khởi tạo hoặc thực thi handler
            Log.e(TAG, "Error while handling package ${lpparam.packageName}", e)
        }
    }
}
