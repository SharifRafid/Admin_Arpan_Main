package admin.arpan.delivery.utils.networking.responses

import admin.arpan.delivery.models.ArpanStatistics
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.models.User
import com.google.gson.annotations.SerializedName


data class GetAllDAResponse (

  var error: Boolean?,
  var message: String?,
  @SerializedName("results"      ) var results      : ArrayList<DaItemResponse> = arrayListOf(),
  @SerializedName("page"         ) var page         : Int?               = null,
  @SerializedName("limit"        ) var limit        : Int?               = null,
  @SerializedName("totalPages"   ) var totalPages   : Int?               = null,
  @SerializedName("totalResults" ) var totalResults : Int?               = null

)

data class DaItemResponse(
  var daItem : User? = null,
  var statistics : ArpanStatistics = ArpanStatistics()
)

