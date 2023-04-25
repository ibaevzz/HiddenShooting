package com.ibaevzz.hiddenshooting

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.io.File

class RecordService : Service() {

    private lateinit var cameraManager: CameraManager
    private lateinit var recorder: MediaRecorder
    private lateinit var camera: CameraDevice
    private val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera/${System.currentTimeMillis()}.mp4")

    private val sessionCallback = object: CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            builder.addTarget(recorder.surface)
            recorder.start()
            session.setSingleRepeatingRequest(builder.build(),
                mainExecutor,
                object : CameraCaptureSession.CaptureCallback() {})
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Toast.makeText(this@RecordService, "ConfigFail", Toast.LENGTH_SHORT).show()
        }
    }
    private val callback = object: StateCallback(){
        override fun onOpened(camera: CameraDevice) {
            this@RecordService.camera = camera
            camera.createCaptureSession(
                SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    listOf(OutputConfiguration(recorder.surface)),
                    mainExecutor,
                    sessionCallback)
            )
        }

        override fun onDisconnected(camera: CameraDevice) {

        }

        override fun onError(camera: CameraDevice, error: Int) {
            Toast.makeText(this@RecordService, "Ошибка запуска", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        recorder = MediaRecorder(this)
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.stop()
        recorder.release()
        isRunning = false
        MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), null){_, _ ->

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        startForeground(101, createNotification())
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) {
            val i = intent?.getIntExtra("camera", 0)
            cameraManager.openCamera(cameraManager.cameraIdList[i?:0], mainExecutor, callback)
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                if(i==0){
                    setOrientationHint(90)
                }else if(i==1){
                    setOrientationHint(270)
                }
                setVideoEncodingBitRate(10*1024*1024)
                setVideoSize(1920, 1080)
                setOutputFile(file.absolutePath)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                prepare()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun createNotification(): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        return NotificationCompat.Builder(this, createNotificationChannel("shoot", "progress"))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Shooting")
            .setContentText("Shooting in progress")
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object{
        var isRunning = false
    }
}