package com.xechoz.app.lib.viewcache

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.view.View

class ViewCache(context: Context) {
    private var cacheSize = 16
    private var patcher: IPatcher = EmptyPatcher()
    private var supportView: List<IViewCache<out View>> = listOf(TextViewCache(), ImageViewCache())
    private var recyclerImpl: MutableList<IViewRecycler> = mutableListOf()

    companion object {
        private const val TAG = "ViewCache"
        private lateinit var context: Context

        private val instance: ViewCache by lazy {
            ViewCache(context).apply {
                useSetting()
            }
        }

        fun of(context: Context): ViewCache {
            if (!this::context.isInitialized) {
                Log.d(TAG, "init")
                this.context = context.applicationContext
            }

            return instance
        }
    }

    @JvmOverloads
    fun useSetting(
        cacheSize: Int = 16,
        patcher: IPatcher = EmptyPatcher(),
        supportView: List<IViewCache<out View>> = listOf(TextViewCache(), ImageViewCache()),
        autoRecycleActivity: Boolean = true,
        autoRecycleFragment: Boolean = true
    ) : ViewCache {
        Log.d(TAG, "useSetting $cacheSize, $patcher, $supportView, $autoRecycleActivity, $autoRecycleFragment")
        this.cacheSize = cacheSize
        this.patcher = patcher
        this.supportView = supportView

        recyclerImpl.clear()
        recyclerImpl.add(ApplicationRecycler())

        if (autoRecycleActivity) {
            recyclerImpl.add(ActivityRecycler())
        }

        if (autoRecycleFragment) {
            recyclerImpl.add(FragmentRecycler())
        }

        return this
    }
}

interface IPatcher {

}

class EmptyPatcher : IPatcher {
}

class ViewCacheProvider: ContentProvider() {
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