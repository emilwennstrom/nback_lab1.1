package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository
import java.util.LinkedList
import java.util.Queue

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

private const val TAG = "GameVm"

interface GameViewModel {
    val gameState: StateFlow<GameState>
    val settings: StateFlow<GameSettings>

    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val visualGameMode: StateFlow<Boolean>
    val audioGameMode: StateFlow<Boolean>
    val playSound: StateFlow<Boolean>
    val showDialog: StateFlow<Boolean>

    val positionButtonFeedback: StateFlow<Int>
    val soundButtonFeedback: StateFlow<Int>

    fun setGameType(gameType: GameType)
    fun startGame()

    fun checkVisualMatch()
    fun checkAudioMatch()

    fun changeVisualMode()

    fun changeAudioMode()

    fun setNBack(value: Float)

    fun setEvents(value: Float)

    fun setGridSize(value: Float)

    fun setAudioCombinations(value: Float)

    fun setEventInterval(value: Float)

    fun saveSettings()

    fun showDialog(value: Boolean)

}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
) : GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _settings = MutableStateFlow(GameSettings(1, 10, 3, 10))
    override val settings: StateFlow<GameSettings>
        get() = _settings.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val _visualGameMode = MutableStateFlow(true)
    override val visualGameMode: StateFlow<Boolean>
        get() = _visualGameMode.asStateFlow()

    private val _audioGameMode = MutableStateFlow(false)
    override val audioGameMode: StateFlow<Boolean>
        get() = _audioGameMode.asStateFlow()

    private val _playSound = MutableStateFlow(true)
    override val playSound: StateFlow<Boolean>
        get() = _playSound.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    override val showDialog: StateFlow<Boolean>
        get() = _showDialog.asStateFlow()

    private val _positionButtonFeedback = MutableStateFlow(0)
    override val positionButtonFeedback: StateFlow<Int>
        get() = _positionButtonFeedback.asStateFlow()

    private val _soundButtonFeedback = MutableStateFlow(0)
    override val soundButtonFeedback: StateFlow<Int>
        get() = _soundButtonFeedback.asStateFlow()

    private var job: Job? = null  // coroutine job for the game event

    private val resetInterval: Long = 200L  // 200 ms

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var visualEvents = emptyArray<Int>()  // Array with all events
    private var audioEvents = emptyArray<Int>()

    private var visualQueue: Queue<Int> = LinkedList()
    private var audioQueue: Queue<Int> = LinkedList()

    private var canMatchVisual: Boolean = true
    private var canMatchAudio: Boolean = true

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop
        resetGameState()
        val visualCombinations = settings.value.gridSize * settings.value.gridSize
        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> {
                    audioEvents = nBackHelper.generateNBackString(
                        settings.value.events,
                        settings.value.audioCombinations,
                        30,
                        settings.value.nBack
                    ).toList().toTypedArray()
                    runAudioGame(audioEvents)
                }

                GameType.Visual -> {
                    visualEvents = nBackHelper.generateNBackString(
                        settings.value.events,
                        visualCombinations,
                        30,
                        settings.value.nBack
                    ).toList().toTypedArray()
                    runVisualGame(visualEvents)
                }

                GameType.AudioVisual -> {
                    audioEvents = nBackHelper.generateNBackString(
                        settings.value.events,
                        settings.value.audioCombinations,
                        30,
                        settings.value.nBack
                    ).toList().toTypedArray()
                    visualEvents = nBackHelper.generateNBackString(
                        settings.value.events,
                        visualCombinations,
                        30,
                        settings.value.nBack
                    ).toList().toTypedArray()

                    // The case if SRand doesnt reseed (it won't)

                    while (visualEvents.contentEquals(audioEvents)) {
                        audioEvents = nBackHelper.generateNBackString(
                            settings.value.events,
                            settings.value.audioCombinations,
                            30,
                            settings.value.nBack
                        ).toList().toTypedArray()
                    }

                    launch { runAudioGame(audioEvents) }
                    launch { runVisualGame(visualEvents) }

                }

                GameType.None -> {}
            }

            userPreferencesRepository.highscore.collect { highscore ->
                _highscore.value = highscore
                Log.d(TAG, "Current Highscore: $highscore")
                if (highscore < _score.value) {
                    userPreferencesRepository.saveHighScore(_score.value)
                    Log.d(TAG, "Current Highscore: $highscore")
                }

            }


            Log.d(TAG, "Total score: " + _score.value.toString())
        }
    }

    private fun resetGameState() {
        _gameState.value =
            _gameState.value.copy(visualEventValue = -1, matches = 0, audioEventValue = -1)
        _playSound.value = true
        _score.value = 0
    }

    override fun changeVisualMode() {
        _visualGameMode.value = !visualGameMode.value
        changeGameMode()
    }

    override fun changeAudioMode() {
        _audioGameMode.value = !audioGameMode.value
        changeGameMode()
    }

    override fun setNBack(value: Float) {
        _settings.value = _settings.value.copy(nBack = value.toInt())
    }

    override fun setEvents(value: Float) {
        _settings.value = _settings.value.copy(events = value.toInt())
    }

    override fun setGridSize(value: Float) {
        _settings.value = _settings.value.copy(gridSize = value.toInt())
    }

    override fun setAudioCombinations(value: Float) {
        _settings.value = _settings.value.copy(audioCombinations = value.toInt())
    }

    override fun setEventInterval(value: Float) {
        val interval = value.toLong() * 1000
        _settings.value = _settings.value.copy(eventInterval = interval)
    }

    override fun saveSettings() {
        viewModelScope.launch {
            Log.d(TAG, "Settings saved")
            userPreferencesRepository.saveSettings(settings.value)
        }
    }

    override fun showDialog(value: Boolean) {
        _showDialog.value = value
    }

    private fun changeGameMode() {
        if (_visualGameMode.value && _audioGameMode.value) {
            setGameType(GameType.AudioVisual)
        } else if (_visualGameMode.value) {
            setGameType(GameType.Visual)
        } else if (_audioGameMode.value) {
            setGameType(GameType.Audio)
        } else {
            setGameType(GameType.None)
        }
        Log.d(TAG, gameState.value.gameType.name)

    }


    override fun checkVisualMatch() {
        var state = 0
        if (_visualGameMode.value) {
            _playSound.value = false
            if (canMatchVisual && visualQueue.size == settings.value.nBack + 1) {
                if (visualQueue.last() == visualQueue.peek()) {
                    state = 1
                    _gameState.value =
                        _gameState.value.copy(matches = _gameState.value.matches.inc())
                    _score.value += 10
                    Log.d(TAG, "Visual match! $visualQueue")
                } else {
                    state = -1
                    _score.value -= 5
                }
            }
            canMatchVisual = false
            visualButtonChange(state)
        }
    }

    private fun visualButtonChange(state: Int) {
        viewModelScope.launch {
            _positionButtonFeedback.value = state
            delay(settings.value.eventInterval / 4)
            _positionButtonFeedback.value = 0
        }
    }


    override fun checkAudioMatch() {
        if (_audioGameMode.value) {
            var state = 0
            _playSound.value = false
            if (canMatchAudio && audioQueue.size == settings.value.nBack + 1) {
                if (audioQueue.last() == audioQueue.peek()) {
                    state = 1
                    _gameState.value =
                        _gameState.value.copy(matches = _gameState.value.matches.inc())
                    _score.value += 10
                    Log.d(TAG, "Audio match! $audioQueue")
                } else if (_score.value >= 5) {      // Not a match
                    state = -1
                    _score.value -= 5
                }
            }
            canMatchAudio = false
            audioButtonChange(state)
        }

    }

    private fun audioButtonChange(state: Int) {
        viewModelScope.launch {
            _soundButtonFeedback.value = state
            delay(settings.value.eventInterval / 4)
            _soundButtonFeedback.value = 0
        }
    }

    private suspend fun runAudioGame(events: Array<Int>) {
        audioQueue.clear()
        delay(2000)
        for (value in events) {
            _gameState.value = _gameState.value.copy(audioEventValue = value)
            audioQueue.offer(_gameState.value.audioEventValue)
            if (audioQueue.size == settings.value.nBack + 1) {
                _playSound.value = true
                canMatchAudio = true
            }
            delay(settings.value.eventInterval)
            _gameState.value = _gameState.value.copy(audioEventValue = -1)
            delay(resetInterval)
            if (audioQueue.size == settings.value.nBack + 1) {
                audioQueue.poll()
            }
        }
        Log.d(TAG, "No of matches ${_gameState.value.matches}")
    }

    private suspend fun runVisualGame(events: Array<Int>) {
        visualQueue.clear()
        delay(2000)
        for (value in events) {
            _gameState.value = _gameState.value.copy(visualEventValue = value)
            visualQueue.offer(_gameState.value.visualEventValue)
            if (visualQueue.size == settings.value.nBack + 1) {
                canMatchVisual = true
            }
            delay(settings.value.eventInterval)
            _gameState.value = _gameState.value.copy(visualEventValue = -1)
            delay(resetInterval)
            if (visualQueue.size == settings.value.nBack + 1) {
                visualQueue.poll()
            }
        }
        Log.d(TAG, "No of matches ${_gameState.value.matches}")
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        viewModelScope.launch {
            launch {
                userPreferencesRepository.nback.collect {
                    _settings.value = _settings.value.copy(nBack = it)
                }
            }
            launch {
                userPreferencesRepository.events.collect {
                    _settings.value = _settings.value.copy(events = it)
                }
            }
            launch {
                userPreferencesRepository.audioCombinations.collect {
                    _settings.value = _settings.value.copy(audioCombinations = it)
                }
            }
            launch {
                userPreferencesRepository.gridSize.collect {
                    _settings.value = _settings.value.copy(gridSize = it)
                }
            }

            launch {
                userPreferencesRepository.eventInterval.collect {
                    _settings.value = _settings.value.copy(eventInterval = it)
                }
            }


            launch {
                userPreferencesRepository.highscore.collect {
                    _highscore.value = it
                }
            }
        }

    }
}

data class GameSettings(
    val nBack: Int = 1,
    val events: Int = 10,
    val gridSize: Int = 3,
    val audioCombinations: Int = 10,
    val eventInterval: Long = 2000
)

// Class with the different game types
enum class GameType {
    Audio, Visual, AudioVisual, None
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val visualEventValue: Int = -1,  // The value of the array string
    val audioEventValue: Int = -1, val matches: Int = 0
)


class FakeVM : GameViewModel {
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val settings: StateFlow<GameSettings>
        get() = MutableStateFlow(GameSettings(1, 10, 3, 10)).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val visualGameMode: StateFlow<Boolean>
        get() = MutableStateFlow(false).asStateFlow()
    override val audioGameMode: StateFlow<Boolean>
        get() = MutableStateFlow(false).asStateFlow()
    override val playSound: StateFlow<Boolean>
        get() = MutableStateFlow(false).asStateFlow()
    override val showDialog: StateFlow<Boolean>
        get() = MutableStateFlow(false).asStateFlow()
    override val positionButtonFeedback: StateFlow<Int>
        get() = MutableStateFlow(0).asStateFlow()
    override val soundButtonFeedback: StateFlow<Int>
        get() = MutableStateFlow(0).asStateFlow()

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkVisualMatch() {
    }

    override fun checkAudioMatch() {

    }

    override fun changeVisualMode() {

    }

    override fun changeAudioMode() {

    }

    override fun setNBack(value: Float) {

    }

    override fun setEvents(value: Float) {

    }

    override fun setGridSize(value: Float) {

    }

    override fun setAudioCombinations(value: Float) {

    }

    override fun setEventInterval(value: Float) {

    }

    override fun saveSettings() {

    }

    override fun showDialog(value: Boolean) {

    }
}