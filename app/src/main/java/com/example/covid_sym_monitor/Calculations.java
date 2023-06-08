package com.example.covid_sym_monitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Calculations {

    public int countZeroCrossings(List<Double> points) {
        List<Double> extremes = new ArrayList<Double>();
        double previous = points.get(0);
        double previousSlope = 0;
        double p;
        int peak_count = 0;
        for (int i = 1; i < points.size(); i++) {
            p = points.get(i);
            double slope = p - previous;
            if (slope * previousSlope < 0) {
                extremes.add(previous);
                peak_count += 1;
            }
            previousSlope = slope;
            previous = p;
        }
        return peak_count;
    }

    public List<Double> calcMovAvg(int period, List<Double> data) {
        CalculateMovingAverage sma = new CalculateMovingAverage(period);
        List<Double> avgData = sma.getMovingAverage(data);
        return avgData;
    }
}

class CalculateMovingAverage {
    Queue<Double> window = new LinkedList<Double>();
    private final int period;
    private double sum;

    public List<Double> getMovingAverage(List<Double> data){
        List<Double> movingAverage = new ArrayList<Double>(data.size());
        for (double x : data) {
            newNum(x);
            movingAverage.add(getAvg());
        }
        return movingAverage;
    }

    public CalculateMovingAverage(int period) {
        assert period > 0 : "Period must be a positive integer!";
        this.period = period;
    }

    public void newNum(double num) {
        sum += num;
        window.add(num);
        if (window.size() > period) {
            sum -= window.remove();
        }
    }

    public double getAvg() {
        if (window.isEmpty()) return 0; // technically the average is undefined
        return sum / window.size();
    }

}
