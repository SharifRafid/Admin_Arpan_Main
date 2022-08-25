package admin.arpan.delivery.utils.networking.responses

import admin.arpan.delivery.models.SlidingTextItem
import com.google.gson.annotations.SerializedName


data class GetAllNoticesResponse (

  var error: Boolean?,
  var message: String?,
  @SerializedName("results"      ) var results      : ArrayList<SlidingTextItem> = arrayListOf(),
  @SerializedName("page"         ) var page         : Int?               = null,
  @SerializedName("limit"        ) var limit        : Int?               = null,
  @SerializedName("totalPages"   ) var totalPages   : Int?               = null,
  @SerializedName("totalResults" ) var totalResults : Int?               = null

)

