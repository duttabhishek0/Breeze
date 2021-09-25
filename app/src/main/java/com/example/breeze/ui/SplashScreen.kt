package com.example.breeze.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

private const val SplashWaitTime: Long = 5000

class SplashScreen : AppCompatActivity() {
    private var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    )
    private val permissionCode = 100001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkPermission()) {
            goHome()
        } else {
            ActivityCompat.requestPermissions(
                this@SplashScreen,
                permissions,
                permissionCode
            )
        }
    }


    //    A function to check whether permission was granted or not
    //    If either of the two is denied, the function will
    //    return false, otherwise true!
    private fun checkPermission(): Boolean {
        for (perms in permissions) {
            val data = application.checkCallingOrSelfPermission(perms)
            if (data != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun goHome() {
        Handler().postDelayed({
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }, SplashWaitTime)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            permissionCode -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        goHome()
                    } else {
                        showToast("Grant both the permissions please")
                    }
                } else {
                    showToast("Grant both the permissions")
                }
            }
            else -> {
                showToast("Error Occurred!")
            }
        }
    }

    private fun showToast(errorMessage: String) {
        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
        finish()
    }
}