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
import kotlin.math.log10
import kotlin.math.sqrt

class MainActivity : ComponentActivity(){

    private var mic: AudioRecord? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Much of this setup and permissions-checking I found online or
        //was directly fed the code for by Android Studio itself.
        //Loosely, I understand that I have added audio recording in the
        //manifest and also need to ask permission to use it. However,
        //I admit that I don't know specifically what everything here is doing.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), 200
            )
            return
        }
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val format = AudioFormat.ENCODING_PCM_FLOAT
        val bufferSize = AudioRecord.getMinBufferSize(   //input buffer size
            44100,
            channelConfig,
            format
        )
        //val bufferSize = 1024
        mic = AudioRecord(
            MediaRecorder.AudioSource.MIC,  //audioSource
            sampleRate,              //sample rate in Hz
            channelConfig,    //audio channel config; guaranteed to work on all devices
            format, //audio return format
            bufferSize
        )

        setContent {
            IndividualAssignment_53Theme {
                mic?.startRecording()
                MakeScreen(mic, bufferSize)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mic?.startRecording()
    }

    override fun onDestroy() {
        super.onDestroy()
        mic?.release()
    }
}

@Composable
fun MakeScreen(mic: AudioRecord?, bufferSize: Int = 0){
    if(mic == null) return

    var decibel by rememberSaveable { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        val inArray = FloatArray(bufferSize)
        while(true) {
            if(decibel < 0) {
                decibel = 0.0
                mic.startRecording()
            }
            val read = mic.read(inArray, 0, bufferSize, READ_BLOCKING)
            if (read > 0) {
                var sum = 0.0
                for (i in 0 until bufferSize) {
                    sum += inArray[i] * inArray[i]
                }
                val rms = sqrt(sum/bufferSize)
                decibel = 20 * log10(rms.coerceAtLeast(0.000000001)) + 80
            } else {
                decibel = 0.0
            }
            delay(100)
        }
    }
    Scaffold(

    ){innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = String.format("Decibel level: %.0fdb", decibel.toFloat()),
                fontSize = 24.sp,
            )

            if(decibel > 30){
                Text(
                    text = "Too loud! Try to keep it below 30 db.",
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic,
                    color = androidx.compose.ui.graphics.Color.Red
                )
            }

            val color0 = if(decibel > 0)
                Color(0xFF29C335)
                else androidx.compose.ui.graphics.Color.Transparent
            val color1 = if(decibel > 6)
                Color(0xFF97C329)
                else androidx.compose.ui.graphics.Color.Transparent
            val color2 = if(decibel > 12)
                Color(0xFFB7C329)
                else androidx.compose.ui.graphics.Color.Transparent
            val color3 = if(decibel > 18)
                Color(0xFFC3A529)
                else androidx.compose.ui.graphics.Color.Transparent
            val color4 = if(decibel > 24)
                Color(0xFFE1A118)
                else androidx.compose.ui.graphics.Color.Transparent
            val color5 = if(decibel > 30)
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