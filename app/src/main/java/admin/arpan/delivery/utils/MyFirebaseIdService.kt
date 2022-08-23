package admin.arpan.delivery.utils

import admin.arpan.delivery.R
import admin.arpan.delivery.ui.auth.MainActivity2
import admin.arpan.delivery.ui.home.HomeActivity
import admin.arpan.delivery.ui.home.HomeActivityMain
import admin.arpan.delivery.ui.order.OrdresActivity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseIdService : FirebaseMessagingService() {

    var db = FirebaseFirestore.getInstance()
    var registrationTokens: List<String>? = null

    override fun onNewToken(s: String) {
        super.onNewToken(s)

        //FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        val prefs = applicationContext.getSharedPreferences("USER_PREF",
                MODE_PRIVATE)

        val uid = getSharedPreferences("user_details", MODE_PRIVATE).getString("key", "").toString()

        getRegistrationTokens(uid)

        if (!uid.equals("null", ignoreCase = true)) {
            if (registrationTokens != null && !registrationTokens!!.contains(s)) {
                updateToken(s, uid)
            }
        }
    }

    private fun updateToken(token: String, uid: String) {
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        val tokenArray: MutableMap<String, Any> = HashMap()
        tokenArray["registrationTokens"] = FieldValue.arrayUnion(token)
        addRegistrationToken(tokenArray, uid)
    }

    private fun addRegistrationToken(token: Map<String, Any>, uid: String) {
        db.collection("admin_app_notification_data_tokens").document(uid).update(token)
    }

    private fun getRegistrationTokens(uid: String) {
        db.collection("admin_app_notification_data_tokens")
                .document("admin_app_notification_data_tokens")
                .get()
                .addOnCompleteListener(OnCompleteListener<DocumentSnapshot?> { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document!!.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.data)
                            registrationTokens = document["registrationTokens"] as List<String>?
                        } else {
                            Log.d(TAG, "No such document")
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.exception)
                    }
                })
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("msg", "onMessageReceived: " + remoteMessage.data["message"])
        val intent = Intent(this, MainActivity2::class.java)
//        val intent = Intent(this, HomeActivityMain::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        for(entry in remoteMessage.data.entries){
            intent.putExtra(entry.key, entry.value.toString())
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }
        val channelId = "Default"
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_arpan_icon_notification)
            .setContentTitle(remoteMessage.notification!!.title)
            .setContentText(remoteMessage.notification!!.body).setAutoCancel(true)
            .setContentIntent(pendingIntent)
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N){
            builder.priority = NotificationCompat.PRIORITY_HIGH
            builder.setDefaults(Notification.DEFAULT_ALL)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.priority = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                channelId,
                "Default channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
        manager.notify(0, builder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseIdService"
    }
}