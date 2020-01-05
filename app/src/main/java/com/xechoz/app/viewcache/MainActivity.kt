package com.xechoz.app.viewcache

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.xechoz.app.lib.viewcache.ViewCache
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewCache = ViewCache
            .of(baseContext).useSetting(cacheSize = 32)

        goDemoBtn.setOnClickListener {
            startActivity(Intent(this, DemoActivity::class.java))
        }
    }
}
