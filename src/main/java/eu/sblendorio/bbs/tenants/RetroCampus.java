package eu.sblendorio.bbs.tenants;

public class RetroCampus extends WordpressProxy {

    public RetroCampus() {
        super();
        this.logo = LOGO;
        this.domain = "http://www.retrocampus.com";
        this.pageSize = 9;
        this.screenRows = 18;
    }

    private final static byte[] LOGO = new byte[] {
        18, 5, -84, -94, -69, -84, -94, -69, -94, -94, -69, -94, -94, 32, -94, -94,
        -69, -110, 32, -84, -94, 32, 32, -69, -84, -94, -94, -94, -94, -84, -94, -69,
        -84, 32, 32, -69, -84, -94, -69, 13, -95, 18, -94, -110, -68, -95, 18, -94,
        -69, 32, -110, -68, 18, 32, -110, -68, -66, 18, -69, -95, -110, -95, 18, -95,
        -110, -84, -66, 32, -66, 18, -65, -110, -68, -69, -95, 18, -95, -110, 32, -95,
        18, -95, -110, 32, 18, -95, -95, -110, 32, 32, -95, -65, -69, -68, 13, -95,
        18, -95, -110, -95, -95, 18, -95, 32, 32, -110, 32, 18, 32, -110, 32, 18,
        32, -95, -95, 32, -95, -110, -68, -69, 32, -69, 18, -68, -110, -94, -95, -95,
        18, -95, -110, 32, -95, 18, -95, -94, -110, -66, 18, -95, -110, 32, 32, -95,
        -69, -68, -65, 13, 18, -68, -66, -68, -68, -110, -94, 18, -66, 32, -110, -94,
        18, 32, -110, -94, 18, 32, -66, -110, -94, -94, 18, -66, -110, 32, -68, 18,
        -94, -110, 32, -66, 32, -66, -66, -68, 32, -66, -68, 32, 32, 32, 18, -94,
        -94, -110, 32, -68, 18, -94, -110, -66, 13, 13
    };

}
