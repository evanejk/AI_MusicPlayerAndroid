package com.happynicetime.ai_musicplayerandroid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.documentfile.provider.DocumentFile
import com.happynicetime.ai_musicplayerandroid.AI.currentBrain
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private val NOTIFICATION_ID: Int = 412812
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StaticWorks.activity = this
        StaticWorks.context = applicationContext
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if(result.data != null) {
                    val uri = result.data?.data // Get the selected folder URI
                    if(uri != null){
                        val docFile: DocumentFile =
                            DocumentFile.fromTreeUri(applicationContext,uri)!!
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
                                StaticWorks.filesList.add(listFile)
                            }catch(e: Exception){
                                println("can not load file for mediaPlayer: "+listFile.uri.toString())
                            }
                        }
                        //test media player
                        //val mediaPlayer = MediaPlayer()
                        //mediaPlayer.setDataSource(applicationContext, filesList.first.uri)
                        //mediaPlayer.prepare()
                        //mediaPlayer.setOnPreparedListener {
                        //    mediaPlayer.start()
                        //}
                        //mediaPlayer.setOnCompletionListener {
                        //    mediaPlayer.release()
                        //}
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
                        startForegroundService(serviceIntent)
                        println("START SERVICE")
                        startedMediaPlayer = true
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
        buttonFindSong.setOnClickListener{
            StaticWorks.findSong()
        }
        buttonBassUp = findViewById(R.id.buttonBassUp)
        buttonBassDown = findViewById(R.id.buttonBassDown)
        buttonBassUp.setOnClickListener {
            StaticWorks.bassUp();
        }
        buttonBassDown.setOnClickListener {
            StaticWorks.bassDown();
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        StaticWorks.mediaPlayer.stop()
        StaticWorks.mediaPlayer.release()
        AI.save()
        println("APPLICATION ON DESTROY")
    }
    class MediaPlayerService : Service(){
        var notification: Notification =
            NotificationCompat.Builder(StaticWorks.context, "AI_MediaPlayer")
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .build()
        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("AI_MediaPlayer", "AI_MediaPlayer", importance)
            channel.description = "playing music in background"

            notificationManager.createNotificationChannel(channel)
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification,FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
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
            super.onCreate()
            if(StaticWorks.filesList.size == 0){
                stopSelf()
                return
            }
            StaticWorks.mediaPlayer = MediaPlayer()
            StaticWorks.playingSongString = StaticWorks.filesList.get(StaticWorks.songPlay).name
            StaticWorks.mediaPlayer.setDataSource(applicationContext, StaticWorks.filesList.get(StaticWorks.songPlay).uri)
            StaticWorks.mediaPlayer.prepareAsync()
            StaticWorks.mediaPlayer.setOnPreparedListener {
                StaticWorks.mediaPlayer.start()
            }
            StaticWorks.mediaPlayer.setOnCompletionListener(MyCompletionListener())
            StaticWorks.activity.setSongPlayingLabel()
        }

    }
    fun setSongPlayingLabel() {
        textViewSongName.setText(StaticWorks.playingSongString);
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
}