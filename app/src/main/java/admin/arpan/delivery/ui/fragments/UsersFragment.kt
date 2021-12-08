package admin.arpan.delivery.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.model.UserItem
import admin.arpan.delivery.ui.home.HomeViewModelMainData
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.android.synthetic.main.fragment_users.view.*
import java.lang.ClassCastException
import java.util.*
import kotlin.collections.ArrayList


class UsersFragment : Fragment() {

    private lateinit var contextMain : Context
    private lateinit var homeMainNewInterface: HomeMainNewInterface
    private val TAG = "UsersFragment"
    private lateinit var homeViewModelMainData: HomeViewModelMainData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homeViewModelMainData = activity?.let { ViewModelProvider(it).get(HomeViewModelMainData::class.java) }!!
//        if(!homeViewModelMainData.getUsersMainArrayListData().value.isNullOrEmpty()){
//            homeMainNewInterface.loadUsersMainArrayListData()
//        }
//
//        homeViewModelMainData.getUsersMainArrayListData().observe(requireActivity(), Observer {
//            if(it.isNotEmpty()){
//                val arrayListUsers = ArrayList<UserItem>()
//                for(user in it){
//                    arrayListUsers.add(user.copy())
//                }
//                Log.e(TAG, arrayListUsers.size.toString())
//                for(userItem in arrayListUsers){
//                    for(order in homeViewModelMainData.getLastMonthOrdersMainData().value!!){
//                        if(order.userId == userItem.key){
//                            Log.e(TAG, order.userId)
//                            userItem.ordersCountLastMonth += 1
//                        }
//                    }
//                }
//                Collections.sort(arrayListUsers, kotlin.Comparator { o1, o2 ->
//                    o1.ordersCountLastMonth.compareTo(o2.ordersCountLastMonth)
//                })
//                val arrayStringUsers = ArrayList<String>()
//                for(userItem in arrayListUsers){
//                    arrayStringUsers.add("Name : "+userItem.name+"  Address : "+userItem.address+"  Phone : "+userItem.phone+"  Orders : "+userItem.ordersCountLastMonth)
//                }
//                view.recyclerViewUsers.adapter = ArrayAdapter(contextMain, R.layout.support_simple_spinner_dropdown_item,arrayStringUsers)
//            }
//        })


        FirebaseFirestore.getInstance().collection("users")
            .get(Source.CACHE).addOnCompleteListener {
                if(it.isSuccessful){
                    val arrayListUsers = ArrayList<UserItem>()
                    for(user in it.result!!.documents){
                        var userI = user.toObject(UserItem::class.java)!!
                        userI.key = user.id
                        arrayListUsers.add(userI)
                    }
                    Log.e(TAG, arrayListUsers.size.toString())
                    val arrayStringUsers = ArrayList<String>()
                    val arrayOrdersOldMOnth = homeViewModelMainData.getLastMonthOrdersMainData().value!!
                    Log.e(TAG, arrayOrdersOldMOnth.size.toString())
                    for(userItem in arrayListUsers){
                        for(order in arrayOrdersOldMOnth){
                            Log.e(TAG, order.userId)
                            Log.e(TAG, userItem.key)
                            if(order.userId == userItem.key){
                                Log.e(TAG, order.userId)
                                userItem.ordersCountLastMonth += 1
                            }
                        }
                    }
                    Collections.sort(arrayListUsers, kotlin.Comparator { o1, o2 ->
                        o2.ordersCountLastMonth.compareTo(o1.ordersCountLastMonth)
                    })
                    for(userItem in arrayListUsers){
                        arrayStringUsers.add("Name : "+userItem.name+"\nAddress : "+userItem.address+"\nPhone : "+userItem.phone+"\nOrders : "+userItem.ordersCountLastMonth)
                    }
                    view.recyclerViewUsers.adapter = ArrayAdapter(contextMain, R.layout.list_view_layout, R.id.textView, arrayStringUsers)
                } else{
                    it.exception?.printStackTrace()
                }
            }
    }

}