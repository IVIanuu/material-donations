package com.ivianuu.materialdonations

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * Builder for a [MaterialDonationsDialog]
 */
class MaterialDonationsDialogBuilder @PublishedApi internal constructor(private val context: Context) {

    private var title: String? = null
    private var negativeButtonText: String? = null
    private var donatedMsg: String? = null
    private var errorMsg: String? = null
    private var canceledMsg: String? = null
    private var sortOrder = MaterialDonationsDialog.SortOrder.NONE
    private var consume = true

    private val skus = mutableSetOf<String>()

    fun title(title: String): MaterialDonationsDialogBuilder = apply {
        this.title = title
    }

    fun title(titleRes: Int): MaterialDonationsDialogBuilder =
        title(context.getString(titleRes))

    fun negativeButtonText(negativeButtonText: String): MaterialDonationsDialogBuilder = apply {
        this.negativeButtonText = negativeButtonText
    }

    fun negativeButtonText(negativeButtonTextRes: Int): MaterialDonationsDialogBuilder =
        negativeButtonText(context.getString(negativeButtonTextRes))

    fun donatedMsg(donatedMsg: String?): MaterialDonationsDialogBuilder = apply {
        this.donatedMsg = donatedMsg
    }

    fun donatedMsg(donatedMsgRes: Int): MaterialDonationsDialogBuilder =
        donatedMsg(context.getString(donatedMsgRes))

    fun errorMsg(errorMsg: String?): MaterialDonationsDialogBuilder = apply {
        this.errorMsg = errorMsg
    }

    fun errorMsg(errorMsgRes: Int): MaterialDonationsDialogBuilder =
        errorMsg(context.getString(errorMsgRes))

    fun canceledMsg(canceledMsg: String?): MaterialDonationsDialogBuilder = apply {
        this.canceledMsg = canceledMsg
    }

    fun canceledMsg(canceledMsgRes: Int): MaterialDonationsDialogBuilder =
        canceledMsg(context.getString(canceledMsgRes))

    fun addSkus(vararg skus: String): MaterialDonationsDialogBuilder = apply {
        this.skus.addAll(skus)
    }

    fun addSkus(skus: Iterable<String>): MaterialDonationsDialogBuilder = apply {
        this.skus.addAll(skus)
    }

    fun sortOrder(sortOrder: MaterialDonationsDialog.SortOrder): MaterialDonationsDialogBuilder =
        apply {
        this.sortOrder = sortOrder
    }

    fun consume(consume: Boolean): MaterialDonationsDialogBuilder = apply {
        this.consume = consume
    }

    fun create(): MaterialDonationsDialog {
        return MaterialDonationsDialog.create(
            MaterialDonationsDialog.Args(
                title,
                negativeButtonText,
                donatedMsg,
                errorMsg,
                canceledMsg,
                skus,
                sortOrder,
                consume
            )
        )
    }

    fun show(fm: FragmentManager): MaterialDonationsDialog {
        val dialog = create()
        dialog.show(fm, MaterialDonationsDialog.FRAGMENT_TAG)
        return dialog
    }
}

inline fun FragmentActivity.donationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit): MaterialDonationsDialog {
    return MaterialDonationsDialogBuilder(this)
        .apply(block)
        .create()
}

inline fun FragmentActivity.showDonationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit): MaterialDonationsDialog {
    return MaterialDonationsDialogBuilder(this)
        .apply(block)
        .show(supportFragmentManager)
}

inline fun Fragment.donationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit): MaterialDonationsDialog {
    return MaterialDonationsDialogBuilder(requireContext())
        .apply(block)
        .create()
}

inline fun Fragment.showDonationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit): MaterialDonationsDialog {
    return MaterialDonationsDialogBuilder(requireContext())
        .apply(block)
        .show(childFragmentManager)
}