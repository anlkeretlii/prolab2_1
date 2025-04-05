package com.Kocaeli.model;

public class SonrakiDurak {
    private String stopId;   // Sonraki durağın ID’si
    private double mesafe;   // Mesafe (km)
    private int sure;        // Süre (dakika)
    private double ucret;    // Ücret (TL)

    // JSON’dan gelen veriler için varsayılan yapıcı (Gson tarafından kullanılır)
    public SonrakiDurak() {
    }

    // Ters yön desteği için manuel yapıcı
    public SonrakiDurak(String stopId, double mesafe, int sure, double ucret) {
        this.stopId = stopId;
        this.mesafe = mesafe;
        this.sure = sure;
        this.ucret = ucret;
    }

    // Getter’lar
    public String getStopId() {
        return stopId;
    }

    public double getMesafe() {
        return mesafe;
    }

    public int getSure() {
        return sure;
    }

    public double getUcret() {
        return ucret;
    }

    // Setter’lar (Gson için gerekli)
    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public void setMesafe(double mesafe) {
        this.mesafe = mesafe;
    }

    public void setSure(int sure) {
        this.sure = sure;
    }

    public void setUcret(double ucret) {
        this.ucret = ucret;
    }

    // Debugging için toString metodu (opsiyonel)
    @Override
    public String toString() {
        return "SonrakiDurak{" +
                "stopId='" + stopId + '\'' +
                ", mesafe=" + mesafe +
                ", sure=" + sure +
                ", ucret=" + ucret +
                '}';
    }
}