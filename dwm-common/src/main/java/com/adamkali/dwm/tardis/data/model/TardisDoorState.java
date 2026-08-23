package com.adamkali.dwm.tardis.data.model;

public class TardisDoorState {
    public boolean isOpen;
    public float doorSwing;

    public TardisDoorState() {
        this.isOpen = false;
        this.doorSwing = 0.0f;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TardisDoorState) {
            TardisDoorState other = (TardisDoorState) obj;
            return this.isOpen == other.isOpen && this.doorSwing == other.doorSwing;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TardisDoorState [isOpen=" + isOpen + ", doorSwing=" + doorSwing + ']';
    }
}