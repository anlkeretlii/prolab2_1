package com.Kocaeli.model;

public class Segment {
    private Arac arac;
    private double mesafe;
    private double maliyet;
    private int sure;

    public Segment(Arac arac, double mesafe, int sure, double ucret, Yolcu yolcu) {
        this.arac = arac;
        this.mesafe = mesafe;
        this.sure = sure;
        this.maliyet = yolcu.indirimUygula(ucret); // JSON’dan gelen ücreti kullan
    }

    public double getMaliyet() { return maliyet; }
    public int getSure() { return sure; }
    public Arac getArac() { return arac; }
    public double getMesafe() { return mesafe; }
}