package com.ivianuu.materialdonations.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.ivianuu.materialdonations.MaterialDonationsDialog
import com.ivianuu.materialdonations.MaterialDonationsPlugins
import com.ivianuu.materialdonations.billingx.DebugBillingClientFactory
import com.pixite.android.billingx.BillingStore
import com.pixite.android.billingx.SkuDetailsBuilder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MaterialDonationsDialog.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDebugBillingStuff()

        donate.setOnClickListener {
            MaterialDonationsDialog.newBuilder(this)
                .title("Donate")
                .negativeButtonText("Cancel")
                .donatedMsg("Thanks for the donation!")
                .canceledMsg("Oh no canceled!")
                .errorMsg("Something went wrong please try it again!")
                .addSkus(SKUS)
                .sortOrder(MaterialDonationsDialog.SORT_ORDER_PRICE_ASC)
                .show(supportFragmentManager)

        }
    }

    override fun onDonated(sku: String) {
        Log.d("testtt", "on donated")
    }

    override fun onDonationCanceled() {
        Log.d("testtt", "on donation canceled")
    }

    override fun onDonationError(error: MaterialDonationsDialog.Error) {
        Log.d("testtt", "on donation error $error")
    }

    private fun initDebugBillingStuff() {
        val billingStore = BillingStore.defaultStore(this)

        billingStore.clearProducts()

        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = SKU_DONATION_BEER, type = BillingClient.SkuType.INAPP,
                price = "$0.99", priceAmountMicros = 990000, priceCurrencyCode = "USD",
                title = "Beer", description = "description.."
            ).build()
        )

        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = SKU_DONATION_BIGGER_MEAL, type = BillingClient.SkuType.INAPP,
                price = "$0.99", priceAmountMicros = 990000, priceCurrencyCode = "USD",
                title = "Bigger Meal", description = "description.."
            ).build()
        )

        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = SKU_DONATION_BURGER, type = BillingClient.SkuType.INAPP,
                price = "$0.99", priceAmountMicros = 990000, priceCurrencyCode = "USD",
                title = "Burger", description = "description.."
            ).build()
        )

        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = SKU_DONATION_COFFEE, type = BillingClient.SkuType.INAPP,
                price = "$0.99", priceAmountMicros = 990000, priceCurrencyCode = "USD",
                title = "Coffee", description = "description.."
            ).build()
        )

        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = SKU_DONATION_MEAL, type = BillingClient.SkuType.INAPP,
                price = "$0.99", priceAmountMicros = 990000, priceCurrencyCode = "USD",
                title = "Meal", description = "description.."
            ).build()
        )

        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = SKU_DONATION_PIZZA, type = BillingClient.SkuType.INAPP,
                price = "$0.99", priceAmountMicros = 990000, priceCurrencyCode = "USD",
                title = "Pizza", description = "description.."
            ).build()
        )

        MaterialDonationsPlugins.billingClientFactory =
                DebugBillingClientFactory()
    }

    private companion object {
        private const val SKU_DONATION_BEER = "donation_beer"
        private const val SKU_DONATION_BIGGER_MEAL = "donation_bigger_meal"
        private const val SKU_DONATION_BURGER = "donation_burger"
        private const val SKU_DONATION_COFFEE = "donation_coffee"
        private const val SKU_DONATION_MEAL = "donation_meal"
        private const val SKU_DONATION_PIZZA = "donation_pizza"

        val SKUS = listOf(
            SKU_DONATION_BEER,
            SKU_DONATION_BIGGER_MEAL,
            SKU_DONATION_BURGER,
            SKU_DONATION_COFFEE,
            SKU_DONATION_MEAL,
            SKU_DONATION_PIZZA
        )
    }
}
