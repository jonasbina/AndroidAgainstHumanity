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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
const val lowerCaseEnabled = false

class GameScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current!!
        val context = LocalContext.current
        val model =
            navigator.rememberNavigatorScreenModel { GameScreenModel(context) }
        val state by model.state.collectAsState()
        val scope = rememberCoroutineScope()
        Crossfade(targetState = state.loaded, label = "") { isLoaded ->
            if (isLoaded) {
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
                                selectedCards = state.selectedWhiteCards,
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
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
                                key = { it.text }
                            ) { card ->
                                WhiteCardItem(
                                    card = card,
                                    selected = card in state.selectedWhiteCards,
                                    onClick = {
                                        if (card in state.selectedWhiteCards) {
                                            scope.launch {
                                                model.unselectWhiteCard(card)
                                            }
                                        } else {
                                            scope.launch {
                                                model.selectWhiteCard(card)
                                            }
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
                                        model.addJoke(
                                            SavedJoke(
                                                state.currentBlackCard!!,
                                                state.selectedWhiteCards
                                            )
                                        )
                                    }
                                }) {
                                    AnimatedContent(state.saved, label = "") {
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
            } else {
                SkeletonLoader()
            }
        }
    }

    @Composable
    private fun SkeletonLoader() {
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
                GameHeader(0)

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedBlackCard(
                    blackCard = GameScreenModel.BlackCard(
                        "Hold on a second, we're loading the game for you",
                        1,
                    ),
                    selectedCards = emptySet(),
                )


                Spacer(modifier = Modifier.height(24.dp))
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
                        items = listOf(
                            GameScreenModel.WhiteCard("Loading"),
                            GameScreenModel.WhiteCard("The game"),
                            GameScreenModel.WhiteCard("For you"),
                            GameScreenModel.WhiteCard("Hang"),
                            GameScreenModel.WhiteCard("On"),
                            GameScreenModel.WhiteCard("For a second"),
                            GameScreenModel.WhiteCard("There"),
                            GameScreenModel.WhiteCard("Mate"),
                            GameScreenModel.WhiteCard("Thanks"),
                            GameScreenModel.WhiteCard("For patience.")
                        ),
                        key = { it.text }
                    ) { card ->
                        WhiteCardItem(
                            card = card,
                            selected = false,
                            onClick = {},
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }

                }


                AnimatedNextRoundButton(
                    enabled = false,
                    onClick = { }
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
        var oldCount by remember { mutableIntStateOf(count) }
        val animatedCount by animateIntAsState(
            targetValue = count,
            animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing), label = ""
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
        selectedCards: Set<GameScreenModel.WhiteCard>,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                val filledText =
                    fillIn(blackCard, selectedCards.toList())
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
    private fun WhiteCardItem(
        card: GameScreenModel.WhiteCard,
        selected: Boolean,
        onClick: () -> Unit,
        cornerRadius: Dp = 12.dp,
        modifier: Modifier = Modifier
    ) {
        val scale by animateFloatAsState(
            targetValue = if (selected) 1f else 0.95f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ), label = ""
        )
        val textStyle = MaterialTheme.typography.bodyMedium.let { baseStyle ->
            when {
                card.text.length <= 15 -> baseStyle.copy(fontSize = 18.sp)
                card.text.length <= 30 -> baseStyle.copy(fontSize = 16.sp)
                card.text.length <= 50 -> baseStyle.copy(fontSize = 14.sp)
                else -> baseStyle
            }
        }

        val surface = MaterialTheme.colorScheme.surface
        val primary = MaterialTheme.colorScheme.primaryContainer
        val targetColor = if (selected) primary else surface
        val targetElevation = if (selected) 16.dp else 8.dp
        val animatedColor by animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ), label = ""
        )
        val animatedElevation by animateDpAsState(
            targetValue = targetElevation,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ), label = ""
        )

        val lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        val minCardHeight =
            (lineHeight.value * 2.5).dp

        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(cornerRadius))
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = animatedColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation
            ),
            shape = RoundedCornerShape(cornerRadius),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .defaultMinSize(minHeight = minCardHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.text,
                    style = textStyle,
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
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), label = ""
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

fun fillIn(
    blackCard: GameScreenModel.BlackCard,
    selectedCards: List<GameScreenModel.WhiteCard>
): AnnotatedString {
    return buildAnnotatedString {
        if (blackCard.text.count { it == '_' } == 0 && selectedCards.size == 1) {
            withStyle(style = SpanStyle(color = Color.White)) {
                append(blackCard.text.trimEnd())
                append(" ")
            }
            withStyle(style = SpanStyle(color = Color.Green)) {
                append(selectedCards[0].text)
            }
            return@buildAnnotatedString
        }
        if (blackCard.text.count { it == '_' } == 0 && selectedCards.size > 1) {
            withStyle(style = SpanStyle(color = Color.White)) {
                append(blackCard.text.trimEnd())
                append(" ")
            }
            withStyle(style = SpanStyle(color = Color.Green)) {
                selectedCards.forEachIndexed { index, card ->
                    var cardText = card.text
                    val isLast = index == selectedCards.size - 1
                    cardText =
                        if (cardText.last() == '.' && !isLast) cardText.dropLast(1) else cardText
                    if (!isLast) {
                        cardText += ", "
                    }
                    append(cardText)
                }

            }
            return@buildAnnotatedString
        }

        var tempText = blackCard.text
        selectedCards.forEach { card ->
            var cardText = card.text
            cardText = if (cardText.last() == '.') cardText.dropLast(1) else cardText
            if (!tempText.startsWith("_") && lowerCaseEnabled) {
                cardText = cardText.first().lowercase() + cardText.drop(1)
            }
            val splitText = tempText.split("_", limit = 2)
            if (splitText.isNotEmpty()) {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append(splitText[0])
                }
                withStyle(style = SpanStyle(color = Color.Green)) {
                    append(cardText)
                }
                tempText = if (splitText.size > 1) splitText[1] else ""
            }
        }
        if (tempText.isNotEmpty()) {
            withStyle(style = SpanStyle(color = Color.White)) {
                append(tempText)
            }
        }
    }
}
//                AnimatedVisibility(
//                    visible = state.selectedWhiteCards.isNotEmpty(),
//                    enter = fadeIn() + expandVertically(),
//                    exit = fadeOut() + shrinkVertically()
//                ) {
//                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Text(
//                            text = "Selected Cards",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = MaterialTheme.colorScheme.onBackground,
//                            modifier = Modifier.padding(bottom = 8.dp),
//                            fontFamily = inter
//                        )
//                        state.selectedWhiteCards.forEach { card ->
//                            SelectedWhiteCard(card = card) {
//                                model.unselectWhiteCard(card)
//                            }
//                        }
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//@Composable
//    private fun SelectedWhiteCard(card: GameScreenModel.WhiteCard, onClick: () -> Unit) {
//        var isVisible by remember { mutableStateOf(false) }
//        val cornerRadius by animateDpAsState(
//            targetValue = if (isVisible) 12.dp else 0.dp,
//            animationSpec = tween(
//                durationMillis = 300,
//                easing = FastOutSlowInEasing
//            ), label = ""
//        )
//
//        LaunchedEffect(Unit) {
//            isVisible = true
//        }
//
//        AnimatedVisibility(
//            visible = isVisible,
//            enter = fadeIn() + expandHorizontally() + slideInHorizontally(),
//            exit = fadeOut() + shrinkHorizontally() + slideOutHorizontally()
//        ) {
//            WhiteCardItem(
//                card = card,
//                selected = true,
//                onClick = onClick,
//                cornerRadius = cornerRadius
//            )
//        }
//    }