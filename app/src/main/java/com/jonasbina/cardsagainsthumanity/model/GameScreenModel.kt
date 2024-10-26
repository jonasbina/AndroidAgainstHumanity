package com.jonasbina.cardsagainsthumanity.model

import android.content.Context
import com.jonasbina.cardsagainsthumanity.screen.PackSelectionMode
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.jonasbina.cardsagainsthumanity.R
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.extensions.get
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
)

class GameScreenModel(context: Context) : ScreenModel {
    private val sharedPreferencesManager = SharedPreferencesManager(context)
    private val kstore: KStore<List<SavedJoke>> = storeOf("${context.dataDir}/jokes.json".toPath())
    private val cardPackPreviewStore:KStore<List<CardPackPreview>> = storeOf("${context.dataDir}/cardPackPreviews.json".toPath())
    private val cardsStore: KStore<List<CardPack>> = storeOf("${context.dataDir}/cards.json".toPath())
    private val _state = MutableStateFlow(
        GameState(
            selectedPackIndices = setOf(0),
            currentWhiteCards = emptySet(),
            currentBlackCard = null,
            blackCardsInPlay = emptySet(),
            whiteCardsInPlay = emptySet(),
            roundsDone = 0,
            packSelectionMode = PackSelectionMode.DEFAULT,
            cardPreviews = emptyList()
        )
    )
    val state = _state.asStateFlow()

    @Serializable
    data class CardPack(
        val name: String,
        val official: Boolean,
        val white: Set<WhiteCard>,
        val black: Set<BlackCard>,
        val isEnglish:Boolean,
        val id:Int
    )
    @Serializable
    data class CardPackPreview(
        val name: String,
        val official: Boolean,
        val isEnglish: Boolean,
        val id:Int
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
        val packSelectionMode: PackSelectionMode,
        val cardPreviews:List<CardPackPreview>,
        val italian:Boolean = false,
        val czech:Boolean = false,
        val loaded:Boolean = false
    ) {
        val availableWhiteCards: Set<WhiteCard> get() = whiteCardsInPlay
        val availableBlackCards: Set<BlackCard> get() = blackCardsInPlay
    }

    init {
        screenModelScope.launch {
            if (cardPackPreviewStore.get(0)==null){
                cardPackPreviewStore.set(loadCardPackPreviewsFromFile(context.resources.openRawResource(R.raw.cahfull).readBytes().decodeToString()))
            }
            _state.update {
                it.copy(cardPreviews = cardPackPreviewStore.get()?:it.cardPreviews)
            }
        }
        updateSelectedPacks(setOf(0))
        screenModelScope.launch {
            if (cardsStore.get(0)==null){
                cardsStore.set(loadCardPacksFromFile(context.resources.openRawResource(R.raw.cahfull).readBytes().decodeToString()))
            }
        }
        val czech = sharedPreferencesManager.loadBoolean("czech",false)
        val italian = sharedPreferencesManager.loadBoolean("italian",false)
        _state.update {
            it.copy(czech = czech,italian = italian)
        }
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

        if (state.selectedWhiteCards.size == (state.currentBlackCard?.pick ?: 0)) {
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
        _state.update {
            it.copy(
                selectedPackIndices = selectedIndices,
                roundsDone = 0,
                selectedWhiteCards = emptySet(),
                saved = false
            )
        }
    }
    fun setCzech(czech:Boolean){
        _state.update {
            it.copy(czech = czech)
        }
    }
    fun setItalian(italian:Boolean){
        _state.update {
            it.copy(italian = italian)
        }
    }
    fun startGame() = screenModelScope.launch {
        _state.update {
            it.copy(loaded = false)
        }
        val selectedPacks = state.value.selectedPackIndices.map { cardsStore.get(it) }
        val allWhiteCards = selectedPacks.flatMap { it?.white ?: emptySet() }.toSet()
        val allBlackCards = selectedPacks.flatMap { it?.black ?: emptySet() }.toSet()
        _state.update {
            it.copy(
                currentWhiteCards = allWhiteCards.shuffled().take(10).toSet(),
                currentBlackCard = allBlackCards.random(),
                blackCardsInPlay = allBlackCards,
                whiteCardsInPlay = allWhiteCards,
                roundsDone = 0,
                selectedWhiteCards = emptySet(),
                saved = false,
                loaded = true
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
        kstore.update { savedJokes -> savedJokes?.filter { it != savedJoke } }
    }

    private fun loadCardPacksFromFile(fileContent: String): List<CardPack> {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(fileContent)
    }
    private fun loadCardPackPreviewsFromFile(fileContent: String): List<CardPackPreview> {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(fileContent)
    }
}