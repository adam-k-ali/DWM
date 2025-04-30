package com.adamkali.dwm;

public class DWMReference {
    public static final String MOD_ID = "dwm";
    public static final boolean IS_DEVELOPER = System.getenv().getOrDefault("IS_DEVELOPER", "false").equals("true");
    public static final boolean IS_ANALYTICS_ENABLED = false;
    public static final boolean IS_CHAMELEON_GUI_ENABLED = IS_DEVELOPER;
}
