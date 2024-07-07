package com.happynicetime.ai_musicplayerandroid;

public class Neuron {
    int[] coefficents;
    int threshhold = 0;
    int output;
    Neuron(int numOfInputs) {
        coefficents = new int[numOfInputs];
        for(int i = 0;i < coefficents.length;i++){
            coefficents[i] = Numbers.getRandomNumber();
        }
    }
    Neuron() {
    }
    Neuron cloneNeuron() {
        Neuron newNeuron = new Neuron();
        newNeuron.coefficents = new int[coefficents.length];
        System.arraycopy(coefficents, 0, newNeuron.coefficents, 0, coefficents.length);
        newNeuron.threshhold = threshhold;
        newNeuron.output = output;
        return newNeuron;
    }
    void randomChange() {
        for(int i = 0;i < coefficents.length;i++){
            if(Numbers.rand.nextFloat() <= 0.005){
                //System.out.println("mutation 1/200 reached");
                coefficents[i] = Numbers.getRandomNumber();
            }
        }
    }

}