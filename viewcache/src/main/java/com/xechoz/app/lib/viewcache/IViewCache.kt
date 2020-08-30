package com.xechoz.app.lib.viewcache

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import java.util.*

private const val TAG = "ViewCache"

interface IViewCache<T : View> {
    fun isMatch(context: Context, name: String?, clazz: Class<out View>?): Boolean
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
        Log.d(TAG, "putCache $this, size ${cache.size}, $view")
    }

    private fun getCache(): T? {
        Log.d(TAG, "getCache $this, size ${cache.size}")
        return if (cache.isEmpty()) null else cache.pop()
    }

    override fun recycle(view: T) {
        clearAttr(view)
        putCache(view)
    }

    private fun clearAttr(view: T) {

        view.layoutParams = ViewGroup.LayoutParams(0, 0)
        view.background = null
        view.clearAnimation()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): T? {
        return getCache()
    }

    override fun clear() {
        Log.d(TAG, "clear")
        cache.clear()
    }
}

