package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
import android.content.res.Resources.Theme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameSettings
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

@Composable
fun HomeScreen(
    vm: GameViewModel = viewModel(), onStartGameButtonClicked: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) {
        if (isLandscape) {
            LandscapeHomeScreen(
                vm, scope, snackBarHostState, onStartGameButtonClicked, Modifier.padding(it)
            )
        } else {
            PortraitHomeScreen(
                vm, scope, snackBarHostState, onStartGameButtonClicked, Modifier.padding(it)
            )
        }
    }
}

@Composable
fun PortraitHomeScreen(
    vm: GameViewModel,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    onStartGameButtonClicked: () -> Unit,
    modifier: Modifier
) {


    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .weight(1F),
            horizontalArrangement = Arrangement.Center
        ) {
            HighScoreText(highScore = vm.highscore.collectAsState())
        }
        val settings = vm.settings.collectAsState().value

        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp)
                .weight(1F)
        ) {
            SettingsText(settings, modifier.weight(1F))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .weight(1F)
                    .fillMaxHeight()
            ) {
                SettingsButton(
                    vm = vm,
                    showSnackBar = { message, duration ->
                        showSnackbar(
                            scope,
                            snackBarHostState,
                            message,
                            duration
                        )
                    })
            }
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .weight(1F),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            GameModeButtonsWithSwiches(
                vm.audioGameMode.collectAsState(),
                vm.visualGameMode.collectAsState(),
                vm::changeAudioMode,
                vm::changeVisualMode
            )


        }



        Row(
            modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {

            StartButton(
                vm.audioGameMode.collectAsState(),
                vm.visualGameMode.collectAsState(),
                showSnackBar = { message, duration ->
                    showSnackbar(
                        scope,
                        snackBarHostState,
                        message,
                        duration
                    )
                },
                vm::startGame,
                onStartGameButtonClicked
            )

        }

    }

}

private fun showSnackbar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String,
    duration: SnackbarDuration
) {
    scope.launch {
        snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Close",
            duration = duration
        )
    }
}


@Composable
fun LandscapeHomeScreen(
    vm: GameViewModel,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    onStartGameButtonClicked: () -> Unit,
    modifier: Modifier
) {

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary)
    ) {

        Column(
            modifier = modifier
                .weight(1F)
                .padding(20.dp)
                .fillMaxSize()
        ) {
            SettingsText(settings = vm.settings.collectAsState().value)
        }

        Column(
            modifier = modifier
                .weight(1F)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SettingsButton(
                vm = vm,
                showSnackBar = { message, duration ->
                    showSnackbar(
                        scope,
                        snackBarHostState,
                        message,
                        duration
                    )
                })
        }


        Column(
            modifier = modifier.weight(3F)
        ) {

            Row(
                modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                HighScoreText(highScore = vm.highscore.collectAsState())
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 100.dp)
                    .padding(top = 50.dp), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GameModeButtonsWithSwiches(
                    vm.audioGameMode.collectAsState(),
                    vm.visualGameMode.collectAsState(),
                    vm::changeAudioMode,
                    vm::changeVisualMode
                )


            }


            Row(
                modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                StartButton(
                    vm.audioGameMode.collectAsState(),
                    vm.visualGameMode.collectAsState(),
                    showSnackBar = { message, duration ->
                        showSnackbar(
                            scope,
                            snackBarHostState,
                            message,
                            duration
                        )
                    },
                    vm::startGame,
                    onStartGameButtonClicked
                )
            }
        }

    }
}

@Composable
fun SettingsText(settings: GameSettings, modifier: Modifier = Modifier) {
    val gridSize = settings.gridSize.toString() + " x " + settings.gridSize
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Current settings", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
        Text(text = "NBack: " + settings.nBack, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
        Text(text = "Events: " + settings.events, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
        Text(text = "Audio Combinations: " + settings.audioCombinations, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
        Text(text = "Grid Size: $gridSize", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)
        Text(text = "Event interval: " + settings.eventInterval / 1000 + "s", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall)

    }
}

@Composable
fun SettingsButton(vm: GameViewModel, showSnackBar: (String, SnackbarDuration) -> Unit) {
    val showDialog = vm.showDialog.collectAsState().value
    val image = painterResource(id = R.drawable.settings)

    Button(
        onClick = { vm.showDialog(true) },
        colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
        Image(painter = image, contentDescription = "Settings", Modifier.size(50.dp))
    }
    SettingsDialog(vm, showDialog = showDialog, onDismissRequest = {
        vm.showDialog(false)
        vm.saveSettings()
        showSnackBar("Saved", SnackbarDuration.Short)

    }

    )
}

@Composable
fun HighScoreText(highScore: State<Int>) {
    Text(
        modifier = Modifier.padding(top = 30.dp),
        text = "Current highscore: ${highScore.value}",
        color = MaterialTheme.colorScheme.onPrimary,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun GameModeButtonsWithSwiches(
    audioGameMode: State<Boolean>,
    visualGameMode: State<Boolean>,
    changeAudioMode: () -> Unit,
    changeVisualMode: () -> Unit,

    ) {
    val audioMode by audioGameMode
    val visualMode by visualGameMode
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = if (audioMode) R.drawable.hearing_24 else R.drawable.hearing_disabled),
            contentDescription = "Sound",
            modifier = Modifier
                .height(48.dp)
                .aspectRatio(3f / 2f)
        )
        Switch(checked = audioMode, onCheckedChange = { changeAudioMode() }, colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer))
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = if (visualMode) R.drawable.visibility else R.drawable.visibility_off),
            contentDescription = "Visual",
            modifier = Modifier
                .height(48.dp)
                .aspectRatio(3f / 2f)
        )
        Switch(checked = visualMode, onCheckedChange = { changeVisualMode() }, colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer
        ))
    }

}

@Composable
fun StartButton(
    audioGameMode: State<Boolean>,
    visualGameMode: State<Boolean>,
    showSnackBar: (String, SnackbarDuration) -> Unit,
    startGame: () -> Unit,
    onStartGameButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audio by audioGameMode
    val visual by visualGameMode
    Button(
        onClick = {
            if (!audio && !visual) {
                showSnackBar("Please choose a game mode!", SnackbarDuration.Short)
            } else {
                startGame()
                onStartGameButtonClicked()
            }
        },
        shape = RectangleShape,
        modifier = modifier
            .padding(20.dp)
            .aspectRatio(2F),
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer)

    ) {
        val image = painterResource(id = R.drawable.play_arrow)
        Image(painter = image, contentDescription = null, modifier = Modifier.fillMaxSize(0.5F))
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface {
        HomeScreen(FakeVM()) {}
    }
}

@Preview(device = Devices.AUTOMOTIVE_1024p, heightDp = 415)
@Composable
fun HomeScreenPreviewLandscape() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface {
        HomeScreen(FakeVM()) {}
    }
}


@Composable
fun SettingsDialog(
    vm: GameViewModel, showDialog: Boolean, onDismissRequest: () -> Unit
) {

    val settings = vm.settings.collectAsState().value

    if (showDialog) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = MaterialTheme.shapes.medium, shadowElevation = 8.dp
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Text(text = "Settings", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    // NBack Setting
                    Text(text = "NBack: " + settings.nBack.toString())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPickerSlider(range = 1..4,
                            selectedNumber = settings.nBack.toFloat(),
                            onNumberSelected = { newValue ->
                                vm.setNBack(newValue)
                            })

                    }
                    // Events
                    Text(text = "Events: " + settings.events.toString())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPickerSlider(range = 10..20,
                            selectedNumber = settings.events.toFloat(),
                            onNumberSelected = { newValue ->
                                vm.setEvents(newValue)
                            })
                    }
                    // Grid size
                    val grid = settings.gridSize.toString() + " x " + settings.gridSize.toString()
                    Text(text = "Grid size: $grid")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPickerSlider(range = 3..5,
                            selectedNumber = settings.gridSize.toFloat(),
                            onNumberSelected = { newValue ->
                                vm.setGridSize(newValue)
                            })
                    }

                    // Audio combinations
                    Text(text = "Audio Combinations:     " + settings.audioCombinations.toString())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPickerSlider(range = 9..18,
                            selectedNumber = settings.audioCombinations.toFloat(),
                            onNumberSelected = { newValue ->
                                vm.setAudioCombinations(newValue)
                            })
                    }

                    // Event timer
                    val timerValue = (settings.eventInterval / 1000)
                    if (timerValue.toInt() == 1) {
                        Text(text = "Timer: $timerValue second")
                    } else {
                        Text(text = "Timer: $timerValue seconds")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPickerSlider(range = 1..4,
                            selectedNumber = (settings.eventInterval.toFloat()) / 1000,
                            onNumberSelected = { newValue ->
                                vm.setEventInterval(newValue)
                            })
                    }
                }

            }
        }
    }
}

@Composable
fun NumberPickerSlider(
    range: ClosedRange<Int>, selectedNumber: Float, onNumberSelected: (Float) -> Unit
) {
    Slider(
        value = selectedNumber,
        onValueChange = onNumberSelected,
        valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
        steps = range.endInclusive - range.start - 1
    )
}






