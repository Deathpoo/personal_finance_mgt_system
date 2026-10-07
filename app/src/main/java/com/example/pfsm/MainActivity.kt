package com.example.pfsm

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.pfsm.ui.theme.design.PFSMTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
                val animatedView = splashScreenViewProvider.iconView ?: splashScreenViewProvider.view

                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.4f)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.4f)
                val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)

                ObjectAnimator.ofPropertyValuesHolder(
                    animatedView, scaleX, scaleY, alpha
                ).apply {
                    interpolator = AnticipateInterpolator()
                    duration = 350L
                    doOnEnd { splashScreenViewProvider.remove() }
                    start()
                }
            }
        }
        enableEdgeToEdge()
        setContent {
            PFSMApp()
        }
    }
}
