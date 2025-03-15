package com.Kocaeli.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.swing.JOptionPane;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class UlasimVerisi {
    private List<Durak> duraklar;
    private Taksi taxi;

    public UlasimVerisi() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("src/main/resources/veriseti.json")) {
            UlasimVerisi ulasimVerisi = gson.fromJson(reader, UlasimVerisi.class);
            this.duraklar = ulasimVerisi.duraklar;
            this.taxi = ulasimVerisi.taxi;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "JSON dosyası yüklenemedi: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }


    public List<Durak> getDuraklar() {
        return duraklar;
    }

    public Taksi getTaxi() {
        return taxi;
    }
}