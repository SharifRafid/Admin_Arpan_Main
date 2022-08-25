package admin.arpan.delivery.ui.interfaces

import admin.arpan.delivery.db.model.OrderItemMain
import android.os.Bundle

interface HomeMainNewInterface {
    fun navigateToFragment(index : Int)
    fun openSelectedOrderItemAsDialog(position: Int, mainItemPositions: Int, docId: String, userId: String, orderItemMain: OrderItemMain)
    fun callOnBackPressed()
    fun openFeedBackDialog()
    fun navigateToFragment(index: Int, bundle: Bundle)
    fun logOutUser()
}