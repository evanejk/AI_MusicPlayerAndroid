package com.happynicetime.ai_musicplayerandroid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;

class AI {
    static File brainFile;
    static Brain currentBrain;
    static Brain mutatedBrain;
    static Brain goodBrain;
    static void load(Context mcoContext) {
        brainFile = new File(mcoContext.getFilesDir(),"brain.mbrain");
        if(!brainFile.exists()){
            //make new brain
            currentBrain = new Brain(50 * 2);
        }else{
            try {
                DataInputStream in = new DataInputStream(new
                        BufferedInputStream(new FileInputStream(brainFile)));
                currentBrain = new Brain();
                //read number of neurons for layer 1
                currentBrain.layer1 = new Neuron[in.readInt()];
                for(int neuronI = 0;neuronI < currentBrain.layer1.length;neuronI++){
                    currentBrain.layer1[neuronI] = new Neuron();
                    //read number of coefficients
                    currentBrain.layer1[neuronI].coefficents = new int[in.readInt()];
                    //read coefficients
                    for(int coefI = 0;coefI < currentBrain.layer1[neuronI].coefficents.length;coefI++){
                        currentBrain.layer1[neuronI].coefficents[coefI] = in.readInt();
                    }
                }
                //read number of neurons for layer 2
                currentBrain.layer2 = new Neuron[in.readInt()];
                for(int neuronI = 0;neuronI < currentBrain.layer2.length;neuronI++){
                    currentBrain.layer2[neuronI] = new Neuron();
                    //read number of coefficients
                    currentBrain.layer2[neuronI].coefficents = new int[in.readInt()];
                    //read coefficients
                    for(int coefI = 0;coefI < currentBrain.layer2[neuronI].coefficents.length;coefI++){
                        currentBrain.layer2[neuronI].coefficents[coefI] = in.readInt();
                    }
                }
                //read number of neurons for layer 3
                currentBrain.layer3 = new Neuron[in.readInt()];
                for(int neuronI = 0;neuronI < currentBrain.layer3.length;neuronI++){
                    currentBrain.layer3[neuronI] = new Neuron();
                    //read number of coefficients
                    currentBrain.layer3[neuronI].coefficents = new int[in.readInt()];
                    //read coefficients
                    for(int coefI = 0;coefI < currentBrain.layer3[neuronI].coefficents.length;coefI++){
                        currentBrain.layer3[neuronI].coefficents[coefI] = in.readInt();
                    }
                }
                in.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        goodBrain = currentBrain;
        mutatedBrain = goodBrain.getMutatedCopy();
    }

    static void save() {
        if(brainFile == null)
            return;
        if(brainFile.exists()){
            brainFile.delete();
        }
        try {
            brainFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(brainFile)));
            //write number of neurons for layer 1
            out.writeInt(goodBrain.layer1.length);
            for(int neuronI = 0;neuronI < goodBrain.layer1.length;neuronI++){
                //write number of coefficients
                out.writeInt(goodBrain.layer1[neuronI].coefficents.length);
                //write coefficients
                for(int coefI = 0;coefI < goodBrain.layer1[neuronI].coefficents.length;coefI++){
                    out.writeInt(goodBrain.layer1[neuronI].coefficents[coefI]);
                }
            }
            //write number of neurons for layer 2
            out.writeInt(goodBrain.layer2.length);
            for(int neuronI = 0;neuronI < goodBrain.layer2.length;neuronI++){
                //write number of coefficients
                out.writeInt(goodBrain.layer2[neuronI].coefficents.length);
                //write coefficients
                for(int coefI = 0;coefI < goodBrain.layer2[neuronI].coefficents.length;coefI++){
                    out.writeInt(goodBrain.layer2[neuronI].coefficents[coefI]);
                }
            }
            //write number of neurons for layer 3
            out.writeInt(goodBrain.layer3.length);
            for(int neuronI = 0;neuronI < goodBrain.layer3.length;neuronI++){
                //write number of coefficients
                out.writeInt(goodBrain.layer3[neuronI].coefficents.length);
                //write coefficients
                for(int coefI = 0;coefI < goodBrain.layer3[neuronI].coefficents.length;coefI++){
                    out.writeInt(goodBrain.layer3[neuronI].coefficents[coefI]);
                }
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
