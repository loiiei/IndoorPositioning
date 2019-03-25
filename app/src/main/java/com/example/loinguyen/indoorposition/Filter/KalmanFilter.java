package com.example.loinguyen.indoorposition.Filter;

/**
 * Using Kalman Filter
 */
public class KalmanFilter implements Filter{
    private double processNoise;
    private double measurementNoise;
    private double estimateRSSI;
    private double errorCovarianceRSSI;
    private boolean isIntilized = false;

    public KalmanFilter()
    {
        this.processNoise = 0;
        this.measurementNoise = 5;
    }

    public KalmanFilter(double processNoise, double measurementNoise) {
        this.processNoise = processNoise;
        this.measurementNoise = measurementNoise;
    }

    @Override
    public double updateRssi(double rssi) {
        double priorRSSI;
        double priorErrorCovarianceRSSI;
        double kalmanGain;
        if(!isIntilized){
            priorRSSI = rssi;
            priorErrorCovarianceRSSI = 10;
            isIntilized = true;
        }
        //estimate rssi
        else
        {
            //estimate rssi is the old one
            priorRSSI = estimateRSSI;
            //errorCovariance is the old one
            priorErrorCovarianceRSSI = errorCovarianceRSSI + processNoise;
        }

        kalmanGain = errorCovarianceRSSI / (errorCovarianceRSSI + measurementNoise);
        estimateRSSI =  priorRSSI + kalmanGain * (rssi - priorRSSI);
        errorCovarianceRSSI = (1 - kalmanGain) * priorErrorCovarianceRSSI;
        return estimateRSSI;
    }
}