package admin.arpan.delivery.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import admin.arpan.delivery.R
import admin.arpan.delivery.db.adapter.ShopItemRecyclerAdapter
import admin.arpan.delivery.ui.interfaces.HomeMainNewInterface
import admin.arpan.delivery.ui.shops.AddShop
import admin.arpan.delivery.ui.shops.ShopCategoryActivity
import admin.arpan.delivery.utils.LiveDataUtil
import admin.arpan.delivery.utils.showToast
import admin.arpan.delivery.viewModels.HomeViewModel
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_shops.view.*
import java.lang.ClassCastException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShopsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class ShopsFragment : Fragment() {
  // TODO: Rename and change types of parameters
  private var param1: String? = null
  private var param2: String? = null
  private lateinit var homeMainNewInterface: HomeMainNewInterface
  private val TAG = "ShopsFragment"
  private lateinit var contextMain: Context
  private val viewModel: HomeViewModel by viewModels()

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
    } catch (classCastException: ClassCastException) {
      Log.e(TAG, "This activity does not implement the interface / listener")
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_shops, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    view.addShopsButton.setOnClickListener {
      val intent = Intent(contextMain, AddShop::class.java)
      intent.putExtra("array_size", "0")
      startActivity(intent)
    }
    view.addCategoriessButton.setOnClickListener {
      val intent = Intent(contextMain, ShopCategoryActivity::class.java)
      startActivity(intent)
    }
    loadDataFirestore(view)
  }

  private fun loadDataFirestore(view: View) {
    LiveDataUtil.observeOnce(viewModel.getShops()) {
      if (it.error == true) {
        contextMain.showToast(it.message.toString(), FancyToast.ERROR)
      } else {
        val arrayList = it.results
        val array_size = arrayList.size
        view.mainRecyclerView.layoutManager = LinearLayoutManager(contextMain)
        val adapterShops = ShopItemRecyclerAdapter(contextMain, arrayList, "")
        adapterShops.setHasStableIds(true)
        view.mainRecyclerView.adapter = adapterShops
        view.addShopsButton.setOnClickListener {
          val intent = Intent(contextMain, AddShop::class.java)
          intent.putExtra("array_size", array_size.toString())
          startActivity(intent)
        }
      }
    }
  }

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShopsFragment.
     */
    // TODO: Rename and change types and number of parameters
    @JvmStatic
    fun newInstance(param1: String, param2: String) =
      ShopsFragment().apply {
        arguments = Bundle().apply {
          putString(ARG_PARAM1, param1)
          putString(ARG_PARAM2, param2)
        }
      }
  }
}