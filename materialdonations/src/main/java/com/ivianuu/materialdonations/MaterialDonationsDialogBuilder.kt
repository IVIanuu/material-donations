package com.ivianuu.materialdonations

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * Builder for a [MaterialDonationsDialog]
 */
class MaterialDonationsDialogBuilder internal constructor(private val context: Context) {

    private var title: CharSequence? = null
    private var negativeButtonText: CharSequence? = null
    private var donatedMsg: CharSequence? = null
    private var errorMsg: CharSequence? = null
    private var canceledMsg: CharSequence? = null
    private var sortOrder = MaterialDonationsDialog.SortOrder.NONE
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

    fun sortOrder(sortOrder: MaterialDonationsDialog.SortOrder) = apply {
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
                putCharSequence(
                    MaterialDonationsDialog.KEY_TITLE,
                    this@MaterialDonationsDialogBuilder.title
                )
                putCharSequence(
                    MaterialDonationsDialog.KEY_NEGATIVE_BUTTON_TEXT,
                    negativeButtonText
                )
                putCharSequence(MaterialDonationsDialog.KEY_DONATED_MSG, donatedMsg)
                putCharSequence(MaterialDonationsDialog.KEY_ERROR_MSG, errorMsg)
                putCharSequence(MaterialDonationsDialog.KEY_CANCELED_MSG, canceledMsg)
                putStringArrayList(MaterialDonationsDialog.KEY_SKUS, ArrayList(skus))
                putInt(MaterialDonationsDialog.KEY_SORT_ORDER, sortOrder.value)
                putBoolean(MaterialDonationsDialog.KEY_CONSUME, consume)
            }
        }
    }

    fun show(fm: FragmentManager): MaterialDonationsDialog {
        val dialog = create()
        dialog.show(fm, MaterialDonationsDialog.FRAGMENT_TAG)
        return dialog
    }
}

inline fun Context.createDonationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit) =
    MaterialDonationsDialog.newBuilder(this)
        .apply(block)
        .create()

inline fun Context.showDonationsDialog(
    fm: FragmentManager,
    block: MaterialDonationsDialogBuilder.() -> Unit
) = MaterialDonationsDialog.newBuilder(this)
    .apply(block)
    .show(fm)

inline fun FragmentActivity.createDonationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit) =
    MaterialDonationsDialog.newBuilder(this)
        .apply(block)
        .create()

inline fun FragmentActivity.showDonationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit) =
    MaterialDonationsDialog.newBuilder(this)
        .apply(block)
        .show(supportFragmentManager)

inline fun Fragment.createDonationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit) =
    MaterialDonationsDialog.newBuilder(requireContext())
        .apply(block)
        .create()

inline fun Fragment.showDonationsDialog(block: MaterialDonationsDialogBuilder.() -> Unit) =
    MaterialDonationsDialog.newBuilder(requireContext())
        .apply(block)
        .show(childFragmentManager)