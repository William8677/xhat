/*
 * Updated: 2025-01-21 19:30:12
 * Author: William8677
 */

package com.williamfq.xhat.domain.model

sealed class GameState {
    object NotStarted : GameState()
    object WaitingForOpponent : GameState()
    data class InProgress(
        val gameId: String,
        val players: List<GamePlayer>,
        val currentTurn: String,
        val board: GameBoard,
        val startTime: Long = System.currentTimeMillis()
    ) : GameState()
    data class Finished(
        val winnerId: String?,
        val reason: GameEndReason
    ) : GameState()
    data class Error(val message: String) : GameState()
}

data class GamePlayer(
    val id: String,
    val name: String,
    val avatar: String?,
    val score: Int = 0,
    val isReady: Boolean = false
)

data class GameBoard(
    val cells: List<GameCell>,
    val size: Int,
    val validMoves: List<Position> = emptyList()
)

data class GameCell(
    val position: Position,
    val piece: GamePiece?,
    val isHighlighted: Boolean = false
)

data class Position(
    val row: Int,
    val col: Int
) {
    fun isValid(boardSize: Int): Boolean =
        row in 0 until boardSize && col in 0 until boardSize
}

sealed class GamePiece(
    open val playerId: String,
    open val type: PieceType
) {
    data class CheckersPiece(
        override val playerId: String,
        override val type: PieceType,
        val isKing: Boolean = false
    ) : GamePiece(playerId, type)

    data class ChessPiece(
        override val playerId: String,
        override val type: PieceType,
        val chessPieceType: ChessPieceType
    ) : GamePiece(playerId, type)
}

enum class PieceType {
    NORMAL,
    SPECIAL
}

enum class ChessPieceType {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
}

enum class GameEndReason {
    NORMAL_WIN,
    SURRENDER,
    TIMEOUT,
    DISCONNECT
}

enum class GameType {
    CHECKERS,
    CHESS,
    TIC_TAC_TOE,
    CONNECT_FOUR
}

data class GameMove(
    val gameId: String,
    val playerId: String,
    val from: Position,
    val to: Position,
    val timestamp: Long = System.currentTimeMillis()
)