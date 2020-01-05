package com.xechoz.app.lib.viewcache

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.core.view.setPadding

private const val TAG = "TextViewCache"

class TextViewCache : AbsViewCache<TextView>() {
    override fun recycle(view: TextView) {
        super.recycle(view)
        view.apply {
            text = null
            setTextColor(Color.WHITE)
            background = null
            setPadding(0)
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): TextView? {
        return super.onCreateView(name, context, attrs)?.apply {
            val start = System.nanoTime()

            for (index in 0 until attrs.attributeCount) {
                val attrName = attrs.getAttributeName(index)
                val attrValue = attrs.getAttributeValue(index)

                when (attrName) {
                    "text" -> {
                        text = attrValue
                    }
                    "textSize" -> {
                        textSize = string2Sp(attrValue)
                    }
                    "textColor" -> {
                        if (attrValue.startsWith("#")) {
                            setTextColor(
                                Color.parseColor(
                                    attrValue
                                )
                            )
                        } else {
                            setTextColor(
                                context.resources.getColor(
                                    attrs.getAttributeResourceValue(
                                        index,
                                        0
                                    )
                                )
                            )
                        }
                    }
                    "background" -> {
                        if (attrValue.startsWith("#")) {
                            setBackgroundColor(
                                Color.parseColor(
                                    attrValue
                                )
                            )
                        } else {
                            setBackgroundResource(attrs.getAttributeResourceValue(
                                index,
                                0
                            ))
                        }
                    }
                }
            }

            val spend = (System.nanoTime() - start) / 1000_000f
            Log.d(TAG, "onCreateView spend $spend, ${hashCode()}")
        }
    }

    private fun string2Sp(sp: String): Float {
        return sp.substring(0, sp.length - 2).toFloat()
    }
}

