package admin.arpan.delivery.utils.networking.requests

class GetOrdersRequest(
  var startTimeMillis: Long,
  var endTimeMillis: Long,
  var limit: Int,
  var page: Int,
)