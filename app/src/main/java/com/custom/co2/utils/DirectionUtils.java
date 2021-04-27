package com.custom.co2.utils;

import java.io.Serializable;

public class DirectionUtils implements Serializable {
    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setStAddress(String stAddress) {
        this.stAddress = stAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String distance;
    public String duration;
    public String stAddress;
    public String endAddress;
}
