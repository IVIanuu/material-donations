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
class MaterialDonationsDialog : DialogFragment() {

    private var callback: Callback? = null

    private val purchasesUpdateListener =
        PurchasesUpdatedListener { responseCode, purchases ->
            callback?.let {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    if (purchases?.any { it.sku == currentDonation } == true) {
                        it.onDonated(currentDonation!!)
                    } else {
                        it.onDonationError()
                    }
                } else {
                    it.onDonationError()
                }
            }
            dismiss()
        }

    private val stateListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(responseCode: Int) {
            if (responseCode != BillingClient.BillingResponse.OK) {
                callback?.onDonationError()
                dismiss()
            }
        }

        override fun onBillingServiceDisconnected() {

        }
    }

    private lateinit var billingClient: BillingClient
    private lateinit var epoxyController: DonationEpoxyController

    private var currentDonation: String? = null

    override fun onAttach(context: Context?) {
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
            .createBillingClient(requireContext(), purchasesUpdateListener)

        billingClient.startConnection(stateListener)

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
        callback?.onDonationCanceled()
        super.onCancel(dialog)
    }

    override fun onDetach() {
        billingClient.endConnection()
        callback = null
        super.onDetach()
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
            callback?.onDonationError()
            dismiss()
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
                    epoxyController.setData(skuDetailsList)
                } else {
                    callback?.onDonationError()
                }
            } else {
                callback?.onDonationError()
                dismiss()
            }
        }
    }

    companion object {
        private const val FRAGMENT_TAG = "MaterialDonationsDialog"

        private const val KEY_TITLE = "title"
        private const val KEY_NEGATIVE_BUTTON_TEXT = "negative_button_text"
        private const val KEY_SKUS = "skus"

        private const val KEY_CURRENT_DONATION = "current_donation"

        fun newBuilder(context: Context) = Builder(context)
    }

    interface Callback {
        fun onDonated(sku: String)
        fun onDonationCanceled()
        fun onDonationError()
    }

    class Builder(private val context: Context) {

        private var title: CharSequence? = null
        private var negativeButtonText: CharSequence? = null

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

        fun addSkus(vararg skus: String): Builder {
            this.skus.addAll(skus)
            return this
        }

        fun addSkus(skus: Collection<String>): Builder {
            this.skus.addAll(skus)
            return this
        }

        fun create(): MaterialDonationsDialog {
            return MaterialDonationsDialog().apply {
                arguments = Bundle().apply {
                    putCharSequence(KEY_TITLE, this@Builder.title)
                    putCharSequence(KEY_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                    putStringArrayList(KEY_SKUS, ArrayList(skus))
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