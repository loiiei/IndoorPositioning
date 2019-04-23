package com.example.loinguyen.indoorposition.Bean;

import java.io.Serializable;

public class IBeacon implements Serializable {
    private int id;
    private double x;
    private double y;
    private double rssi1;
    private double rssi2;
    private double rssi3;
    private int major;
    private int roomid;
    public IBeacon()
    {

    }
    public IBeacon(double x, double y, float rssi1, float rssi2, float rssi3, int major) {
        this.x = x;
        this.y = y;
        this.rssi1 = rssi1;
        this.rssi2 = rssi2;
        this.rssi3 = rssi3;
        this.major = major;
    }

    public IBeacon(int id, double x, double y, float rssi1, float rssi2, float rssi3, int major, int roomid) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.rssi1 = rssi1;
        this.rssi2 = rssi2;
        this.rssi3 = rssi3;
        this.major = major;
        this.roomid = roomid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setRssi1(double rssi1) {
        this.rssi1 = rssi1;
    }

    public void setRssi2(double rssi2) {
        this.rssi2 = rssi2;
    }

    public void setRssi3(double rssi3) {
        this.rssi3 = rssi3;
    }

    public double getRssi1() {
        return rssi1;
    }

    public double getRssi2() {
        return rssi2;
    }

    public double getRssi3() {
        return rssi3;
    }

    public int getMajor() {
        return this.major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getRoomid() {
        return roomid;
    }

    public void setRoomid(int roomid) {
        this.roomid = roomid;
    }
}
