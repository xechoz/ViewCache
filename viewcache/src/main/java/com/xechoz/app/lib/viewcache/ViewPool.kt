package com.xechoz.app.lib.viewcache

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

private const val TAG = "ViewCache.ViewPool"
internal
class ViewPool(context: Context, private val cacheImpl: List<IViewCache<out View>>) : IViewPool {
    override val appContext: Context by lazy {
        context
    }

    private var missCount = 0
    private var hitCount = 0

    private fun doRecycle(parent: ViewGroup) {
        parent.children.toList().forEach {
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

                    doRecycleView(it)
                }
                else -> {
                    doRecycleView(it)
                }
            }
        }
    }

    private fun doRecycleView(view: View) {
        Log.d(TAG, "doRecycleView $view")
        findCacheImpl(view.context, "", view::class.java)?.let {
            (view.parent as? ViewGroup)?.removeView(view)
            (it as IViewCache<View>).recycle(view)
        }
    }

    private fun findCacheImpl(context: Context, name: String?, clazz: Class<out View>?): IViewCache<out View>? {
        return cacheImpl.find { it.isMatch(context, name, clazz) }
    }

    override fun recycle(view: View) {
        if (view is ViewGroup) {
            doRecycle(view)
        } else {
            doRecycleView(view)
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        val instance =  findCacheImpl(context, name, null)?.onCreateView(name, context, attrs)

        if (instance == null) {
            missCount++
            Log.d(TAG, "onCreateView $context, $name, cache miss, $missCount:$hitCount")
        } else {
            Log.d(TAG, "onCreateView $context, $name, cache hit, $missCount:$hitCount")
            hitCount++
        }
        return instance
    }

    override fun onTrimMemory() {
        cacheImpl.forEach { it.clear() }
    }
}