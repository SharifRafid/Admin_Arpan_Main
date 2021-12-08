package admin.arpan.delivery.ui.home

import admin.arpan.delivery.db.model.DaStatusItem
import admin.arpan.delivery.db.model.LocationItem
import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.db.model.SavedPrefClientTf
import androidx.lifecycle.ViewModel
import kotlin.math.hypot

class HomeViewModel : ViewModel() {

    private var daStatusList = ArrayList<DaStatusItem>()
    private var locationsArrayList = ArrayList<LocationItem>()
    private var ordersMainArrayList = ArrayList<OrderItemMain>()
    private var userSavedPrefClientTfArrayList = ArrayList<SavedPrefClientTf>()


    fun setUserSavedPrefClientTfArrayList(l : ArrayList<SavedPrefClientTf>){
        userSavedPrefClientTfArrayList.clear()
        userSavedPrefClientTfArrayList.addAll(l)
    }

    fun getUserSavedPrefClientTfArrayList(): ArrayList<SavedPrefClientTf> {
        return userSavedPrefClientTfArrayList
    }

    fun setOrdersMainArrayList(l : ArrayList<OrderItemMain>){
        ordersMainArrayList.clear()
        ordersMainArrayList.addAll(l)
    }

    fun getOrderItemMainList(): ArrayList<OrderItemMain> {
        return ordersMainArrayList
    }

    fun setDaStatusList(l : ArrayList<DaStatusItem>){
        daStatusList.clear()
        daStatusList.addAll(l)
    }

    fun getDaStatusList(): ArrayList<DaStatusItem> {
        return daStatusList
    }

    fun getLocationArrayList(): ArrayList<LocationItem> {
        return locationsArrayList
    }

    private var selectedOrderItem = OrderItemMain()

    fun setLocationsArrayList(locationsArrayList: ArrayList<LocationItem>) {
        this.locationsArrayList.clear()
        this.locationsArrayList.addAll(locationsArrayList)
    }

    fun getCurrentSelectedOrderItem(): OrderItemMain {
        return selectedOrderItem
    }

    fun setCurrentSelectedOrderItem(oim: OrderItemMain) {
        selectedOrderItem = oim
    }

}