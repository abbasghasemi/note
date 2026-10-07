package ghasemi.abbas.note.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ghasemi.abbas.note.R
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var time = System.currentTimeMillis()/1000

    private fun updateResources(context: Context, language: String): Context? {
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateResources(newBase, "fa"))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.statusBarColor))
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val root = findViewById<android.view.View>(R.id.main_root)
        val navHostView = findViewById<android.view.View>(R.id.nav_host_fragment)
        root.post {
            val actionBar = findViewById<android.view.View>(androidx.appcompat.R.id.action_bar_container)
            if (actionBar != null) {
                val barPosition = IntArray(2)
                val rootPosition = IntArray(2)
                actionBar.getLocationOnScreen(barPosition)
                root.getLocationOnScreen(rootPosition)
                val top = (barPosition[1] + actionBar.height - rootPosition[1]).coerceAtLeast(0)
                val params = navHostView.layoutParams as ConstraintLayout.LayoutParams
                if (params.topMargin != top) {
                    params.topMargin = top
                    navHostView.layoutParams = params
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val bottom = maxOf(
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom,
                insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            )
            val params = navHostView.layoutParams as ConstraintLayout.LayoutParams
            if (params.bottomMargin != bottom) {
                params.bottomMargin = bottom
                navHostView.layoutParams = params
            }
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun finishRequired(): Boolean = navController.currentDestination?.id != R.id.notesFragment

    override fun onBackPressed() {
        if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            super.onBackPressed()
            return
        }
        if (finishRequired())  {
            navController.popBackStack()
            return
        }
        val now = System.currentTimeMillis() / 1000
        if (now - time > 1) {
            time = now
            return
        }
        super.onBackPressed()
    }
}
