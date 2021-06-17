package admin.arpan.delivery.utils

import admin.arpan.delivery.R
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shashank.sony.fancytoastlib.FancyToast


fun Context.showToast(message: String, status: Int){
    FancyToast.makeText(this, message, FancyToast.LENGTH_SHORT, status, false).show()
}

fun Context.createProgressDialog() : Dialog {
    val dialog = AlertDialog.Builder(this)
        .setView(LayoutInflater.from(this).inflate(R.layout.dialog_progress_layout_main, null))
        .create()
    dialog.setCancelable(false)
    dialog.setCanceledOnTouchOutside(false)
    dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    return dialog
}

fun callPermissionCheck(context: Context, activity: Activity): Boolean {
    return if(ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CALL_PHONE
            )
        ) {
            val mDialog = AlertDialog.Builder(activity)
                .setTitle(context.getString(R.string.give_call_permission))
                .setMessage(context.getString(R.string.call_permission_for_first_time))
                .setCancelable(false)
                .setPositiveButton(
                    context.getString(R.string.yes_ok)
                ) { diaInt, _ ->
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        0
                    )
                    diaInt.dismiss()
                }
                .setNegativeButton(
                    context.getString(R.string.no_its_ok)
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .create()
            mDialog.show()
        } else {
            ActivityCompat.requestPermissions(activity,arrayOf(Manifest.permission.CALL_PHONE),
                0)
        }
        false
    } else {
        true
    }
}

