package com.jonasbina.cardsagainsthumanity

import MenuScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.FadeTransition
import com.jonasbina.cardsagainsthumanity.ui.theme.CardsAgainstHumanityTheme

class MainActivity : ComponentActivity() {
    private val viewModel : SplashScreenViewmodel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.isLoading.value }
        }

        enableEdgeToEdge()
        val actualSavedState = null // Explicitly ignore saved state
        super.onCreate(actualSavedState)
        setContent {
            CardsAgainstHumanityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    innerPadding
                    Navigator(MenuScreen(resources.openRawResource(R.raw.cahfull).readBytes().decodeToString(), "$dataDir/store.json","$dataDir/jokes.json")){
                        FadeTransition(it)
                    }
                }
            }
        }

    }
    override fun onSaveInstanceState(outState: Bundle) {
        // Don't save instance state to prevent TransactionTooLargeException
        super.onSaveInstanceState(Bundle())
    }
}
