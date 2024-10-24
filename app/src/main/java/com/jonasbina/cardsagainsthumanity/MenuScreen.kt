import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.jonasbina.cardsagainsthumanity.GameScreen
import com.jonasbina.cardsagainsthumanity.GameScreenModel
import com.jonasbina.cardsagainsthumanity.SavedJokesScreen
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okio.Path
import okio.Path.Companion.toPath

@Serializable
data class MenuPreferences(
    val packSelectionMode: PackSelectionMode = PackSelectionMode.DEFAULT,
    val selectedPackIndices: List<Int> = listOf(0)
)

enum class PackSelectionMode {
    DEFAULT, OFFICIAL, ALL, CZECH, CUSTOM
}

class MenuScreen(private val content: String, private val storePath: String, private val savedJokesPath: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current!!
        val model = navigator.rememberNavigatorScreenModel { GameScreenModel(content,savedJokesPath) }
        val state by model.state.collectAsState()
        val scope = rememberCoroutineScope()

        var packSelectionMode by remember { mutableStateOf(state.packSelectionMode) }
        var selectedPackIndices by remember { mutableStateOf(listOf(0)) }
        var expanded by remember { mutableStateOf(false) }

        // Load saved preferences
        val store: KStore<MenuPreferences> = remember { storeOf(storePath.toPath()) }
        LaunchedEffect(Unit) {
            store.get()?.let { prefs ->
                selectedPackIndices = prefs.selectedPackIndices
                // Apply saved pack selection
//                val selectedPacks = when (prefs.packSelectionMode) {
//                    PackSelectionMode.DEFAULT -> model.cards.take(1).toSet()
//                    PackSelectionMode.OFFICIAL -> model.cards.filter { it.official == true }.toSet()
//                    PackSelectionMode.ALL -> model.cards.toSet()
//                    PackSelectionMode.CUSTOM -> prefs.selectedPackIndices.map { model.cards[it] }.toSet()
//                }
//                model.updateCards(selectedPacks)
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
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { expanded = !expanded }
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
                                        PackSelectionMode.CUSTOM -> "Custom Selection"
                                    }
                                )
                                Text(
                                    text = "${state.selectedPackIndices.size} packs selected (${state.whiteCardsInPlay.size + state.blackCardsInPlay.size} cards)",
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
                                PackSelectionMode.values().forEach { mode ->
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
                                                        PackSelectionMode.DEFAULT -> model.cardPacks.take(
                                                            1
                                                        ).toSet()
                                                        PackSelectionMode.OFFICIAL -> model.cardPacks.filter { it.official == true }.toSet()
                                                        PackSelectionMode.ALL -> model.cardPacks.filter { it.name!="Czech" }.toSet()
                                                        PackSelectionMode.CZECH -> model.cardPacks.filter { it.name=="Czech" }.toSet()
                                                        PackSelectionMode.CUSTOM -> if (selectedPackIndices.isNotEmpty()) selectedPackIndices.map { model.cardPacks[it] }.toSet() else model.cardPacks.take(
                                                            1
                                                        ).toSet()
                                                    }
                                                    model.updateSelectedPacks(selectedPacks.map { it.black?.firstOrNull()?.pack?:0 }.toSet())
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
                                                PackSelectionMode.ALL -> "All Packs"
                                                PackSelectionMode.CZECH -> "Czech"
                                                PackSelectionMode.CUSTOM -> "Custom Selection"
                                            },
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
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
                                        items(model.cardPacks.withIndex().toList()) { (index, pack) ->
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
                                                                selectedPackIndices
                                                            }
                                                        }
                                                        scope.launch {
                                                            val selectedPacks =
                                                                selectedPackIndices.map { model.cardPacks[it] }.toSet()
                                                            model.updateSelectedPacks(selectedPacks.map { it.black?.firstOrNull()?.pack?:0 }.toSet())
                                                            store.set(
                                                                MenuPreferences(
                                                                    packSelectionMode,
                                                                    selectedPackIndices
                                                                )
                                                            )
                                                        }
                                                    }
                                                )
                                                Text(
                                                    text = pack.name ?: "Pack ${index + 1}",
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
                    onClick = { navigator.push(GameScreen(content, savedJokesPath)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Play")
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton (
                    onClick = { navigator.push(SavedJokesScreen(content, savedJokesPath)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("See saved plays")
                }
            }
        }
    }
}