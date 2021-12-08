package admin.arpan.delivery.ui.home

import admin.arpan.delivery.db.model.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.collections.ArrayList

class HomeViewModelMainData : ViewModel() {
    var shopsArrayListDataHolder : MutableLiveData<ArrayList<ShopItem>> = MutableLiveData(ArrayList<ShopItem>())
    fun setShopsMainArrayListData(tempShopArrayList: ArrayList<ShopItem>) {
       shopsArrayListDataHolder.value = tempShopArrayList
    }
    fun getShopsMainArrayListData(): MutableLiveData<ArrayList<ShopItem>>{
        return shopsArrayListDataHolder
    }

    var daAgentArrayListDataHolder : MutableLiveData<ArrayList<DaAgent>> = MutableLiveData(ArrayList<DaAgent>())
    fun setDaMainListData(tempDaList: ArrayList<DaAgent>) {
        daAgentArrayListDataHolder.value = tempDaList
    }
    fun getDaMainListData() : MutableLiveData<ArrayList<DaAgent>>{
        return daAgentArrayListDataHolder
    }

    var todayOrdersMainArrayList : MutableLiveData<ArrayList<OrderItemMain>> = MutableLiveData(ArrayList<OrderItemMain>())
    fun setOrdersOneDayDataMainList(tempOrdersMainArrayList: ArrayList<OrderItemMain>) {
        todayOrdersMainArrayList.value = tempOrdersMainArrayList
    }
    fun getOrdersOneDayDataMainList() : MutableLiveData<ArrayList<OrderItemMain>>{
        return todayOrdersMainArrayList
    }

    var daStatusRealtimeList = MutableLiveData(ArrayList<DaStatusItem>())
    fun setDaRealtimeStatusList(daStatusList: java.util.ArrayList<DaStatusItem>) {
        daStatusRealtimeList.value = daStatusList
    }
    fun getDaRealtimeStatusList() : MutableLiveData<ArrayList<DaStatusItem>>{
        return daStatusRealtimeList
    }

    var mainLocationsItemArrayList = MutableLiveData(ArrayList<LocationItem>())
    fun setLocationsArrayList(tempLocationsArrayList: java.util.ArrayList<LocationItem>) {
        mainLocationsItemArrayList.value = tempLocationsArrayList
    }
   fun getLocationsArrayList(): MutableLiveData<java.util.ArrayList<LocationItem>> {
        return mainLocationsItemArrayList
    }

    var mainUserSavedPrefClientTfArrayList = MutableLiveData(ArrayList<SavedPrefClientTf>())
    fun setUserSavedPrefClientTfArrayList(tempUserSavedPrefClientTfArrayList: java.util.ArrayList<SavedPrefClientTf>) {
        mainUserSavedPrefClientTfArrayList.value = tempUserSavedPrefClientTfArrayList
    }
    fun getUserSavedPrefClientTfArrayList() : MutableLiveData<ArrayList<SavedPrefClientTf>> {
        return mainUserSavedPrefClientTfArrayList
    }

    var currentSelectedOrderItem = OrderItemMain()
    fun setCurrentSelectedOrderItemMain(orderItemMain: OrderItemMain) {
        currentSelectedOrderItem = orderItemMain
    }
    fun getCurrentSelectedOrderItemMain() : OrderItemMain {
        return currentSelectedOrderItem
    }
    var ordersThisMonthArrayList = MutableLiveData(ArrayList<OrderItemMain>())
    fun setThisMonthOrdersMainArrayList(tempOrdersThisMonthArrayList: java.util.ArrayList<OrderItemMain>) {
        ordersThisMonthArrayList.value = tempOrdersThisMonthArrayList
    }
    fun getThisMonthOrdersArrayList() : MutableLiveData<ArrayList<OrderItemMain>>{
        return ordersThisMonthArrayList
    }

    var ordersLastMonthOrdersMainData = MutableLiveData(ArrayList<OrderItemMain>())
    fun setLastMonthOrdersMainData(tempOrdersLastMonthArrayList: java.util.ArrayList<OrderItemMain>) {
        ordersLastMonthOrdersMainData.value = tempOrdersLastMonthArrayList
    }
    fun getLastMonthOrdersMainData() : MutableLiveData<ArrayList<OrderItemMain>>{
        return ordersLastMonthOrdersMainData
    }

    var usersArrayListMainData = MutableLiveData(ArrayList<UserItem>())
    fun setUsersMainArrayListData(usersArrayList: ArrayList<UserItem>){
        usersArrayListMainData.value = usersArrayList
    }
    fun getUsersMainArrayListData() : MutableLiveData<ArrayList<UserItem>>{
        return usersArrayListMainData
    }

    var currentSelectedOrderItemToEdit = OrderItemMain()
}