package admin.arpan.delivery.ui.auth

import admin.arpan.delivery.R
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.viewModels.AuthViewModel
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val viewModel: AuthViewModel by viewModels()
  private lateinit var progressDialog: Dialog

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    progressDialog = createProgressDialog()

    progressDialog.show()
    LiveDataUtil.observeOnce(viewModel.getRefreshResponse()) {
      runOnUiThread {
        progressDialog.dismiss()
      }
      viewModel.switchActivity(it)
    }

    buttonDone.setOnClickListener {
      if (editTextTextPassword.text.isNotEmpty() && editTextTextPersonName.text.isNotEmpty()) {
        progressDialog.show()
        LiveDataUtil.observeOnce(
          viewModel.getLoginResponse(
            editTextTextPersonName.text.toString(),
            editTextTextPassword.text.toString()
          )
        ) {
          runOnUiThread {
            progressDialog.dismiss()
          }
          viewModel.switchActivity(it)
        }
      }
    }
  }

}

