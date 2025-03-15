package com.Kocaeli.model;

public class GenelYolcu extends Yolcu {
    public GenelYolcu() {
        this.tur = "genel";
    }

    @Override
    public double indirimUygula(double temelMaliyet) {
        return temelMaliyet;
    }
}