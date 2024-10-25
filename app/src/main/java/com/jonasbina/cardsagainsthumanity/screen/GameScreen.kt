package com.jonasbina.cardsagainsthumanity.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.jonasbina.cardsagainsthumanity.model.GameScreenModel
import com.jonasbina.cardsagainsthumanity.R
import com.jonasbina.cardsagainsthumanity.model.SavedJoke
import kotlinx.coroutines.launch

val inter = FontFamily(
    listOf(
        Font(R.font.inter_regular)
    )
)
val lowerCaseEnabled = false

class GameScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current!!
        val context = LocalContext.current
        val model =
            navigator.rememberNavigatorScreenModel { GameScreenModel(context) }
        val state by model.state.collectAsState()
        val scope = rememberCoroutineScope()

        Scaffold { padding ->
            val pd = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding(),
                end = 16.dp,
                bottom = padding.calculateBottomPadding()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(pd)
            ) {
                GameHeader(state.roundsDone)

                Spacer(modifier = Modifier.height(24.dp))
                state.currentBlackCard?.let {
                    AnimatedBlackCard(
                        blackCard = it,
                        selectedCards = state.selectedWhiteCards
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selected white cards section with animations
                AnimatedVisibility(
                    visible = state.selectedWhiteCards.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Selected Cards",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp),
                            fontFamily = inter
                        )
                        state.selectedWhiteCards.forEach { card ->
                            SelectedWhiteCard(card = card){
                                model.unselectWhiteCard(card)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Text(
                    text = "Your Cards",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontFamily = inter
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = state.currentWhiteCards.toList(),
                        key = { it.text }  // Add an id property to WhiteCard class
                    ) { card ->
                        WhiteCardItem(
                            card = card,
                            selected = card in state.selectedWhiteCards,
                            onClick = {
                                scope.launch {
                                    model.selectWhiteCard(card)
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { model.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                    AnimatedVisibility(state.currentBlackCard?.pick == state.selectedWhiteCards.size) {
                        Button(onClick = {
                            if (state.saved) {
                                model.removeJoke(
                                    SavedJoke(
                                        state.currentBlackCard!!,
                                        state.selectedWhiteCards
                                    )
                                )
                            } else {
                                model.addJoke(SavedJoke(state.currentBlackCard!!, state.selectedWhiteCards))
                            }
                        }) {
                            AnimatedContent(state.saved) {
                                Text(
                                    if (it) "Remove from saved" else "Save",
                                    fontFamily = inter
                                )
                            }
                        }
                    }

                }


                AnimatedNextRoundButton(
                    enabled = state.selectedWhiteCards.size == state.currentBlackCard!!.pick,
                    onClick = { model.newRound() }
                )
            }
        }
    }

    @Composable
    private fun GameHeader(roundsDone: Int) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cards Against Humanity",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = inter
            )
            AnimatedCounter(count = roundsDone + 1)
        }
    }

    @Composable
    private fun AnimatedCounter(count: Int) {
        var oldCount by remember { mutableStateOf(count) }
        val animatedCount by animateIntAsState(
            targetValue = count,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        LaunchedEffect(count) {
            oldCount = count
        }

        Text(
            text = "Round $animatedCount",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = inter
        )
    }

    @Composable
    private fun AnimatedBlackCard(
        blackCard: GameScreenModel.BlackCard,
        selectedCards: Set<GameScreenModel.WhiteCard>
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                val filledText = remember(blackCard.text, selectedCards) {
                    generateFilledText(blackCard.text, selectedCards)
                }

                Text(
                    text = filledText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Pick ${blackCard.pick} card${if (blackCard.pick > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontFamily = inter
                )
            }
        }
    }

    @Composable
    private fun SelectedWhiteCard(card: GameScreenModel.WhiteCard, onClick: () -> Unit) {
        var isVisible by remember { mutableStateOf(false) }
        val cornerRadius by animateDpAsState(
            targetValue = if (isVisible) 12.dp else 0.dp,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )

        LaunchedEffect(Unit) {
            isVisible = true
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + expandHorizontally() + slideInHorizontally(),
            exit = fadeOut() + shrinkHorizontally() + slideOutHorizontally()
        ) {
            WhiteCardItem(
                card = card,
                selected = true,
                onClick = onClick,
                cornerRadius = cornerRadius
            )
        }
    }

    @Composable
    private fun WhiteCardItem(
        card: GameScreenModel.WhiteCard,
        selected: Boolean,
        onClick: () -> Unit,
        cornerRadius: Dp = 12.dp,
        modifier: Modifier = Modifier
    ) {
        val scale by animateFloatAsState(
            targetValue = if (selected) 1.05f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Card(
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(cornerRadius))
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = if (selected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (selected) 8.dp else 4.dp
            ),
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.text,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    fontFamily = inter
                )
            }
        }
    }

    @Composable
    private fun AnimatedNextRoundButton(
        enabled: Boolean,
        onClick: () -> Unit
    ) {
        val buttonColor by animateColorAsState(
            targetValue = if (enabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            )
        ) {
            Text(
                "Next Round",
                fontFamily = inter
            )
        }
    }
}

fun generateFilledText(
    originalText: String,
    selectedCards: Set<GameScreenModel.WhiteCard>
): String {
    var filledText = originalText
    selectedCards.forEachIndexed { index, card ->
        var cardText = card.text
        cardText = if (cardText.last() == '.') cardText.dropLast(1) else cardText
        if (!filledText.startsWith("_") && lowerCaseEnabled) {
            cardText = cardText.first().lowercase() + cardText.drop(1)
        }
        filledText = filledText.replaceFirst("_", cardText)
    }
    if (originalText.count { it == '_' } == 0 && selectedCards.size == 1) {
        filledText += (" " + selectedCards.toList()[0].text)
    }
    return filledText
}