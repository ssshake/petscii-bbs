package eu.sblendorio.bbs.core;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.ArrayList;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;


public class WebBrowser {

    public static Document getWebpage(String url) throws Exception {
        Connection conn;
        try {
            conn = Jsoup.connect(url);
        } catch (Exception e1) {
            // log("Couldn't connect with the website.");
            return null;
        }
        return conn
                //.header("HTTP-User-Agent", "")
                .get();
    }

    public static String formattedWebpage(Document webpage){
        final String result = webpage == null ? "" :webpage
                .toString()
                .replaceAll("<img [^>]*?>", "<br>[IMAGE] ")
                .replaceAll("<a [^>]*?>(.*)?</a>", " <br>[LINK] $1")
                .replaceAll("&quot;", "\"")
                .replaceAll("&apos;", "'")
                .replaceAll("&#xA0;", " ")
                .replaceAll("(?is)<style(\\s|>).*?</style>", EMPTY)
                .replaceAll("(?is)<script(\\s|>).*?</script>", EMPTY)
                .replaceAll("(?is)^[\\s\\n\\r]+|^\\s*(</?(br|div|figure|iframe|img|p|h[0-9])[^>]*>\\s*)+", EMPTY)
                .replaceAll("(?is)^(<[^>]+>(\\s|\n|\r)*)+", EMPTY);
        return result;
    }

    public static String makeUrl(String url) {
        if (!defaultString(url).startsWith("http")) return "http://" + defaultString(url);
        return url;
    }

    public static List<WebBrowserLink> getAllLinks(Document webpage) throws Exception {
        List<WebBrowserLink> urls = new ArrayList<>(); //why
        Elements links = webpage.select("a[href]");
        Element link;

        for(int j=0; j < links.size(); j++){
            link=links.get(j);
            final String label = defaultIfBlank(link.text(), link.attr("href"));

            urls.add(new WebBrowserLink(link.absUrl("href"), label));

        }
        return urls;
    }
    
}


