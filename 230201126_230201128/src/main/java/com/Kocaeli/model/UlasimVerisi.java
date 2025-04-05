package com.Kocaeli.model;

import java.util.List;

public class UlasimVerisi {
    private String city;
    private TaksiBilgi taxi;
    private List<Durak> duraklar;
    private List<Arac> araclar;
    private double kartUcreti = 1000.0; // Sabit kart ücreti
    private double kalanBakiye = kartUcreti; // Kalan bakiye

    // Getters ve Setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public TaksiBilgi getTaxi() { return taxi; }
    public void setTaxi(TaksiBilgi taxi) { this.taxi = taxi; }
    public List<Durak> getDuraklar() { return duraklar; }
    public void setDuraklar(List<Durak> duraklar) { this.duraklar = duraklar; }
    public List<Arac> getAraclar() { return araclar; }
    public void setAraclar(List<Arac> araclar) { this.araclar = araclar; }
    public double getKartUcreti() { return kartUcreti; }
    public void setKartUcreti(double kartUcreti) { this.kartUcreti = kartUcreti; }
    public double getKalanBakiye() { return kalanBakiye; }
    
    public void kartKullan(double harcananTutar) {
        if (kalanBakiye >= harcananTutar) {
            kalanBakiye -= harcananTutar;
        }
    }
    
    public void kartYukle(double yuklenecekTutar) {
        kalanBakiye += yuklenecekTutar;
    }
    
    public void kartSifirla() {
        kalanBakiye = kartUcreti;
    }
}