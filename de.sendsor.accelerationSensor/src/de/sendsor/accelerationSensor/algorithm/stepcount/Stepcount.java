/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.sendsor.accelerationSensor.algorithm.stepcount;

import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;



/**
 * Implementation of stepcount algorithmus used in my Diplomarbeit
 * @author Alexander Stautner
 */
public class Stepcount {
    //Parameter for algorithm
    private float thresholdtip = 0.25f; // Minima has to be below this value
    private int windowLength = 150;  // Number of Values in one Window
    private long steplengthMin = 661; // Minimum length of one step in milliseconds
    private long steplengthMax = 1700; // Maximum length of one step in milliseconds
    private double lastVal = 0.0; //Starting value
    private int steps = 0; // Starting value for number of steps
    private double intensityLevel = 0.1; // Minimum intesity in windows so steps are calculated
    private Vector<Vector> data; // Vector contains all Data
    private List<Vector> window; // Simulating one window of the Sensor

    private KalmanFilter kalmanFilter;
   
    
    //Parameter Kalman filter
    private double errorCovariance = 1;
    private double sensorError = 59;
    private double processNoise = 12;
    private double dt = 1.47;
    
    /**
     * Method which perfoms the logic of the algorithm 
     * @param begin First time in data
     * @param end Last time in data
     * @param data Vector containing all Data
     */
    public void iniatialize(Timestamp begin, Timestamp end, Vector<Vector> data)
    {
        data = transform(data); //transforming the data with Kalmann Filter
        long lastCandidateTime = begin.getTime(); //inialize value for algorithm
        int windows = data.size() / windowLength; // number of windows
        int stepsTemp = 0; // inialize value for steps
        this.lastVal = (Double)data.firstElement().get(0); // inialize value
        Vector<Vector> tipsCandidates = new Vector<Vector>(); // vector containing all TipCandidates
        int count = 0;
        //Logic of the algorithm
        if(windows == 0)
        {
            double intensity = 0; //used for identification of activity
            int startIndex = 0;
            int endIndex = data.size();
            //System.out.println("Endindex: " + endIndex);
            window = data.subList(startIndex, endIndex);
            //Calculation of activity
            for(int j = 0; j< windowLength; j++)
            {
               intensity = intensity + (Double)window.get(j).get(0);
            }
            
            intensity = intensity / windowLength; //standardization with windowsLength

            //System.out.println("Intensität: "+intensity);

            if(intensity > intensityLevel)
            {
                tipsCandidates = findTIP(window, lastVal); // finding TIPcandidates
                findSteps(tipsCandidates, lastCandidateTime); // finding steps
                Timestamp time = (Timestamp)tipsCandidates.lastElement().get(1);
                lastCandidateTime = time.getTime();
            }
            //System.out.println("TIP Candidates: "+tipsCandidates.size()+"\n");

        }
        else
        {
            for(int i = 0; i< windows; i++)
            {
                //Finding the activity of the window
                double intensity = 0;
                int startIndex = i*windowLength;
                int endIndex = startIndex + windowLength;
                //System.out.println("Endindex: " + endIndex);
                window = data.subList(startIndex, endIndex);
                System.out.println("Window:" + window.size());
                 for(int j = 0; j< windowLength;j++)
                 {
                    intensity = intensity + (Double)window.get(j).get(0);
                 }
                intensity = intensity / windowLength;

                System.out.println("Intensität: "+intensity);
                tipsCandidates = findTIP(window, lastVal);
                if (tipsCandidates.size()> 0 && intensity > intensityLevel)
                {
                    findSteps(tipsCandidates, lastCandidateTime);
                    Timestamp time = (Timestamp)tipsCandidates.lastElement().get(1);
                    lastCandidateTime = time.getTime();
                    //System.out.println("TIP Candidates: "+tipsCandidates.size()+"\n");
                }

            }
            //needed if size of last window < windowLength
            int startIndex = windows*windowLength;
            int endIndex = data.size();
            double intensity = 0;
            //System.out.println("Endindex: " + endIndex);
            window = data.subList(startIndex, endIndex);
             for(int j = 0; j< window.size();j++)
            {
               intensity = intensity + (Double)window.get(j).get(0);
            }
            intensity = intensity/windowLength;
            System.out.println("Intensität: "+intensity);
            tipsCandidates = findTIP(window, lastVal);
            //System.out.println("Größe: "+tipsCandidates.size());
            if (tipsCandidates.size()> 0 && intensity > intensityLevel)
            {
                findSteps(tipsCandidates, lastCandidateTime);
                Timestamp time = (Timestamp)tipsCandidates.lastElement().get(1);
                lastCandidateTime = time.getTime();
                //System.out.println("TIP Candidates: "+tipsCandidates.size()+"\n");
            }
        }
        //System.out.println("Count:"+count);
        //System.out.println("TIPs: "+tipsCount);
                       
        
    }
    /**
     * Method for transforming vector with Kalman Filter
     * @param vector Vector to be transformed
     * @return The transformed vector
     */

    private Vector<Vector> transform(Vector<Vector> vector)
    {
        Vector result = new Vector();
        this.kalmanFilter = new KalmanFilter(errorCovariance);

        for(int i = 0; i < vector.size()-1; i++)
        {
            // Adding x and z axis together
            double x = (((Integer)vector.get(i).get(0)/128F)*2)+0.981;//0.981 Gravity
            double z = (((Integer)vector.get(i).get(2)/128F)*2);
            double xplusz = Math.pow(x,2)+Math.pow(z,2);
            xplusz = Math.round(xplusz*1000)/1000.;
            
            //filtering with Kalman Filter
            kalmanFilter.timeUpdate(dt, processNoise);
            kalmanFilter.measurementUpdate2(xplusz, sensorError);
            xplusz = kalmanFilter.getExpectedValue();
            
            //Adding filtered to result array
            Vector temp = new Vector();
            temp.add(xplusz);
            temp.add(vector.get(i).get(3));
            result.add(i,temp);
            
            
            //Needed for visual validation issues could be ignored
            Timestamp ts = (Timestamp)vector.get(i).get(3);
            Date date = new Date(ts.getTime());
            
        }
        return result;
    }

    /**
     * Method for finding TIPs
     * @param window The values in witch the Tips should be found
     * @param lastVal Startvalue
     * @return Vector of Tips
     */
    private Vector<Vector> findTIP(List<Vector> window, double lastVal)
    {
        Vector<Vector> tipsList = new Vector<Vector>();
        int n = window.size()-1;
        int i = 0;
        double firstValue = lastVal;
        //When all values at the beginning have the same size you can ignore them until the first change
        while(i < n && (Double)window.get(i).get(0) == firstValue)
        {
            i = i+1;
            
        }
        //finding the first TIP
        if(i < n && (Double)window.get(i).get(0) < firstValue)
        {

            Vector temp = findMin(i, window);
            i = (Integer)temp.get(0);
            if(temp.size() == 2)
            {
                tipsList.add((Vector)temp.get(1));
            }
        }
        //finding the other TIPs
        while(i < n)
        {
            Vector temp = findMin(i, window);
            i = (Integer)temp.get(0);
            
            if(temp.size() == 2)
            {
                Vector vector = (Vector)temp.get(1);
                if((Double)vector.get(0)< thresholdtip)
                {
                   tipsList.add((Vector)temp.get(1));
                }
            }
        }
        //calculation the start value for the following window
        if(window.size() > 0)
        {
            this.lastVal = (Double)window.get(window.size()-1).get(0);
        }
        return tipsList;
    }

    /**
     * Method for finding minimas
     * @param i Startvalue
     * @param vectorList List in witch the minimas should be found
     * @return vector of reseted i and minima
     */
    private Vector findMin(int i, List<Vector> vectorList)
    {
        int n = vectorList.size()-1;
        Vector result = new Vector();
        while(i < n && (Double)vectorList.get(i).get(0) >= (Double)vectorList.get(i+1).get(0))
        {
            i = i+1;
        }
        if(i < n)
        {
            result.add(0,vectorList.get(i));
        }
        result.add(0,i+1);
        return result;
    }

    /**
     * Calculation of steps
     * @param tipsCandidates Vector with TIPCandidates
     * @param lastTIPCandidate time of last TIPCandidate
     */
    private void findSteps(Vector<Vector> tipsCandidates, long lastTIPCandidate)
    {
        long lastCandidateTime = lastTIPCandidate;
        int count = 0;
        for(Vector item : tipsCandidates)
        {
            //Calculating the timedifference between lastTIPCandidate and current TIPCandidate
            Timestamp currentCandidateTimestamp = (Timestamp)item.get(1);
            Long currentCanditateTime = currentCandidateTimestamp.getTime();
            Long timeDifference =currentCanditateTime - lastCandidateTime;
            //When timedifference between steplengthMin and steplengthMax adding one step
            if(timeDifference >=steplengthMin && timeDifference <= steplengthMax)
            {
                lastCandidateTime = currentCanditateTime;
                //System.out.println("STEP: "+item);
                count++;
                //System.out.println("Zeitunterschied: "+timeDifference+" Count: "+count);
            }
            else if(timeDifference > steplengthMax)
            {
              lastCandidateTime = currentCanditateTime;
            }
            //System.out.println("Zeitunterschied: "+timeDifference+" Count: "+count);
        }
        steps = steps + count;
        //System.out.println("Schritte: " + steps);

    }
    /**
     * Method for getting founded steps
     * @return Number of steps found in data
     */
    public int getSteps()
    {
        return steps;
    }

    // getting and setting Methods for tweaking different paramenters
   public void setWindowLength(int windowLength)
    {
        this.windowLength = windowLength;
        //System.out.println("WindowLength: "+this.windowLength );
    }

   public void setThresholdTIP(int threshold)
   {
       this.thresholdtip = threshold /100f;
   }

     
   public void setSteplengthMin(int min)
   {
       this.steplengthMin = min;
   }

   public void setSteplengthMax(int max)
   {
       this.steplengthMax = max;
   }
   public void setErrorCovariance(int value)
   {
       this.errorCovariance = value;
   }

   public void setSensorError(int value)
   {
       this.sensorError = value;
   }

   public void setProcessNoise(int value)
   {
       this.processNoise = value;
   }

   public void setDT(int value)
   {
       this.dt = value/100.;
   }


    //method for testing of the algorithm
    public static void main(String[] args)
    {
        Timestamp begin = new Timestamp(new GregorianCalendar(2010,6,9,14,33,00).getTimeInMillis());
        Timestamp end = new Timestamp(new GregorianCalendar(2010,6,9,14,49,00).getTimeInMillis());
       
        int[] werte = {1,3,24,147,400,1000};
        

        for(int i = 1; i <= 1; i++)
        {
            
            
            Stepcount sc = new Stepcount();
            //sc.setSensorError(werte[i]);
            //sc.setErrorCovariance(i);
            //sc.setWindowLength(i);
            //sc.setThresholdTIP(i);
            //sc.setSteplengthMin(i);
            //sc.setSteplengthMax(i);
            //sc.setProcessNoise(i);
            //sc.setDT(werte[i]);
//            sc.iniatialize(begin, end, daten);
            System.out.println("Fenstergröße: "+i +" Gefundene Schritte: "+sc.getSteps());

        }
               

    }

}

