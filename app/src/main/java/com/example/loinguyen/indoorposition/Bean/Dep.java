package com.example.loinguyen.indoorposition.Bean;

public class Dep {
    private int start;
    private int target;
    private double distance;

    public Dep(){};
    public Dep(int start, int target, double distance) {
        this.start = start;
        this.target = target;
        this.distance = distance;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
