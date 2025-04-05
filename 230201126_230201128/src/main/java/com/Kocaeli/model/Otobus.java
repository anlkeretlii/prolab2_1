package com.Kocaeli.model;

public class Otobus extends Arac {
    public Otobus() {
        this.tur = "otobus";
    }

    @Override
    public double maliyetHesapla(double mesafe, Yolcu yolcu) {
        return mesafe * 0.86; // jsondan çekilecek
    }
}