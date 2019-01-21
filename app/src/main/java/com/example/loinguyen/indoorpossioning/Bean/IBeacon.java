package com.example.loinguyen.indoorpossioning.Bean;

import java.io.Serializable;

public class IBeacon implements Serializable {
    private int id;
    private float xCoord;
    private float yCoord;
    private int rssi1;
    private int rssi2;
    private int rssi3;
    private int Major;
    public IBeacon()
    {

    }
    public IBeacon(float xCoord, float yCoord, int rssi1, int rssi2, int rssi3, int major) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.rssi1 = rssi1;
        this.rssi2 = rssi2;
        this.rssi3 = rssi3;
        Major = major;
    }

    public IBeacon(int id, float xCoord, float yCoord, int rssi1, int rssi2, int rssi3, int major) {
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

    public float getxCoord() {
        return xCoord;
    }

    public void setxCoord(float xCoord) {
        this.xCoord = xCoord;
    }

    public float getyCoord() {
        return yCoord;
    }

    public void setyCoord(float yCoord) {
        this.yCoord = yCoord;
    }

    public int getRssi1() {
        return rssi1;
    }

    public void setRssi1(int rssi1) {
        this.rssi1 = rssi1;
    }

    public int getRssi2() {
        return rssi2;
    }

    public void setRssi2(int rssi2) {
        this.rssi2 = rssi2;
    }

    public int getRssi3() {
        return rssi3;
    }

    public void setRssi3(int rssi3) {
        this.rssi3 = rssi3;
    }

    public int getMajor() {
        return Major;
    }

    public void setMajor(int major) {
        Major = major;
    }
}
