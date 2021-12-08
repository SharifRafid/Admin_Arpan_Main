package admin.arpan.delivery

import admin.arpan.delivery.db.model.CalculationResult
import admin.arpan.delivery.db.model.CartProductEntity
import admin.arpan.delivery.db.model.OrderItemMain
import kotlin.math.roundToInt


class CalculationLogics {
    private val BKASH_CHARGE_PERCENTAGE = 0.0185f

    fun getBkashChargePercentage(): Float {return BKASH_CHARGE_PERCENTAGE}

    fun calculateArpansStatsForArpan(orders : ArrayList<OrderItemMain>) : CalculationResult{
        val calculationResult = CalculationResult()

        // We need totalOrdersCount, successFulOrders, CancelledOrders, TotalArpan'sIncome

        calculationResult.totalOrders = orders.size
        calculationResult.completed = 0
        calculationResult.cancelled = 0
        calculationResult.arpansIncome = 0
        calculationResult.agentsDueToArpan = 0
        calculationResult.agentsDueToArpanPermanent = 0
        calculationResult.agentsIncome = 0
        calculationResult.agentsIncomePermanent = 0

        for(orderItemMain in orders){
            if(orderItemMain.orderStatus=="COMPLETED" && orderItemMain.orderCompletedStatus!="CANCELLED"){
                calculationResult.completed += 1

                //Order Was Successfully completed so we can calculate this value
                if(orderItemMain.products.size == 1 && !orderItemMain.products.any { it.product_item }){
                    // This is  a custom order so it'll have separate calculation

                    // The amount that the customer gives to the delivery agent
                    val customersAmount = orderItemMain.totalPrice + orderItemMain.deliveryCharge

                    // The amount the delivery agent pays to the shop/store before collecting the product
                    val shopAmount = orderItemMain.totalPrice

                    var deliveryAgentsDueToArpan = (customersAmount - shopAmount) - orderItemMain.daCharge

                    if(orderItemMain.paymentMethod == "bKash"){
                        val bkashChargeExtra = roundNumberPriceTotal((orderItemMain.totalPrice+orderItemMain.deliveryCharge)
                                *BKASH_CHARGE_PERCENTAGE).toInt()
                        deliveryAgentsDueToArpan -= bkashChargeExtra
                    }

                    calculationResult.arpansIncome += deliveryAgentsDueToArpan
                    calculationResult.agentsDueToArpan += deliveryAgentsDueToArpan
                    calculationResult.agentsIncome += orderItemMain.daCharge
                    calculationResult.agentsDueToArpanPermanent += deliveryAgentsDueToArpan
                }else{
                    // This is a shop order so this will also have another type of calculation

                    // The amount that the customer gives to the delivery agent
                    val customersAmount = orderItemMain.totalPrice + orderItemMain.deliveryCharge

                    // The amount the delivery agent pays to the shop/store before collecting the product
                    val shopAmount = calculateShopAmount(orderItemMain.products)

                    var deliveryAgentsDueToArpan = (customersAmount - shopAmount) - orderItemMain.daCharge

                    if(orderItemMain.paymentMethod == "bKash"){
                        val bkashChargeExtra = roundNumberPriceTotal((orderItemMain.totalPrice+orderItemMain.deliveryCharge)
                                *BKASH_CHARGE_PERCENTAGE).toInt()
                        deliveryAgentsDueToArpan -= bkashChargeExtra
                    }

                    calculationResult.arpansIncome += deliveryAgentsDueToArpan
                    calculationResult.agentsDueToArpan += deliveryAgentsDueToArpan
                    calculationResult.agentsIncome += orderItemMain.daCharge
                    calculationResult.agentsDueToArpanPermanent += deliveryAgentsDueToArpan
                }

            }else if(orderItemMain.orderCompletedStatus=="CANCELLED"){
                calculationResult.cancelled += 1
            }
        }

        return calculationResult
    }

    private fun calculateShopAmount(products: List<CartProductEntity>): Int {
        var shopAmount = 0
        for(product in products){
            shopAmount += (product.product_item_price-product.product_arpan_profit)*product.product_item_amount
        }
        return shopAmount
    }
    private fun roundNumberPriceTotal(d: Float): Int {
        //This  is a special round function exclusively for this  page of the app
        //not usable for general parts and other parts of   the code or apps
        return if(d > d.toInt()){
            d.toInt()+1
        }else{
            d.roundToInt()
        }
    }
}