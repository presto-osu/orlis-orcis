package com.saladdressing.veterondo.pojos;

public class WeatherPaletteGenerator {

    private static String[] RAIN_PALETTE = new String[]{"#9575CD", "#7E57C2", "#673AB7", "#5E35B1", "#512DA8", "#4527A0", "#311B92", "#311B92", "#7986CB", "#5C6BC0", "#5C6BC0", "#3F51B5"};
    private static String[] SUNNY_PALETTE = new String[]{"#FFFF8D", "#FFFF00", "#FFEA00", "#FFF176", "#FFEE58", "#FFEB3B", "#EF6C00", "#E65100", "#81D4FA", "#4FC3F7", "#29B6F6", "#03A9F4"};
    private static String[] CLOUDY_PALETTE = new String[]{"#FAFAFA", "#F5F5F5", "#EEEEEE", "#E0E0E0", "#BDBDBD", "#9E9E9E", "#ECEFF1", "#CFD8DC", "#B0BEC5", "#90A4AE", "#78909C", "#607D8B"};
    private static String[] SNOWY_PALETTE = new String[]{"#FFFFFF"};
    private static String[] NIGHTLY_PALETTE = new String[]{"#FFFFFF", "#000000"};
    private static String[] FUNKY_PALETTE = new String[]{"#EF5350", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#039BE5", "#18FFFF", "#009688", "#00E676", "#76FF03", "#FFEB3B", "#FFC107", "#FF5722"};
    private static String[] DUSTY_PALETTE = new String[]{"#BCAAA4", "#A1887F", "#8D6E63", "#795548", "#6D4C41", "#5D4037", "#4E342E", "#3E2723", "#757575", "#616161", "#424242", "#212121"};
    private static String[] SPRING_PALETTE = new String[] {"#84FFFF","#18FFFF","#00E5FF","#00B8D4","#FFF59D","#FFF176","#CCFF90","#B2FF59","#76FF03","#64DD17","#F4FF81","#EEFF41", "#C6FF00", "#AEEA00"};
    private static String[] HOT_PALETTE = new String[] {"#FFF176","#FFEE58","#FFEB3B","#FFD54F","#FFCA28","#FFC107","#EF6C00","#E65100","#FF5722","#F4511E","#E53935","#D32F2F"};


    public WeatherPaletteGenerator() {

    }

    public static String[] getFunkyPalette() {
        return FUNKY_PALETTE;
    }

    public static String[] getHotPalette() { return HOT_PALETTE; }

    public static String[] getSpringPalette() { return SPRING_PALETTE; }

    public static String[] getRainPalette() {
        return RAIN_PALETTE;
    }

    public static String[] getSunnyPalette() {
        return SUNNY_PALETTE;
    }

    public static String[] getCloudyPalette() {
        return CLOUDY_PALETTE;
    }

    public static String[] getSnowyPalette() {
        return SNOWY_PALETTE;
    }

    public static String[] getNightlyPalette() {
        return NIGHTLY_PALETTE;
    }

    public static String[] getDustyPalette() { return DUSTY_PALETTE; }
}
