package com.jonasbina.cardsagainsthumanity

import MenuScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cafe.adriel.voyager.navigator.Navigator
import com.jonasbina.cardsagainsthumanity.ui.theme.CardsAgainstHumanityTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition{false}
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            var show by remember { mutableStateOf(true) }




            CardsAgainstHumanityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    innerPadding
                    Navigator(MenuScreen(resources.openRawResource(R.raw.cahfull).readBytes().decodeToString(), "$dataDir/store.json","$dataDir/jokes.json") )
                }
            }
        }

    }
}