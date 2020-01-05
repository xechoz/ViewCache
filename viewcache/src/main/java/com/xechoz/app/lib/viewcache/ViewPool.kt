package com.xechoz.app.lib.viewcache

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.forEach

internal
class ViewPool(context: Context) : IViewPool {
    override val appContext: Context by lazy {
        context
    }

    private val cacheImpl =
        mapOf<String, IViewCache<View>>(
            TextView::class.java.simpleName to TextViewCache() as IViewCache<View>
        )

    fun recycle(parent: ViewGroup) {
        parent.forEach {
            when (it) {
                is ViewGroup -> {
                    // 1. 保存 children
                    it.children.toList()
                        .apply {
                            // 2. 先remove，避免后续回收view 的操作引起 view tree 的 layout, draw 等
                            it.removeAllViewsInLayout()
                        }
                        .forEach { item ->
                            // 3. 回收 children
                            recycle(item)
                        }

                    recycle(it)
                }
                else -> {
                    recycle(view = it)
                }
            }
        }
    }

    override fun recycle(view: View) {
        cacheImpl[view::class.java.simpleName]?.recycle(view)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return cacheImpl[name]?.onCreateView(name, context, attrs) as View?
    }

    override fun onTrimMemory() {
        cacheImpl.values.forEach {
            it.clear()
        }
    }
}