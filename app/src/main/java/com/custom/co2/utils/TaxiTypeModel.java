package com.custom.co2.utils;

public class TaxiTypeModel {

    private int img_taxi;
    private String taxi_name;
    private boolean setSecelt;

    public boolean isSetSecelt() {
        return setSecelt;
    }

    public void setSetSecelt(boolean setSecelt) {
        this.setSecelt = setSecelt;
    }

    public int getImg_taxi() {
        return img_taxi;
    }

    public void setImg_taxi(int img_taxi) {
        this.img_taxi = img_taxi;
    }

    public String getTaxi_name() {
        return taxi_name;
    }

    public void setTaxi_name(String taxi_name) {
        this.taxi_name = taxi_name;
    }
}
