package org.example.project.tutorScreen

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import org.example.project.tutorScreen.tutorIntro.TutorIntroFragment
class TutorActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                .replace(frameLayout.id, TutorIntroFragment())
                .commit()
        }
    }
}
