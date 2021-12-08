package admin.arpan.delivery.ui.auth

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.Moderator
import admin.arpan.delivery.ui.home.HomeActivity
import admin.arpan.delivery.ui.home.HomeActivityMain
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val progress = createProgressDialog()
        val sharedPreferences = getSharedPreferences("MODERATOR", MODE_PRIVATE)

        if(sharedPreferences.contains("email")){
            progress.show()
            FirebaseFirestore.getInstance().collection("moderators")
                .whereEqualTo("email", sharedPreferences.getString("email", ""))
                .whereEqualTo("pass", sharedPreferences.getString("pass", ""))
                .get().addOnCompleteListener {
                    buttonDone.isEnabled = true
                    progress.dismiss()
                    if(it.isSuccessful){
                        if(it.result!!.documents.isEmpty()){
                            showToast("Wrong Email Or Pass", FancyToast.ERROR)
                        }else{
                            val moderator = it.result!!.documents[0].toObject(Moderator::class.java)
                            if(moderator!!.enabled){
                                startActivity(Intent(this, HomeActivityMain::class.java))
                            }else{
                                showToast("Account Disabled", FancyToast.ERROR)
                            }
                        }
                    }else{
                        showToast("Wrong Email Or Pass", FancyToast.ERROR)
                    }
                }
        }

        buttonDone.setOnClickListener {
            val email = editTextTextPersonName.text.toString()
            val pass = editTextTextPassword.text.toString()
            if(email.isEmpty() || pass.isEmpty()){
                showToast("Empty Email Or Pass", FancyToast.ERROR)
            }else{
                buttonDone.isEnabled = false
                FirebaseFirestore.getInstance().collection("moderators")
                    .whereEqualTo("email", email)
                    .whereEqualTo("pass", pass)
                    .get().addOnCompleteListener {
                        buttonDone.isEnabled = true
                        if(it.isSuccessful){
                            if(it.result!!.documents.isEmpty()){
                                showToast("Wrong Email Or Pass", FancyToast.ERROR)
                            }else{
                                getSharedPreferences("MODERATOR", MODE_PRIVATE)
                                    .edit()
                                    .putString("email",email)
                                    .putString("pass", pass)
                                    .putString("userId", it.result!!.documents[0].id)
                                    .apply()
                                val moderator = it.result!!.documents[0].toObject(Moderator::class.java)
                                if(moderator!!.enabled){
                                    startActivity(Intent(this, HomeActivityMain::class.java))
                                }
                            }
                        }else{
                            showToast("Wrong Email Or Pass", FancyToast.ERROR)
                        }
                    }
            }
        }
    }
}