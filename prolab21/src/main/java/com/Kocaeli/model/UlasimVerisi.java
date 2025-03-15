package com.Kocaeli.model;

import java.util.List;

public class UlasimVerisi {
    private String city;
    private TaksiBilgi taxi;
    private List<Durak> duraklar;

    // Getters ve Setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public TaksiBilgi getTaxi() { return taxi; }
    public void setTaxi(TaksiBilgi taxi) { this.taxi = taxi; }
    public List<Durak> getDuraklar() { return duraklar; }
    public void setDuraklar(List<Durak> duraklar) { this.duraklar = duraklar; }
}