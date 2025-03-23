package com.williamfq.xhat.utils

import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import android.text.TextWatcher
import android.text.Editable
import com.google.android.material.textfield.TextInputEditText

object BindingAdapters {

    @BindingAdapter("android:text")
    @JvmStatic fun setText(view: TextInputEditText, value: String?) {
        if (view.text.toString() != value) {
            view.setText(value ?: "")
        }
    }

    @InverseBindingAdapter(attribute = "android:text")
    @JvmStatic fun getText(view: TextInputEditText): String {
        return view.text.toString()
    }

    @BindingAdapter("android:textAttrChanged")
    @JvmStatic fun setTextWatcher(view: TextInputEditText, listener: InverseBindingListener?) {
        val newWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                listener?.onChange()
            }
        }

        val oldWatcher = getWatcher(view)
        if (oldWatcher != null) {
            view.removeTextChangedListener(oldWatcher)
        }

        view.addTextChangedListener(newWatcher)
        setWatcher(view, newWatcher)
    }

    private val watchers = mutableMapOf<TextInputEditText, TextWatcher>()

    private fun setWatcher(view: TextInputEditText, watcher: TextWatcher) {
        watchers[view] = watcher
    }

    private fun getWatcher(view: TextInputEditText): TextWatcher? {
        return watchers[view]
    }
}