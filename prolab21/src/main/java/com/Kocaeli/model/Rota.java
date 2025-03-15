package com.Kocaeli.model;

import java.util.ArrayList;
import java.util.List;

public class Rota {
    private List<Segment> segmentler = new ArrayList<>();

    public void segmentEkle(Arac arac, double mesafe, int sure, double ucret, Yolcu yolcu, Durak baslangicDurak, Durak hedefDurak) {
        segmentler.add(new Segment(arac, mesafe, sure, ucret, yolcu, baslangicDurak, hedefDurak));
    }


    public double toplamMaliyetGetir() {
        return segmentler.stream().mapToDouble(Segment::getMaliyet).sum();
    }

    public int toplamSureGetir() {
        return segmentler.stream().mapToInt(Segment::getSure).sum();
    }

    public List<Segment> getSegmentler() {
        return segmentler;
    }
    public double getToplamMaliyet() {
        return toplamMaliyetGetir();
    }

    public int getToplamSure() {
        return toplamSureGetir();
    }
}