package com.jonasbina.cardsagainsthumanity.model

import android.content.Context
import com.jonasbina.cardsagainsthumanity.screen.PackSelectionMode
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.jonasbina.cardsagainsthumanity.R
import com.jonasbina.cardsagainsthumanity.screen.generateFilledText
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
@Serializable
data class SavedJoke(
    val blackCard: GameScreenModel.BlackCard,
    val whiteCards: Set<GameScreenModel.WhiteCard>,
) {
    val filledIn = generateFilledText(blackCard.text, whiteCards)
}

class GameScreenModel(context: Context) : ScreenModel {
    // Keep cards as a private property, not in state
    private val cardDecks = loadCardPacksFromFile(context.resources.openRawResource(R.raw.cahfull).readBytes().decodeToString())
    val kstore: KStore<List<SavedJoke>> = storeOf("${context.dataDir}/jokes.json".toPath())

    private val _state = MutableStateFlow(
        GameState(
            // Store minimal initial state
            selectedPackIndices = setOf(0),
            currentWhiteCards = emptySet(),
            currentBlackCard = null,
            blackCardsInPlay = emptySet(),
            whiteCardsInPlay = emptySet(),
            roundsDone = 0,
            packSelectionMode = PackSelectionMode.DEFAULT
        )
    )
    val state = _state.asStateFlow()

    @Serializable
    data class CardPack(
        val name: String?,
        val official: Boolean?,
        val white: Set<WhiteCard>?,
        val black: Set<BlackCard>?
    )

    @Serializable
    data class WhiteCard(
        val text: String,
        val pack: Int
    )

    @Serializable
    data class BlackCard(
        val text: String,
        val pick: Int,
        val pack: Int
    )

    data class GameState(
        val selectedPackIndices: Set<Int>,
        val currentWhiteCards: Set<WhiteCard>,
        val currentBlackCard: BlackCard?,
        val blackCardsInPlay: Set<BlackCard>,
        val whiteCardsInPlay: Set<WhiteCard>,
        val roundsDone: Int,
        val selectedWhiteCards: Set<WhiteCard> = emptySet(),
        val savedJokes: List<SavedJoke> = emptyList(),
        val saved: Boolean = false,
        val packSelectionMode: PackSelectionMode
    ) {
        // Add computed properties to get available cards based on selected packs
        val availableWhiteCards: Set<WhiteCard> get() = whiteCardsInPlay
        val availableBlackCards: Set<BlackCard> get() = blackCardsInPlay
    }

    init {
        // Initialize the game with first pack
        updateSelectedPacks(setOf(0))

        // Load saved jokes
        screenModelScope.launch {
            val savedJokes = kstore.get()
            if (savedJokes != null) {
                _state.update { it.copy(savedJokes = savedJokes) }
            } else {
                kstore.set(emptyList())
            }
        }
    }
    fun setPackSeletionMode(packSelectionMode: PackSelectionMode){
        _state.update {
            it.copy(packSelectionMode = packSelectionMode)
        }
    }

    fun newRound() {
        val state = state.value
        val newBlackCard = (state.availableBlackCards - (state.currentBlackCard)).random()
        val newWhiteCards = (state.availableWhiteCards - state.currentWhiteCards)
            .shuffled()
            .take(state.selectedWhiteCards.size)
            .toSet()

        _state.update {
            it.copy(
                currentWhiteCards = it.currentWhiteCards - it.selectedWhiteCards + newWhiteCards,
                currentBlackCard = newBlackCard,
                roundsDone = it.roundsDone + 1,
                selectedWhiteCards = emptySet(),
                saved = false
            )
        }
    }

    fun selectWhiteCard(whiteCard: WhiteCard) {
        val state = state.value
        if (state.selectedWhiteCards.contains(whiteCard)) return

        if (state.selectedWhiteCards.size == state.currentBlackCard?.pick ?: 0) {
            _state.update { it.copy(selectedWhiteCards = emptySet()) }
        }

        _state.update {
            it.copy(selectedWhiteCards = it.selectedWhiteCards + whiteCard, saved = false)
        }
    }

    fun unselectWhiteCard(whiteCard: WhiteCard) {
        _state.update {
            it.copy(selectedWhiteCards = it.selectedWhiteCards - whiteCard)
        }
    }

    fun reload() {
        val state = state.value
        val newCards = state.availableWhiteCards.shuffled().take(10).toSet()
        _state.update { it.copy(currentWhiteCards = newCards) }
    }

    fun updateSelectedPacks(selectedIndices: Set<Int>) {
        // Get all cards from selected packs
        val selectedPacks = selectedIndices.map { cardDecks[it] }
        val allWhiteCards = selectedPacks.flatMap { it.white ?: emptySet() }.toSet()
        val allBlackCards = selectedPacks.flatMap { it.black ?: emptySet() }.toSet()

        // Initialize the game state with new cards
        _state.update {
            it.copy(
                selectedPackIndices = selectedIndices,
                currentWhiteCards = allWhiteCards.shuffled().take(10).toSet(),
                currentBlackCard = allBlackCards.random(),
                blackCardsInPlay = allBlackCards,
                whiteCardsInPlay = allWhiteCards,
                roundsDone = 0,
                selectedWhiteCards = emptySet(),
                saved = false
            )
        }
    }

    fun addJoke(savedJoke: SavedJoke) = screenModelScope.launch {
        _state.update {
            it.copy(savedJokes = it.savedJokes + savedJoke, saved = true)
        }
        kstore.update { (it ?: emptyList()) + savedJoke }
    }

    fun removeJoke(savedJoke: SavedJoke) = screenModelScope.launch {
        _state.update {
            it.copy(savedJokes = it.savedJokes - savedJoke, saved = false)
        }
        kstore.update { it?.filter { it != savedJoke } }
    }

    private fun loadCardPacksFromFile(fileContent: String): List<CardPack> {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(fileContent)
    }

    // Add helper property to access all card packs
    val cardPacks: List<CardPack> get() = cardDecks
}