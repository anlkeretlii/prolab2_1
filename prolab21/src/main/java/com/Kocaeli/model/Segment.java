package com.Kocaeli.model;

public class Segment {
    private Arac arac;
    private double mesafe;
    private double maliyet;
    private int sure;
    private Durak baslangicDurak; // Başlangıç durağı eklendi
    private Durak hedefDurak; // Hedef durağı eklendi

    public Segment(Arac arac, double mesafe, int sure, double ucret, Yolcu yolcu, Durak baslangicDurak, Durak hedefDurak) {
        this.arac = arac;
        this.mesafe = mesafe;
        this.sure = sure;
        this.maliyet = yolcu.indirimUygula(ucret);
        this.baslangicDurak = baslangicDurak;
        this.hedefDurak = hedefDurak;
    }

    public double getMaliyet() { return maliyet; }
    public int getSure() { return sure; }
    public Arac getArac() { return arac; }
    public double getMesafe() { return mesafe; }

    // Eksik metodlar eklendi
    public Durak getBaslangicDurak() { return baslangicDurak; }
    public Durak getHedefDurak() { return hedefDurak; }
}
