package com.xechoz.app.lib.viewcache

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.core.view.setPadding

private const val TAG = "ViewCache.TextViewCache"

class TextViewCache<T : TextView> : AbsViewCache<T>() {
   private val attrFun = mapOf<String, (textView: TextView, attr: AttributeSet, index: Int, reset: Boolean) -> Unit>(
        "text" to { textView, attr, index, reset ->
            textView.text = if (reset) "" else attr.getAttributeValue(index)
        },
        "textSize" to { textView, attr, index, reset ->
            textView.textSize = if (reset) 24f else string2Sp(attr.getAttributeValue(index))
        },
        "textColor" to { textView, attr, index, reset ->
            if (!reset) {
                val attrValue = attr.getAttributeValue(index)
                if (attrValue.startsWith("#")) {
                    textView.setTextColor(Color.parseColor(attrValue))
                } else {
                    textView.setTextColor(
                        textView.context.resources.getColor(
                            attr.getAttributeResourceValue(
                                index,
                                0
                            )
                        )
                    )
                }
            } else {

            }
        },
        "background" to { textView, attr, index, reset ->
            if (!reset) {
                val attrValue = attr.getAttributeValue(index)

                if (attrValue.startsWith("#")) {
                    textView.setBackgroundColor(
                        Color.parseColor(
                            attrValue
                        )
                    )
                } else {
                    textView.setBackgroundResource(
                        attr.getAttributeResourceValue(
                            index,
                            0
                        )
                    )
                }
            } else {
                textView.background = null
            }
        },
        "" to { textView, attr, index, reset ->

        }
    )

    private val commonAttr = arrayOf("background", "id", "visible", "padding", "paddingLeft")
    private val allAttr = arrayOf("text", "textSize", "textColor", "textAlign")

    override fun recycle(view: T) {
        super.recycle(view)
        Log.d(TAG, "recycle $view")
        view.apply {
            text = ""
            background = null
            setPadding(0)
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): T? {
        return super.onCreateView(name, context, attrs)?.apply {
            Log.d(TAG, "onCreateView $this")
            val start = System.nanoTime()

            for (index in 0 until attrs.attributeCount) {
                val attrName = attrs.getAttributeName(index)
                attrFun[attrName]?.invoke(this, attrs, index, false)
            }

            // todo 其余的属性，需要clear
            val spend = (System.nanoTime() - start) / 1000_000f
            Log.d(TAG, "onCreateView spend $spend, ${hashCode()}")
        }
    }

    private fun string2Sp(sp: String): Float {
        return sp.substring(0, sp.length - 2).toFloat()
    }
}

