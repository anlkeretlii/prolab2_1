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
        aracTipleri.put(baslangic.getId(), baslangic.getType());

        System.out.println("Rota hesaplanıyor: " + baslangic.getId() + " -> " + hedef.getId());

        while (!kuyruk.isEmpty()) {
            String suankiId = kuyruk.poll();
            Durak suanki = durakMap.get(suankiId);

            if (suankiId.equals(hedef.getId()) || !ziyaretEdilen.add(suankiId)) {
                continue;
            }

            // Doğrudan bağlantılar
            if (suanki.getNextStops() != null) {
                for (SonrakiDurak next : suanki.getNextStops()) {
                    int yeniSure = sureler.get(suankiId) + next.getSure();
                    if (!sureler.containsKey(next.getStopId()) || yeniSure < sureler.get(next.getStopId())) {
                        sureler.put(next.getStopId(), yeniSure);
                        oncekiDurak.put(next.getStopId(), suankiId);
                        durakSegmentleri.put(next.getStopId(), next);
                        aracTipleri.put(next.getStopId(), suanki.getType());
                        kuyruk.add(next.getStopId());
                    }
                }
            }

            // Ters yön bağlantılar
            for (Durak digerDurak : durakMap.values()) {
                if (!digerDurak.getId().equals(suankiId) && digerDurak.getNextStops() != null) {
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
                            aracTipleri.put(digerDurak.getId(), digerDurak.getType());
                            kuyruk.add(digerDurak.getId());
                        }
                    }
                }
            }

            // Aktarma bağlantıları
            if (suanki.getTransfer() != null) {
                String transferId = suanki.getTransfer().getTransferStopId();
                int transferSure = suanki.getTransfer().getTransferSure();
                double transferUcret = suanki.getTransfer().getTransferUcret();
                int yeniSure = sureler.get(suankiId) + transferSure;
                if (!sureler.containsKey(transferId) || yeniSure < sureler.get(transferId)) {
                    double mesafe = MesafeHesaplayici.haversine(suanki.getLat(), suanki.getLon(), 
                                                                durakMap.get(transferId).getLat(), durakMap.get(transferId).getLon());
                    sureler.put(transferId, yeniSure);
                    oncekiDurak.put(transferId, suankiId);
                    durakSegmentleri.put(transferId, new SonrakiDurak(transferId, mesafe, transferSure, transferUcret));
                    aracTipleri.put(transferId, suanki.getType());
                    kuyruk.add(transferId);
                }
            }
        }

        if (!sureler.containsKey(hedef.getId())) {
            System.out.println("Hata: Hedefe ulaşılamadı.");
            return null;
        }

        Rota rota = new Rota();
        String currentId = hedef.getId();
        while (currentId != null && !currentId.equals(baslangic.getId())) {
            String oncekiId = oncekiDurak.get(currentId);
            SonrakiDurak segment = durakSegmentleri.get(currentId);
            String aracTipi = aracTipleri.get(currentId);
            Arac arac = aracTipi.equals("bus") ? new Otobus() : new Tramvay();
            double ucret = segment.getUcret();
            if (yolcu.getTur().equals("ogrenci")) ucret *= 0.5;
            if (java.time.LocalDate.now().getDayOfMonth() == 1) ucret = 0.0;
            double mesafe = Math.round(segment.getMesafe() * 100.0) / 100.0; // 2 basamak yuvarlama
            
            // Segment oluştur ve durak bilgilerini ekle
            Segment yeniSegment = new Segment(arac, mesafe, segment.getSure(), ucret, yolcu);
            yeniSegment.setBaslangicDurak(durakMap.get(oncekiId));
            yeniSegment.setBitisDurak(durakMap.get(currentId));
            
            // Aktarma varsa bilgilerini ekle
            Durak suankiDurak = durakMap.get(currentId);
            if (suankiDurak.getTransfer() != null && 
                suankiDurak.getTransfer().getTransferStopId().equals(oncekiId)) {
                yeniSegment.setAktarmaSuresi(suankiDurak.getTransfer().getTransferSure());
                yeniSegment.setAktarmaUcreti(suankiDurak.getTransfer().getTransferUcret());
            }
            
            rota.segmentEkle(yeniSegment);
            currentId = oncekiId;
        }
        Collections.reverse(rota.getSegmentler());

        // Sadeleştirilmiş çıktı
        System.out.println("\nEn kısa rota bulundu:");
        System.out.println("Başlangıç: " + baslangic.getId() + " -> Hedef: " + hedef.getId());
        System.out.println("Toplam Süre: " + sureler.get(hedef.getId()) + " dk");
        double toplamMaliyet = Math.round(rota.getSegmentler().stream().mapToDouble(Segment::getMaliyet).sum() * 10.0) / 10.0; // 1 basamak yuvarlama
        System.out.println("Toplam Maliyet: " + toplamMaliyet + " TL");
        System.out.println("Rota detayları:");
        for (Segment segment : rota.getSegmentler()) {
            String arac = segment.getArac() instanceof Otobus ? "Otobüs" : "Tramvay";
            System.out.printf("- %s: %.2f km, %d dk, %.1f TL\n", arac, segment.getMesafe(), segment.getSure(), segment.getMaliyet());
        }

        return rota;
    }

    public Rota rotaHesapla(double baslangicLat, double baslangicLon, double hedefLat, double hedefLon, Yolcu yolcu) {
        Durak baslangicDurak = enYakinDuragiBul(baslangicLat, baslangicLon);
        Durak hedefDurak = enYakinDuragiBul(hedefLat, hedefLon);
        double baslangicMesafe = Math.round(MesafeHesaplayici.haversine(baslangicLat, baslangicLon, baslangicDurak.getLat(), baslangicDurak.getLon()) * 100.0) / 100.0;
        double hedefMesafe = Math.round(MesafeHesaplayici.haversine(hedefLat, hedefLon, hedefDurak.getLat(), hedefDurak.getLon()) * 100.0) / 100.0;

        Rota rota = new Rota();
        Taksi taksi = new Taksi(veri.getTaxi().getOpeningFee(), veri.getTaxi().getCostPerKm());

        System.out.println("Başlangıç: " + baslangicDurak.getId() + ", Hedef: " + hedefDurak.getId());
        System.out.println("Başlangıç mesafe: " + baslangicMesafe + " km, Hedef mesafe: " + hedefMesafe + " km");

        if (baslangicMesafe > 3.0) {
            double taksiUcreti = taksi.maliyetHesapla(baslangicMesafe, yolcu);
            Segment taksiSegment = new Segment(taksi, baslangicMesafe, (int)(baslangicMesafe * 10), taksiUcreti, yolcu);
            taksiSegment.setBitisDurak(baslangicDurak);
            rota.segmentEkle(taksiSegment);
        }

        if (!baslangicDurak.getId().equals(hedefDurak.getId())) {
            Rota topluTasimaRotasi = bulEnKisaRota(baslangicDurak, hedefDurak, yolcu);
            if (topluTasimaRotasi != null) {
                rota.getSegmentler().addAll(topluTasimaRotasi.getSegmentler());
            } else {
                System.out.println("Uyarı: Rota bulunamadı.");
            }
        }

        if (hedefMesafe > 3.0) {
            double taksiUcreti = taksi.maliyetHesapla(hedefMesafe, yolcu);
            Segment taksiSegment = new Segment(taksi, hedefMesafe, (int)(hedefMesafe * 10), taksiUcreti, yolcu);
            taksiSegment.setBaslangicDurak(hedefDurak);
            rota.segmentEkle(taksiSegment);
        }

        return rota;
    }
}