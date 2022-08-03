package admin.arpan.delivery.models

import com.google.gson.annotations.SerializedName

data class Shop(
  @SerializedName("isClient") var isClient: Boolean? = null,
  @SerializedName("open") var open: Boolean? = null,
  @SerializedName("categories") var categories: ArrayList<String> = arrayListOf(),
  @SerializedName("productCategories") var productCategories: ArrayList<String> = arrayListOf(),
  @SerializedName("name") var name: String? = null,
  @SerializedName("order") var order: Int? = null,
  @SerializedName("dynamicLink") var dynamicLink: String? = null,
  @SerializedName("coverPhoto") var coverPhoto: Image? = Image(),
  @SerializedName("icon") var icon: Image? = Image(),
  @SerializedName("activeHours") var activeHours: String? = null,
  @SerializedName("location") var location: String? = null,
  @SerializedName("deliveryCharge") var deliveryCharge: Int? = null,
  @SerializedName("daCharge") var daCharge: Int? = null,
  @SerializedName("notices") var notices: ArrayList<Notice> = arrayListOf(),
  @SerializedName("id") var id: String? = null

)