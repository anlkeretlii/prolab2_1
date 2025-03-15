package com.Kocaeli.service;

import com.Kocaeli.model.*;

import java.util.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;

public class RotaHesaplayici {
    private UlasimVerisi ulasimVerisi;

    public RotaHesaplayici(UlasimVerisi ulasimVerisi) {
        this.ulasimVerisi = ulasimVerisi;
    }

    public Rota rotaHesapla(double baslangicLat, double baslangicLon, double hedefLat, double hedefLon, Yolcu yolcu) {
        Rota rota = new Rota();
        Durak baslangicDurak = findNearestDurak(baslangicLat, baslangicLon);
        Durak hedefDurak = findNearestDurak(hedefLat, hedefLon);

        if (baslangicDurak == null || hedefDurak == null) {
            return null; // Durak bulunamadı
        }

        // Basit bir rota simülasyonu (örneğin, taksi ile başla, sonra otobüs/tramvay)
        double taksiMesafe = 2.0; // Sabit taksi mesafesi (örnek)
        double taksiMaliyet = ulasimVerisi.getTaxi().maliyetHesapla(taksiMesafe, yolcu);
        rota.segmentEkle(ulasimVerisi.getTaxi(), taksiMesafe, 5, taksiMaliyet, yolcu, baslangicDurak, hedefDurak);

        // Gerçek duraklar arası mesafe ve süre (JSON'dan nextStops'u kullanabiliriz)
        double toplamMesafe = MesafeHesaplayici.haversine(baslangicLat, baslangicLon, hedefLat, hedefLon);
        int toplamSure = (int) (toplamMesafe * 2);
        double toplamMaliyet = toplamMesafe * 1.5;

        Arac arac = "otobus".equals(baslangicDurak.getType()) ? new Otobus() : new Tramvay();
        rota.segmentEkle(ulasimVerisi.getTaxi(), taksiMesafe, 5, taksiMaliyet, yolcu, baslangicDurak, hedefDurak);

        return rota;
    }

    private Durak findNearestDurak(double lat, double lon) {
        Durak nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Durak durak : ulasimVerisi.getDuraklar()) {
            double distance = MesafeHesaplayici.haversine(lat, lon, durak.getLat(), durak.getLon());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = durak;
            }
        }
        return nearest;
    }
}