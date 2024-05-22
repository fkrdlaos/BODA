package org.techtown.boda

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.techtown.boda.databinding.ActivityCatchBinding

/**
 * Main entry point into our app. This app follows the single-activity pattern, and all
 * functionality is implemented in the form of fragments.
 */
class CatchActivity : AppCompatActivity() {

    private lateinit var activityCatchBinding: ActivityCatchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCatchBinding = ActivityCatchBinding.inflate(layoutInflater)
        setContentView(activityCatchBinding.root)
        val word = intent.getStringExtra("word")
        if (savedInstanceState == null) {
            val fragment = CatchFragment()
            val bundle = Bundle()
            bundle.putString("word", word)
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }

    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            Log.i("finish", "version error")

            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }
}
