package com.xechoz.app.viewcache

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xechoz.app.lib.viewcache.ViewCache
import kotlinx.android.synthetic.main.activity_demo.*
import kotlinx.android.synthetic.main.demo_fragment.view.*
import java.util.concurrent.atomic.AtomicInteger

class DemoActivity : AppCompatActivity() {
    companion object {
        var count = AtomicInteger()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ViewCache.of(this).useSetting()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        var last = DemoFragment(count = count.getAndIncrement())

        fun addFragment(fragment: DemoFragment) {
            supportFragmentManager.beginTransaction().apply {
                last = fragment
                replace(R.id.container, last, "tag_" + System.nanoTime())
                commit()
            }
        }

        addFragment(last)
        btn.setOnClickListener {
            supportFragmentManager.beginTransaction().remove(last)
                .commitNow()

            container.postDelayed({
                addFragment(DemoFragment(count = count.getAndIncrement()))
            }, 1000)
        }
    }

    override fun getSystemService(name: String): Any? {
        return if (LAYOUT_INFLATER_SERVICE == name) {
            ViewCache.of(this).wrapperInflater(super.getSystemService(name) as LayoutInflater)
        } else {
            super.getSystemService(name)
        }
    }
}

class DemoFragment(private val count: Int) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            if (count % 2 == 0) R.layout.demo_fragment else R.layout.demo_fragment_2,
            container,
            false
        )
    }
}