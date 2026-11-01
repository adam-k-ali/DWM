package com.adamkali.dwm.model.tileentity;

/**
 * Screen glass of a TARDIS monitor mesh in rendered model space (pixels, Y up, block centre at
 * x = z = 0, screen facing -Z). {@code minX} is the viewer's right edge because the viewer looks
 * toward +Z.
 */
public record MonitorScreenRect(float minX, float maxX, float minY, float maxY, float frontZ) {
    public float width() {
        return maxX - minX;
    }

    public float height() {
        return maxY - minY;
    }
}
