package admin.arpan.delivery.ui.da

import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.DaItemRecyclerAdapter
import admin.arpan.delivery.db.model.DaAgent
import admin.arpan.delivery.utils.createProgressDialog
import admin.arpan.delivery.utils.showToast
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_da.*

class DaActivity : AppCompatActivity() {

    lateinit var progressDialog : Dialog
    lateinit var daItemRecyclerAdapter: DaItemRecyclerAdapter
    var daList: ArrayList<DaAgent> = ArrayList()
    var selectedDaAgent = DaAgent()
    var selectedAgentPostition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_da)
        initVars()
        initLogic()
    }

    private fun initLogic() {
        progressDialog.show()
        FirebaseFirestore.getInstance()
                .collection("da_agents_main_list_collection")
                .get().addOnCompleteListener {
                    if(it.isSuccessful){
                        if(it.result!!.documents.size==0){
                            showToast("No Da Found", FancyToast.ERROR)
                            progressDialog.dismiss()
                        }else{
                            daList.clear()
                            for(document in it.result!!.documents){
                                val da = document.toObject(DaAgent::class.java)
                                da!!.key = document.id
                                daList.add(da)
                            }
                            mainRecyclerView.layoutManager = LinearLayoutManager(this)
                            daItemRecyclerAdapter = DaItemRecyclerAdapter(this, daList)
                            mainRecyclerView.adapter = daItemRecyclerAdapter
                            progressDialog.dismiss()
                        }
                    }
                }

    }

    private fun initVars() {
        progressDialog = createProgressDialog()
    }

    fun addDaClick(view: View) {
        AddDaFragment().show(supportFragmentManager, "")
    }
}