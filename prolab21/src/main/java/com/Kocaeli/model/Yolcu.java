package com.Kocaeli.model;

public abstract class Yolcu {
    protected String tur;
    
    public abstract double indirimUygula(double temelMaliyet);
    
    public String getTur() {
        return tur;
    }
}
