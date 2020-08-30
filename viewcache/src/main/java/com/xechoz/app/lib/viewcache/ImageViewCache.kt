package com.xechoz.app.lib.viewcache

import android.content.Context
import android.view.View
import android.widget.ImageView

class ImageViewCache : AbsViewCache<ImageView>() {
    override fun isMatch(context: Context, name: String?, clazz: Class<out View>?): Boolean {
        return false
    }
}