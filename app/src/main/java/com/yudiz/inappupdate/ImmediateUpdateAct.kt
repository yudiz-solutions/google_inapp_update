package com.yudiz.inappupdate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability

class ImmediateUpdateAct : AppCompatActivity() {
    private var inAppUpdateManager: AppUpdateManager? = null
    private var IMMEDIATE_UPDATE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setInAppUpdateListeners()
    }

    private fun setInAppUpdateListeners() {
        inAppUpdateManager = AppUpdateManagerFactory.create(this)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoIntent = inAppUpdateManager?.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoIntent?.addOnSuccessListener { appUpdateInfo ->
            // checks if the update is available or not
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)
            ) {
                inAppUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo, IMMEDIATE,
                    this, IMMEDIATE_UPDATE_REQUEST
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                Log.e("appUpdateInfo", "OnResume")
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    inAppUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        IMMEDIATE,
                        this,
                        IMMEDIATE_UPDATE_REQUEST
                    )
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMMEDIATE_UPDATE_REQUEST) {
            if (resultCode != RESULT_OK) {
                finish()
                // update dialog is cancelled by user or fails, it should be handled as per as requirement
                // you can close the app or you can request to start the update again.
            }
        }
    }
}