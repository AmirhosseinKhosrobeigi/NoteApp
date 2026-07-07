package io.github.amirhosseinkhosrobeigi.notes.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import io.github.amirhosseinkhosrobeigi.notes.R


class CustomAlertDialogBuilder(private val context: Context) {

    private var title: String = ""
    private var message: String = ""
    private var iconResId: Int = R.drawable.ic_delete
    private var iconTint: Int = Color.parseColor("#FFBE03")
    private var positiveButtonText: String = "تایید"
    private var negativeButtonText: String = "انصراف"
    private var positiveButtonColor: Int = Color.parseColor("#FF00BCD4")
    private var negativeButtonColor: Int = Color.parseColor("#FFFF5252")
    private var positiveButtonAction: (() -> Unit)? = null
    private var negativeButtonAction: (() -> Unit)? = null
    private var cancelable: Boolean = true

    fun setTitle(title: String): CustomAlertDialogBuilder {
        this.title = title
        return this
    }

    fun setMessage(message: String): CustomAlertDialogBuilder {
        this.message = message
        return this
    }

    fun setIcon(iconResId: Int): CustomAlertDialogBuilder {
        this.iconResId = iconResId
        return this
    }

    fun setIconTint(color: Int): CustomAlertDialogBuilder {
        this.iconTint = color
        return this
    }

    fun setPositiveButton(
        text: String = "تایید",
        color: Int = Color.parseColor("#FF00BCD4"),
        action: () -> Unit
    ): CustomAlertDialogBuilder {
        this.positiveButtonText = text
        this.positiveButtonColor = color
        this.positiveButtonAction = action
        return this
    }

    fun setNegativeButton(
        text: String = "انصراف",
        color: Int = Color.parseColor("#FFFF5252"),
        action: () -> Unit
    ): CustomAlertDialogBuilder {
        this.negativeButtonText = text
        this.negativeButtonColor = color
        this.negativeButtonAction = action
        return this
    }

    fun setCancelable(cancelable: Boolean): CustomAlertDialogBuilder {
        this.cancelable = cancelable
        return this
    }

    fun build(): Dialog {
        val dialog = Dialog(context, R.style.CustomAlertDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(cancelable)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_alert, null)
        dialog.setContentView(view)

        val iconView = view.findViewById<ImageView>(R.id.dialogIcon)
        iconView.setImageResource(iconResId)
        iconView.setColorFilter(iconTint)

        val titleView = view.findViewById<TextView>(R.id.dialogTitle)
        titleView.text = title

        val messageView = view.findViewById<TextView>(R.id.dialogMessage)
        messageView.text = message

        val positiveButton = view.findViewById<Button>(R.id.btnPositive)
        positiveButton.text = positiveButtonText
        positiveButton.setTextColor(positiveButtonColor)
        positiveButton.setOnClickListener {
            positiveButtonAction?.invoke()
            dialog.dismiss()
        }

        val negativeButton = view.findViewById<Button>(R.id.btnNegative)
        negativeButton.text = negativeButtonText
        negativeButton.setTextColor(negativeButtonColor)
        negativeButton.setOnClickListener {
            negativeButtonAction?.invoke()
            dialog.dismiss()
        }

        return dialog
    }

    fun show(): Dialog {
        val dialog = build()
        dialog.show()
        return dialog
    }
}

fun Context.showCustomAlert(
    title: String,
    message: String,
    iconResId: Int = R.drawable.ic_delete,
    positiveAction: () -> Unit = {},
    negativeAction: () -> Unit = {},
    positiveText: String = "تایید",
    negativeText: String = "انصراف"
): Dialog {
    return CustomAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setIcon(iconResId)
        .setPositiveButton(positiveText) { positiveAction() }
        .setNegativeButton(negativeText) { negativeAction() }
        .show()
}
