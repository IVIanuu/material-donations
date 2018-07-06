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
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.epoxy.TypedEpoxyController
import com.android.billingclient.api.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_donation.*

/**
 * Material Donations Dialog
 */
class MaterialDonationsDialog : DialogFragment(), PurchasesUpdatedListener,
    BillingClientStateListener {

    private var callback: Callback? = null

    private lateinit var billingClient: BillingClient
    private lateinit var epoxyController: DonationEpoxyController

    private var currentDonation: String? = null

    private var canceled = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = activity as? Callback
        if (callback == null) {
            callback = parentFragment as? Callback
        }
        if (callback == null) {
            callback = targetFragment as? Callback
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            currentDonation = it.getString(KEY_CURRENT_DONATION)
        }

        billingClient = MaterialDonationsPlugins.billingClientFactory
            .createBillingClient(requireContext(), this)
            .also { it.startConnection(this) }

        val appName = try {
            requireActivity().application.applicationInfo.loadLabel(
                requireActivity().packageManager)
        } catch (e: Exception) {
            ""
        }

        epoxyController = DonationEpoxyController(this, appName.toString())

        getDonations()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog.Builder(requireContext())
            .title(
                arguments!!.getString(KEY_TITLE)
                        ?: getString(R.string.default_donation_dialog_title)
            )
            .negativeText(
                arguments!!.getString(KEY_NEGATIVE_BUTTON_TEXT)
                        ?: getString(R.string.default_donation_dialog_title)
            )
            .onAny { _, which ->
                when(which) {
                    DialogAction.NEGATIVE -> {
                        callback?.onDonationCanceled()
                        dismiss()
                    }
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
        callback = null
        super.onDetach()
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                if (purchases?.any { it.sku == currentDonation } == true) {
                    onDonated(currentDonation!!)
                }
            }
            BillingClient.BillingResponse.USER_CANCELED -> {
                onCanceled()
            }
            else -> {
                onError(Error.PURCHASE_FAILURE)
            }
        }

        currentDonation = null

        dismiss()
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        if (responseCode != BillingClient.BillingResponse.OK) {
            onError(Error.SETUP_ERROR)
        }
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
            onError(Error.FAILED_TO_START_PURCHASE_FLOW)
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
                    val finalList = when (arguments!!.getInt(KEY_SORT_ORDER, SORT_ORDER_NONE)) {
                        SORT_ORDER_TITLE_ASC -> skuDetailsList.sortedBy { it.title }
                        SORT_ORDER_TITLE_DESC -> skuDetailsList.sortedByDescending { it.title }
                        SORT_ORDER_PRICE_ASC -> skuDetailsList.sortedBy { it.priceAmountMicros }
                        SORT_ORDER_PRICE_DESC -> skuDetailsList.sortedByDescending { it.priceAmountMicros }
                        else -> skuDetailsList.toList()
                    }

                    epoxyController.setData(finalList)
                } else {
                    onError(Error.SKUS_EMPTY)
                }
            } else {
                onError(Error.FAILED_TO_LOAD_SKUS)
            }
        }
    }

    private fun onDonated(sku: String) {
        arguments!!.getCharSequence(KEY_DONATED_MSG)?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        callback?.onDonated(sku)
    }

    private fun onCanceled() {
        if (canceled) return
        canceled = true
        arguments!!.getCharSequence(KEY_CANCELED_MSG)?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        callback?.onDonationCanceled()
        dismiss()
    }

    private fun onError(error: Error) {
        callback?.onDonationError(error)
        arguments!!.getCharSequence(KEY_ERROR_MSG)?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        dismiss()
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

        private const val KEY_CURRENT_DONATION = "current_donation"

        const val SORT_ORDER_NONE = 0
        const val SORT_ORDER_TITLE_ASC = 1
        const val SORT_ORDER_TITLE_DESC = 2
        const val SORT_ORDER_PRICE_ASC = 3
        const val SORT_ORDER_PRICE_DESC = 4

        fun newBuilder(context: Context) = Builder(context)
    }

    interface Callback {
        fun onDonated(sku: String)
        fun onDonationCanceled()
        fun onDonationError(error: Error)
    }

    enum class Error {
        SETUP_ERROR, CONNECTION_ERROR, SKUS_EMPTY,
        FAILED_TO_LOAD_SKUS, FAILED_TO_START_PURCHASE_FLOW, PURCHASE_FAILURE, UNKNOWN
    }

    class Builder(private val context: Context) {

        private var title: CharSequence? = null
        private var negativeButtonText: CharSequence? = null
        private var donatedMsg: CharSequence? = null
        private var errorMsg: CharSequence? = null
        private var canceledMsg: CharSequence? = null
        private var sortOrder = SORT_ORDER_NONE

        private val skus = mutableSetOf<String>()

        fun title(title: CharSequence): Builder {
            this.title = title
            return this
        }

        fun titleRes(titleRes: Int) =
                title(context.getString(titleRes))

        fun negativeButtonText(negativeButtonText: CharSequence): Builder {
            this.negativeButtonText = negativeButtonText
            return this
        }

        fun negativeButtonTextRes(negativeButtonTextRes: Int) =
                negativeButtonText(context.getString(negativeButtonTextRes))

        fun donatedMsg(donatedMsg: CharSequence?): Builder {
            this.donatedMsg = donatedMsg
            return this
        }

        fun donatedMsgRes(donatedMsgRes: Int) =
            donatedMsg(context.getString(donatedMsgRes))

        fun errorMsg(errorMsg: CharSequence?): Builder {
            this.errorMsg = errorMsg
            return this
        }

        fun errorMsgRes(errorMsgRes: Int) =
            errorMsg(context.getString(errorMsgRes))

        fun canceledMsg(canceledMsg: CharSequence?): Builder {
            this.canceledMsg = canceledMsg
            return this
        }

        fun canceledMsgRes(canceledMsgRes: Int) =
            canceledMsg(context.getString(canceledMsgRes))

        fun addSkus(vararg skus: String): Builder {
            this.skus.addAll(skus)
            return this
        }

        fun addSkus(skus: Collection<String>): Builder {
            this.skus.addAll(skus)
            return this
        }

        fun sortOrder(sortOrder: Int): Builder {
            this.sortOrder = sortOrder
            return this
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
                    putInt(KEY_SORT_ORDER, sortOrder)
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
        private val dialog: MaterialDonationsDialog,
        private val appName: String
    ) : TypedEpoxyController<List<SkuDetails>>() {
        override fun buildModels(data: List<SkuDetails>) {
            data
                .distinctBy { it.sku }
                .forEach {
                    DonationItemModel(it, dialog, appName)
                        .addTo(this)
                }
        }
    }

    private class DonationItemModel(
        private val sku: SkuDetails,
        private val dialog: MaterialDonationsDialog,
        private val appName: String
    ) : EpoxyModelWithHolder<DonationItemModel.Holder>() {

        init {
            id(sku.sku)
        }

        override fun bind(holder: Holder) {
            super.bind(holder)
            with(holder) {
                title.text = sku.title.replace(" ($appName)", "")
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
    }
}