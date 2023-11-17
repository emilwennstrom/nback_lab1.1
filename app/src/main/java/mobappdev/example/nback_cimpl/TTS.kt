package mobappdev.example.nback_cimpl

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TTS {
    companion object {
        private lateinit var textToSpeech: TextToSpeech
        fun initTTS(context: Context): TextToSpeech {

            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = textToSpeech.setLanguage(Locale.UK)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language is not supported")
                    } else {
                        Log.e("TTS", "Text to speech initialized")
                    }
                } else {
                    Log.e("TTS", "Initialization failed")
                }
            }

            return textToSpeech
        }
    }


}