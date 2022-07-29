package admin.arpan.delivery.models

data class User(
    val address: String,
    val blocked: Boolean,
    val email: String,
    val id: String,
    val image: Image,
    val name: String,
    val password: String,
    val phone: Int,
    val registrationTokens: List<String>,
    val registrationTokensAdmin: List<Any>,
    val registrationTokensDA: List<Any>,
    val registrationTokensModerator: List<Any>,
    val role: String,
    val roles: List<String>
)