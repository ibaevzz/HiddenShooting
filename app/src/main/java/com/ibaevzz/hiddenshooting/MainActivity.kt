package com.ibaevzz.hiddenshooting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.ibaevzz.hiddenshooting.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(RecordService.isRunning) {
            binding.start.text = "Stop shooting"
        }

        binding.start.setOnClickListener{
            if(!RecordService.isRunning) {
                if (binding.front.isChecked) {
                    startService(1)
                } else {
                    startService(0)
                }
                binding.start.text = "Stop shooting"
            }else{
                stopService()
                binding.start.text = "Start shooting"
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
            startService()
        }
    }

    private fun startService(id: Int = 0){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS), 1)
        }else{
            val intent = Intent(this, RecordService::class.java)
            intent.putExtra("camera", id)
            startForegroundService(intent)
        }
    }

    private fun stopService(){
        val intent = Intent(this, RecordService::class.java)
        stopService(intent)
    }
}