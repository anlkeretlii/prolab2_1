package com.Kocaeli.model;

public class Tramvay extends Arac {
    public Tramvay() {
        this.tur = "tramvay";
    }

    @Override
    public double maliyetHesapla(double mesafe, Yolcu yolcu) {
        return mesafe * 0.5; // jsondan çekilecek
    }
}