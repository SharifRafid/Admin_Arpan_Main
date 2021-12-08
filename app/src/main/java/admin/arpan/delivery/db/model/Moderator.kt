package admin.arpan.delivery.db.model

data class Moderator(
    var email : String = "",
    var pass : String = "",
    var enabled : Boolean = false,
)
