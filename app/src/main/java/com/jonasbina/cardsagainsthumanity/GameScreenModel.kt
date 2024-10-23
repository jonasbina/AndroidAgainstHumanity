package com.jonasbina.cardsagainsthumanity

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.extensions.storeOf
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.Path
import okio.Path.Companion.toPath

class GameScreenModel(content: String, path: String) : ScreenModel {
    val cards = loadCardPacksFromFile(content)
    private val _state = MutableStateFlow(
        GameState(
            cardPacks = cards.take(1).toSet(),
            whiteCards = cards[0].white!!.shuffled().take(10).toSet(),
            blackCard = cards[0].black!!.shuffled()[0],
            blackCardsAvailable = cards[0].black ?: emptySet(),
            whiteCardsAvailable = cards[0].white ?: emptySet(),
            roundsDone = 0
        )
    )
    val kstore: KStore<List<SavedJoke>> = storeOf(path.toPath())
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
        val pick: Int, // how many white cards needed
        val pack: Int
    )

    private fun loadCardPacksFromFile(fileContent: String): List<CardPack> {
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString(fileContent)
    }

    fun newRound() {
        val newBlackCard =
            (state.value.blackCardsAvailable - state.value.blackCard).shuffled().first()
        val newWhiteCards = (state.value.whiteCardsAvailable - state.value.whiteCards).shuffled()
            .take(state.value.selectedWhiteCards.size)
        _state.update {
            it.copy(
                it.cardPacks,
                it.whiteCards - it.selectedWhiteCards + newWhiteCards,
                newBlackCard,
                it.blackCardsAvailable,
                it.whiteCardsAvailable,
                it.roundsDone + 1,
                emptySet(),
                it.savedJokes,
                false
            )
        }
    }

    fun selectWhiteCard(whiteCard: WhiteCard) {
        if (state.value.selectedWhiteCards.contains(whiteCard)) {
            return
        }
        if (state.value.selectedWhiteCards.size == state.value.blackCard.pick) {
            _state.update {
                it.copy(selectedWhiteCards = emptySet())
            }
        }
        _state.update {
            it.copy(selectedWhiteCards = it.selectedWhiteCards + whiteCard)
        }

    }

    fun reload() {
        val newCards = state.value.whiteCardsAvailable.shuffled().take(10).toSet()
        _state.update { it.copy(whiteCards = newCards) }
    }

    fun updateCards(selectedPacks: Set<CardPack>) {
        _state.update {
            it.copy(
                cardPacks = selectedPacks,

                whiteCards = selectedPacks.map { it.white ?: emptySet() }.flatten().shuffled()
                    .take(10).toSet(),
                blackCard = selectedPacks.map { it.black ?: emptySet() }.flatten().shuffled()[0],
                blackCardsAvailable = selectedPacks.map { it.black ?: emptySet() }.flatten()
                    .toSet(),
                whiteCardsAvailable = selectedPacks.map { it.white ?: emptySet() }.flatten()
                    .toSet(),
                roundsDone = 0
            )
        }
    }

    fun addJoke(savedJoke: SavedJoke) = screenModelScope.launch {
        _state.update {
            it.copy(savedJokes = it.savedJokes + savedJoke, saved = true)
        }
        kstore.update {
            (it ?: emptyList()) + savedJoke
        }
    }

    fun removeJoke(savedJoke: SavedJoke) = screenModelScope.launch {
        _state.update {
            it.copy(savedJokes = it.savedJokes - savedJoke, saved = false)
        }
        kstore.update {
            it?.filter { it != savedJoke }
        }
    }

    init {
        screenModelScope.launch {
            val savedJokes = kstore.get()
            if (savedJokes != null) {
                _state.update {
                    it.copy(
                        savedJokes = savedJokes
                    )
                }
            } else {
                kstore.set(emptyList())
            }
        }
    }
}

@Serializable
data class SavedJoke(
    val blackCard: GameScreenModel.BlackCard,
    val whiteCards: Set<GameScreenModel.WhiteCard>,
) {
    val filledIn = generateFilledText(blackCard.text, whiteCards)
}

data class GameState(
    val cardPacks: Set<GameScreenModel.CardPack>,
    val whiteCards: Set<GameScreenModel.WhiteCard>,
    val blackCard: GameScreenModel.BlackCard,
    val blackCardsAvailable: Set<GameScreenModel.BlackCard>,
    val whiteCardsAvailable: Set<GameScreenModel.WhiteCard>,
    val roundsDone: Int,
    val selectedWhiteCards: Set<GameScreenModel.WhiteCard> = emptySet(),
    val savedJokes: List<SavedJoke> = emptyList(),
    val saved: Boolean = false
)
