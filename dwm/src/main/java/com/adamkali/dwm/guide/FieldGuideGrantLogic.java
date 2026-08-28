package com.adamkali.dwm.guide;

/**
 * Pure eligibility for the once-per-player Field Guide gift.
 */
public final class FieldGuideGrantLogic {
    private FieldGuideGrantLogic() {
    }

    public static boolean shouldGrant(boolean alreadyReceived) {
        return !alreadyReceived;
    }
}
