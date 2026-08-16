package com.adamkali.dwm.block;

import com.adamkali.dwm.block.FirstDoctorConsoleControls.LookTarget;

/**
 * Geometry for one First Doctor console control: panel mount + local AABB before deck transforms.
 */
public record ConsoleControlSpec(
        LookTarget target,
        float panelYaw,
        float scale,
        float mountX,
        float mountY,
        float mountZ,
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ
) {
}
