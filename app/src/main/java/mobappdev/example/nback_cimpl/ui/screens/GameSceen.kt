package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

private const val TAG = "GameScreen"


private fun speak(text: String, textToSpeech: TextToSpeech) {
    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
}

@Composable
fun GameScreen(
    vm: GameViewModel,
    textToSpeech: TextToSpeech
) {


    val gameState by vm.gameState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE


    if (gameState.audioEventValue != -1 && vm.audioGameMode.collectAsState().value) {
        val character: String = gameState.audioEventValue.toString()
        if (vm.playSound.collectAsState().value)
            speak(character, textToSpeech)
    }


    Scaffold {
        if (isLandscape) {
            LandscapeGameScreen(vm, Modifier.padding(it))
        } else {
            PortraitGameScreen(vm, Modifier.padding(it))
        }
    }


}

@Composable
fun LandscapeGameScreen(vm: GameViewModel, modifier: Modifier = Modifier) {
    val gameState = vm.gameState.collectAsState().value
    val rowsAndCols = vm.settings.collectAsState().value.gridSize
    Row(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier
                .weight(3F),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            VisualGrid(
                rowsAndCols = rowsAndCols,
                eventValue = gameState.visualEventValue,
                modifier = Modifier,
                vm.visualGameMode.collectAsState().value
            )
        }

        Column(
            modifier
                .weight(2.5F)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {

            Row(
                modifier = modifier
                    .weight(1F)
                    .padding(top = 30.dp)
            ) {
                ScoreText(score = vm.score.collectAsState().value.toString())
            }

            Row(modifier = modifier) {
                StimuliButtons(
                    vm::checkAudioMatch,
                    vm::checkVisualMatch,
                    vm.positionButtonFeedback.collectAsState().value,
                    vm.soundButtonFeedback.collectAsState().value,
                    vm.visualGameMode.collectAsState().value,
                    vm.audioGameMode.collectAsState().value
                )
            }
        }

    }

}

@Composable
fun ScoreText(score: String) {
    Text(
        text = "Score: $score",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
fun PortraitGameScreen(vm: GameViewModel, modifier: Modifier = Modifier) {

    val gameState = vm.gameState.collectAsState().value
    val rowsAndCols = vm.settings.collectAsState().value.gridSize

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = modifier
                .padding(top = 40.dp)
                .weight(0.2F)
        ) {
            ScoreText(vm.score.collectAsState().value.toString())
        }


        Row(modifier = Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
            Column {
                VisualGrid(
                    rowsAndCols = rowsAndCols,
                    eventValue = gameState.visualEventValue,
                    modifier = Modifier,
                    vm.visualGameMode.collectAsState().value
                )
            }
        }


        Row(verticalAlignment = Alignment.Bottom) {
            StimuliButtons(
                vm::checkAudioMatch,
                vm::checkVisualMatch,
                vm.positionButtonFeedback.collectAsState().value,
                vm.soundButtonFeedback.collectAsState().value,
                vm.visualGameMode.collectAsState().value,
                vm.audioGameMode.collectAsState().value
            )
        }


    }
}

@Composable
fun StimuliButtons(
    onAudioButtonPressed: () -> Unit,
    onVisualButtonPressed: () -> Unit,
    positionFeedback: Int, // If 1 match, if -1 not match, else 0
    soundFeedBack: Int,      // If 1 match, if -1 not match, else 0
    visualGameMode: Boolean,
    audioGameMode: Boolean,
    modifier: Modifier = Modifier
) {
    // Audio Button

    val audioButtonColor = when (soundFeedBack) {
        1 -> Color.Green
        -1 -> Color.Red
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Row {
        Button(
            modifier = Modifier
                .padding(5.dp)
                .weight(1F)
                .aspectRatio(1F),
            onClick = { onAudioButtonPressed() },
            shape = RectangleShape,
            enabled = audioGameMode,
            colors = ButtonDefaults.buttonColors(audioButtonColor, audioButtonColor, Color.Transparent, Color.Transparent)

        ) {
           var image = painterResource(id = R.drawable.hearing_disabled)
            if (audioGameMode){
                image = painterResource(id = R.drawable.hearing_24)
            }
            Image(painter = image, contentDescription = null, modifier = modifier
                .fillMaxSize(0.5F))
        }

        // Visual Button

        val positionButtonColor = when (positionFeedback) {
            1 -> Color.Green
            -1 -> Color.Red
            else -> MaterialTheme.colorScheme.primaryContainer
        }

        Button(
            onClick = { onVisualButtonPressed() },
            modifier = Modifier
                .padding(5.dp)
                .weight(1F)
                .aspectRatio(1F),
            shape = RectangleShape,
            enabled = visualGameMode,
            colors = ButtonDefaults.buttonColors(positionButtonColor, positionButtonColor, Color.Transparent, Color.Transparent)
        ) {
            var image = painterResource(id = R.drawable.visibility_off)
            if (visualGameMode){
                image = painterResource(id = R.drawable.visibility)
            }

            Image(painter = image, contentDescription = null, modifier = modifier
                .fillMaxSize(0.5F))
        }
    }

}

@Composable
fun VisualGrid(
    rowsAndCols: Int,
    eventValue: Int,
    modifier: Modifier = Modifier,
    visualGame: Boolean
) {
    var id = 1     // Id for each box
    Column {
        for (i in 0 until rowsAndCols) {
            Row(
                modifier = modifier
                    .padding(1.dp)
                    .weight(1F, false)

            ) {
                for (j in 0 until rowsAndCols) {
                    Column(
                        modifier = Modifier
                            .padding(1.dp)
                            .weight(1F, false)
                    ) {
                        val currentId = id
                        if (eventValue == currentId && visualGame) {
                            Box(
                                modifier = modifier
                                    .aspectRatio(1F)
                                    .clip(shape = RoundedCornerShape(15.dp))
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        } else {
                            Box(
                                modifier = modifier
                                    .aspectRatio(1F)
                                    .clip(shape = RoundedCornerShape(15.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)

                            )
                        }
                        id++
                    }
                }
            }

        }
    }

}


/*
@Preview
@Composable
fun GameScreenPreview() {
    GameScreen(vm = FakeVM())
}


@Preview(device = Devices.AUTOMOTIVE_1024p, heightDp = 415)
@Composable
fun GameScreenPreviewLandscape() {
    GameScreen(vm = FakeVM()
}
*/