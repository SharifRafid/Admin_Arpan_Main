package admin.arpan.delivery.ui.da

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.DaItemRecyclerAdapter
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.models.User
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.networking.responses.DaItemResponse
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.DAViewModel
import admin.arpan.delivery.viewModels.UploadViewModel
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_da.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


@AndroidEntryPoint
class DaActivity : AppCompatActivity() {

  lateinit var progressDialog: Dialog
  lateinit var daItemRecyclerAdapter: DaItemRecyclerAdapter
  var daList: ArrayList<DaItemResponse> = ArrayList()
  var selectedDaAgent = User()
  var selectedAgentPostition = 0
  private val daViewModel: DAViewModel by viewModels()


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_da)
    initVars()
    initLogic()
  }

  private fun initLogic() {
    progressDialog.show()
    LiveDataUtil.observeOnce(daViewModel.getAllItems()) {
      progressDialog.dismiss()
      if (it.error == true) {
        showToast("Failed to find", FancyToast.ERROR)
      } else {
        daList.clear()
        daList.addAll(it.results)
        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        progressDialog.dismiss()
      }
    }

  }

  private fun initVars() {
    progressDialog = createProgressDialog()
  }

  fun addDaClick(view: View) {

  }
}