package com.Kocaeli.model;
import java.util.List;

public class Durak {
    private String id;
    private String name;
    private String type;
    private double lat;
    private double lon;
    private boolean sonDurak;
    private List<SonrakiDurak> nextStops;
    private TransferDurak transfer;

    // Getter'lar
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public boolean isSonDurak() { return sonDurak; }
    public List<SonrakiDurak> getNextStops() { return nextStops; }
    public TransferDurak getTransfer() { return transfer; }

    // Constructor (Gson için)
    public Durak() {}
}