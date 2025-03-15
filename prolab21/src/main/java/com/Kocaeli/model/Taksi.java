package com.Kocaeli.model;

public class Taksi extends Arac {
    private double acilisUcreti;
    private double kmBasiUcret;

    public Taksi(double acilisUcreti, double kmBasiUcret) {
        this.tur = "taksi";
        this.acilisUcreti = acilisUcreti;
        this.kmBasiUcret = kmBasiUcret;
    }

    @Override
    public double maliyetHesapla(double mesafe, Yolcu yolcu) {
        return acilisUcreti + (mesafe * kmBasiUcret);
    }
}

