package com.adamkali.dwm.tardis.data.model;

public class TardisDoorState {
    public boolean isOpen;
    public float doorSwing;

    public TardisDoorState() {
        this.isOpen = false;
        this.doorSwing = 0.0f;
    }
}