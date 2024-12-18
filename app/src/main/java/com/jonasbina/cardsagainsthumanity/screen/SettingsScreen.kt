package com.jonasbina.cardsagainsthumanity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    @OptIn(ExperimentalMaterial3Api::class)
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
        var catalan by remember {
            mutableStateOf(sharedPreferencesManager.loadBoolean("catalan"))
        }
        sharedPreferencesManager.saveBoolean(italian, "italian")
        sharedPreferencesManager.saveBoolean(czech, "czech")
        sharedPreferencesManager.saveBoolean(catalan, "catalan")
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("More languages", fontFamily = inter) },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }) { pp ->


            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = pp.calculateTopPadding(), bottom = 0.dp, start = 20.dp, end = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShowSettingsOption(
                    checked = czech,
                    onCheckedChange = {
                        sharedPreferencesManager.saveBoolean(
                            it,
                            "czech"
                        ); czech = it
                        viewModel.setCzech(it)
                    },
                    text = "Enable the Czech pack",
                    note = ""
                )
                Spacer(modifier = Modifier.height(20.dp))
                ShowSettingsOption(checked = italian, onCheckedChange = {
                    sharedPreferencesManager.saveBoolean(
                        it,
                        "italian"
                    );italian=it
                    viewModel.setItalian(it)
                }, text = "Enable the Italian pack", note = "")
                Spacer(modifier = Modifier.height(20.dp))
                ShowSettingsOption(checked = catalan, onCheckedChange = {
                    sharedPreferencesManager.saveBoolean(
                        it,
                        "catalan"
                    );catalan=it
                    viewModel.setCatalan(it)
                }, text = "Enable the Catalan pack", note = "")
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
            modifier = Modifier.padding(20.dp).fillMaxWidth()
        ) {

            Switch(checked, onCheckedChange)
            Column {
                Text(text = text, fontFamily = inter)
                if (note.isNotEmpty()) {
                    Text(
                        text = note,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic,
                        fontFamily = inter,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}