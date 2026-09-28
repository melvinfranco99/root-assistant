# Root Assistant

App Android que **analiza tu dispositivo** y te genera el **procedimiento de rooteo exacto para tu modelo**. En lugar de buscar los pasos cada vez que cambias de móvil, la app detecta el hardware y te dice qué hacer.

> ⚠️ **Nota honesta:** no existe (ni es técnicamente posible) un botón que rootee *automáticamente* cualquier móvil. El rooteo moderno requiere **desbloquear el bootloader** (con PC, borra los datos) y **parchear el `boot.img`** del firmware con Magisk. Esta app te **guía** con los pasos concretos de tu marca; **no ejecuta exploits ni modifica el sistema** por ti. Úsala solo en dispositivos de tu propiedad.

## Funciones

- **Analizar sistema** — fabricante, marca, modelo, chipset, arquitectura, versión de Android, parche de seguridad, estado del bootloader (bloqueado/desbloqueado) y detección de root (binario `su`, Magisk, KernelSU).
- **Rootear** — muestra el diálogo *«¿Estás seguro de que quieres rootear?»* con las advertencias y, al confirmar, escribe un plan paso a paso adaptado al fabricante (Xiaomi/Redmi/POCO, Google Pixel, Samsung, OnePlus/OPPO/Realme, Motorola, ASUS, Sony, Nothing, Huawei/Honor y genérico).

## Descargar

La web de descarga (GitHub Pages) sirve el APK más reciente:
**https://melvinfranco99.github.io/root-assistant/**

El APK se compila **automáticamente con GitHub Actions** en cada push a `main`.

## Compilar en local

Necesitas el SDK de Android y JDK 17.

```bash
gradle assembleDebug
# salida: app/build/outputs/apk/debug/app-debug.apk
```

## Estructura

- `app/` — código de la app (Kotlin, Android Views).
  - `MainActivity.kt` — UI y diálogos de confirmación.
  - `DeviceInfo.kt` — recogida de datos y detección de root/bootloader.
  - `RootGuide.kt` — generación de la guía por fabricante.
- `.github/workflows/build.yml` — compila el APK y publica la web en GitHub Pages.
- `docs/` — web de descarga (GitHub Pages).

## Tecnología

Android nativo · Kotlin · minSdk 26 (Android 8.0) · targetSdk 34.
Solo lee propiedades públicas del sistema; no requiere permisos especiales.
