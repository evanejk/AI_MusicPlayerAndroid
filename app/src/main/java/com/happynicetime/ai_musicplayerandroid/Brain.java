package com.happynicetime.ai_musicplayerandroid;

public class Brain {
    int songSkips = 0;
    int songPlays = 0;
    Neuron[] layer1 = new Neuron[1500];
    Neuron[] layer2 = new Neuron[1500];
    Neuron[] layer3 = new Neuron[1500];
    Brain(int numOfInputs) {
        for(int i = 0;i < layer1.length;i++){
            layer1[i] = new Neuron(numOfInputs);
        }
        for(int i = 0;i < layer2.length;i++){
            layer2[i] = new Neuron(layer1.length);
        }
        for(int i = 0;i < layer3.length;i++){
            layer3[i] = new Neuron(layer2.length);
        }
    }
    Brain(){

    }

    Brain getMutatedCopy() {
        Brain mutatedBrain = copyBrain();
        mutatedBrain.randomChange();
        return mutatedBrain;
    }

    private Brain copyBrain() {
        Brain newBrain = new Brain();
        newBrain.layer1 = cloneNeurons(layer1);
        newBrain.layer2 = cloneNeurons(layer2);
        newBrain.layer3 = cloneNeurons(layer3);
        return newBrain;
    }

    private void randomChange() {
        for(int i = 0;i < layer1.length;i++){
            layer1[i].randomChange();
        }
        for(int i = 0;i < layer2.length;i++){
            layer2[i].randomChange();
        }
        for(int i = 0;i < layer3.length;i++){
            layer3[i].randomChange();
        }

    }

    private Neuron[] cloneNeurons(Neuron[] layerToClone) {
        Neuron[] newLayer = new Neuron[layerToClone.length];
        for(int i = 0;i < layerToClone.length;i++){
            newLayer[i] = layerToClone[i].cloneNeuron();
        }
        return newLayer;
    }

    int compute(int[] inputs) {
        //for each neuron in layer multiply coefficents by inputs and check if sum of all those is over threshhold
        //if over threshhold store 1 in output
        //for last layer just get the sum of the (coefficents times inputs)
        //take that result and to % wordBank.length to get end result
        //layer 1:
        for(int neuronIndex = 0;neuronIndex < layer1.length;neuronIndex++){
            Neuron neuron = layer1[neuronIndex];
            int coeffByInputTotal = 0;
            for(int coefficentsIndex = 0;coefficentsIndex < neuron.coefficents.length;coefficentsIndex++){
                coeffByInputTotal = coeffByInputTotal + (neuron.coefficents[coefficentsIndex] * inputs[coefficentsIndex]);
            }
            if(coeffByInputTotal > neuron.threshhold){
                neuron.output = 1;
            }else{
                neuron.output = 0;
            }
        }
        //layer 2: use layer 1 output for layer 2 input
        for(int neuronIndex = 0;neuronIndex < layer2.length;neuronIndex++){
            Neuron neuron = layer2[neuronIndex];
            int coeffByInputTotal = 0;
            for(int coefficentsIndex = 0;coefficentsIndex < neuron.coefficents.length;coefficentsIndex++){
                coeffByInputTotal = coeffByInputTotal + (neuron.coefficents[coefficentsIndex] * layer1[coefficentsIndex].output);
            }
            if(coeffByInputTotal > neuron.threshhold){
                neuron.output = 1;
            }else{
                neuron.output = 0;
            }
        }
        //layer 3: get final result
        int finalResult = 0;
        for(int neuronIndex = 0;neuronIndex < layer3.length;neuronIndex++){
            Neuron neuron = layer3[neuronIndex];
            int coeffByInputTotal = 0;
            for(int coefficentsIndex = 0;coefficentsIndex < neuron.coefficents.length;coefficentsIndex++){
                coeffByInputTotal = coeffByInputTotal + (neuron.coefficents[coefficentsIndex] * layer2[coefficentsIndex].output);
            }
            finalResult += coeffByInputTotal;
        }
        finalResult = finalResult % StaticWorks.filesList.size();
        if(finalResult < 0){
            finalResult = -finalResult;

        }
        return finalResult;
    }

}
