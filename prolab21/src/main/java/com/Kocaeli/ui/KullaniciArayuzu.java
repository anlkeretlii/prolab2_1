package com.Kocaeli.ui;

import com.google.gson.Gson;
import com.Kocaeli.model.*;
import com.Kocaeli.service.RotaHesaplayici;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;

public class KullaniciArayuzu {
    public static void main(String[] args) {
        Gson gson = new Gson();
        UlasimVerisi veri;
        try (Reader reader = new InputStreamReader(KullaniciArayuzu.class.getResourceAsStream("/veriseti.json"))) {
            veri = gson.fromJson(reader, UlasimVerisi.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RotaHesaplayici hesaplayici = new RotaHesaplayici(veri);
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);

        System.out.print("Başlangıç enlemini girin: ");
        double baslangicLat = scanner.nextDouble();
        System.out.print("Başlangıç boylamını girin: ");
        double baslangicLon = scanner.nextDouble();
        System.out.print("Hedef enlemini girin: ");
        double hedefLat = scanner.nextDouble();
        System.out.print("Hedef boylamını girin: ");
        double hedefLon = scanner.nextDouble();
        System.out.print("Yolcu tipini girin (genel/ogrenci/yasli): ");
        String yolcuTipi = scanner.next();

        Yolcu yolcu;
        switch (yolcuTipi.toLowerCase()) {
            case "ogrenci": yolcu = new OgrenciYolcu(); break;
            case "yasli": yolcu = new YasliYolcu(); break;
            default: yolcu = new GenelYolcu(); break;
        }

        Rota rota = hesaplayici.rotaHesapla(baslangicLat, baslangicLon, hedefLat, hedefLon, yolcu);
        System.out.println("Toplam Maliyet: " + rota.toplamMaliyetGetir() + " TL");
        System.out.println("Toplam Süre: " + rota.toplamSureGetir() + " dakika");

        // Rota detaylarını yazdır
        for (Segment segment : rota.getSegmentler()) {
            System.out.println(segment.getArac().getTur() + " ile " + segment.getMesafe() + " km, Süre: " + segment.getSure() + " dk, Maliyet: " + segment.getMaliyet() + " TL");
        }
    }
}