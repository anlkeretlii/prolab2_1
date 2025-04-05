package com.Kocaeli.model;

public class YasliYolcu extends Yolcu {
    private int kalanUcretsizSeyahat = 20; // Örnek: 20 ücretsiz seyahat limiti

    public YasliYolcu() {
        this.tur = "yasli";
    }

    @Override
    public double indirimUygula(double temelMaliyet) {
        if (kalanUcretsizSeyahat > 0) {
            kalanUcretsizSeyahat--;
            return 0.0; // Ücretsiz
        }
        return temelMaliyet; // Limit dolduysa tam ücret
    }
}