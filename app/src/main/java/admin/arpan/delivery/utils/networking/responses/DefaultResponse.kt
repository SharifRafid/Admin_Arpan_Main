package admin.arpan.delivery.utils.networking.responses

import admin.arpan.delivery.models.Access
import admin.arpan.delivery.models.Refresh

data class DefaultResponse(
    val error: Boolean?,
    val message: String?
)