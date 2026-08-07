package dev.robin.privacify.core.autoguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import dev.robin.privacify.R
import dev.robin.privacify.core.security.PrivacyControllersProvider
import dev.robin.privacify.core.settings.UserPreferencesManager
import dev.robin.privacify.data.sensorlog.SensorEvent
import dev.robin.privacify.data.sensorlog.SensorLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Real Auto-Guard engine (open-source implementation).
 *
 * Blocks mic/camera/location automatically while apps could use them, and
 * pauses the block when the sensor is legitimately in use (a call for the mic,
 * a camera app for the camera, a navigation app for the location).
 * Writes sensor-log events and optionally shows toasts on every transition.
 */
class AutoGuardService : Service() {

    companion object {
        private const val TAG = "AutoGuard"
        private const val NOTIF_ID = 1001
        private const val CHANNEL_ID = "auto_guard"
        private const val POLL_INTERVAL_MS = 4000L

        private val NAV_APPS = setOf(
            "com.google.android.apps.maps",
            "com.waze",
            "com.yandex.navigator",
            "com.sygic.aura",
            "com.tomtom.gplay.navapp",
            "com.here.app.maps",
            "com.mapswithme.maps",
            "org.osmand"
        )

        fun start(context: Context) {
            val i = Intent(context, AutoGuardService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AutoGuardService::class.java))
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var prefs: UserPreferencesManager
    private lateinit var sensorLog: SensorLogRepository
    private lateinit var cameraManager: CameraManager
    private lateinit var audioManager: AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private var cameraInUse = false
    private var callActive = false
    private var lastMic: Boolean? = null
    private var lastCamera: Boolean? = null
    private var lastLocation: Boolean? = null

    private val cameraCallback = object : CameraManager.AvailabilityCallback() {
        override fun onCameraUnavailable(cameraId: String) { cameraInUse = true }
        override fun onCameraAvailable(cameraId: String) { cameraInUse = false }
    }

    private val audioModeCallback = object : AudioManager.AudioModeCallback() {
        override fun onAudioModeChanged(mode: Int) { callActive = isCallMode(mode) }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = UserPreferencesManager.getInstance(this)
        sensorLog = SensorLogRepository(this)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        callActive = isCallMode(audioManager.mode)
        startForeground(NOTIF_ID, buildNotification())
        registerCallbacks()
        scope.launch { monitorLoop() }
        Log.d(TAG, "Auto-Guard service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        scope.cancel()
        try { cameraManager.unregisterCallback(cameraCallback) } catch (_: Exception) {}
        try { audioManager.unregisterAudioModeCallback(audioModeCallback) } catch (_: Exception) {}
        cameraExecutor.shutdown()
        Log.d(TAG, "Auto-Guard service stopped")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isCallMode(mode: Int): Boolean =
        mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION

    private fun registerCallbacks() {
        try {
            cameraManager.registerAvailabilityCallback(cameraExecutor, cameraCallback)
        } catch (e: Exception) { Log.w(TAG, "camera callback error", e) }
        try {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                audioManager.registerAudioModeCallback(audioModeCallback, mainHandler)
            }
        } catch (e: Exception) { Log.w(TAG, "audio callback error", e) }
    }

    private suspend fun monitorLoop() {
        while (true) {
            try {
                if (!prefs.automationEnabled.value) {
                    Log.d(TAG, "Auto-Guard disabled, stopping service")
                    stopSelf()
                    return
                }
                tick()
            } catch (e: Exception) {
                Log.w(TAG, "tick error", e)
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    private suspend fun tick() {
        val controller = PrivacyControllersProvider.rootPrivacyController
        val shared = getSharedPreferences("privacify_prefs", Context.MODE_PRIVATE)
        val micOn = shared.getBoolean("auto_guard_mic_enabled", true)
        val cameraOn = shared.getBoolean("auto_guard_camera_enabled", true)
        val locationOn = shared.getBoolean("auto_guard_location_enabled", false)
        val toasts = prefs.autoGuardToastEnabled.value
        val foreground = getForegroundApp()

        val micBlock = micOn && !callActive
        val cameraBlock = cameraOn && !(cameraInUse && isCameraApp(foreground))
        val locationBlock = locationOn && !isNavApp(foreground)

        if (micBlock != lastMic) {
            lastMic = micBlock
            controller.setMicDisabled(micBlock)
            logAndToast(SensorEvent.TYPE_MIC, micBlock, foreground, toasts)
        }
        if (cameraBlock != lastCamera) {
            lastCamera = cameraBlock
            controller.setCameraDisabled(cameraBlock)
            logAndToast(SensorEvent.TYPE_CAMERA, cameraBlock, foreground, toasts)
        }
        if (locationBlock != lastLocation) {
            lastLocation = locationBlock
            controller.setLocationDisabled(locationBlock)
            logAndToast(SensorEvent.TYPE_LOCATION, locationBlock, foreground, toasts)
        }
    }

    private fun logAndToast(type: String, blocked: Boolean, app: String?, toasts: Boolean) {
        sensorLog.addEvent(
            SensorEvent(
                type = type,
                action = if (blocked) SensorEvent.ACTION_STARTED else SensorEvent.ACTION_STOPPED,
                timestamp = System.currentTimeMillis(),
                appPackage = app
            )
        )
        if (toasts) {
            val sensorName = when (type) {
                SensorEvent.TYPE_MIC -> getString(R.string.sensor_mic)
                SensorEvent.TYPE_CAMERA -> getString(R.string.sensor_camera)
                else -> getString(R.string.sensor_location)
            }
            val msg = if (blocked) getString(R.string.autoguard_toast_blocked, sensorName)
            else getString(R.string.autoguard_toast_unblocked, sensorName)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getForegroundApp(): String? {
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val start = end - 15000L
            val events = usm.queryEvents(start, end)
            var top: String? = null
            var lastTime = 0L
            val e = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && e.timeStamp > lastTime) {
                    lastTime = e.timeStamp
                    top = e.packageName
                }
            }
            top
        } catch (_: Exception) { null }
    }

    private fun isCameraApp(pkg: String?): Boolean {
        if (pkg == null) return false
        return try {
            val info = packageManager.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
            info.requestedPermissions?.contains(android.Manifest.permission.CAMERA) == true
        } catch (_: Exception) { false }
    }

    private fun isNavApp(pkg: String?): Boolean = pkg != null && NAV_APPS.contains(pkg)

    private fun buildNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.feature_auto_guard),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
        val builder = if (android.os.Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle(getString(R.string.autoguard_service_notification))
            .setContentText(getString(R.string.autoguard_service_notification_desc))
            .setSmallIcon(R.drawable.ic_lockdown)
            .setOngoing(true)
            .build()
    }
}
