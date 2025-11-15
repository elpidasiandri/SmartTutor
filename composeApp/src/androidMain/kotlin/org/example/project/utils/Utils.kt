package org.example.project.utils

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object Utils {
    fun Fragment.navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(
                (view?.parent as? ViewGroup)?.id
                    ?: requireActivity().findViewById<View>(android.R.id.content).id,
                fragment
            )
            .addToBackStack(null)
            .commit()
    }

    fun FragmentActivity.loadFragmentContainer(fragment: Fragment, savedInstanceState: Bundle?) {
        val frameLayout = FrameLayout(this).apply {
            id = View.generateViewId()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        setContentView(frameLayout)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(frameLayout.id, fragment)
                .commit()
        }
    }

}