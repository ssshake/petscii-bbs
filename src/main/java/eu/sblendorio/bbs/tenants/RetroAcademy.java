package eu.sblendorio.bbs.tenants;

import eu.sblendorio.bbs.core.Hidden;

@Hidden
public class RetroAcademy extends WordpressProxy {

    public RetroAcademy() {
        super();
        this.logo = LOGO;
        this.domain = "https://www.retroacademy.it";
    }

    private final static byte[] LOGO = new byte[] {
        32,  32,  32,  32,  32,  28, -84,  32,  32,  32,  32,  32,  32,  32,  32,  32,
        32,  32,  32,  32,  32,-104, -69,  13,  18,  28, -95, -65,-110, -84,  18, -69,
        -110, -69,  18, -69,-110, -66,  18, -68,-110, -66,  18, -65,-110, -65,-104, -84,
        18, -94,-110, -95,  18, -65,-110, -66,  18, -65, -69,-110, -84,  18, -94,-110,
        -95,  18, -65, -68, -95, -69,-110, -65,  18, -95,-110,  32, -95,  13,  18,  28,
        -95,-110,  32, -68,  18, -68,-110,  32, -68, -69, -95,  32, -65,  18, -65,-110,
        -104, -68, -94, -95, -65, -69, -65,  18, -66,-110, -68, -94, -95,  18, -69,-110,
        -69,  18, -95, -95, -95,-110, -68, -94, -95,  30, -94, -94,  13,  32,  32,  32,
        32,  32,  32,  32,  32,  32,  32,  32,  32,  32,  32,  32,  32,  32,  32,  32,
        32,  32,  32,  32,  32,  32,  32,  32,-104, -94, -66,  13
    };
}
