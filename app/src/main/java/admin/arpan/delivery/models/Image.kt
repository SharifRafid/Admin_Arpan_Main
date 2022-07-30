package admin.arpan.delivery.models

import com.google.gson.annotations.SerializedName

data class Image (

    @SerializedName("name" ) var name : String? = null,
    @SerializedName("type" ) var type : String? = null,
    @SerializedName("path" ) var path : String? = null,
    @SerializedName("id"   ) var id   : String? = null

)
