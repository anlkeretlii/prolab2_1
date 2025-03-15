package com.Kocaeli.ui;

import com.Kocaeli.model.*;
import com.Kocaeli.service.RotaHesaplayici;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RoutePlannerGUI extends JFrame {
    private JTextField baslangicEnlemField;
    private JTextField baslangicBoylamField;
    private JTextField hedefEnlemField;
    private JTextField hedefBoylamField;
    private JComboBox<String> yolcuTipiCombo;
    private JComboBox<String> odemeTipiCombo;
    private JTextArea guzergahTextArea;
    private JLabel haritaLabel;
    private UlasimVerisi ulasimVerisi;
    private RotaHesaplayici rotaHesaplayici;

    public RoutePlannerGUI() {
        // Veri setini initialize et
        ulasimVerisi = new UlasimVerisi();
        rotaHesaplayici = new RotaHesaplayici(ulasimVerisi);

        // Arayüzü ayarla
        setTitle("EKOMOBIL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Sol Panel (Girdi Alanları)
        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Giriş Koordinatları:"));
        inputPanel.add(new JLabel());
        inputPanel.add(new JLabel("Başlangıç Enlem:"));
        baslangicEnlemField = new JTextField();
        inputPanel.add(baslangicEnlemField);
        inputPanel.add(new JLabel("Başlangıç Boylam:"));
        baslangicBoylamField = new JTextField();
        inputPanel.add(baslangicBoylamField);
        inputPanel.add(new JLabel("Hedef Enlem:"));
        hedefEnlemField = new JTextField();
        inputPanel.add(hedefEnlemField);
        inputPanel.add(new JLabel("Hedef Boylam:"));
        hedefBoylamField = new JTextField();
        inputPanel.add(hedefBoylamField);

        inputPanel.add(new JLabel("Yolcu Türü:"));
        String[] yolcuTipleri = {"Genel", "Öğrenci", "Yaşlı"};
        yolcuTipiCombo = new JComboBox<>(yolcuTipleri);
        inputPanel.add(yolcuTipiCombo);

        inputPanel.add(new JLabel("Ödeme Türü:"));
        String[] odemeTipleri = {"Nakit"};
        odemeTipiCombo = new JComboBox<>(odemeTipleri);
        inputPanel.add(odemeTipiCombo);

        JButton rotaOlusturButton = new JButton("Rota Oluştur");
        inputPanel.add(rotaOlusturButton);
        inputPanel.add(new JLabel());

        add(inputPanel, BorderLayout.WEST);

        // Orta Panel (Harita)
        JPanel haritaPanel = new JPanel(new BorderLayout());
        haritaLabel = new JLabel(new ImageIcon("src/main/resources/harita.png"));
        haritaPanel.add(haritaLabel, BorderLayout.CENTER);
        add(haritaPanel, BorderLayout.CENTER);

        // Sağ Panel (Güzergah Bilgisi)
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        guzergahTextArea = new JTextArea(15, 30);
        guzergahTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(guzergahTextArea);
        outputPanel.add(scrollPane, BorderLayout.CENTER);
        add(outputPanel, BorderLayout.EAST);

        // Buton Dinleyicisi
        rotaOlusturButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Boş giriş kontrolü
                    if (baslangicEnlemField.getText().trim().isEmpty() || baslangicBoylamField.getText().trim().isEmpty() ||
                            hedefEnlemField.getText().trim().isEmpty() || hedefBoylamField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(RoutePlannerGUI.this, "Lütfen tüm koordinat alanlarını doldurun!", "Hata", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Kullanıcıdan alınan veriler
                    double baslangicLat = Double.parseDouble(baslangicEnlemField.getText());
                    double baslangicLon = Double.parseDouble(baslangicBoylamField.getText());
                    double hedefLat = Double.parseDouble(hedefEnlemField.getText());
                    double hedefLon = Double.parseDouble(hedefBoylamField.getText());
                    String yolcuTipi = (String) yolcuTipiCombo.getSelectedItem();
                    String odemeTipi = (String) odemeTipiCombo.getSelectedItem();
                    Yolcu yolcu = new Yolcu() {
                        @Override
                        public double indirimUygula(double temelMaliyet) {
                            return 0;
                        }
                    };

                    // Rota hesapla
                    Rota rota = rotaHesaplayici.rotaHesapla(baslangicLat, baslangicLon, hedefLat, hedefLon, yolcu);
                    if (rota == null || rota.getSegmentler().isEmpty()) {
                        throw new Exception("Rota oluşturulamadı.");
                    }

                    // Çıktı oluştur
                    StringBuilder output = new StringBuilder();
                    output.append("Başlangıç Durağı: ").append(rota.getSegmentler().get(0).getBaslangicDurak().getName()).append("\n");
                    output.append("Hedef Durağı: ").append(rota.getSegmentler().get(rota.getSegmentler().size() - 1).getHedefDurak().getName()).append("\n");
                    output.append("Rota:\n");

                    for (int i = 0; i < rota.getSegmentler().size(); i++) {
                        Segment segment = rota.getSegmentler().get(i);
                        String aracTipi = segment.getArac().toString().equals("otobus") ? "🚌 Otobüs" :
                                segment.getArac().toString().equals("tramvay") ? "🚋 Tramvay" :
                                        "🚕 Taksi";
                        String baslangicDurakAdi = (i == 0) ? rota.getSegmentler().get(0).getBaslangicDurak().getName() :
                                rota.getSegmentler().get(i - 1).getHedefDurak().getName();
                        output.append("⿣").append(baslangicDurakAdi)
                                .append(" → ").append(segment.getHedefDurak().getName())
                                .append(" (").append(aracTipi).append(")\n")
                                .append("⏳ Süre: ").append(segment.getSure()).append(" dk\n")
                                .append("💰 Ücret: ").append(String.format("%.2f TL", segment.getMaliyet()))
                                .append(" (Öğrenci %50 → ").append(String.format("%.2f TL?", segment.getMaliyet() * 0.5))
                                .append(") (Özel Gün → 0 TL?)\n");
                    }

                    output.append("Toplam Maliyet: ").append(String.format("%.2f TL", rota.getToplamMaliyet())).append("\n");
                    output.append("Toplam Süre: ").append(rota.getToplamSure()).append(" dakika\n");
                    output.append("Yolcu Tipi: ").append(yolcuTipi).append("\n");
                    output.append("Ödeme Tipi: ").append(odemeTipi).append("\n");

                    guzergahTextArea.setText(output.toString());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(RoutePlannerGUI.this, "Lütfen koordinatları doğru formatta girin!", "Hata", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RoutePlannerGUI.this, "Rota hesaplanırken bir hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RoutePlannerGUI().setVisible(true);
            }
        });
    }
}