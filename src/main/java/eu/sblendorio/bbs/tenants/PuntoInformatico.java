package eu.sblendorio.bbs.tenants;

public class PuntoInformatico extends WordpressProxy {

    public PuntoInformatico() {
        super();
        this.logo = LOGO_BYTES;
        this.domain = "https://www.punto-informatico.it";
        this.pageSize = 9;
        this.screenRows = 19;
        this.showAuthor = true;
    }

    private static final byte[] LOGO_BYTES = new byte[] {
        18, -97, -69, -94, -110, -69, 32, 32, 32, 32, -84, 18, -68, -110,
        32, 32, 32, 32, 5, -69, 32, 32, 32, 18, -65, -110, -66, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 18, -66, -110, -69, 32, -69, 32, 32, 32, 32,
        -101, 46, -55, -44, 13, 18, -97, -95, -110, -94, -66, -69, -84, -84, -94, 32,
        -95, 32, -84, -69, 5, -84, -69, -84, -94, 32, 18, -84, -110, 32, -94, 32,
        -69, -69, -94, -94, 32, -68, -65, 18, -95, -110, 32, -84, -69, 32, -94, 32,
        -94, 13, 18, -97, -95, -110, 32, 32, -95, 18, -95, -95, -110, 32, -95, -95,
        32, -95, 18, -95, -110, 32, 5, -95, 18, -95, -110, 32, -95, -95, 18, -95,
        -110, 32, -95, 18, -84, -110, 32, -95, -95, -95, 18, -65, -69, -95, -110, 32,
        32, -95, 18, -95, -110, 32, 18, -95, -110, 32, -95, 13, 18, -97, -94, -110,
        -66, 32, -68, 18, -94, -110, -68, 32, -66, -68, -66, -68, -66, 5, -68, 18,
        -94, -110, -68, 32, -66, -66, 32, 18, -94, -110, 32, -66, 32, -66, -66, -66,
        -68, 18, -94, -110, 32, 18, -94, -110, -68, 18, -94, -110, 32, 18, -94, -110,
        32, 18, -94, -110, 13
    };

}
