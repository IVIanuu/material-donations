package com.ivianuu.materialdonations.sample

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import com.ivianuu.materialdonations.BillingClientFactory
import com.ivianuu.materialdonations.MaterialDonationsDialog
import com.ivianuu.materialdonations.MaterialDonationsPlugins
import com.ivianuu.materialdonations.billingx.DebugBillingClientFactory
import com.pixite.android.billingx.BillingStore
import com.pixite.android.billingx.DebugBillingClient
import com.pixite.android.billingx.SkuDetailsBuilder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MaterialDonationsDialog.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val billingStore = BillingStore.defaultStore(this)
        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = "pizza", type = BillingClient.SkuType.INAPP,
                price = "$0.99", priceAmountMicros = 990000, priceCurrencyCode = "USD",
                title = "Pizza", description = "description..").build()
        )
        billingStore.addProduct(
            SkuDetailsBuilder(
                sku = "kebab", type = BillingClient.SkuType.INAPP,
                price = "$0.99", priceAmountMicros = 990000, priceCurrencyCode = "USD",
                title = "Kebab", description = "description..").build()
        )

        MaterialDonationsPlugins.billingClientFactory =
                DebugBillingClientFactory()

        donate.setOnClickListener {
            MaterialDonationsDialog.newBuilder(this)
                .addSkus("pizza", "kebab")
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
}
