package com.jonasbina.cardsagainsthumanity

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GameScreenModel(content:String) : ScreenModel{
    private val cards = loadCardPacksFromFile(content)[0]
    private val _state = MutableStateFlow(GameState(
        cardPack = cards,
        whiteCards = cards.white!!.shuffled().take(10),
        blackCard = cards.black!!.shuffled()[0],
        blackCardsAvailable = cards.black,
        whiteCardsAvailable = cards.white,
        roundsDone = 0
    ))
    val state = _state.asStateFlow()
    @Serializable
    data class CardPack(
        val name: String?,
        val official: Boolean?,
        val white: List<WhiteCard>?,
        val black: List<BlackCard>?
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
    fun newRound(){
        val newBlackCard = state.value.blackCardsAvailable.shuffled().first()
        val newWhiteCards = state.value.whiteCardsAvailable.shuffled().take(state.value.selectedWhiteCards.size)
        _state.update {
            it.copy(it.cardPack, it.whiteCards - it.selectedWhiteCards + newWhiteCards, newBlackCard, it.blackCardsAvailable-newBlackCard, it.whiteCardsAvailable-newWhiteCards, it.roundsDone + 1, emptyList())
        }
    }
    fun selectWhiteCard(whiteCard: WhiteCard){
        if (state.value.selectedWhiteCards.contains(whiteCard)){
            return
        }
        if (state.value.selectedWhiteCards.size == state.value.blackCard.pick){
            _state.update {
                it.copy(selectedWhiteCards = emptyList())
            }
        }
        _state.update {
            it.copy(selectedWhiteCards = it.selectedWhiteCards + whiteCard)
        }

    }
    fun reload(){
        val newCards = state.value.whiteCardsAvailable.shuffled().take(10)
        _state.update { it.copy(whiteCards = newCards) }
    }
}

data class GameState(
    val cardPack:GameScreenModel.CardPack,
    val whiteCards:List<GameScreenModel.WhiteCard>,
    val blackCard:GameScreenModel.BlackCard,
    val blackCardsAvailable:List<GameScreenModel.BlackCard>,
    val whiteCardsAvailable:List<GameScreenModel.WhiteCard>,
    val roundsDone:Int,
    val selectedWhiteCards:List<GameScreenModel.WhiteCard> = emptyList()
)
