package admin.arpan.delivery.utils.networking.responses

import admin.arpan.delivery.models.Tokens
import admin.arpan.delivery.models.User

data class LoginResponse(
    val error: Boolean?,
    val message: String?,
    val tokens: Tokens?,
    val user: User?
)