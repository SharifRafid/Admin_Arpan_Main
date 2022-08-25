package admin.arpan.delivery.db.model

import admin.arpan.delivery.models.Location
import admin.arpan.delivery.models.User

data class OrderItemMain(
    var error: Boolean? = null,
    var message: String? =  null,
    var id : String? = null,
    var userId : String? = null,
    var userPhoneAccount : String? = null,
    var userName : String? = null,
    var userNumber : String? = null,
    var userNote : String? = null,
    var userAddress : String? = null,
    var products : List<CartProductEntity> = ArrayList(),
    var promoCodeApplied : Boolean = false,
    var promoCode: PromoCode? = null,
    var paymentMethod : String? = null,
    var totalPrice : Int = 0,
    var locationItem: Location? = null,
    var deliveryCharge : Int = 0,
    var orderPlacingTimeStamp : Long = 0,
    var lastTouchedTimeStamp : Long = 0,
    var orderStatus : String? = null,
    var paid : Boolean = false,
    var orderId : String? = null,
    var pickDropOrder : Boolean = false,
    var pickDropOrderItem : PickDropOrderItem? = null,
    var lattitude : String? = null,
    var longtitude : String? = null,
    var orderCompletedStatus : String? = null,
    var daDetails : User? = null,
    var daID : String? = null,
    var daCharge : Int = 0,
    var adminOrder : Boolean = false,
    var verifiedTimeStampMillis : Long = 0,
    var processingTimeStampMillis : Long = 0,
    var pickedUpTimeStampMillis : Long = 0,
    var completedTimeStampMillis : Long = 0,
    var assignedToDaTimeStampMillis : Long = 0,
    var cancelledOrderReasonFromAdmin : String? = null,
    var adminOrderNote : String? = null,
    var paymentRequested : Boolean = false,
    var paymentCompleted : Boolean = false,
    var pickUpTime : String? = null,
)











































