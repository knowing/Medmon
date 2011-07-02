/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.sendsor.accelerationSensor.algorithm.moennig.segmentation;


/**
 *
 * @author Alexander Stautner
 */
public class AutoCorrelation {

    private double[] calcMeanAndSTD(double[] numbers)
    {
        int n = numbers.length;
        double mean = 0;
        double std = 0;
        double squares = 0;
        for(int i = 0; i < n; i++){
            mean += numbers[i];
            squares += numbers[i]*numbers[i];
        }
        mean /= n;
        squares /= n;
        double meanSquare = mean * mean;
        std = Math.sqrt(squares - meanSquare);
        double[] result = {mean,std};
        return result;
        
    }
    
    public double calcAutocorrelation(double[] sample, double[] data){
        double result = 0;
        if(sample.length == data.length){
            double[] meanStdSample = calcMeanAndSTD(sample);
            double[] meanStdData = calcMeanAndSTD(data);
            for(int j = 0; j < sample.length; j++){
                result+= (sample[j]-meanStdSample[0])*(data[j]-meanStdData[0]);
            }
            result /= sample.length;
            result /= meanStdSample[1]*meanStdData[1];
        }
        return result;
    }

}
