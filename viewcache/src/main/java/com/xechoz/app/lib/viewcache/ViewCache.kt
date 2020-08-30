package com.xechoz.app.lib.viewcache

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ViewCache(private val context: Context) {
    private var cacheSize = 16
    private var patcher: IPatcher = EmptyPatcher()
    private var supportView: List<IViewCache<out View>> = listOf(TextViewCache(), ImageViewCache())
    private var recyclerImpl: MutableList<IViewRecycler> = mutableListOf()
    private lateinit var viewPool: ViewPool

    private lateinit var inflater: LayoutInflater
    private lateinit var originInflater: LayoutInflater

    fun wrapperInflater(from: LayoutInflater): LayoutInflater {
        if (!this::inflater.isInitialized) {
            originInflater = from
            this.inflater = from.cloneInContext(context)

            val compat = (context as? AppCompatActivity)?.delegate

            this.inflater.factory2 = object: LayoutInflater.Factory2 {
                override fun onCreateView(
                    parent: View?,
                    name: String,
                    context: Context,
                    attrs: AttributeSet
                ): View? {
                    return viewPool.onCreateView(name, context, attrs) ?: compat?.createView(parent, name, context, attrs)
                }

                override fun onCreateView(
                    name: String,
                    context: Context,
                    attrs: AttributeSet
                ): View? {
                    return viewPool.onCreateView(name, context, attrs) ?: compat?.createView(null, name, context, attrs)
                }
            }
        }

        return inflater
    }

    companion object {
        private const val TAG = "ViewCache"
        private val cache = WeakHashMap<Context, ViewCache>()

        fun of(context: Context): ViewCache {
            return cache.getOrPut(context) {
                Log.d(TAG, "ViewCache create instance, $context")
                ViewCache(context)
            }
        }
    }

    @JvmOverloads
    fun setting(
        cacheSize: Int = 16,
        patcher: IPatcher = EmptyPatcher(),
        supportView: List<IViewCache<out View>> = listOf(TextViewCache(), ImageViewCache()),
        autoRecycleActivity: Boolean = true,
        autoRecycleFragment: Boolean = true
    ): ViewCache {
        Log.d(TAG, "useSetting $cacheSize, $patcher, $supportView, $autoRecycleActivity, $autoRecycleFragment")
        this.cacheSize = cacheSize
        this.patcher = patcher
        this.supportView = supportView
        this.viewPool = ViewPool(context, supportView)
        recyclerImpl.clear()
        recyclerImpl.add(ApplicationRecycler())

        if (autoRecycleActivity) {
            recyclerImpl.add(ActivityRecycler())
        }

        if (autoRecycleFragment) {
            recyclerImpl.add(FragmentRecycler(viewPool))
        }

        recyclerImpl.forEach {
            it.onCreate(viewPool)
        }
        return this
    }
}

interface IPatcher {

}

class EmptyPatcher : IPatcher {
}

class ViewCacheProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.let {
            ViewCache.of(it)
        }
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = -1

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = -1

    override fun getType(uri: Uri): String? = ""
}