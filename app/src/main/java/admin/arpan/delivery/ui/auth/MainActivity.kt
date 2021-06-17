package admin.arpan.delivery.ui.auth

import admin.arpan.delivery.R
import admin.arpan.delivery.ui.home.HomeActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser!=null){
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }else{
            buttonDone.setOnClickListener {
                if(editTextTextPassword.text.isNotEmpty() && editTextTextPersonName.text.isNotEmpty()){
                    buttonDone.isEnabled = false
                    firebaseAuth.signInWithEmailAndPassword(
                        editTextTextPersonName.text.toString(),
                        editTextTextPassword.text.toString()
                    ).addOnCompleteListener {
                        if(it.isSuccessful){
                            if(firebaseAuth.currentUser!=null){
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }else{
                                Toast.makeText(this, "Try again...", Toast.LENGTH_SHORT).show()
                                buttonDone.isEnabled = true
                            }
                        }else{
                            Toast.makeText(this, "Try again...", Toast.LENGTH_SHORT).show()
                            buttonDone.isEnabled = true
                        }
                    }
                }else{
                    Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

