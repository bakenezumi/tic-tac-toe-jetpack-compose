package com.example.compose_tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose_tictactoe.ui.theme.ComposeTictactoeTheme
import androidx.compose.foundation.lazy.items

sealed class Player(val stringValue: String)
object X : Player("✖")
object O : Player("◯")
typealias Line = Triple<Int, Int, Int>

data class Winner(val player: Player, val line: Line)

@Composable
fun Square(onClick: () -> Unit, value: Player?, isHighlight: Boolean) {
    val color = if (isHighlight) Color.Red else Color.Black
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RectangleShape)
            .background(Color.LightGray)
            .border(width = 2.dp, color = Color.DarkGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = value?.stringValue ?: "", fontSize = 70.sp, color = color)
    }
}

@Composable
fun Board(
    squares: Array<Player?>,
    onClick: (i: Int) -> Unit,
    winLine: Line?
) {
    @Composable
    fun renderSquare(i: Int) {
        Square(
            onClick = { onClick(i) },
            value = squares[i],
            isHighlight = (winLine != null) && winLine.toList().contains(i)
        )
    }

    Column {
        Row {
            renderSquare(0)
            renderSquare(1)
            renderSquare(2)
        }
        Row {
            renderSquare(3)
            renderSquare(4)
            renderSquare(5)
        }
        Row {
            renderSquare(6)
            renderSquare(7)
            renderSquare(8)
        }
    }
}

@Composable
fun Game() {
    val (history, setHistory) = remember { mutableStateOf(listOf(arrayOfNulls<Player?>(9))) }
    val (stepNumber, setStepNumber) = remember { mutableStateOf(0) }
    val (nextPlayer, setNextPlayer) = remember { mutableStateOf<Player>(X) }

    val current = history[stepNumber]
    val winner = calculateWinner(current)

    fun handleClick(i: Int) {
        if (current[i] != null || calculateWinner(current) != null) {
            return
        }
        setNextPlayer(if (nextPlayer == X) O else X)
        val squares = current.clone()
        squares[i] = nextPlayer
        val history_ = history.slice(0..stepNumber)
        setHistory(history_.plusElement(squares))
        setStepNumber(history_.size)
    }

    fun jumpTo(step: Int) {
        setStepNumber(step)
        setNextPlayer(if ((step % 2) == 0) X else O)
    }

    @Composable
    fun moves() {
        val indexes = history.mapIndexed { index, players -> index }
        LazyColumn {
            items(indexes) { move ->
                val desc = if (move != 0) {
                    "Go to move #${move}"
                } else {
                    "Go to game start"
                }
                Text(
                    modifier = Modifier
                        .clickable { jumpTo(move) },
                    text = desc
                )
            }
        }
    }

    val status =
        if (winner != null) "Winner: ${winner.player.stringValue}" else "Next player: ${nextPlayer.stringValue}"

    Row {
        Board(squares = current, onClick = { handleClick(it) }, winLine = winner?.line)
        Column(modifier = Modifier.padding(20.dp, 0.dp, 0.dp, 0.dp)) {
            Text(text = status, fontSize = 20.sp)
            moves()
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTictactoeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Game()
                }
            }
        }
    }
}

fun calculateWinner(squares: Array<Player?>): Winner? {
    val lines = arrayOf(
        Triple(0, 1, 2),
        Triple(3, 4, 5),
        Triple(6, 7, 8),
        Triple(0, 3, 6),
        Triple(1, 4, 7),
        Triple(2, 5, 8),
        Triple(0, 4, 8),
        Triple(2, 4, 6),
    )
    for ((a, b, c) in lines) {
        val player = squares[a]
        if (player != null && player == squares[b] && player == squares[c]) {
            return Winner(player = player, line = Triple(a, b, c))
        }
    }
    return null
}