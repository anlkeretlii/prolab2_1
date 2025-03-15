package com.Kocaeli.service;

import com.Kocaeli.model.*;

import java.util.*;

public class RotaHesaplayici {
    private UlasimVerisi veri;
    private Map<String, Durak> durakMap;

    public RotaHesaplayici(UlasimVerisi veri) {
        this.veri = veri;
        this.durakMap = new HashMap<>();
        for (Durak durak : veri.getDuraklar()) {
            durakMap.put(durak.getId(), durak);
        }
    }

    public Durak enYakinDuragiBul(double lat, double lon) {
        Durak enYakin = null;
        double minMesafe = Double.MAX_VALUE;
        for (Durak durak : veri.getDuraklar()) {
            double mesafe = MesafeHesaplayici.haversine(lat, lon, durak.getLat(), durak.getLon());
            if (mesafe < minMesafe) {
                minMesafe = mesafe;
                enYakin = durak;
            }
        }
        return enYakin;
    }

    private Rota bulEnKisaRota(Durak baslangic, Durak hedef, Yolcu yolcu) {
        Map<String, Integer> sureler = new HashMap<>();
        Map<String, String> oncekiDurak = new HashMap<>();
        Map<String, SonrakiDurak> durakSegmentleri = new HashMap<>();
        Map<String, String> aracTipleri = new HashMap<>();
        PriorityQueue<String> kuyruk = new PriorityQueue<>(Comparator.comparing(sureler::get));
        Set<String> ziyaretEdilen = new HashSet<>();

        sureler.put(baslangic.getId(), 0);
        kuyruk.add(baslangic.getId());
        aracTipleri.put(baslangic.getId(), baslangic.getId().startsWith("bus") ? "otobus" : "tramvay");

        System.out.println("Dijkstra Başladı - Başlangıç: " + baslangic.getId() + ", Hedef: " + hedef.getId());

        while (!kuyruk.isEmpty()) {
            String suankiId = kuyruk.poll();
            Durak suanki = durakMap.get(suankiId);
            System.out.println("Ziyaret edilen durak: " + suankiId + " (Süre: " + sureler.get(suankiId) + " dk)");

            if (suankiId.equals(hedef.getId())) {
                if (!ziyaretEdilen.contains(suankiId)) {
                    ziyaretEdilen.add(suankiId);
                }
            } else if (!ziyaretEdilen.contains(suankiId)) {
                ziyaretEdilen.add(suankiId);
            } else {
                continue;
            }

            // Doğrudan bağlantılar (JSON’dan ücret)
            if (suanki.getNextStops() != null) {
                for (SonrakiDurak next : suanki.getNextStops()) {
                    int yeniSure = sureler.get(suankiId) + next.getSure();
                    if (!sureler.containsKey(next.getStopId()) || yeniSure < sureler.get(next.getStopId())) {
                        sureler.put(next.getStopId(), yeniSure);
                        oncekiDurak.put(next.getStopId(), suankiId);
                        durakSegmentleri.put(next.getStopId(), next);
                        aracTipleri.put(next.getStopId(), suankiId.startsWith("bus") ? "otobus" : "tramvay");
                        kuyruk.add(next.getStopId());
                        System.out.println("Doğrudan bağlantı güncellendi: " + suankiId + " -> " + next.getStopId() + " (Süre: " + yeniSure + " dk, Ücret: " + next.getUcret() + " TL)");
                    }
                }
            }

            // Ters yön bağlantılar (JSON’dan ücret)
            for (Durak digerDurak : durakMap.values()) {
                if (!digerDurak.getId().equals(suankiId)) {
                    if (digerDurak.getNextStops() != null) {
                        SonrakiDurak tersYon = digerDurak.getNextStops().stream()
                                .filter(ns -> ns.getStopId().equals(suankiId))
                                .findFirst()
                                .orElse(null);
                        if (tersYon != null) {
                            int yeniSure = sureler.get(suankiId) + tersYon.getSure();
                            if (!sureler.containsKey(digerDurak.getId()) || yeniSure < sureler.get(digerDurak.getId())) {
                                sureler.put(digerDurak.getId(), yeniSure);
                                oncekiDurak.put(digerDurak.getId(), suankiId);
                                durakSegmentleri.put(digerDurak.getId(), new SonrakiDurak(suankiId, tersYon.getMesafe(), tersYon.getSure(), tersYon.getUcret()));
                                aracTipleri.put(digerDurak.getId(), digerDurak.getId().startsWith("bus") ? "otobus" : "tramvay");
                                kuyruk.add(digerDurak.getId());
                                System.out.println("Ters yön güncellendi: " + digerDurak.getId() + " -> " + suankiId + " (Süre: " + yeniSure + " dk, Ücret: " + tersYon.getUcret() + " TL)");
                            }
                        }
                    }
                }
            }

            // Aktarma bağlantıları (ücretsiz)
            for (Durak digerDurak : durakMap.values()) {
                if (!digerDurak.getId().equals(suankiId) && !suanki.getId().startsWith(digerDurak.getId().substring(0, 4))) {
                    double aktarmaMesafe = MesafeHesaplayici.haversine(suanki.getLat(), suanki.getLon(), digerDurak.getLat(), digerDurak.getLon());
                    if (aktarmaMesafe < 0.5) {
                        int yeniSure = sureler.get(suankiId) + 5;
                        if (!sureler.containsKey(digerDurak.getId()) || yeniSure < sureler.get(digerDurak.getId())) {
                            sureler.put(digerDurak.getId(), yeniSure);
                            oncekiDurak.put(digerDurak.getId(), suankiId);
                            durakSegmentleri.put(digerDurak.getId(), new SonrakiDurak(suankiId, aktarmaMesafe, 5, 0.0));
                            aracTipleri.put(digerDurak.getId(), digerDurak.getId().startsWith("bus") ? "otobus" : "tramvay");
                            kuyruk.add(digerDurak.getId());
                            System.out.println("Aktarma güncellendi: " + suankiId + " -> " + digerDurak.getId() + " (Süre: " + yeniSure + " dk, Ücret: 0.0 TL)");
                        }
                    }
                }
            }
        }

        if (!sureler.containsKey(hedef.getId())) {
            System.out.println("Hata: Hedef durak (" + hedef.getId() + ") bulunamadı. Ziyaret edilenler: " + ziyaretEdilen);
            return null;
        }

        Rota rota = new Rota();
        String currentId = hedef.getId();
        while (currentId != null && !currentId.equals(baslangic.getId())) {
            String oncekiId = oncekiDurak.get(currentId);
            SonrakiDurak segment = durakSegmentleri.get(currentId);
            String aracTipi = aracTipleri.get(currentId);
            Arac arac = aracTipi.equals("otobus") ? new Otobus() : new Tramvay();
            rota.segmentEkle(arac, segment.getMesafe(), segment.getSure(), segment.getUcret(), yolcu); // JSON’dan ücret
            System.out.println("Segment eklendi: " + oncekiId + " -> " + currentId + " (" + segment.getMesafe() + " km, " + segment.getSure() + " dk, " + segment.getUcret() + " TL, " + aracTipi + ")");
            currentId = oncekiId;
        }
        Collections.reverse(rota.getSegmentler());
        return rota;
    }

    public Rota rotaHesapla(double baslangicLat, double baslangicLon, double hedefLat, double hedefLon, Yolcu yolcu) {
        Durak baslangicDurak = enYakinDuragiBul(baslangicLat, baslangicLon);
        Durak hedefDurak = enYakinDuragiBul(hedefLat, hedefLon);
        double baslangicMesafe = MesafeHesaplayici.haversine(baslangicLat, baslangicLon, baslangicDurak.getLat(), baslangicDurak.getLon());
        double hedefMesafe = MesafeHesaplayici.haversine(hedefLat, hedefLon, hedefDurak.getLat(), hedefDurak.getLon());

        Rota rota = new Rota();
        Taksi taksi = new Taksi(veri.getTaxi().getOpeningFee(), veri.getTaxi().getCostPerKm());

        System.out.println("Başlangıç durak: " + baslangicDurak.getId() + ", Hedef durak: " + hedefDurak.getId());
        System.out.println("Başlangıç mesafe: " + baslangicMesafe + " km, Hedef mesafe: " + hedefMesafe + " km");

        if (baslangicMesafe > 3.0) {
            double taksiUcreti = taksi.maliyetHesapla(baslangicMesafe, yolcu);
            rota.segmentEkle(taksi, baslangicMesafe, (int)(baslangicMesafe * 10), taksiUcreti, yolcu);
        }

        if (!baslangicDurak.getId().equals(hedefDurak.getId())) {
            Rota topluTasimaRotasi = bulEnKisaRota(baslangicDurak, hedefDurak, yolcu);
            if (topluTasimaRotasi != null) {
                for (Segment segment : topluTasimaRotasi.getSegmentler()) {
                    rota.segmentEkle(segment.getArac(), segment.getMesafe(), segment.getSure(), segment.getMaliyet(), yolcu);
                }
            } else {
                System.out.println("Uyarı: Duraklar arasında rota bulunamadı.");
            }
        }

        if (hedefMesafe > 3.0) {
            double taksiUcreti = taksi.maliyetHesapla(hedefMesafe, yolcu);
            rota.segmentEkle(taksi, hedefMesafe, (int)(hedefMesafe * 10), taksiUcreti, yolcu);
        }

        return rota;
    }
}