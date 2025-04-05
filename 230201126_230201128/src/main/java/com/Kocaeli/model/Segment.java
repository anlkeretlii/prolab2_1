package com.Kocaeli.model;

public class Segment {
    private Arac arac;
    private double mesafe;
    private double maliyet;
    private int sure;
    private Durak baslangicDurak;
    private Durak bitisDurak;
    private int aktarmaSuresi;
    private double aktarmaUcreti;
    private boolean isTaxi = false;
    private double taxiDistance = 0.0;

    public Segment(Arac arac, double mesafe, int sure, double ucret, Yolcu yolcu) {
        this.arac = arac;
        this.mesafe = mesafe;
        this.sure = sure;
        this.maliyet = yolcu.indirimUygula(ucret); // İndirim uygula
        this.aktarmaSuresi = 0;
        this.aktarmaUcreti = 0;
    }

    public double getMaliyet() { return maliyet; }
    public double getOrijinalMaliyet() { return maliyet * (this.arac.getTur().equals("otobus") || this.arac.getTur().equals("tramvay") ? 2.0 : 1.0); } // İndirimsiz ücret
    public int getSure() { return sure; }
    public Arac getArac() { return arac; }
    public double getMesafe() { return mesafe; }
    public Durak getBaslangicDurak() { return baslangicDurak; }
    public Durak getBitisDurak() { return bitisDurak; }
    public int getAktarmaSuresi() { return aktarmaSuresi; }
    public double getAktarmaUcreti() { return aktarmaUcreti; }

    public void setBaslangicDurak(Durak durak) { this.baslangicDurak = durak; }
    public void setBitisDurak(Durak durak) { this.bitisDurak = durak; }
    public void setAktarmaSuresi(int sure) { this.aktarmaSuresi = sure; }
    public void setAktarmaUcreti(double ucret) { this.aktarmaUcreti = ucret; }

    public void setTaxiInfo(double distance) {
        this.isTaxi = true;
        this.taxiDistance = distance;
        // Taksi ücreti: 10 TL taban + km başı 4 TL
        this.maliyet = 10.0 + (distance * 4.0);
        // Taksi süresi: km başı 2 dakika
        this.sure = (int)(distance * 2);
    }
    
    public boolean isTaxi() {
        return isTaxi;
    }
    
    public double getTaxiDistance() {
        return taxiDistance;
    }
}