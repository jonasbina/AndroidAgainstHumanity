package com.jonasbina.cardsagainsthumanity

import android.util.Log
import androidx.compose.animation.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
class SavedJokesScreen(private val content: String, private val savedJokesPath: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current!!
        val model =
            navigator.rememberNavigatorScreenModel { GameScreenModel(content, savedJokesPath) }
        val state by model.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Saved Jokes", fontFamily = inter) },
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
            }
        ) { padding ->
            if (state.savedJokes.isEmpty()) {
                EmptyStateMessage(Modifier.padding(padding))
            } else {
                SavedJokesList(
                    jokes = state.savedJokes.reversed(),
                    onDeleteJoke = { model.removeJoke(it) },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    @Composable
    private fun EmptyStateMessage(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No saved jokes yet.\nSave some jokes to see them here!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = inter
            )
        }
    }

    @Composable
    private fun SavedJokesList(
        jokes: List<SavedJoke>,
        onDeleteJoke: (SavedJoke) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = jokes,
                key = { it.hashCode() }
            ) { joke ->
                SavedJokeCard(
                    joke = joke,
                    onDelete = { onDeleteJoke(joke) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    @Composable
    private fun SavedJokeCard(
        joke: SavedJoke,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var showDeleteDialog by remember { mutableStateOf(false) }
        var expand by remember { mutableStateOf(false) }
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            onClick ={
                Log.e("fuck", joke.filledIn)
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Black card section


                // Filled in text
                Text(
                    text = joke.filledIn,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fontFamily = inter
                )
                AnimatedVisibility(expand) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Surface(
                            color = Color.Black,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = joke.blackCard.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp),
                                fontFamily = inter
                            )
                        }

                        // White cards section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            joke.whiteCards.forEach { whiteCard ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = whiteCard.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp),
                                        fontFamily = inter
                                    )
                                }
                            }
                        }
                    }
                }
                // Delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete joke",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Delete", fontFamily = inter)
                    }

                    TextButton(
                        onClick = { expand = !expand },
                    ) {
                        AnimatedContent(expand, label = "") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    if (it) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand/collapse",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(if (it) "Minimize" else "Expand", fontFamily = inter)
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Joke", fontFamily = inter) },
                text = {
                    Text(
                        "Are you sure you want to delete this saved joke?",
                        fontFamily = inter
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDelete()
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete", fontFamily = inter)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", fontFamily = inter)
                    }
                }
            )
        }
    }
}