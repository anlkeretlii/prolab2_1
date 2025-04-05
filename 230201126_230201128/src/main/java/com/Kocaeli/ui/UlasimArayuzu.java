package com.Kocaeli.ui;

import com.google.gson.Gson;
import com.Kocaeli.model.*;
import com.Kocaeli.service.RotaHesaplayici;
import com.Kocaeli.service.MesafeHesaplayici;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class UlasimArayuzu extends JFrame {
    private JTextField baslangicKoordinat;
    private JTextField hedefKoordinat;
    private JComboBox<String> yolcuTipi;
    private JComboBox<String> odemeTipi;
    private JXMapViewer mapViewer;
    private JTextArea rotaBilgisi;
    private RotaHesaplayici rotaHesaplayici;
    private UlasimVerisi veri;
    private Set<DurakWaypoint> waypoints;
    private Map<String, Durak> durakMap;

    // Özel Waypoint sınıfı
    private static class DurakWaypoint implements Waypoint {
        private final GeoPosition pos;
        private final String name;

        public DurakWaypoint(double lat, double lon, String name) {
            this.pos = new GeoPosition(lat, lon);
            this.name = name;
        }

        @Override
        public GeoPosition getPosition() {
            return pos;
        }

        public String getName() {
            return name;
        }
    }

    // Özel WaypointRenderer sınıfı
    private static class DurakWaypointRenderer implements WaypointRenderer<DurakWaypoint> {
        private final Color fillColor = new Color(0, 150, 255, 200);
        private final Color borderColor = new Color(0, 100, 255);
        private final Color textBgColor = new Color(255, 255, 255, 200);
        private final int size = 12;

        @Override
        public void paintWaypoint(Graphics2D g, JXMapViewer map, DurakWaypoint waypoint) {
            Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());
            int x = (int) point.getX() - size / 2;
            int y = (int) point.getY() - size / 2;

            // Anti-aliasing için
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Durak noktası çizimi
            g.setColor(fillColor);
            g.fillOval(x, y, size, size);
            g.setColor(borderColor);
            g.setStroke(new BasicStroke(2));
            g.drawOval(x, y, size, size);

            // Durak ismi gösterimi
            String name = waypoint.getName();
            g.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(name);
            int textHeight = fm.getHeight();

            // İsim için arka plan - artık tüm duraklar için göster
            int padding = 4;
            g.setColor(textBgColor);
            g.fillRoundRect(x - textWidth/2 + size/2 - padding, 
                          y - textHeight - padding,
                          textWidth + padding*2,
                          textHeight + padding,
                          8, 8);
            
            g.setColor(Color.BLACK);
            g.drawString(name, x - textWidth/2 + size/2, y - 5);
        }
    }

    public UlasimArayuzu() {
        setTitle("EKOMOBİL");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        initializeComponents();
        loadData();
        layoutComponents();
    }

    private void initializeComponents() {
        // Sol panel bileşenleri
        baslangicKoordinat = new JTextField();
        hedefKoordinat = new JTextField();
        yolcuTipi = new JComboBox<>(new String[]{"Genel Yolcu", "Öğrenci", "Yaşlı"});
        odemeTipi = new JComboBox<>(new String[]{"Nakit", "Kart"});
        
        // Harita bileşeni
        mapViewer = createMapViewer();
        
        // Rota bilgisi alanı
        rotaBilgisi = new JTextArea();
        rotaBilgisi.setEditable(false);
        
        waypoints = new HashSet<>();

        // Mouse listener ekle
        mapViewer.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                mapViewer.repaint();
            }
        });
    }

    private void loadData() {
        Gson gson = new Gson();
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/veriseti.json"))) {
            veri = gson.fromJson(reader, UlasimVerisi.class);
            rotaHesaplayici = new RotaHesaplayici(veri);
            
            // durakMap'i oluştur
            durakMap = new HashMap<>();
            for (Durak durak : veri.getDuraklar()) {
                durakMap.put(durak.getId(), durak);
            }
            
            duraklariHaritayaEkle();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Veri yüklenirken hata oluştu: " + e.getMessage(),
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void layoutComponents() {
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Sol panel
        JPanel solPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        solPanel.setBorder(BorderFactory.createTitledBorder("Giriş Koordinatları"));
        
        solPanel.add(new JLabel("Başlangıç Koordinatı:"));
        solPanel.add(baslangicKoordinat);
        
        solPanel.add(new JLabel("Hedef Koordinatı:"));
        solPanel.add(hedefKoordinat);
        
        solPanel.add(new JLabel("Yolcu Türü:"));
        solPanel.add(yolcuTipi);
        
        solPanel.add(new JLabel("Ödeme Türü:"));
        solPanel.add(odemeTipi);
        
        JButton rotaOlustur = new JButton("Rota Oluştur");
        rotaOlustur.addActionListener(e -> hesaplaVeGoster());
        solPanel.add(rotaOlustur);
        
        // Rota bilgisi scroll panel
        JScrollPane scrollPane = new JScrollPane(rotaBilgisi);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        
        // Panelleri yerleştir
        mainPanel.add(solPanel, BorderLayout.WEST);
        mainPanel.add(mapViewer, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JXMapViewer createMapViewer() {
        mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        mapViewer.setTileFactory(tileFactory);

        // Kocaeli bölgesini merkeze al - daha yakın zoom seviyesi
        GeoPosition kocaeli = new GeoPosition(40.7654, 29.9408);
        mapViewer.setZoom(6); // Daha yakın zoom seviyesi
        mapViewer.setAddressLocation(kocaeli);

        // Mouse wheel zoom desteği
        mapViewer.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                mapViewer.setZoom(Math.max(1, mapViewer.getZoom() - 1));
            } else {
                mapViewer.setZoom(Math.min(15, mapViewer.getZoom() + 1));
            }
        });

        // Pan desteği ekle
        PanMouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);

        return mapViewer;
    }

    private void duraklariHaritayaEkle() {
        waypoints.clear();
        for (Durak durak : veri.getDuraklar()) {
            String durakAdi = durak.getName();
            if (durakAdi == null || durakAdi.trim().isEmpty()) {
                durakAdi = durak.getId();
            }

            // Durak tipine göre renk ve stil ayarla
            DurakWaypoint waypoint = new DurakWaypoint(durak.getLat(), durak.getLon(), durakAdi);
            waypoints.add(waypoint);
        }
        
        WaypointPainter<DurakWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        
        // Özel renderer kullan
        DurakWaypointRenderer renderer = new DurakWaypointRenderer();
        waypointPainter.setRenderer(renderer);
        
        mapViewer.setOverlayPainter(waypointPainter);
        
        // Haritayı tüm durakları gösterecek şekilde ayarla
        Set<GeoPosition> positions = new HashSet<>();
        for (DurakWaypoint waypoint : waypoints) {
            positions.add(waypoint.getPosition());
        }
        mapViewer.zoomToBestFit(positions, 0.7);
    }

    private void rotayiHaritadaGoster(List<GeoPosition> rotaNoktalar) {
        RoutePainter routePainter = new RoutePainter(rotaNoktalar);
        WaypointPainter<DurakWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new DurakWaypointRenderer());
        
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(routePainter);
        painters.add(waypointPainter);
        
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);
    }

    private void hesaplaVeGoster() {
        try {
            String[] baslangic = baslangicKoordinat.getText().split(",");
            String[] hedef = hedefKoordinat.getText().split(",");
            
            double baslangicLat = Double.parseDouble(baslangic[0].trim());
            double baslangicLon = Double.parseDouble(baslangic[1].trim());
            double hedefLat = Double.parseDouble(hedef[0].trim());
            double hedefLon = Double.parseDouble(hedef[1].trim());
            
            // Yolcu tipini belirle
            Yolcu yolcu;
            switch (yolcuTipi.getSelectedItem().toString()) {
                case "Öğrenci": yolcu = new OgrenciYolcu(); break;
                case "Yaşlı": yolcu = new YasliYolcu(); break;
                default: yolcu = new GenelYolcu(); break;
            }

            StringBuilder bilgi = new StringBuilder();

            // Rota hesaplama
            Rota rota = rotaHesaplayici.rotaHesapla(baslangicLat, baslangicLon, hedefLat, hedefLon, yolcu);

            // Başlangıç ve hedef için en yakın durak mesafeleri
            double baslangicMesafe = MesafeHesaplayici.haversine(baslangicLat, baslangicLon, 
                rota.getSegmentler().get(0).getBitisDurak().getLat(), 
                rota.getSegmentler().get(0).getBitisDurak().getLon()) * 1000; // metreye çevir

            double hedefMesafe = MesafeHesaplayici.haversine(
                rota.getSegmentler().get(rota.getSegmentler().size()-1).getBitisDurak().getLat(),
                rota.getSegmentler().get(rota.getSegmentler().size()-1).getBitisDurak().getLon(),
                hedefLat, hedefLon) * 1000; // metreye çevir

            // Başlangıç durağı bilgisi
            Durak baslangicDuragi = rota.getSegmentler().get(0).getBitisDurak();
            bilgi.append("Kullanıcı Konumuna En Yakın Durak:\n");
            if (baslangicMesafe <= 3000) {
                bilgi.append(String.format("🔹 %s (%.0f m) → 🚶 Yürüme = 0 TL\n\n", 
                    baslangicDuragi.getName(), baslangicMesafe));
            } else {
                double taksiYaklasimUcreti = veri.getTaxi().getOpeningFee() + 
                    ((baslangicMesafe / 1000.0) * veri.getTaxi().getCostPerKm());
                bilgi.append(String.format("🔹 %s (%.0f m) → 🚕 Taksi = %.2f TL\n\n", 
                    baslangicDuragi.getName(), baslangicMesafe, taksiYaklasimUcreti));
            }

            // Alternatif rotalar
            bilgi.append("🔄 ALTERNATIF ROTALAR:\n\n");

            // 1. Sadece Taksi rotası
            double toplamMesafe = MesafeHesaplayici.haversine(baslangicLat, baslangicLon, hedefLat, hedefLon);
            double taksiUcreti = veri.getTaxi().getOpeningFee() + (toplamMesafe * veri.getTaxi().getCostPerKm());
            bilgi.append("1. 🚕 Sadece Taksi (Daha hızlı, ancak maliyetli):\n");
            bilgi.append(String.format("   ⏳ Süre: %.0f dakika\n", toplamMesafe * 2));
            bilgi.append(String.format("   💰 Ücret: %.2f TL\n", taksiUcreti));
            bilgi.append("   ℹ️ En hızlı seçenek\n\n");

            // 2. Sadece Otobüs rotası
            List<Segment> otobusSegmentler = rota.getSegmentler().stream()
                .filter(s -> s.getArac().getTur().equals("otobus"))
                .collect(Collectors.toList());
            
            if (!otobusSegmentler.isEmpty()) {
                bilgi.append("2. 🚍 Sadece Otobüs (Daha uygun maliyetli):\n");
                double otobusUcret = otobusSegmentler.stream()
                    .mapToDouble(Segment::getMaliyet)
                    .sum();
                int otobusSure = otobusSegmentler.stream()
                    .mapToInt(Segment::getSure)
                    .sum();
                
                bilgi.append(String.format("   ⏳ Süre: %d dakika\n", otobusSure));
                bilgi.append(String.format("   💰 Ücret: %.2f TL\n", otobusUcret));
                bilgi.append("   ℹ️ En ekonomik seçenek\n\n");
            }

            // 3. Tramvay Öncelikli rota
            List<Segment> tramvaySegmentler = rota.getSegmentler().stream()
                .filter(s -> s.getArac().getTur().equals("tramvay"))
                .collect(Collectors.toList());
            
            if (!tramvaySegmentler.isEmpty()) {
                bilgi.append("3. 🚋 Tramvay Öncelikli (Rahat ve dengeli):\n");
                double tramvayUcret = tramvaySegmentler.stream()
                    .mapToDouble(Segment::getMaliyet)
                    .sum();
                int tramvaySure = tramvaySegmentler.stream()
                    .mapToInt(Segment::getSure)
                    .sum();
                
                bilgi.append(String.format("   ⏳ Süre: %d dakika\n", tramvaySure));
                bilgi.append(String.format("   💰 Ücret: %.2f TL\n", tramvayUcret));
                bilgi.append("   ℹ️ Konforlu yolculuk\n\n");
            }

            // 4. En Az Aktarmalı rota
            List<Segment> aktarmaliSegmentler = new ArrayList<>();
            int aktarmaSayisi = 0;
            double aktarmaliUcret = 0;
            int aktarmaliSure = 0;

            for (Segment segment : rota.getSegmentler()) {
                if (segment.getAktarmaSuresi() > 0) {
                    aktarmaSayisi++;
                }
                aktarmaliSegmentler.add(segment);
                aktarmaliUcret += segment.getMaliyet();
                aktarmaliSure += segment.getSure();
                
                if (segment.getAktarmaSuresi() > 0) {
                    aktarmaliUcret += segment.getAktarmaUcreti();
                    aktarmaliSure += segment.getAktarmaSuresi();
                }
            }

            if (aktarmaSayisi > 0) {
                bilgi.append("4. 🛑 En Az Aktarmalı Rota:\n");
                bilgi.append(String.format("   ⏳ Süre: %d dakika\n", aktarmaliSure));
                bilgi.append(String.format("   💰 Ücret: %.2f TL\n", aktarmaliUcret));
                bilgi.append(String.format("   🔄 Aktarma sayısı: %d\n", aktarmaSayisi));
                bilgi.append("   ℹ️ Minimum durak ve bekleme\n\n");
            }

            // 5. Detaylı Toplu Taşıma Rotası
            bilgi.append("5. 🚌 Detaylı Toplu Taşıma Rotası:\n");
            bilgi.append(String.format("   📍 En Yakın Durak: %s (%.0f m)\n",
                rota.getSegmentler().get(0).getBitisDurak().getName(),
                baslangicMesafe));
            
            int adim = 1;
            for (Segment segment : rota.getSegmentler()) {
                String aracEmoji = "";
                String aracTuru = segment.getArac().getTur();
                
                if (aracTuru.equals("otobus")) {
                    aracEmoji = "🚌";
                } else if (aracTuru.equals("tramvay")) {
                    aracEmoji = "🚋";
                }
                
                if (segment.getBaslangicDurak() != null && segment.getBitisDurak() != null) {
                    bilgi.append(String.format("   %d. %s → %s (%s %s)\n",
                        adim++,
                        segment.getBaslangicDurak().getName(),
                        segment.getBitisDurak().getName(),
                        aracEmoji,
                        aracTuru));
                    
                    bilgi.append(String.format("      ⏳ Süre: %d dk\n", segment.getSure()));
                    
                    if (yolcu instanceof OgrenciYolcu || yolcu instanceof YasliYolcu) {
                        double orijinalUcret = segment.getOrijinalMaliyet();
                        double indirimliUcret = segment.getMaliyet();
                        String yolcuTipi = yolcu instanceof OgrenciYolcu ? "Öğrenci" : "65+";
                        bilgi.append(String.format("      💰 Ücret: %.2f TL (%s %%50 → %.2f TL)\n", 
                            orijinalUcret, yolcuTipi, indirimliUcret));
                    } else {
                        bilgi.append(String.format("      💰 Ücret: %.2f TL\n", segment.getMaliyet()));
                    }
                    
                    if (segment.getAktarmaSuresi() > 0) {
                        bilgi.append(String.format("   %d. %s → %s (🔄 Transfer)\n",
                            adim++,
                            segment.getBitisDurak().getName(),
                            durakMap.get(segment.getBitisDurak().getTransfer().getTransferStopId()).getName()));
                        bilgi.append(String.format("      ⏳ Süre: %d dk\n", segment.getAktarmaSuresi()));
                        bilgi.append(String.format("      💰 Ücret: %.2f TL\n", segment.getAktarmaUcreti()));
                    }
                }
            }
            
            bilgi.append("\n📊 Toplu Taşıma Toplam:\n");
            bilgi.append(String.format("⏳ Toplam Süre: %d dakika\n", rota.toplamSureGetir()));
            
            // 3km'den fazla mesafe varsa taksi ücretini ekle
            double toplamUcret = rota.toplamMaliyetGetir();
            if (baslangicMesafe > 3000) {
                double yaklasimTaksiUcreti = veri.getTaxi().getOpeningFee() + 
                    ((baslangicMesafe / 1000.0) * veri.getTaxi().getCostPerKm());
                toplamUcret += yaklasimTaksiUcreti;
                
                // Ödeme tipine göre hesaplama
                if (odemeTipi.getSelectedItem().toString().equals("Kart")) {
                    double kalanBakiye = veri.getKalanBakiye();
                    if (toplamUcret > kalanBakiye) {
                        bilgi.append(String.format("💰 Toplam Ücret: %.2f TL (Kart Bakiyesi: %.2f TL → Ödenecek: %.2f TL)\n", 
                            toplamUcret, kalanBakiye, toplamUcret - kalanBakiye));
                    } else {
                        veri.kartKullan(toplamUcret); // Karttan ücreti düş
                        bilgi.append(String.format("💰 Toplam Ücret: %.2f TL (Kart Bakiyesi: %.2f TL →  Kalan Bakiye: %.2f TL)\n", 
                            toplamUcret, kalanBakiye, veri.getKalanBakiye()));
                    }
                } else {
                    bilgi.append(String.format("💰 Toplam Ücret: %.2f TL (Taksi: %.2f TL + Toplu Taşıma: %.2f TL)\n", 
                        toplamUcret, yaklasimTaksiUcreti, rota.toplamMaliyetGetir()));
                }
            } else {
                // Ödeme tipine göre hesaplama
                if (odemeTipi.getSelectedItem().toString().equals("Kart")) {
                    double kalanBakiye = veri.getKalanBakiye();
                    if (toplamUcret > kalanBakiye) {
                        bilgi.append(String.format("💰 Toplam Ücret: %.2f TL (Kart Bakiyesi: %.2f TL → Ödenecek: %.2f TL)\n", 
                            toplamUcret, kalanBakiye, toplamUcret - kalanBakiye));
                    } else {
                        veri.kartKullan(toplamUcret); // Karttan ücreti düş
                        bilgi.append(String.format("💰 Toplam Ücret: %.2f TL (Kart Bakiyesi: %.2f TL → Kalan Bakiye: %.2f TL)\n", 
                            toplamUcret, kalanBakiye, veri.getKalanBakiye()));
                    }
                } else {
                    bilgi.append(String.format("💰 Toplam Ücret: %.2f TL\n", toplamUcret));
                }
            }
            
            // OSRM'den detaylı rota al ve göster
            List<GeoPosition> rotaNoktalar = getOSRMRoute(baslangicLat, baslangicLon, hedefLat, hedefLon);
            rotayiHaritadaGoster(rotaNoktalar);
            rotaBilgisi.setText(bilgi.toString());
            
            // Harita görünümünü ayarla
            int zoom = 4;
            Point2D start = mapViewer.getTileFactory().geoToPixel(
                new GeoPosition(baslangicLat, baslangicLon), zoom);
            Point2D end = mapViewer.getTileFactory().geoToPixel(
                new GeoPosition(hedefLat, hedefLon), zoom);

            while (zoom < 15) {
                double distance = start.distance(end);
                if (distance > mapViewer.getWidth() || distance > mapViewer.getHeight()) {
                    break;
                }
                zoom++;
                start = mapViewer.getTileFactory().geoToPixel(
                    new GeoPosition(baslangicLat, baslangicLon), zoom);
                end = mapViewer.getTileFactory().geoToPixel(
                    new GeoPosition(hedefLat, hedefLon), zoom);
            }

            mapViewer.setZoom(Math.max(1, zoom - 1));
            mapViewer.setAddressLocation(new GeoPosition(
                (baslangicLat + hedefLat) / 2,
                (baslangicLon + hedefLon) / 2
            ));
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Hata oluştu: " + ex.getMessage(),
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // RoutePainter iç sınıfı
    private class RoutePainter implements Painter<JXMapViewer> {
        private List<GeoPosition> track;
        private final Color routeColor = new Color(66, 133, 244, 180);  // Google Maps mavi
        private final Color shadowColor = new Color(0, 0, 0, 50);

        public RoutePainter(List<GeoPosition> track) {
            this.track = track;
        }

        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            g = (Graphics2D) g.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Rota çizgisi için noktaları hazırla
            List<Point2D> points = new ArrayList<>();
            for (GeoPosition gp : track) {
                points.add(map.getTileFactory().geoToPixel(gp, map.getZoom()));
            }

            // Gölge efekti
            g.setColor(shadowColor);
            g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawRoute(g, points);

            // Ana rota
            g.setColor(routeColor);
            g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawRoute(g, points);

            g.dispose();
        }

        private void drawRoute(Graphics2D g, List<Point2D> points) {
            for (int i = 0; i < points.size() - 1; i++) {
                Point2D p1 = points.get(i);
                Point2D p2 = points.get(i + 1);
                g.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
            }
        }
    }

    private List<GeoPosition> getOSRMRoute(double startLat, double startLon, 
                                         double endLat, double endLon) {
        List<GeoPosition> routePoints = new ArrayList<>();
        try {
            String urlStr = String.format(
                "http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                startLon, startLat, endLon, endLat);
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // JSON yanıtını parse et
                Gson gson = new Gson();
                Map<String, Object> jsonResponse = gson.fromJson(response.toString(), Map.class);
                List<Map<String, Object>> routes = (List<Map<String, Object>>) jsonResponse.get("routes");
                
                if (!routes.isEmpty()) {
                    Map<String, Object> route = routes.get(0);
                    Map<String, Object> geometry = (Map<String, Object>) route.get("geometry");
                    List<List<Double>> coordinates = (List<List<Double>>) geometry.get("coordinates");
                    
                    for (List<Double> coord : coordinates) {
                        routePoints.add(new GeoPosition(coord.get(1), coord.get(0)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Hata durumunda basit düz çizgi
            routePoints.add(new GeoPosition(startLat, startLon));
            routePoints.add(new GeoPosition(endLat, endLon));
        }
        return routePoints;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UlasimArayuzu arayuz = new UlasimArayuzu();
            arayuz.setVisible(true);
        });
    }
} 