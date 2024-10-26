package com.jonasbina.cardsagainsthumanity.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.jonasbina.cardsagainsthumanity.model.GameScreenModel
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okio.Path.Companion.toPath

@Serializable
data class MenuPreferences(
    val packSelectionMode: PackSelectionMode = PackSelectionMode.DEFAULT,
    val selectedPackIndices: List<Int> = listOf(0)
)

enum class PackSelectionMode {
    DEFAULT, OFFICIAL, ALL, CZECH, ITALIAN, CUSTOM
}

class MenuScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current!!
        val context = LocalContext.current
        val model = navigator.rememberNavigatorScreenModel { GameScreenModel(context) }
        val state by model.state.collectAsState()
        val scope = rememberCoroutineScope()

        var packSelectionMode by remember { mutableStateOf(state.packSelectionMode) }
        var selectedPackIndices by remember { mutableStateOf(listOf(0)) }
        var expanded by remember { mutableStateOf(false) }
        val store: KStore<MenuPreferences> =
            remember { storeOf("${context.dataDir}/store.json".toPath()) }
        LaunchedEffect(Unit) {
            store.get()?.let { prefs ->
                selectedPackIndices = prefs.selectedPackIndices
            }
        }

        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        top = padding.calculateTopPadding(),
                        end = 16.dp,
                        bottom = padding.calculateBottomPadding()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cards Against Humanity",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { expanded = !expanded },
//                    onClick = { expanded = !expanded }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {


                            Column {
                                Text(
                                    text = when (packSelectionMode) {
                                        PackSelectionMode.DEFAULT -> "Default Pack"
                                        PackSelectionMode.OFFICIAL -> "Official Packs"
                                        PackSelectionMode.ALL -> "All Packs"
                                        PackSelectionMode.CZECH -> "Czech"
                                        PackSelectionMode.ITALIAN -> "Italian"
                                        PackSelectionMode.CUSTOM -> "Custom Selection"
                                    }
                                )
                                Text(
                                    text = "${state.selectedPackIndices.size} packs selected",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expanded) "Collapse" else "Expand"
                            )
                        }

                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                PackSelectionMode.entries.filter { if (!state.czech&&!state.italian) it!=PackSelectionMode.CZECH&&it!=PackSelectionMode.ITALIAN else if (!state.czech) it != PackSelectionMode.CZECH else if (!state.italian) it!=PackSelectionMode.ITALIAN else true }.forEach { mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = packSelectionMode == mode,
                                            onClick = {
                                                packSelectionMode = mode
                                                model.setPackSeletionMode(mode)
                                                scope.launch {
                                                    val selectedPacks = when (mode) {
                                                        PackSelectionMode.DEFAULT -> state.cardPreviews.take(
                                                            1
                                                        ).toSet()

                                                        PackSelectionMode.OFFICIAL -> state.cardPreviews.filter { it.official }
                                                            .toSet()

                                                        PackSelectionMode.ALL -> state.cardPreviews.filter { it.isEnglish }
                                                            .toSet()

                                                        PackSelectionMode.CZECH -> state.cardPreviews.filter { it.name == "Czech" }
                                                            .toSet()
                                                        PackSelectionMode.ITALIAN -> state.cardPreviews.filter { it.name == "Italian" }
                                                            .toSet()
                                                        PackSelectionMode.CUSTOM -> if (selectedPackIndices.isNotEmpty()) selectedPackIndices.map { state.cardPreviews[it] }
                                                            .toSet() else state.cardPreviews.take(
                                                            1
                                                        ).toSet()
                                                    }
                                                    model.updateSelectedPacks(selectedPacks.map {
                                                        it.id
                                                    }.toSet())
                                                    store.set(
                                                        MenuPreferences(
                                                            mode,
                                                            selectedPackIndices
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                        Text(
                                            text = when (mode) {
                                                PackSelectionMode.DEFAULT -> "Default Pack Only"
                                                PackSelectionMode.OFFICIAL -> "Official Packs Only"
                                                PackSelectionMode.ALL -> "All Packs (Only english)"
                                                PackSelectionMode.CZECH -> "Czech"
                                                PackSelectionMode.ITALIAN -> "Italian"
                                                PackSelectionMode.CUSTOM -> "Custom Selection"
                                            },
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                        AnimatedVisibility(visible = packSelectionMode == PackSelectionMode.CUSTOM&&mode==PackSelectionMode.CUSTOM) {
                                            TextButton(onClick = {
                                                selectedPackIndices = emptyList()
                                                scope.launch {
                                                    val selectedPacks = state.cardPreviews.take(1).toSet()
                                                    model.updateSelectedPacks(selectedPacks.map { it.id }.toSet())
                                                    store.set(MenuPreferences(packSelectionMode, selectedPackIndices))
                                                }
                                            }) {
                                                Text("Clear", color = Color.Red)
                                            }
                                        }
                                    }
                                }

                                AnimatedVisibility(
                                    visible = packSelectionMode == PackSelectionMode.CUSTOM,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                            .padding(top = 8.dp)
                                    ) {
                                        items(
                                            state.cardPreviews.withIndex().toList()
                                        ) { (index, pack) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = index in selectedPackIndices,
                                                    onCheckedChange = { checked ->
                                                        selectedPackIndices = if (checked) {
                                                            selectedPackIndices + index
                                                        } else {
                                                            if (selectedPackIndices.size > 1) {
                                                                selectedPackIndices - index
                                                            } else {
                                                                emptyList()
                                                            }
                                                        }

                                                        scope.launch {
                                                            val selectedPacks =
                                                                selectedPackIndices.map { state.cardPreviews[it] }
                                                                    .toSet()
                                                            model.updateSelectedPacks(selectedPacks.map{it.id}.toSet())
                                                            store.set(
                                                                MenuPreferences(
                                                                    packSelectionMode,
                                                                    selectedPackIndices
                                                                )
                                                            )
                                                        }
                                                        if (selectedPackIndices.isEmpty()) {
                                                            scope.launch {
                                                                val selectedPacks = state.cardPreviews.take(1).toSet()
                                                                model.updateSelectedPacks(selectedPacks.map { it.id }.toSet())
                                                                store.set(MenuPreferences(packSelectionMode, selectedPackIndices))
                                                            }
                                                        }
                                                    }
                                                )
                                                Text(
                                                    text = pack.name,
                                                    modifier = Modifier.padding(start = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { model.startGame();navigator.push(GameScreen()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Play")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { navigator.push(SavedJokesScreen()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("Saved plays")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(
                        onClick = { navigator.push(SettingsScreen()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Text("More languages")
                    }
                }
            }
        }
    }
}
