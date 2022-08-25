package admin.arpan.delivery.ui.home

import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.*
import admin.arpan.delivery.ui.auth.MainActivity
import admin.arpan.delivery.ui.feedback.UserFeedBackFragment
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.Preference
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.AuthViewModel
import admin.arpan.delivery.viewModels.HomeViewModel
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class HomeActivityMain : AppCompatActivity(), HomeMainNewInterface {

  private final val TAG = "HomeActivityMain"

  private lateinit var navController: NavController

  private val firebaseFirestore = FirebaseFirestore.getInstance()
  private val firebaseDatabase = FirebaseDatabase.getInstance().reference
  private val firebaseAuth = FirebaseAuth.getInstance()
  private var selectedRecyclerAdapterItem = 0
  private var mainItemPositionsRecyclerAdapter = 0

  private lateinit var homeViewModelMainData: HomeViewModelMainData
  private val authViewModel: AuthViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home_main)
    initVars()
    initLogics()
  }

  private fun initLogics() {
    initFirebaseMessaging()
    loadSavedClientsPrefData()
  }

  private fun initVars() {
    homeViewModelMainData = ViewModelProvider(this).get(HomeViewModelMainData::class.java)
    navController = Navigation.findNavController(this, R.id.main_home_fragment_container)
  }

  private fun loadSavedClientsPrefData() {
    FirebaseDatabase.getInstance().reference
      .child("SavedPrefClientTf")
      .addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val tempUserSavedPrefClientTfArrayList = ArrayList<SavedPrefClientTf>()
          for (snap in snapshot.children) {
            tempUserSavedPrefClientTfArrayList.add(snap.getValue(SavedPrefClientTf::class.java)!!)
          }
          homeViewModelMainData.setUserSavedPrefClientTfArrayList(tempUserSavedPrefClientTfArrayList)
        }

        override fun onCancelled(error: DatabaseError) {
          error.toException().printStackTrace()
        }

      })
  }

  override fun navigateToFragment(id: Int) {
    navController.navigate(id)
  }

  override fun navigateToFragment(index: Int, bundle: Bundle) {
    navController.navigate(index, bundle)
  }

  private fun initFirebaseMessaging() {
    if (FirebaseAuth.getInstance().currentUser != null) {
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

  override fun openSelectedOrderItemAsDialog(
    position: Int,
    mainItemPositions: Int,
    docId: String,
    userId: String,
    orderItemMain: OrderItemMain
  ) {
    selectedRecyclerAdapterItem = position
    mainItemPositionsRecyclerAdapter = mainItemPositions
    val bundle = Bundle()
    bundle.putString("orderID", docId)
    bundle.putString("customerId", userId)
    navController.navigate(R.id.orderHistoryFragment, bundle)
  }

  override fun callOnBackPressed() {
    onBackPressed()
  }

  override fun openFeedBackDialog() {
    UserFeedBackFragment().show(supportFragmentManager, "")
  }

  override fun logOutUser() {
    AlertDialog.Builder(this)
      .setTitle("Sure to logout ?")
      .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
        dialog.dismiss()
        LiveDataUtil.observeOnce(authViewModel.getLogoutResponse()){
          Log.e("er",it.message.toString())
          showToast("Logged Out", FancyToast.SUCCESS)
          Preference(this.application).clear()
          finish()
        }
      })
      .create().show()
  }

}