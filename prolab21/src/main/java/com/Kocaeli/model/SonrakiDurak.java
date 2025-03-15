package com.Kocaeli.model;

public class SonrakiDurak {
    private String stopId;
    private double mesafe;
    private int sure;
    private double ucret;

    // Parametresiz constructor (Gson için)
    public SonrakiDurak() {}

    // Parametreli constructor
    public SonrakiDurak(String stopId, double mesafe, int sure, double ucret) {
        this.stopId = stopId;
        this.mesafe = mesafe;
        this.sure = sure;
        this.ucret = ucret;
    }

    // Getter'lar
    public String getStopId() { return stopId; }
    public double getMesafe() { return mesafe; }
    public int getSure() { return sure; }
    public double getUcret() { return ucret; }

    // Setter'lar (isteğe bağlı, Gson için gerekli değil ama ekleyebiliriz)
    public void setStopId(String stopId) { this.stopId = stopId; }
    public void setMesafe(double mesafe) { this.mesafe = mesafe; }
    public void setSure(int sure) { this.sure = sure; }
    public void setUcret(double ucret) { this.ucret = ucret; }
}