package admin.arpan.delivery.utils.networking.responses

import admin.arpan.delivery.models.Access
import admin.arpan.delivery.models.Refresh

data class RefreshResponse(
    val error: Boolean?,
    val message: String?,
    val access: Access?,
    val refresh: Refresh?
)