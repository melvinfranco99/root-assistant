package com.melvin.rootassistant

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

/**
 * Recoge información del dispositivo y determina el estado de root/bootloader.
 * Todo se hace leyendo propiedades públicas del sistema; la app NO ejecuta
 * exploits ni modifica particiones.
 */
object DeviceInfo {

    data class Report(
        val manufacturer: String,
        val brand: String,
        val model: String,
        val device: String,
        val soc: String,
        val abi: String,
        val androidRelease: String,
        val sdkInt: Int,
        val securityPatch: String,
        val buildId: String,
        val tags: String,
        val isRooted: Boolean,
        val rootStatus: String,
        val bootloaderUnlocked: Boolean?,
        val bootloaderStatus: String,
        val recommendedMethod: String
    )

    fun collect(context: Context): Report {
        val manufacturer = Build.MANUFACTURER ?: "desconocido"
        val brand = Build.BRAND ?: "desconocido"
        val model = Build.MODEL ?: "desconocido"
        val device = Build.DEVICE ?: "desconocido"

        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Build.SOC_MANUFACTURER, Build.SOC_MODEL)
                .filter { !it.isNullOrBlank() && it != Build.UNKNOWN }
                .joinToString(" ")
                .ifBlank { Build.HARDWARE ?: "desconocido" }
        } else {
            Build.HARDWARE ?: "desconocido"
        }

        val abi = Build.SUPPORTED_ABIS?.joinToString(", ") ?: "desconocido"
        val securityPatch = Build.VERSION.SECURITY_PATCH ?: "desconocido"

        // --- Root ---
        val rootReasons = mutableListOf<String>()
        findSuBinary()?.let { rootReasons.add("binario su en $it") }
        if (magiskInstalled(context)) rootReasons.add("app Magisk instalada")
        if (kernelSuInstalled(context)) rootReasons.add("app KernelSU instalada")
        if (Build.TAGS?.contains("test-keys") == true) rootReasons.add("build test-keys")
        val isRooted = rootReasons.isNotEmpty()
        val rootStatus = if (isRooted) {
            "SÍ (${rootReasons.joinToString(", ")})"
        } else {
            "No detectado"
        }

        // --- Bootloader ---
        val locked = getProp("ro.boot.flash.locked")
        val vbState = getProp("ro.boot.verifiedbootstate")
        val devState = getProp("ro.boot.vbmeta.device_state")
        val oemUnlockAllowed = getProp("sys.oem_unlock_allowed")

        val bootloaderUnlocked: Boolean? = when {
            devState.equals("unlocked", true) -> true
            devState.equals("locked", true) -> false
            vbState.equals("orange", true) || vbState.equals("red", true) -> true
            vbState.equals("green", true) || vbState.equals("yellow", true) -> false
            locked == "0" -> true
            locked == "1" -> false
            else -> null
        }
        val bootloaderStatus = when (bootloaderUnlocked) {
            true -> "DESBLOQUEADO" + extraBoot(vbState, oemUnlockAllowed)
            false -> "Bloqueado" + extraBoot(vbState, oemUnlockAllowed)
            null -> "No se pudo determinar" + extraBoot(vbState, oemUnlockAllowed)
        }

        val recommended = RootGuide.recommendMethod(manufacturer, brand, Build.VERSION.SDK_INT, isRooted)

        return Report(
            manufacturer = manufacturer,
            brand = brand,
            model = model,
            device = device,
            soc = soc,
            abi = abi,
            androidRelease = Build.VERSION.RELEASE ?: "?",
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = securityPatch,
            buildId = Build.DISPLAY ?: Build.ID ?: "?",
            tags = Build.TAGS ?: "?",
            isRooted = isRooted,
            rootStatus = rootStatus,
            bootloaderUnlocked = bootloaderUnlocked,
            bootloaderStatus = bootloaderStatus,
            recommendedMethod = recommended
        )
    }

    private fun extraBoot(vbState: String, oemAllowed: String): String {
        val parts = mutableListOf<String>()
        if (vbState.isNotBlank()) parts.add("verifiedboot=$vbState")
        if (oemAllowed == "1") parts.add("OEM unlock permitido en Ajustes")
        if (oemAllowed == "0") parts.add("OEM unlock NO permitido (actívalo en Opciones de desarrollador)")
        return if (parts.isEmpty()) "" else " [" + parts.joinToString(", ") + "]"
    }

    private fun findSuBinary(): String? {
        val paths = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su", "/su/bin/su",
            "/vendor/bin/su", "/system/sbin/su", "/data/local/xbin/su",
            "/data/local/bin/su", "/data/local/su", "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su", "/cache/su", "/dev/su"
        )
        for (p in paths) {
            try {
                if (File(p).exists()) return p
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun magiskInstalled(context: Context): Boolean =
        isPackageInstalled(context, "com.topjohnwu.magisk") ||
            isPackageInstalled(context, "io.github.huskydg.magisk")

    private fun kernelSuInstalled(context: Context): Boolean =
        isPackageInstalled(context, "me.weishu.kernelsu")

    private fun isPackageInstalled(context: Context, pkg: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(pkg, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /** Lee una propiedad del sistema (getprop) por reflexión, sin ejecutar procesos. */
    private fun getProp(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            (method.invoke(null, key, "") as? String) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
