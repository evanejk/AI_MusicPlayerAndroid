package com.happynicetime.ai_musicplayerandroid;

import static com.happynicetime.ai_musicplayerandroid.AI.currentBrain;
import static com.happynicetime.ai_musicplayerandroid.AI.goodBrain;
import static com.happynicetime.ai_musicplayerandroid.AI.mutatedBrain;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import androidx.documentfile.provider.DocumentFile;
import android.media.audiofx.Equalizer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;

public class StaticWorks {
    static LinkedList<DocumentFile> filesList = new LinkedList<DocumentFile>();
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
        songsBefore[songsBefore.length - 1] = (int) (1000 * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
        //find next song to play
        songPlay = currentBrain.compute(songsBefore);
        playingSongString = filesList.get(songPlay).getName();
        StaticWorks.mediaPlayer.reset();
        try {
            StaticWorks.mediaPlayer.setDataSource(StaticWorks.context, StaticWorks.filesList.get(StaticWorks.songPlay).getUri());
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
    public static void skipSong(){
        currentBrain.songSkips++;
        if(currentBrain.songSkips >= 4){
            if(currentBrain == mutatedBrain){
                //check if improved //if improved set as new good brain
                if(mutatedBrain.songPlays > goodBrain.songPlays){//if mutation had more plays
                    System.out.println("mutated network won and became good network");
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
        System.out.println("SKIPPING UNTIL FOUND SONG");
        int findIndex = 0;
        String findThisString = activity.editTextSearch.getText().toString();
        if(beforeSearchString.equals(findThisString)){
            findIndex = beforeSearchIndex + 1;
        }
        boolean foundSomething = false;
        for(;findIndex < filesList.size();findIndex++){
            String artistName = null;
            try {
                mediaMetadataRetriever.setDataSource(context, filesList.get(findIndex).getUri());
                artistName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            }catch(Exception e){
                System.out.println("Error getting metadate from song? file "+filesList.get(findIndex).getName());
                activity.textViewSongName.setText("Error getting metadate from song? file "+filesList.get(findIndex).getName());
            }
            if(filesList.get(findIndex).getName().toLowerCase().contains(findThisString.toLowerCase())){
                foundSomething = true;
                break;
            }
            if(artistName != null){
              if(artistName.toLowerCase().contains(findThisString.toLowerCase())){
                  foundSomething = true;
                  break;
              }
            }
        }
        if(foundSomething){
            beforeSearchIndex = findIndex;
            beforeSearchString = findThisString;
            //System.out.println("found "+songPaths.get(findIndex).toString());
            //found something at findIndex in songPaths
            //songPlayingLabel.setText("loading...");
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
            playingSongString = filesList.get(songPlay).getName();
            mediaMetadataRetriever.setDataSource(context,filesList.get(songPlay).getUri());
            artistName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        }
        System.out.println("Took "+totalSkipsToFindSong+" skips to find song.");
        if(currentBrain == goodBrain){
            System.out.println("good network found song");
        }else if(currentBrain == mutatedBrain){
            System.out.println("mutated network found song");
        }
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(context,filesList.get(songPlay).getUri());
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
}
