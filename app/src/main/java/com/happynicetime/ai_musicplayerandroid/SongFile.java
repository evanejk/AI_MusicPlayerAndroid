package com.happynicetime.ai_musicplayerandroid;

import androidx.documentfile.provider.DocumentFile;

public class SongFile {
    DocumentFile file;
    String artistName;
    SongFile(DocumentFile file, String artistName){
        this.file = file;
        this.artistName = artistName;
    }
}
