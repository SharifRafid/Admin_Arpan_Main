package admin.arpan.delivery.ui.home

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.TopMenuRecyclerAdapter
import admin.arpan.delivery.ui.auth.MainActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_home.*
import java.util.HashMap

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val titlesTop = arrayListOf("শপ ম্যানেজমেন্ট", "অফার ম্যানেজমেন্ট", "অর্ডার ম্যানেজমেন্ট", "ডিএ ম্যানেজমেন্ট",
            "ক্লায়েন্ট ম্যানেজমেন্ট", "সেটিংস")
        val imagesTop = arrayListOf(
            R.drawable.ic_arpan_shop_icon,
            R.drawable.ic_offer_icon,
            R.drawable.ic_order_manage,
            R.drawable.ic_da_manage,
            R.drawable.ic_client_icon,
            R.drawable.ic_round_settings_24
        )

        val lm = GridLayoutManager(this, 3)
        lm.orientation = GridLayoutManager.VERTICAL
        mainRecyclerView.layoutManager = lm
        mainRecyclerView.isNestedScrollingEnabled = false
        mainRecyclerView.adapter =
            TopMenuRecyclerAdapter(
                this,
                imagesTop,
                titlesTop
            )

        title_text_view.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        initFirebaseMessaging()
    }
    private fun initFirebaseMessaging() {
        if(FirebaseAuth.getInstance().currentUser!=null){
            FirebaseMessaging.getInstance().isAutoInitEnabled = true
            val token = getSharedPreferences("FCM_TOKEN", MODE_PRIVATE)
                .getString("TOKEN", "")
            FirebaseMessaging.getInstance().token.addOnCompleteListener(
                OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    val t = task.result!!
                    Log.e("TOKEN", t)
                    if (token != t) {
                        val tokenArray: MutableMap<String, Any> = HashMap()
                        tokenArray["registrationTokens"] = FieldValue.arrayUnion(t)
                        val map = HashMap<String, String>()
                        map["registration_token"] = t
                        getSharedPreferences("FCM_TOKEN", MODE_PRIVATE)
                            .edit().putString("TOKEN", t).apply()
                        FirebaseFirestore.getInstance()
                            .collection("admin_app_notification_data_tokens")
                            .document("admin_app_notification_data_tokens")
                            .update(tokenArray)
                    }
                })
        }
    }

}