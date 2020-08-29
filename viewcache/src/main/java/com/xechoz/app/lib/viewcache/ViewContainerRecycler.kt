package com.xechoz.app.lib.viewcache

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks

class ActivityRecycler : IViewRecycler {
    override fun onCreate(viewPool: IViewPool) {
        (viewPool.appContext.applicationContext as? Application)?.apply {
            registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks {
                override fun onActivityDestroyed(activity: Activity) {
                    val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
                    viewPool.recycle(rootView)
                }
            })
        }
    }
}

class FragmentRecycler(private val viewPool: IViewPool): IViewRecycler {
    private val fragmentCallback = object : FragmentLifecycleCallbacks() {
        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            super.onFragmentViewDestroyed(fm, f)

            (f.view as? ViewGroup)?.let {
                viewPool.recycle(it)
            }
        }
    }
    override fun onCreate(viewPool: IViewPool) {
        (viewPool.appContext.applicationContext as? Application)?.apply {
            registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    (activity as? FragmentActivity)?.apply {
                        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallback, true)
                    }
                }

                override fun onActivityDestroyed(activity: Activity) {
                    super.onActivityDestroyed(activity)

                    (activity as? FragmentActivity)?.supportFragmentManager?.unregisterFragmentLifecycleCallbacks(fragmentCallback)
                }
            })
        }
    }
}

class ApplicationRecycler : IViewRecycler {
    override fun onCreate(viewPool: IViewPool) {
        viewPool.appContext.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onLowMemory() {
                viewPool.onTrimMemory()
            }

            override fun onConfigurationChanged(newConfig: Configuration) {
            }

            override fun onTrimMemory(level: Int) {
                when (level) {
                    ComponentCallbacks2.TRIM_MEMORY_COMPLETE, ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
                    ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW, ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                        viewPool.onTrimMemory()
                    }
                    else -> {

                    }
                }
            }
        })
    }
}

interface SimpleActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
    }
}