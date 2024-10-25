package com.jonasbina.cardsagainsthumanity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jonasbina.cardsagainsthumanity.R
import com.jonasbina.cardsagainsthumanity.model.GameScreenModel
import com.jonasbina.cardsagainsthumanity.model.SharedPreferencesManager

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        context.resources.openRawResource(R.raw.cahfull).readBytes().decodeToString()
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.rememberNavigatorScreenModel { GameScreenModel(context) }
        val sharedPreferencesManager = SharedPreferencesManager(context)

        var czech by remember {
            mutableStateOf(sharedPreferencesManager.loadBoolean("czech"))
        }
        var italian by remember {
            mutableStateOf(sharedPreferencesManager.loadBoolean("italian"))
        }
//        var cellular by remember {
//            mutableStateOf(sharedPreferencesManager.loadBoolean("cellular", true))
//        }
//    var workPeriod by remember {
//        mutableIntStateOf(sharedPreferencesManager.loadInt("workPeriod", 15))
//    }
//    var expanded by remember {
//        mutableStateOf(false)
//    }

        sharedPreferencesManager.saveBoolean(italian, "italian")
        sharedPreferencesManager.saveBoolean(czech, "czech")
//        sharedPreferencesManager.saveBoolean(cellular, "cellular")
//    sharedPreferencesManager.saveInt(workPeriod, "workPeriod")
        Scaffold { pp ->


            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = pp.calculateTopPadding(), bottom = 0.dp, start = 20.dp, end = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Settings", fontSize = 25.sp, fontWeight = FontWeight.Bold)

                ShowSettingsOption(
                    checked = czech,
                    onCheckedChange = {
                        sharedPreferencesManager.saveBoolean(
                            it,
                            "czech"
                        ); czech = it
                    },
                    text = "Enable czech pack",
                    note = ""
                )
                ShowSettingsOption(checked = italian, onCheckedChange = {
                    sharedPreferencesManager.saveBoolean(
                        it,
                        "italian"
                    );italian=it
                }, text = "Enable italian pack", note = "")
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ShowSettingsOption(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    note: String
) {
    ElevatedCard(elevation = CardDefaults.cardElevation(30.dp)) {


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(20.dp)
        ) {

            Switch(checked, onCheckedChange)
            Column {
                Text(text = text)
                if (note.isNotEmpty()) {
                    Text(
                        text = note,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 13.sp
                    )
                }
            }


        }
    }
}