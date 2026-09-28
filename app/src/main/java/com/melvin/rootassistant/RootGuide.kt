package com.melvin.rootassistant

/**
 * Genera el procedimiento de rooteo adaptado al fabricante concreto.
 * El estándar moderno es: desbloquear bootloader -> parchear boot.img con
 * Magisk -> flashear por fastboot. Los detalles del desbloqueo cambian por marca.
 */
object RootGuide {

    fun recommendMethod(manufacturer: String, brand: String, sdkInt: Int, isRooted: Boolean): String {
        if (isRooted) return "Ya tienes root. No necesitas repetir el proceso."
        val vendor = "$manufacturer $brand".lowercase()
        val base = "Magisk (parcheo de boot.img + fastboot). Es el método estándar y más seguro en Android moderno."
        val note = when {
            vendor.containsAny("samsung") ->
                "En Samsung se usa Odin en vez de fastboot, y muchos modelos de EE. UU. tienen el bootloader imposible de desbloquear."
            vendor.containsAny("huawei", "honor") ->
                "Huawei/Honor llevan años SIN dar códigos de desbloqueo oficiales: en la mayoría de modelos recientes NO es posible rootear."
            else -> "Comprueba primero que tu bootloader se puede desbloquear."
        }
        return "$base\n$note"
    }

    fun buildSteps(info: DeviceInfo.Report): String {
        val sb = StringBuilder()
        sb.appendLine("PLAN DE ROOTEO — ${info.manufacturer} ${info.model}")
        sb.appendLine("Android ${info.androidRelease} · API ${info.sdkInt} · ${info.soc}")
        sb.appendLine("Bootloader: ${info.bootloaderStatus}")
        sb.appendLine("─────────────────────────────────────")
        sb.appendLine()

        // Paso 0 — copia de seguridad
        sb.appendLine("PASO 0 · Copia de seguridad")
        sb.appendLine("• Guarda fotos, contactos y datos: el paso de desbloqueo BORRA el móvil.")
        sb.appendLine("• Anota tu modelo exacto y la versión de compilación (arriba).")
        sb.appendLine()

        // Paso 1 — preparar PC
        sb.appendLine("PASO 1 · Preparar el ordenador")
        sb.appendLine("• Instala las herramientas de plataforma de Android (adb + fastboot).")
        sb.appendLine("• Activa en el móvil: Ajustes ▸ Acerca del teléfono ▸ pulsa 7 veces en «Número de compilación».")
        sb.appendLine("• En Opciones de desarrollador activa «Depuración USB» y «Desbloqueo de OEM».")
        sb.appendLine()

        // Paso 2 — desbloqueo específico por marca
        sb.appendLine("PASO 2 · Desbloquear el bootloader")
        if (info.bootloaderUnlocked == true) {
            sb.appendLine("• Tu bootloader YA aparece como desbloqueado. Puedes saltar al PASO 3.")
        } else {
            brandUnlockSteps(info).forEach { sb.appendLine("• $it") }
        }
        sb.appendLine()

        // Paso 3 — Magisk
        sb.appendLine("PASO 3 · Parchear boot.img con Magisk")
        sb.appendLine("• Instala la app oficial Magisk (github.com/topjohnwu/Magisk).")
        sb.appendLine("• Consigue el firmware EXACTO de tu build (${info.buildId}) y extrae su boot.img")
        sb.appendLine("  (o payload.bin ▸ boot.img según el formato del fabricante).")
        sb.appendLine("• En Magisk ▸ Instalar ▸ «Seleccionar y parchear un archivo» ▸ elige boot.img.")
        sb.appendLine("• Copia al PC el resultado: magisk_patched-xxxxx.img")
        sb.appendLine()

        // Paso 4 — flasheo
        sb.appendLine("PASO 4 · Flashear la imagen parcheada")
        if (info.manufacturer.lowercase().containsAny("samsung")) {
            sb.appendLine("• En Samsung: empaqueta el boot parcheado y flashéalo con Odin (modo Download).")
            sb.appendLine("• Tras el primer arranque, la protección VBMeta puede requerir un wipe adicional.")
        } else {
            sb.appendLine("• Arranca en fastboot: adb reboot bootloader")
            sb.appendLine("• Flashea: fastboot flash boot magisk_patched-xxxxx.img")
            sb.appendLine("• Reinicia: fastboot reboot")
        }
        sb.appendLine()

        // Paso 5 — verificación
        sb.appendLine("PASO 5 · Comprobar")
        sb.appendLine("• Abre Magisk: debe indicar «Instalado» con una versión.")
        sb.appendLine("• Vuelve aquí y pulsa «Analizar sistema»: Root debería salir como SÍ.")
        sb.appendLine()

        sb.appendLine("CONSEJO: para ocultar el root a apps de banca usa el modo DenyList / Zygisk de Magisk.")
        sb.appendLine()
        sb.appendLine("⚠ Descarga las herramientas SOLO de las fuentes oficiales del fabricante y de Magisk.")
        return sb.toString().trim()
    }

    private fun brandUnlockSteps(info: DeviceInfo.Report): List<String> {
        val vendor = "${info.manufacturer} ${info.brand} ${info.device}".lowercase()
        return when {
            vendor.containsAny("xiaomi", "redmi", "poco") -> listOf(
                "Vincula tu cuenta Mi en Ajustes ▸ Opciones desarrollador ▸ «Estado de desbloqueo Mi».",
                "Descarga «Mi Unlock Tool» oficial de Xiaomi en el PC.",
                "Xiaomi impone una espera (de 7 días a varias semanas) antes de permitir el desbloqueo.",
                "Con el móvil en fastboot, ejecuta Mi Unlock y confirma. Esto borra el dispositivo."
            )
            vendor.containsAny("google", "pixel") -> listOf(
                "Con el móvil en fastboot: fastboot flashing unlock",
                "Confirma con los botones de volumen en la pantalla del dispositivo.",
                "En Pixel el desbloqueo es oficial y directo (requiere «Desbloqueo de OEM» activado)."
            )
            vendor.containsAny("oneplus", "oppo", "realme") -> listOf(
                "Con «Desbloqueo de OEM» activado, entra en fastboot.",
                "Ejecuta: fastboot flashing unlock  (en algunos modelos: fastboot oem unlock).",
                "OPPO/Realme a veces exigen una app oficial «Deep Testing» para autorizar el desbloqueo."
            )
            vendor.containsAny("motorola", "moto", "lenovo") -> listOf(
                "Consigue el código de desbloqueo en la web oficial de Motorola (Unlock Bootloader).",
                "Obtén tu identificador: fastboot oem get_unlock_data",
                "Pega los datos en la web, recibirás el código y ejecuta: fastboot oem unlock CLAVE"
            )
            vendor.containsAny("samsung") -> listOf(
                "Samsung NO usa fastboot. Activa «Desbloqueo de OEM» en Opciones de desarrollador.",
                "Entra en modo Download (según modelo, Vol- + Bixby/encendido) y mantén pulsado Vol+ para desbloquear.",
                "AVISO: muchos Galaxy vendidos en EE. UU. (Snapdragon) tienen el bootloader imposible de desbloquear.",
                "El desbloqueo activa el flag KNOX de forma permanente e irreversible."
            )
            vendor.containsAny("huawei", "honor") -> listOf(
                "Huawei y Honor dejaron de entregar códigos de desbloqueo oficiales.",
                "En la práctica, la mayoría de modelos recientes NO se pueden rootear de forma fiable.",
                "Verifica en comunidades especializadas si existe algún método para tu modelo concreto."
            )
            vendor.containsAny("asus") -> listOf(
                "ASUS ofrece una app/utilidad oficial de desbloqueo para muchos modelos (p. ej. ROG/Zenfone).",
                "Descárgala de la página de soporte de tu modelo y sigue sus instrucciones (borra el móvil)."
            )
            vendor.containsAny("sony", "xperia") -> listOf(
                "Comprueba en la web oficial de Sony si tu modelo permite desbloqueo (algunos no).",
                "Obtén el código con tu IMEI en la web de Sony y ejecuta: fastboot oem unlock 0xCLAVE"
            )
            vendor.containsAny("nothing") -> listOf(
                "Con «Desbloqueo de OEM» activado, entra en fastboot.",
                "Ejecuta: fastboot flashing unlock  y confirma en pantalla."
            )
            else -> listOf(
                "Activa «Desbloqueo de OEM» y «Depuración USB» en Opciones de desarrollador.",
                "Entra en fastboot: adb reboot bootloader",
                "Prueba: fastboot flashing unlock   (algunos usan: fastboot oem unlock).",
                "Si el fabricante exige un código, búscalo en su web oficial de soporte.",
                "Si ninguno funciona, tu modelo puede no permitir el desbloqueo del bootloader."
            )
        }
    }

    private fun String.containsAny(vararg needles: String): Boolean =
        needles.any { this.contains(it) }
}
