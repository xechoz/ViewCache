package com.xechoz.app.viewcache

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.xechoz.app.lib.viewcache.TextViewCache

/**
 * @author ZhengJianHui
 * Date：2020/8/30
 * Description:
 */

class DemoTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        Log.d("DemoTextView", "init")
    }
}

class DemoTextViewCache : TextViewCache<DemoTextView>() {
    override fun isMatch(context: Context, name: String?, clazz: Class<out View>?): Boolean {
        return name == DemoTextView::class.java.canonicalName ||
                clazz == DemoTextView::class.java
    }
}