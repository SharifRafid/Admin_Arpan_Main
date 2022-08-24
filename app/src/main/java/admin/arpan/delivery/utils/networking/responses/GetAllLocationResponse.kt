package admin.arpan.delivery.utils.networking.responses

import admin.arpan.delivery.db.model.LocationItem
import admin.arpan.delivery.models.ArpanStatistics
import admin.arpan.delivery.models.Location
import admin.arpan.delivery.models.Shop
import admin.arpan.delivery.models.User
import com.google.gson.annotations.SerializedName


data class GetAllLocationResponse (
  var error: Boolean?,
  var message: String?,
  @SerializedName("results"      ) var results      : ArrayList<Location> = arrayListOf(),
  @SerializedName("page"         ) var page         : Int?               = null,
  @SerializedName("limit"        ) var limit        : Int?               = null,
  @SerializedName("totalPages"   ) var totalPages   : Int?               = null,
  @SerializedName("totalResults" ) var totalResults : Int?               = null

)

