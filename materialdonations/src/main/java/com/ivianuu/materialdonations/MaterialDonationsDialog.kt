/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.materialdonations

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_donation.*

/**
 * Material Donations Dialog
 */
class MaterialDonationsDialog : DialogFragment(), PurchasesUpdatedListener,
    BillingClientStateListener {

    private lateinit var billingClient: BillingClient
    private lateinit var epoxyController: DonationEpoxyController

    private var currentDonation: String? = null

    private var canceled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            currentDonation = it.getString(KEY_CURRENT_DONATION)
        }

        billingClient = MaterialDonationsPlugins.billingClientFactory
            .createBillingClient(requireContext(), this)
        billingClient.startConnection(this)

        epoxyController = DonationEpoxyController(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog.Builder(requireContext())
            .title(
                arguments!!.getString(KEY_TITLE)
                        ?: getString(R.string.default_donation_dialog_title)
            )
            .negativeText(
                arguments!!.getString(KEY_NEGATIVE_BUTTON_TEXT)
                        ?: getString(android.R.string.cancel)
            )
            .onAny { _, which ->
                when(which) {
                    DialogAction.NEGATIVE -> dismissSafe()
                    else -> {}
                }
            }
            .adapter(epoxyController.adapter, LinearLayoutManager(requireContext()))
            .autoDismiss(false)
            .build()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_DONATION, currentDonation)
    }

    override fun onCancel(dialog: DialogInterface?) {
        onCanceled()
        super.onCancel(dialog)
    }

    override fun onDetach() {
        billingClient.endConnection()
        super.onDetach()
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        val currentDonation = currentDonation ?: return

        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                val purchase = purchases?.firstOrNull { it.sku == currentDonation }

                if (purchase != null) {
                    if (arguments!!.getBoolean(KEY_CONSUME, true)) {
                        try {
                            billingClient.consumeAsync(purchase.purchaseToken) { responseCode, _ ->
                                if (responseCode == BillingClient.BillingResponse.OK) {
                                    onDonated()
                                } else {
                                    onError()
                                }
                            }
                        } catch (e: NotImplementedError) { // todo remove this when billing x supports consumption
                            onDonated()
                        }
                    } else onDonated()
                } else onError()
            }
            BillingClient.BillingResponse.USER_CANCELED -> onCanceled()
            else -> onError()
        }

        this.currentDonation = null
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        if (responseCode != BillingClient.BillingResponse.OK) {
            onError()
        }

        getDonations()
    }

    override fun onBillingServiceDisconnected() {
    }

    internal fun skuClicked(sku: SkuDetails) {
        val params = BillingFlowParams.newBuilder()
            .setType(BillingClient.SkuType.INAPP)
            .setSku(sku.sku)
            .build()

        if (billingClient.launchBillingFlow(requireActivity(), params)
            == BillingClient.BillingResponse.OK) {
            currentDonation = sku.sku
        } else {
            onError()
        }
    }

    private fun getDonations() {
        val skus = arguments!!.getStringArrayList(KEY_SKUS)

        val params = SkuDetailsParams.newBuilder()
            .setType(BillingClient.SkuType.INAPP)
            .setSkusList(skus)
            .build()

        billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
            if (responseCode == BillingClient.BillingResponse.OK) {
                if (skuDetailsList.isNotEmpty()) {
                    val sortOrderInt = arguments!!.getInt(KEY_SORT_ORDER, SortOrder.NONE.value)
                    val sortOrder = SortOrder.values().first { it.value == sortOrderInt }
                    val finalList = when (sortOrder) {
                        SortOrder.TITLE_ASC -> skuDetailsList.sortedBy { it.title }
                        SortOrder.TITLE_DESC -> skuDetailsList.sortedByDescending { it.title }
                        SortOrder.PRICE_ASC -> skuDetailsList.sortedBy { it.priceAmountMicros }
                        SortOrder.PRICE_DESC -> skuDetailsList.sortedByDescending { it.priceAmountMicros }
                        else -> skuDetailsList.toList()
                    }

                    epoxyController.setData(finalList)
                } else {
                    onError()
                }
            } else {
                onError()
            }
        }
    }

    private fun onDonated() {
        arguments!!.getCharSequence(KEY_DONATED_MSG)?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        dismissSafe()
    }

    private fun onCanceled() {
        if (canceled) return
        canceled = true
        arguments!!.getCharSequence(KEY_CANCELED_MSG)?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        dismissSafe()
    }

    private fun onError() {
        arguments!!.getCharSequence(KEY_ERROR_MSG)?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        dismissSafe()
    }

    private fun dismissSafe() {
        try {
            dismissAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    enum class SortOrder(val value: Int) {
        NONE(0), TITLE_ASC(1), TITLE_DESC(2), PRICE_ASC(3), PRICE_DESC(4)
    }

    companion object {
        private const val FRAGMENT_TAG = "MaterialDonationsDialog"

        private const val KEY_TITLE = "title"
        private const val KEY_NEGATIVE_BUTTON_TEXT = "negative_button_text"
        private const val KEY_DONATED_MSG = "donated_msg"
        private const val KEY_ERROR_MSG = "error_msg"
        private const val KEY_CANCELED_MSG = "canceled_msg"
        private const val KEY_SKUS = "skus"
        private const val KEY_SORT_ORDER = "sort_order"
        private const val KEY_CONSUME = "consume"

        private const val KEY_CURRENT_DONATION = "current_donation"

        fun newBuilder(context: Context) = Builder(context)
    }

    class Builder(private val context: Context) {

        private var title: CharSequence? = null
        private var negativeButtonText: CharSequence? = null
        private var donatedMsg: CharSequence? = null
        private var errorMsg: CharSequence? = null
        private var canceledMsg: CharSequence? = null
        private var sortOrder = SortOrder.NONE
        private var consume = true

        private val skus = mutableSetOf<String>()

        fun title(title: CharSequence) = apply {
            this.title = title
        }

        fun titleRes(titleRes: Int) =
                title(context.getString(titleRes))

        fun negativeButtonText(negativeButtonText: CharSequence) = apply {
            this.negativeButtonText = negativeButtonText
        }

        fun negativeButtonTextRes(negativeButtonTextRes: Int) =
                negativeButtonText(context.getString(negativeButtonTextRes))

        fun donatedMsg(donatedMsg: CharSequence?) = apply {
            this.donatedMsg = donatedMsg
        }

        fun donatedMsgRes(donatedMsgRes: Int) =
            donatedMsg(context.getString(donatedMsgRes))

        fun errorMsg(errorMsg: CharSequence?) = apply {
            this.errorMsg = errorMsg
        }

        fun errorMsgRes(errorMsgRes: Int) =
            errorMsg(context.getString(errorMsgRes))

        fun canceledMsg(canceledMsg: CharSequence?) = apply {
            this.canceledMsg = canceledMsg
        }

        fun canceledMsgRes(canceledMsgRes: Int) =
            canceledMsg(context.getString(canceledMsgRes))

        fun addSkus(vararg skus: String) = apply {
            this.skus.addAll(skus)
        }

        fun addSkus(skus: Collection<String>) = apply {
            this.skus.addAll(skus)
        }

        fun sortOrder(sortOrder: SortOrder) = apply {
            this.sortOrder = sortOrder
        }

        fun consume(consume: Boolean) = apply {
            this.consume = consume
        }

        fun create(): MaterialDonationsDialog {
            if (skus.isEmpty()) {
                throw IllegalStateException("at least 1 sku must be added")
            }

            return MaterialDonationsDialog().apply {
                arguments = Bundle().apply {
                    putCharSequence(KEY_TITLE, this@Builder.title)
                    putCharSequence(KEY_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                    putCharSequence(KEY_DONATED_MSG, donatedMsg)
                    putCharSequence(KEY_ERROR_MSG, errorMsg)
                    putCharSequence(KEY_CANCELED_MSG, canceledMsg)
                    putStringArrayList(KEY_SKUS, ArrayList(skus))
                    putInt(KEY_SORT_ORDER, sortOrder.value)
                    putBoolean(KEY_CONSUME, consume)
                }
            }
        }

        fun show(fm: FragmentManager): MaterialDonationsDialog {
            val dialog = create()
            dialog.show(fm, FRAGMENT_TAG)
            return dialog
        }
    }

    private class DonationEpoxyController(
        private val dialog: MaterialDonationsDialog
    ) : TypedEpoxyController<List<SkuDetails>>() {
        override fun buildModels(data: List<SkuDetails>) {
            data
                .distinctBy { it.sku }
                .forEach {
                    DonationItemModel(it, dialog)
                        .addTo(this)
                }
        }
    }

    private class DonationItemModel(
        private val sku: SkuDetails,
        private val dialog: MaterialDonationsDialog
    ) : EpoxyModelWithHolder<DonationItemModel.Holder>() {

        init {
            id(sku.sku)
        }

        override fun bind(holder: Holder) {
            super.bind(holder)
            with(holder) {
                title.text = sku.readableTitle
                desc.text = sku.description
                price.text = sku.price

                containerView.setOnClickListener { dialog.skuClicked(sku) }
            }
        }

        override fun getDefaultLayout() = R.layout.item_donation

        override fun createNewHolder() = Holder()

        class Holder : EpoxyHolder(), LayoutContainer {
            override lateinit var containerView: View
            override fun bindView(itemView: View) {
                containerView = itemView
            }
        }

        private val SkuDetails.readableTitle: String
            get() {
                val title = title
                if (!title.contains("(") || !title.contains(")")) return title
                return title
                    .removeRange(title.indexOfLast { it == '(' }, title.lastIndex + 1)
                    .trimEnd()
            }
    }
}