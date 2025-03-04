package com.example.individualassignment_53

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.READ_BLOCKING
import android.media.AudioRecord.READ_NON_BLOCKING
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.individualassignment_53.ui.theme.IndividualAssignment_53Theme
import kotlinx.coroutines.delay
import java.nio.ShortBuffer
import kotlin.math.log10
import kotlin.math.sqrt

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Much of this setup and permissions-checking I found online or
        //was directly fed the code for by Android Studio itself.
        //Loosely, I understand that I have added audio recording in the
        //manifest and also need to ask permission to use it. However,
        //I admit that I don't know specifically what everything here is doing.
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    setContent {
                        IndividualAssignment_53Theme {
                            MakeScreen()
                        }
                    }
                }
            }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            setContent {
                IndividualAssignment_53Theme {
                    MakeScreen()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

//a suspendable function to collect the audio data
suspend fun getAudio(decibel: MutableState<Float>){
    val sampleRate = 44100  //standard sampling rate
    val channelConfig = AudioFormat.CHANNEL_IN_MONO //universally accepted audio-in channel
    val format = AudioFormat.ENCODING_PCM_16BIT     //16 bit encoding format
    val bufferSize = AudioRecord.getMinBufferSize(   //input buffer size, calculated to be the minimum needed for these settings
        44100,
        channelConfig,
        format
    )
    var mic = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        channelConfig,
        format,
        bufferSize
    )
    mic.startRecording()
    //an array to hold the input values
    val inArray = ShortArray(bufferSize)
    //continually check the audio input for new data
    while(true) {
        val read = mic.read(inArray, 0, bufferSize, READ_NON_BLOCKING)
        //as long as something was read, process it
        if (read > 0) {
            var sum = 0.0
            for (i in 0 until bufferSize) {
                sum += inArray[i] * inArray[i]
            }
            val rms = sqrt(inArray.map {it.toDouble() * it.toDouble()}.sum() / read)
            decibel.value = Math.max((20 * log10(rms.coerceAtLeast(0.0000001))).toFloat(), 0f)
        /*
        I had a big problem with the mic cutting off and not reading anymore. Not totally sure what
        caused it but I believe this solved the problem. If nothing is read, I assume the mic has
        dropped for whatever reason. I release the current mic and remake it with the same settings.
         */
        } else {
            decibel.value = 0f
            mic.release()
            delay(500)
            mic = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                format,
                bufferSize
            )
            mic.startRecording()
        }
        delay(100)
    }
}

//function to make the main screen
@Composable
fun MakeScreen(){
    //stores the decibel value of the input
    val decibel = rememberSaveable { mutableStateOf(0f) }

    //start checking for audio in coroutine
    LaunchedEffect(Unit) {
        getAudio(decibel)
    }
    Scaffold{innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val maxDec = 40 //the "too loud" volume.
            Text(
                text = String.format("Decibel level: %.0fdb", decibel.value),
                fontSize = 24.sp,
            )
            //warning to keep the noise down. Is invisible when the noise is below the max threshold.
            Text(
                text = "Too loud! Try to keep it below $maxDec db.",
                fontSize = 20.sp,
                fontStyle = FontStyle.Italic,
                color = if(decibel.value > maxDec)
                    androidx.compose.ui.graphics.Color.Red
                    else
                    androidx.compose.ui.graphics.Color.Transparent
            )

            //Show the color boxes are insivisble if the volume is below their threshold.
            //As the volume rises, the boxes sequentially appear. It looks like a colored bar
            //rising and falling with the sound.
            val volumeIncrement = maxDec/5
            val color0 = if(decibel.value > 0)
                Color(0xFF29C335)
                else androidx.compose.ui.graphics.Color.Transparent
            val color1 = if(decibel.value > volumeIncrement)
                Color(0xFF97C329)
                else androidx.compose.ui.graphics.Color.Transparent
            val color2 = if(decibel.value > volumeIncrement*2)
                Color(0xFFB7C329)
                else androidx.compose.ui.graphics.Color.Transparent
            val color3 = if(decibel.value > volumeIncrement*3)
                Color(0xFFC3A529)
                else androidx.compose.ui.graphics.Color.Transparent
            val color4 = if(decibel.value > volumeIncrement*4)
                Color(0xFFE1A118)
                else androidx.compose.ui.graphics.Color.Transparent
            val color5 = if(decibel.value > maxDec)
                Color(0xFFFF0000)
                else androidx.compose.ui.graphics.Color.Transparent
            Canvas(
                modifier = Modifier
                    //.fillMaxWidth()
                    .aspectRatio(1f)
                    .weight(1f)
            ) {
                val centerX = (size.width - 400) / 2

                drawRect(
                    color = color5,
                    size = Size(400f, 150f),
                    topLeft = Offset(centerX, 0f),
                )
                drawRect(
                    color = color4,
                    size = Size(400f, 150f),
                    topLeft = Offset(x=centerX, y=150f)
                )
                drawRect(
                    color = color3,
                    size = Size(400f, 150f),
                    topLeft = Offset(x=centerX, y=300f)
                )
                drawRect(
                    color = color2,
                    size = Size(400f, 150f),
                    topLeft = Offset(x=centerX, y=450f)
                )
                drawRect(
                    color = color1,
                    size = Size(400f, 150f),
                    topLeft = Offset(x=centerX, y=600f)
                )
                drawRect(
                    color = color0,
                    size = Size(400f, 150f),
                    topLeft = Offset(x=centerX, y=750f)
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IndividualAssignment_53Theme {

    }
}