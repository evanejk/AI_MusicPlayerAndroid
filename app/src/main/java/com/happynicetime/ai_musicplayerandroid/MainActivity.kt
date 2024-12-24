package com.happynicetime.ai_musicplayerandroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.ServiceCompat
import androidx.documentfile.provider.DocumentFile
import com.happynicetime.ai_musicplayerandroid.AI.currentBrain
import com.happynicetime.ai_musicplayerandroid.StaticWorks.mediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var serviceIntent: Intent
    private var startedMediaPlayer: Boolean = false
    private lateinit var buttonBrowse: Button
    private lateinit var buttonSkip: Button
    lateinit var buttonPause: Button
    lateinit var textViewSongName: TextView
    private lateinit var buttonFindSong: Button
    lateinit var editTextSearch: EditText
    lateinit var buttonBassUp: Button
    lateinit var buttonBassDown: Button
    lateinit var textViewInfo: TextView
    lateinit var seekBar: SeekBar
    val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var buttonFindSongFast: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StaticWorks.activity = this
        StaticWorks.context = applicationContext
        StaticWorks.toastMutated = Toast.makeText(applicationContext, "mutated network won and became good network", Toast.LENGTH_LONG);
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if(result.data != null) {
                    val uri = result.data?.data // Get the selected folder URI
                    if(uri != null){
                        val docFile: DocumentFile =
                            DocumentFile.fromTreeUri(applicationContext,uri)!!
                        StaticWorks.filesList.clear()
                        for (listFile in docFile.listFiles()) {
                            //System.out.println(listFile.uri.toString())
                            try{
                                val mediaPlayer = MediaPlayer()
                                mediaPlayer.setDataSource(applicationContext, listFile.uri)
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnPreparedListener {
                                    // Playback is ready to start
                                    //mediaPlayer.start()
                                }
                                mediaPlayer.release()
                                //println(listFile)
                                StaticWorks.mediaMetadataRetriever.setDataSource(
                                    applicationContext, listFile.uri
                                )
                                var artistName = StaticWorks.mediaMetadataRetriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_ARTIST
                                )
                                if (artistName == null) {
                                    artistName = ""
                                }
                                StaticWorks.filesList.add(SongFile(listFile,artistName))

                            }catch(e: Exception){
                                println("can not load file for mediaPlayer: "+listFile.uri.toString())
                            }
                        }

                        AI.load(applicationContext)
                        //make up songs listened to before

                        // Make up songs listened to before
                        StaticWorks.songsBefore = IntArray(currentBrain.layer1.size) { index ->
                            if (index % 2 == 0) {
                                Random.nextInt(0, StaticWorks.filesList.size)
                            } else {
                                0
                            }
                        }
                        //use before songs for brain input
                        StaticWorks.songPlay = AI.goodBrain.compute(StaticWorks.songsBefore)

                        //stop service
                        if(startedMediaPlayer){
                            val serviceIntent2 = Intent(this, MediaPlayerService::class.java)
                            stopService(serviceIntent2)
                            println("STOP SERVICE")
                        }
                        startedMediaPlayer = false
                        //use brain output to play next song
                        serviceIntent = Intent(this, MediaPlayerService::class.java)
                        serviceIntent.action = "ACTION_STARTUP"
                        startForegroundService(serviceIntent)
                        println("START SERVICE")
                        startedMediaPlayer = true
                        scope.launch {
                            while (mediaPlayer != null) {
                                delay(2000)  // Delay for 2 second
                                runOnUiThread {
                                    if (mediaPlayer?.isPlaying == true) {
                                        if(mediaPlayer.duration == 0 || mediaPlayer.duration == -1){
                                            seekBar.progress = 0
                                        }else {
                                            seekBar.progress =
                                                200 * mediaPlayer.currentPosition / mediaPlayer.duration
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        buttonBrowse = findViewById<Button>(R.id.buttonBrowse)
        buttonSkip = findViewById<Button>(R.id.buttonSkip)
        textViewSongName = findViewById(R.id.textViewSongName)
        buttonPause = findViewById(R.id.buttonPause)
        buttonBrowse.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            launcher.launch(intent)
        }
        buttonSkip.setOnClickListener {
            StaticWorks.skipSong()
        }
        buttonPause.setOnClickListener {
            StaticWorks.pauseSong()
        }
        editTextSearch = findViewById(R.id.editTextSearch)
        buttonFindSong = findViewById(R.id.buttonFindSong)
        buttonFindSongFast = findViewById(R.id.buttonFindSongFast)
        buttonFindSong.setOnClickListener{
            StaticWorks.findSong()
        }
        buttonFindSongFast.setOnClickListener{
            StaticWorks.findSongFast()
        }
        buttonBassUp = findViewById(R.id.buttonBassUp)
        buttonBassDown = findViewById(R.id.buttonBassDown)
        buttonBassUp.setOnClickListener {
            StaticWorks.bassUp()
        }
        buttonBassDown.setOnClickListener {
            StaticWorks.bassDown()
        }
        textViewInfo = findViewById(R.id.textViewInfo)
        seekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    StaticWorks.mediaPlayer?.seekTo(StaticWorks.mediaPlayer.duration * progress / 200)  // Change playback position if user interaction
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //StaticWorks.mediaPlayer.pause()
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //StaticWorks.mediaPlayer.start()
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        StaticWorks.mediaPlayer.stop()
        StaticWorks.mediaPlayer.release()
        AI.save()
        println("APPLICATION ON DESTROY")
    }
    class MediaPlayerService : Service(){

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            when (intent?.action) {
                "ACTION_SKIP_SONG" -> {
                    StaticWorks.skipSong()
                    return START_NOT_STICKY
                }
                "ACTION_STARTUP" -> {
                    val skipSongIntent: Intent = Intent(this, MediaPlayerService::class.java) // Replace with your service class
                    skipSongIntent.action = "ACTION_SKIP_SONG" // Define a unique action for skipping
                    val skipSongPendingIntent =
                        PendingIntent.getService(this, 0, skipSongIntent, PendingIntent.FLAG_IMMUTABLE)
                    val skipSongAction: NotificationCompat.Action = NotificationCompat.Action(
                        R.drawable.baseline_music_note_24,  // skip icon resource
                        "Skip Song",
                        skipSongPendingIntent
                    )
                    val mainActivityIntent: Intent = Intent(this,MainActivity::class.java)
                    val mainActivityPendingIntent =
                        PendingIntent.getActivity(this,0,mainActivityIntent,PendingIntent.FLAG_IMMUTABLE)
                    val notification: Notification =
                        NotificationCompat.Builder(StaticWorks.context, "AI_MediaPlayer")
                            .setSmallIcon(R.drawable.ic_android_black_24dp)
                            .setVisibility(VISIBILITY_PUBLIC)
                            .addAction(skipSongAction)
                            .setContentIntent(mainActivityPendingIntent)
                            .build()
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val channel = NotificationChannel("AI_MediaPlayer", "AI_MediaPlayer", importance)
                    channel.description = "playing music in background"

                    notificationManager.createNotificationChannel(channel)
                    ServiceCompat.startForeground(this, NOTIFICATION_ID, notification,FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                    return START_NOT_STICKY
                }
            }
            return START_NOT_STICKY
        }
        override fun onDestroy() {
            super.onDestroy()
            println("MEDIA PLAYER ON DESTROY")
            StaticWorks.mediaPlayer.stop()
            StaticWorks.mediaPlayer.release()
            return
        }

        private val NOTIFICATION_ID: Int = 412812

        // Custom Binder subclass implementing IBinder indirectly
        private val mBinder: LocalBinder = LocalBinder()
        override fun onBind(intent: Intent?): IBinder? {
            return mBinder
        }
        override fun onCreate() {
            println("MediaPlayerService started")
            super.onCreate()
            if(StaticWorks.filesList.size == 0){
                stopSelf()
                return
            }
            StaticWorks.mediaPlayer = MediaPlayer()
            StaticWorks.playingSongString = StaticWorks.filesList.get(StaticWorks.songPlay).file.name
            StaticWorks.playingSongArtist = StaticWorks.filesList.get(StaticWorks.songPlay).artistName
            StaticWorks.mediaPlayer.setDataSource(applicationContext, StaticWorks.filesList.get(StaticWorks.songPlay).file.uri)
            StaticWorks.mediaPlayer.prepareAsync()
            StaticWorks.mediaPlayer.setOnPreparedListener {
                StaticWorks.setupEqualizer()
                StaticWorks.activity.buttonPause.text = "pause"
                StaticWorks.paused = false
                StaticWorks.mediaPlayer.start()
            }
            StaticWorks.mediaPlayer.setOnCompletionListener(MyCompletionListener())
            StaticWorks.activity.setSongPlayingLabel()
        }

    }
    fun setSongPlayingLabel() {
        textViewSongName.setText(StaticWorks.playingSongString+"\n"+"by "+StaticWorks.playingSongArtist)
        if(AI.currentBrain == AI.goodBrain){
            textViewInfo.setText("good network")
        }else if(AI.currentBrain == AI.mutatedBrain){
            textViewInfo.setText("mutated network")
        }
    }
    class LocalBinder : Binder() {

    }
    class MyCompletionListener : MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer?) {
            currentBrain.songPlays++
            println("finished song "+StaticWorks.playingSongString)
            StaticWorks.playNextSong()
        }
    }
    class MyCompletionListenerForFindSongFast : MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer?) {
            println("finished song "+StaticWorks.playingSongString)
            StaticWorks.playNextSong()
        }
    }
}