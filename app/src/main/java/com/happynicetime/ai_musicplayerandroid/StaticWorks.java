package com.happynicetime.ai_musicplayerandroid;

import static com.happynicetime.ai_musicplayerandroid.AI.currentBrain;
import static com.happynicetime.ai_musicplayerandroid.AI.goodBrain;
import static com.happynicetime.ai_musicplayerandroid.AI.mutatedBrain;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import androidx.documentfile.provider.DocumentFile;
import android.media.audiofx.Equalizer;
import android.text.TextUtils;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;

public class StaticWorks {
    static LinkedList<SongFile> filesList = new LinkedList<SongFile>();
    static int[] songsBefore = new int[100];
    static int songPlay = 0;
    static String playingSongString = "";

    static MainActivity activity;
    static MediaPlayer mediaPlayer = new MediaPlayer();

    public static Context context;

    static boolean paused = false;
    private static String beforeSearchString = "";
    private static int beforeSearchIndex = 0;
    private static double bassAmount = 0;

    public static void pauseSong(){
        if(paused){
            mediaPlayer.start();
            paused = false;
            activity.buttonPause.setText("pause");
        }else{
            mediaPlayer.pause();
            paused = true;
            activity.buttonPause.setText("play");
        }
    }
    public static void playNextSong() {
        for(int i = 0;i < songsBefore.length - 2;i++){
            songsBefore[i] = songsBefore[i + 2];
        }
        //add current song to songsBefore
        songsBefore[songsBefore.length - 2] = songPlay;
        int duration = mediaPlayer.getDuration();
        if(duration == 0 || duration == -1){
            songsBefore[songsBefore.length - 1] = 0;
        }else {
            songsBefore[songsBefore.length - 1] = (int) (1000 * mediaPlayer.getCurrentPosition() / duration);
        }
        //find next song to play
        songPlay = currentBrain.compute(songsBefore);
        playingSongString = filesList.get(songPlay).file.getName();
        StaticWorks.mediaPlayer.reset();
        try {
            StaticWorks.mediaPlayer.setDataSource(StaticWorks.context, StaticWorks.filesList.get(StaticWorks.songPlay).file.getUri());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StaticWorks.mediaPlayer.prepareAsync();
        StaticWorks.mediaPlayer.setOnPreparedListener((listener) -> {
            StaticWorks.mediaPlayer.start();
        });
        StaticWorks.mediaPlayer.setOnCompletionListener(new MainActivity.MyCompletionListener());
        StaticWorks.activity.setSongPlayingLabel();
    }
    static Toast toastMutated;
    public static void skipSong(){
        currentBrain.songSkips++;
        if(currentBrain.songSkips >= 4){
            if(currentBrain == mutatedBrain){
                //check if improved //if improved set as new good brain
                if(mutatedBrain.songPlays > goodBrain.songPlays){//if mutation had more plays
                    System.out.println("mutated network won and became good network");
                    toastMutated.show();
                    goodBrain = mutatedBrain;//set as good brain
                }else{
                    System.out.println("good network won");
                }
                System.out.println("switching to good network");
                currentBrain = goodBrain;
                //reset brain stats
                currentBrain.songPlays = 0;
                currentBrain.songSkips = 0;
                //make new mutated brain
                mutatedBrain = currentBrain.getMutatedCopy();
            }else if(currentBrain == goodBrain){
                System.out.println("switching to mutated network");
                currentBrain = mutatedBrain;
            }
        }
        playNextSong();
        activity.setSongPlayingLabel();
        //pc.pauseSongButton.setText("Pause");
        paused = false;
    }
    static MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

    public static void findSong() {
        int findIndex = 0;
        String findThisString = activity.editTextSearch.getText().toString();
        //System.out.println("findThisString: "+findThisString);
        if(TextUtils.isEmpty(activity.editTextSearch.getText().toString())){
            //System.out.println("returning");
            return;
        }
        if(beforeSearchString.equals(findThisString)){
            findIndex = beforeSearchIndex + 1;
        }
        boolean foundSomething = false;
        for(;findIndex < filesList.size();findIndex++){
            if(filesList.get(findIndex).file.getName().toLowerCase().contains(findThisString.toLowerCase()) ||
                    filesList.get(findIndex).artistName.toLowerCase().contains(findThisString.toLowerCase())){
                foundSomething = true;
                break;
            }
        }
        if(foundSomething){
            beforeSearchIndex = findIndex;
            beforeSearchString = findThisString;
            //System.out.println("found "+songPaths.get(findIndex).toString());
            //found something at findIndex in songPaths
            //songPlayingLabel.setText("loading...");
            System.out.println("SKIPPING UNTIL FOUND SONG");
            skipUntilFound(findThisString);
        }else{
            beforeSearchIndex = 0;
            beforeSearchString = "";
        }
    }
    private static void skipUntilFound(String findThisString) {
        int totalSkipsToFindSong = 0;
        playingSongString = "";
        String artistName = "";
        boolean didntPlaySong = false;
        while(!playingSongString.toLowerCase().contains(findThisString.toLowerCase())){
            if(artistName != null){
                if(artistName.toLowerCase().contains(findThisString.toLowerCase())){
                    break;
                }
            }
            totalSkipsToFindSong++;
            currentBrain.songSkips++;
            if(currentBrain.songSkips >= 4){
                if(currentBrain == mutatedBrain){
                    //check if improved //if improved set as new good brain
                    if(mutatedBrain.songPlays > goodBrain.songPlays){//if mutation had more plays
                        goodBrain = mutatedBrain;//set as good brain
                        toastMutated.show();
                    }
                    currentBrain = goodBrain;
                    //reset brain stats
                    currentBrain.songPlays = 0;
                    currentBrain.songSkips = 0;
                    //make new mutated brain
                    mutatedBrain = currentBrain.getMutatedCopy();
                }else if(currentBrain == goodBrain){
                    currentBrain = mutatedBrain;
                }
            }
            for(int i = 0;i < songsBefore.length - 2;i++){
                songsBefore[i] = songsBefore[i + 2];
            }
            //add current song to songsBefore
            songsBefore[songsBefore.length - 2] = songPlay;
            if(didntPlaySong){
                songsBefore[songsBefore.length - 1] = 0;
            }else{
                songsBefore[songsBefore.length - 1] = (int) (1000 * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
                didntPlaySong = true;
            }
            //find next song to play
            songPlay = AI.currentBrain.compute(songsBefore);
            playingSongString = filesList.get(songPlay).file.getName();
            artistName = filesList.get(songPlay).artistName;
        }
        System.out.println("Took "+totalSkipsToFindSong+" skips to find song.");
        if(currentBrain == goodBrain){
            System.out.println("good network found song");
        }else if(currentBrain == mutatedBrain){
            System.out.println("mutated network found song");
        }
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(context,filesList.get(songPlay).file.getUri());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener((listener)-> {
                    StaticWorks.mediaPlayer.start();
                });
        StaticWorks.mediaPlayer.setOnCompletionListener(new MainActivity.MyCompletionListener());
        activity.setSongPlayingLabel();
        activity.buttonPause.setText("pause");
        paused = false;
    }

    public static void bassUp() {
        bassAmount += 10d;
        if(bassAmount > 550){
            bassAmount = 550;
        }
        setupEqualizer();
    }

    public static void bassDown() {
        bassAmount -= 10d;
        if(bassAmount < 0){
            bassAmount = 0;
        }
        setupEqualizer();
    }

    static Equalizer equalizer;
    private static void setupEqualizer() {
        int audioSessionId = mediaPlayer.getAudioSessionId();
        equalizer = new Equalizer(0, audioSessionId);
        equalizer.setEnabled(true);
        //System.out.println("bands: "+equalizer.getNumberOfBands());//5 bands
        equalizer.setBandLevel((short)0, (short)(10    * bassAmount));
        equalizer.setBandLevel((short)1, (short)(10    * bassAmount));
        equalizer.setBandLevel((short)2, (short)(0     * bassAmount));
        equalizer.setBandLevel((short)3, (short)(-10   * bassAmount));
        equalizer.setBandLevel((short)4, (short)(-10   * bassAmount));

    }

    public static void findSongFast() {
        System.out.println("FIND SONG FAST");
        int findIndex = 0;
        String findThisString = activity.editTextSearch.getText().toString();
        if(beforeSearchString.equals(findThisString)){
            findIndex = beforeSearchIndex + 1;
        }
        DocumentFile foundSongPath = null;
        for(;findIndex < filesList.size();findIndex++){
            if(filesList.get(findIndex).file.getName().toLowerCase().contains(findThisString.toLowerCase())){
                foundSongPath = filesList.get(findIndex).file;
                break;
            }
        }
        if(foundSongPath != null){
            for(int i = 0;i < songsBefore.length - 2;i++){
                songsBefore[i] = songsBefore[i + 2];
            }
            //add current song to songsBefore
            songsBefore[songsBefore.length - 2] = songPlay;
            int duration = mediaPlayer.getDuration();
            if(duration == 0 || duration == -1){
                songsBefore[songsBefore.length - 1] = 0;
            }else {
                songsBefore[songsBefore.length - 1] = (int) (1000 * mediaPlayer.getCurrentPosition() / duration);
            }
            songPlay = findIndex;
            beforeSearchIndex = findIndex;
            beforeSearchString = findThisString;
            //play song
            mediaPlayer.stop();
            playingSongString = foundSongPath.getName();
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(context,filesList.get(songPlay).file.getUri());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener((listener)-> {
                StaticWorks.mediaPlayer.start();
            });
            StaticWorks.mediaPlayer.setOnCompletionListener(new MainActivity.MyCompletionListenerForFindSongFast());
            activity.setSongPlayingLabel();
            activity.buttonPause.setText("pause");
            paused = false;

        }else{
            beforeSearchIndex = 0;
            beforeSearchString = "";
        }
    }
}
