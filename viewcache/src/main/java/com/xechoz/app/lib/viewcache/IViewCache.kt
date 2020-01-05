package com.xechoz.app.lib.viewcache

import android.app.Application
import android.content.Context
import android.util.AttributeSet
import android.view.View
import java.util.*

private const val TAG = "ViewCache"

interface IViewCache<T : View> {
    fun recycle(view: T)
    fun onCreateView(name: String, context: Context, attrs: AttributeSet): T?
    fun clear()
}

interface IViewPool {
    val appContext: Context
    fun onTrimMemory()
    fun recycle(view: View)
    fun onCreateView(name: String, context: Context, attrs: AttributeSet): View?
}

interface IViewRecycler {
    fun onCreate(viewPool: IViewPool)
}

abstract class AbsViewCache<T : View> : IViewCache<T> {
    private val cache = Stack<T>()

    private fun putCache(view: T) {
        cache.push(view)
    }

    private fun getCache(): T? {
        return if (cache.isEmpty()) null else cache.pop()
    }

    override fun recycle(view: T) {
        putCache(view)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): T? {
        return getCache()
    }

    override fun clear() {
        cache.clear()
    }
}

