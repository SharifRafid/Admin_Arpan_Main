package admin.arpan.delivery.models

data class ArpanStatistics(
  var arpansIncome: Int = 0,
  var cancelled: Int = 0,
  var completed: Int = 0,
  var totalOrders: Int = 0,
  var agentsDueToArpan: Int = 0,
  var agentsIncome: Int = 0,
  var agentsIncomePermanent: Int = 0,
  var agentsDueToArpanPermanent: Int = 0,
  var possibleCalculationRemaining: Int = 0
)