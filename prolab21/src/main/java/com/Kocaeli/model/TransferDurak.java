package com.Kocaeli.model;

public class TransferDurak {
    private String transferStopId;
    private int transferSure;
    private double transferUcret;

    // Getter'lar
    public String getTransferStopId() { return transferStopId; }
    public int getTransferSure() { return transferSure; }
    public double getTransferUcret() { return transferUcret; }

    // Constructor (Gson için)
    public TransferDurak() {}
}