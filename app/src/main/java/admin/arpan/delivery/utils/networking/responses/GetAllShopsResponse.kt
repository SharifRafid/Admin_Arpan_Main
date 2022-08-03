package admin.arpan.delivery.utils.networking.responses

import admin.arpan.delivery.models.Shop
import com.google.gson.annotations.SerializedName


data class GetAllShopsResponse (

  var error: Boolean?,
  var message: String?,
  @SerializedName("results"      ) var results      : ArrayList<Shop> = arrayListOf(),
  @SerializedName("page"         ) var page         : Int?               = null,
  @SerializedName("limit"        ) var limit        : Int?               = null,
  @SerializedName("totalPages"   ) var totalPages   : Int?               = null,
  @SerializedName("totalResults" ) var totalResults : Int?               = null

)

