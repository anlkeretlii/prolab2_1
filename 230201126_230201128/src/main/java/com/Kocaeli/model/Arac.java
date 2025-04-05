package com.Kocaeli.model;

public abstract class Arac {
    protected String tur;

    public abstract double maliyetHesapla(double mesafe, Yolcu yolcu);

    public String getTur() {
        return tur;
    }
}