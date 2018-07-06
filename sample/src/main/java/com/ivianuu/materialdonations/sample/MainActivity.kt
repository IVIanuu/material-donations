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
                .addSkus("pizza", "kebab", "pasta")
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

    override fun onDonationError() {
        Log.d("testtt", "on donation error")
    }

    private fun initDebugBillingStuff() {
        val billingStore = BillingStore.defaultStore(this)
        billingStore.clearProducts()
        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = "pizza", type = BillingClient.SkuType.INAPP,
                price = "$7.99", priceAmountMicros = 799, priceCurrencyCode = "USD",
                title = "Pizza", description = "description..").build()
        )
        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = "kebab", type = BillingClient.SkuType.INAPP,
                price = "$5.99", priceAmountMicros = 599, priceCurrencyCode = "USD",
                title = "Kebab", description = "description..").build()
        )
        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = "pasta", type = BillingClient.SkuType.INAPP,
                price = "$6.99", priceAmountMicros = 699, priceCurrencyCode = "USD",
                title = "Pasta", description = "description.."
            ).build()
        )

        MaterialDonationsPlugins.billingClientFactory =
                DebugBillingClientFactory()
    }
}
