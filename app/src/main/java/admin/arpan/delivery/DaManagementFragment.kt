package admin.arpan.delivery

import admin.arpan.delivery.db.adapter.DaItemRecyclerAdapter
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_da_management.view.*
import java.lang.ClassCastException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DaManagementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DaManagementFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var contextMain : Context
    private lateinit var homeMainNewInterface: HomeMainNewInterface
    private val TAG = "DaManagementFragment"
    private lateinit var homeViewModelMainData: HomeViewModelMainData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextMain = context
        try {
            homeMainNewInterface = context as HomeMainNewInterface
        }catch (classCastException : ClassCastException){
            Log.e(TAG, "This activity does not implement the interface / listener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_da_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.titleTextView.setOnClickListener {
            homeMainNewInterface.callOnBackPressed()
        }
        homeViewModelMainData = activity?.let { ViewModelProvider(it).get(HomeViewModelMainData::class.java) }!!
        view.mainRecyclerView.layoutManager = LinearLayoutManager(contextMain)
        if(homeViewModelMainData.getDaMainListData().value.isNullOrEmpty()){
            homeMainNewInterface.loadDaMainListData()
        }
        homeViewModelMainData.getDaMainListData().observe(requireActivity(), Observer {
            val daItemRecyclerAdapter = DaItemRecyclerAdapter(contextMain, it, homeViewModelMainData.todayOrdersMainArrayList.value, homeMainNewInterface)
            view.mainRecyclerView.adapter = daItemRecyclerAdapter
        })
        view.addShopsButton.setOnClickListener {
            homeMainNewInterface.navigateToFragment(R.id.addDaFragment)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DaManagementFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DaManagementFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}