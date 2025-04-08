-dontobfuscate
-dontoptimize

# Giữ lại các lớp Entry và các handler
-keep class com.KTA.STOP.Entry { *; }
-keep class com.KTA.STOP.Launcher3Handler { *; }

# Giữ lại các tiện ích của EzXHelper
-keep class com.github.kyuubiran.ezxhelper.utils.** { *; }

# Giữ lại các lớp từ Rikka Hidden API
-keep class dev.rikka.hidden.** { *; }
-keep class rikka.hidden.compat.** { *; }

# Bỏ qua các cảnh báo về các lớp từ Xposed
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn android.content.res.XModuleResources
-dontwarn android.content.res.XResources
-dontwarn de.robv.android.xposed.IXposedHookLoadPackage
-dontwarn de.robv.android.xposed.IXposedHookZygoteInit$StartupParam
-dontwarn de.robv.android.xposed.IXposedHookZygoteInit
-dontwarn de.robv.android.xposed.XC_MethodHook$MethodHookParam
-dontwarn de.robv.android.xposed.XC_MethodHook$Unhook
-dontwarn de.robv.android.xposed.XC_MethodHook
-dontwarn de.robv.android.xposed.XC_MethodReplacement
-dontwarn de.robv.android.xposed.XposedBridge
-dontwarn de.robv.android.xposed.XposedHelpers
-dontwarn de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam

# Bỏ qua các cảnh báo về các lớp từ thư viện mã hóa
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
