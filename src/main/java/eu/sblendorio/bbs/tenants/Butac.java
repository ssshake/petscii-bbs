package eu.sblendorio.bbs.tenants;

public class Butac extends WordpressProxy {

    public Butac() {
        super();
        this.logo = LOGO_BYTES;
        this.domain = "https://www.butac.it";
        this.pageSize = 11;
        this.screenRows = 18;
        this.showAuthor = true;
    }

    private static final byte[] LOGO_BYTES = new byte[] {
        -97, -84, 18, 32, 32, 32, -110, -69, 32, 32, 18, 5, -69, 32, -69, -68,
        -110, -97, -68, 18, 32, -110, -95, 18, 32, -84, -95, -69, 32, -69, -110, 32,
        18, -66, -84, 32, -110, -69, -84, 18, 32, -69, -68, -110, 13, 18, 32, 5,
        32, -97, 32, 5, 32, -97, 32, -110, 32, 32, 18, 5, -95, 32, -95, 32,
        -110, 32, 18, -97, 32, -110, -95, 18, 32, -110, -95, -68, 18, -95, 32, -110,
        -68, 32, 18, 32, -68, 32, -110, -95, 18, -95, 32, -110, -68, 18, -94, -110,
        13, 18, 32, 32, 5, 32, -97, 32, 32, -110, 32, 32, 18, 5, -95, 32,
        -69, -68, -110, 32, 18, -97, 32, -110, -95, 18, 32, -110, -95, 32, 18, -95,
        32, -110, 32, 32, 18, 32, -110, -95, 18, 32, -110, -95, 18, -95, 32, -110,
        13, 18, -69, 32, 5, 32, -97, 32, -84, -110, 32, 32, 18, 5, -95, 32,
        -95, 32, -110, 32, 18, -97, 32, -110, -95, 18, 32, -110, -95, 32, 18, -95,
        32, -110, 32, 32, 18, 32, -110, -95, 18, 32, -110, -95, 18, -95, 32, -95,
        32, -110, 13, 32, 18, -94, -94, -94, -110, 32, 32, 32, 18, 5, -94, -94,
        -94, -110, -66, 32, -97, -68, 18, -94, -94, -110, 32, 32, 18, -94, -94, -110,
        -66, -68, 18, -94, -110, -66, 18, -94, -94, -110, 32, 18, -94, -94, -110, -66,
        13,
    };

}
