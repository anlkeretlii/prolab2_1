package com.Kocaeli.model;

public class OgrenciYolcu extends Yolcu {
    public OgrenciYolcu() {
        this.tur = "ogrenci";
    }

    @Override
    public double indirimUygula(double temelMaliyet) {
        return temelMaliyet * 0.5; // %50 indirim
    }
}