package admin.arpan.delivery.models

import com.google.gson.annotations.SerializedName

data class Notice(
  @SerializedName("color"       ) var color       : String? = null,
  @SerializedName("bgColor"     ) var bgColor     : String? = null,
  @SerializedName("title"       ) var title       : String? = null,
  @SerializedName("description" ) var description : String? = null,
  @SerializedName("order"       ) var order       : Int?    = null,
  @SerializedName("_id"         ) var Id          : String? = null

)
