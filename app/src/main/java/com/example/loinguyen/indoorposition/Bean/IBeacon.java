package com.example.loinguyen.indoorposition.Bean;

import java.io.Serializable;

public class IBeacon implements Serializable {
    private int id;
    private double xCoord;
    private double yCoord;
    private double rssi1;
    private double rssi2;
    private double rssi3;
    private int Major;
    public IBeacon()
    {

    }
    public IBeacon(double xCoord, double yCoord, float rssi1, float rssi2, float rssi3, int major) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.rssi1 = rssi1;
        this.rssi2 = rssi2;
        this.rssi3 = rssi3;
        Major = major;
    }

    public IBeacon(int id, double xCoord, double yCoord, float rssi1, float rssi2, float rssi3, int major) {
        this.id = id;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.rssi1 = rssi1;
        this.rssi2 = rssi2;
        this.rssi3 = rssi3;
        Major = major;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getxCoord() {
        return xCoord;
    }

    public void setxCoord(double xCoord) {
        this.xCoord = xCoord;
    }

    public double getyCoord() {
        return yCoord;
    }

    public void setyCoord(double yCoord) {
        this.yCoord = yCoord;
    }

    public double getRssi1() {
        return rssi1;
    }

    public void setRssi1(float rssi1) {
        this.rssi1 = rssi1;
    }

    public double getRssi2() {
        return rssi2;
    }

    public void setRssi2(float rssi2) {
        this.rssi2 = rssi2;
    }

    public double getRssi3() {
        return rssi3;
    }

    public void setRssi3(float rssi3) {
        this.rssi3 = rssi3;
    }

    public int getMajor() {
        return Major;
    }

    public void setMajor(int major) {
        Major = major;
    }
}
