package admin.arpan.delivery.models

data class Setting(
  var error: Boolean? = null,
  var message: String? = null,

  var id: String? = null,

  var orderStartTime: String? = null,
  var orderEndTime: String? = null,
  var orderOverTimeAllowed: Boolean? = null,

  var parcelMaxOrders: Int? = null,
  var customMaxOrders: Int? = null,
  var medicineMaxOrders: Int? = null,
  var totalCustomMaxOrders: Int? = null,

  var maxShopPerOrder: Int? = null,
  var maxChargeAfterPershopMaxOrder: Int? = null,
  var maxDaChargeAfterPershopMaxOrder: Int? = null,
  var allowOrderingMoreThanMaxShops: Boolean? = null,

  var alertDialogEmergencyTitleText: String? = null,
  var alertDialogEmergencyMessageText: String? = null,
  var alertDialogeEmergencyStatus: String? = null,

  )
