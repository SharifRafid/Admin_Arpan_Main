package admin.arpan.delivery.utils.networking.requests

import admin.arpan.delivery.models.enums.OrderStatus

class GetOrdersRequest(
  var startTimeMillis: Long? = null,
  var endTimeMillis: Long? = null,
  var limit: Int? = null,
  var page: Int? = null,
  var daId: String? = null,
  var orderStatus: OrderStatus? = null,
)