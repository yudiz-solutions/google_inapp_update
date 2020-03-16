package com.yudiz.inappupdate

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_main.*

class FlexibleUpdateAct : AppCompatActivity(), InstallStateUpdatedListener {
    private var inAppUpdateManager: AppUpdateManager? = null
    private var FLEXIBLE_UPDATE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpInAppUpdate()
    }

    private fun setUpInAppUpdate() {
        inAppUpdateManager = AppUpdateManagerFactory.create(this)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoIntent = inAppUpdateManager?.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoIntent?.addOnSuccessListener { appUpdateInfo ->
            // checks if the update is available or not
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                inAppUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo, AppUpdateType.FLEXIBLE,
                    this, FLEXIBLE_UPDATE_REQUEST
                )
            }
        }
        inAppUpdateManager?.registerListener(this)
    }

    override fun onStateUpdate(state: InstallState?) {
        if (state?.installStatus() == InstallStatus.DOWNLOADED) {
            // invokes when app has been downloaded and still the app is in foreground.
            updateCompletedNotificationSnack()
        }
    }

    /* Displays the snackbar notification and call to action. */
    private fun updateCompletedNotificationSnack() {
        Snackbar.make(
            main,
            getString(R.string.update_complete_message),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.install_update)) {
                inAppUpdateManager?.completeUpdate() // install downloaded build
            }
            setActionTextColor(Color.WHITE)
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                // If the update is downloaded but not installed, notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    updateCompletedNotificationSnack()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FLEXIBLE_UPDATE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                    applicationContext,
                    "The update is started in background",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //cancelled by user, this event should be handled as per as requirement
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // When status updates are no longer needed, unregister the listener.
        inAppUpdateManager?.unregisterListener(this)
    }
}