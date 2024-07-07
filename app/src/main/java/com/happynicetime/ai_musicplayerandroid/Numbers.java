package com.happynicetime.ai_musicplayerandroid;

import java.util.Random;
class Numbers {
    static Random rand = new Random();
    static int getRandomNumber() {
        return rand.nextInt();
    }

}