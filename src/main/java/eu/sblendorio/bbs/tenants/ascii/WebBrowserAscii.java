package eu.sblendorio.bbs.tenants.ascii;
import eu.sblendorio.bbs.core.AsciiThread;
import eu.sblendorio.bbs.core.WebBrowser;

import static eu.sblendorio.bbs.core.Utils.STR_ALPHANUMERIC;
import static eu.sblendorio.bbs.core.Utils.setOfChars;
import eu.sblendorio.bbs.core.HtmlUtils;

import java.net.UnknownHostException;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.text.WordUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebBrowserAscii extends AsciiThread{

    public WebBrowserAscii() {
        super();
    }

    @Override
    public void doLoop() throws Exception {
        log("Internet Browser doLoop");
        
        cls();
        newline();
        println("Welcome to The Old Net!");
        newline();

        try {
            do {
                
                print("Where do you want to go? > ");

                print("[u]rl [p]rev [n]ext [l]inks [b]ack [q]uit");

                String url = readLine(setOfChars(STR_ALPHANUMERIC, "."));
                resetInput();

                println("Going to " + url);

                Document webpage;
                try {
                    webpage = WebBrowser.getWebpage(WebBrowser.makeUrl(url));
                    println("got website");
                } catch (HttpStatusException | UnknownHostException ex) {
                    println("error getting website!!!");
                    webpage = Jsoup.parseBodyFragment("HTTP connection error");
                }

                println("displaying webpage");
                displayPage(webpage, url);

            } while (true);
        } catch (UnsupportedOperationException ex) {
            log("Exit browser");
        }
    }

    protected void displayPage(Document webpage, String url) throws Exception {
        final String content = WebBrowser.formattedWebpage(webpage);
        // println(content);
        List<String> rows = new ArrayList<>();
        rows.addAll(wordWrap(content));

        rows.forEach((row) -> {
            println(row);
        });
    }    
    
    protected List<String> wordWrap(String s) {
        String[] cleaned = filterPrintableWithNewline(HtmlUtils.htmlClean(s)).split("\n");
        List<String> result = new ArrayList<>();
        for (String item: cleaned) {
            String[] wrappedLine = WordUtils
                    .wrap(item, 39, "\n", true)
                    .split("\n");
            result.addAll(asList(wrappedLine));
        }
        return result;
    }

}
