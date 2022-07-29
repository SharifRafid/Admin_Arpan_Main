package admin.arpan.delivery.utils.networking.responses

import admin.arpan.delivery.db.model.OrderItemMain
import admin.arpan.delivery.models.Access
import admin.arpan.delivery.models.Refresh

data class GetOrdersResponse(
    val error: Boolean?,
    val message: String?,
    val results: ArrayList<OrderItemMain>?,
    val page: Int?,
    val limit: Int?,
    val totalPages: Int?,
    val totalResults: Int?,
)