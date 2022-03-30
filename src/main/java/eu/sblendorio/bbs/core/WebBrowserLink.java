package eu.sblendorio.bbs.core;

import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.defaultString;

//TODO this name sucks, its for the browsers
public class WebBrowserLink {
    public final String name;
    public final String url;
    public final String fileType;

    public WebBrowserLink(String url, String name) throws Exception {
        this.url = defaultString(url);
        if (name.length() > 60){
            this.name = " ..." + StringUtils.right(name, 31).trim();
        } else {
            this.name = StringUtils.left(name, 35).trim();
        }
        this.fileType = defaultString(this.name).replaceAll("(?is)^.*\\.(.*?)$", "$1").toLowerCase();
    }
}
