package org.example.project.tutorScreen

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.example.project.tutorScreen.tutorIntro.TutorIntroFragment
import org.example.project.utils.Utils.loadFragmentContainer

class TutorActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFragmentContainer(TutorIntroFragment.newInstance(), savedInstanceState)
    }
}
