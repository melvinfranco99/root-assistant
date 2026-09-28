package com.melvin.rootassistant

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.melvin.rootassistant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAnalyze.setOnClickListener { analyze() }
        binding.btnRoot.setOnClickListener { confirmRoot() }
    }

    // ---------------------------------------------------------------------
    // ANALIZAR SISTEMA
    // ---------------------------------------------------------------------

    private fun analyze() {
        val info = DeviceInfo.collect(this)
        val sb = StringBuilder()

        sb.appendLine("=== DISPOSITIVO ===")
        sb.appendLine("Fabricante : ${info.manufacturer}")
        sb.appendLine("Marca      : ${info.brand}")
        sb.appendLine("Modelo     : ${info.model}")
        sb.appendLine("Nombre HW  : ${info.device}")
        sb.appendLine("Chipset    : ${info.soc}")
        sb.appendLine("Arquitectura: ${info.abi}")
        sb.appendLine()
        sb.appendLine("=== ANDROID ===")
        sb.appendLine("Versión    : Android ${info.androidRelease} (API ${info.sdkInt})")
        sb.appendLine("Parche seg.: ${info.securityPatch}")
        sb.appendLine("Build      : ${info.buildId}")
        sb.appendLine("Tags       : ${info.tags}")
        sb.appendLine()
        sb.appendLine("=== ESTADO ===")
        sb.appendLine("Bootloader : ${info.bootloaderStatus}")
        sb.appendLine("Root       : ${info.rootStatus}")
        sb.appendLine()
        sb.appendLine("=== MÉTODO RECOMENDADO ===")
        sb.appendLine(info.recommendedMethod)

        binding.txtOutput.text = sb.toString().trim()
    }

    // ---------------------------------------------------------------------
    // ROOTEAR (con confirmación)
    // ---------------------------------------------------------------------

    private fun confirmRoot() {
        val info = DeviceInfo.collect(this)

        if (info.isRooted) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Tu dispositivo ya está rooteado")
                .setMessage(
                    "Se ha detectado acceso root en este dispositivo " +
                        "(${info.model}). No hace falta volver a rootear."
                )
                .setPositiveButton("Entendido", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("¿Estás seguro de que quieres rootear?")
            .setMessage(
                "Vas a preparar el rooteo de tu ${info.manufacturer} ${info.model}.\n\n" +
                    "Ten en cuenta que rootear:\n" +
                    "• Puede ANULAR la garantía del fabricante.\n" +
                    "• Suele BORRAR todos los datos al desbloquear el bootloader.\n" +
                    "• Puede impedir usar apps de banca, pagos o DRM.\n" +
                    "• Puede bloquear las actualizaciones OTA.\n\n" +
                    "Esta app no modifica el sistema por ti: te mostrará el " +
                    "procedimiento exacto y seguro para TU modelo. Los pasos que " +
                    "requieren PC y bootloader los haces tú."
            )
            .setPositiveButton("Sí, ver procedimiento") { _, _ -> showRootPlan(info) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRootPlan(info: DeviceInfo.Report) {
        val steps = RootGuide.buildSteps(info)
        binding.txtOutput.text = steps

        AlertDialog.Builder(this)
            .setTitle("Procedimiento para ${info.model}")
            .setMessage(
                "He escrito los pasos detallados en la pantalla principal.\n\n" +
                    "Recuerda: haz copia de seguridad antes de empezar y sigue el " +
                    "orden indicado. El desbloqueo del bootloader borrará el móvil."
            )
            .setPositiveButton("Leer los pasos", null)
            .show()
    }
}
