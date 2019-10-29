package eu.sblendorio.bbs.tenants;

import eu.sblendorio.bbs.core.HtmlUtils;
import eu.sblendorio.bbs.core.PetsciiThread;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.WordUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.sblendorio.bbs.core.Colors.*;
import static eu.sblendorio.bbs.core.Keys.*;
import static eu.sblendorio.bbs.core.Utils.*;
import static java.util.Arrays.asList;
import static eu.sblendorio.bbs.core.Utils.filterPrintable;
import static java.util.Collections.emptyMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TheOldNetBrowserV2 extends PetsciiThread {

    public static final String URL_TEMPLATE = "http://theoldnet.com/get?url=";

    protected int __currentPage = 1;
    protected int __pageSize = 10;
    protected int __screenRows = 18;

    static class Entry {
        public final String name;
        public final String url;
        public final String fileType;

        public Entry(String url, String name) throws Exception {
            this.url = defaultString(url);
            this.name = name;
            this.fileType = defaultString(this.name).replaceAll("(?is)^.*\\.(.*?)$", "$1").toLowerCase();
        }
    }

    protected Map<Integer, Entry> posts = emptyMap();

    public static void main(String[] args) throws Exception {}

    @Override
    public void doLoop() throws Exception {
        do {
            renderHomeScreen();
            resetInput();

            String search = readLine();
            
            if (defaultString(search).trim().equals(".") || isBlank(search)) {
                return;
            }

            String url = URL_TEMPLATE + URLEncoder.encode(search, "UTF-8");

            println();
            println();

            waitOn();
            
            Document webpage = getWebpage(url);
            displayPage(webpage, url);
            
        } while (true);
    }

    void renderHomeScreen() throws Exception {
            logo();
            println();
            print("Enter URL ");
            write(GREY1);
            println("(\".\" to go back):");
            write(GREY3);
            println();
            println(StringUtils.repeat(chr(163), 21));
            write(UP, UP);
            flush();
    }

    protected void displayPage(Document webpage, String url) throws Exception {
        __currentPage = 1;

        cls();
        logo();
        waitOn();

        String title = url;

        String pageAsString = webpage.toString();

        final String content = pageAsString
            .replaceAll("<img.[^>]*>", "<br>[IMAGE] ")
            .replaceAll("<a.[^>]*>", " <br>[LINK] ")
            .replaceAll("&quot;", "\"")
            .replaceAll("&apos;", "'")
            .replaceAll("&#xA0;", " ")
            .replaceAll("(?is)<style>.*</style>", EMPTY)
            .replaceAll("(?is)<script .*</script>", EMPTY)
            .replaceAll("(?is)^[\\s\\n\\r]+|^\\s*(</?(br|div|figure|iframe|img|p|h[0-9])[^>]*>\\s*)+", EMPTY)
            .replaceAll("(?is)^(<[^>]+>(\\s|\n|\r)*)+", EMPTY);


        String head;
        try {
            head = url.split("url=")[1];
        } catch (ArrayIndexOutOfBoundsException e){
            head = url;
        }

        head = "[URL: " + StringUtils.left(head, 30) + "]";

        List<String> rows = wordWrap(head);
        rows.addAll(wordWrap(content));

        waitOff();

        int page = 1;
        int currentRow = 0;
        boolean forward = true;

        while (currentRow < rows.size()) {
            boolean endOfPage = currentRow > 0 && currentRow % __screenRows == 0 && forward;
            if (endOfPage) { 

                println();
                write(WHITE);
                print("PAGE " + page + " (N)EXT  (P)REV  (L)INKS (B)ACK");
                write(GREY3);

                resetInput(); 
                int ch = readKey();

                if (ch == '.' || ch == 'b' || ch == 'B') {

                    return; //should bail

                } else if (ch == 'l' || ch == 'L') {
                    getAndDisplayLinksOnPage(webpage);
                    // return; //should dive into links page

                } else if ((ch == 'p' || ch == 'P') && page > 1) {  //PREVIOUS PAGE

                    currentRow -= (__screenRows * 2); //NO idea why
                    --page;
                    forward = false;
                    cls();
                    logo();
                    continue;

                } else if (ch == 'n' || ch == 'N') {  //NEXT PAGE

                    ++page;

                }

                cls();
                logo();
            }

            //success path
            String row = rows.get(currentRow);
            println(row);
            forward = true;
            ++currentRow;
        }

        //handle end of document
        println();
        println("-- End of Page --");
    }

    public void getAndDisplayLinksOnPage(Document webpage) throws Exception{
        waitOn();
        List<Entry> entries = getUrls(webpage);
        waitOff();
        if (isEmpty(entries)) {
            write(RED); println("Zero result page - press any key");
            flush(); 
            resetInput(); 
            readKey();
            return;
        }
        displayLinksOnPage(entries);
    }

    public void displayLinksOnPage(List<Entry> entries) throws Exception {
        listPosts(entries);
        while (true) {
            log("TheOldNet Browser waiting for input");
            write(WHITE);print("#"); write(GREY3);
            print(", [");
            write(WHITE); print("+-"); write(GREY3);
            print("]Page [");
            write(WHITE); print("H"); write(GREY3);
            print("]elp [");
            write(WHITE); print("R"); write(GREY3);
            print("]eload [");
            write(WHITE); print("."); write(GREY3);
            print("]");
            write(WHITE); print("B"); write(GREY3);
            print("ack> ");
            resetInput();
            flush(); String inputRaw = readLine();
            String input = lowerCase(trim(inputRaw));
            if (".".equals(input) || "exit".equals(input) || "quit".equals(input) || "q".equals(input)) {
                break;
            } else if ("help".equals(input) || "h".equals(input)) {
                help();
                listPosts(entries);
            } else if ("+".equals(input)) {
                ++__currentPage;
                posts = null;
                try {
                    listPosts(entries);
                } catch (NullPointerException e) {
                    --__currentPage;
                    posts = null;
                    listPosts(entries);
                }
            } else if ("-".equals(input) && __currentPage > 1) {
                --__currentPage;
                posts = null;
                listPosts(entries);
            } else if ("--".equals(input) && __currentPage > 1) {
                __currentPage = 1;
                posts = null;
                listPosts(entries);
            } else if ("r".equals(input) || "reload".equals(input) || "refresh".equals(input)) {
                posts = null;
                listPosts(entries);
            } else if (posts.containsKey(toInt(input))) { //what condition is this?
                // displayPost(toInt(input));
                final Entry p = posts.get(toInt(input));

                // displayPage(p.url);
                // getAndDisplayLinksOnPage(p.url);
                log("I DO NOTHING");
                Document webpage = getWebpage(p.url);
                displayPage(webpage, p.url);
                listPosts(entries); //redraw after coming back?
                
                // listPosts(entries);
            } else if ("".equals(input)) {
                listPosts(entries);
            }
        }
        flush();
    }  

    private void listPosts(List<Entry> entries) throws Exception {
        logo();
        write(ORANGE);
        println("Links On Page:");
        println();
        posts = getPosts(entries, __currentPage, __pageSize);
        for (Map.Entry<Integer, Entry> entry: posts.entrySet()) {
            int i = entry.getKey();
            Entry post = entry.getValue();
            write(WHITE); print(i + "."); write(GREY3);
            final int iLen = 37-String.valueOf(i).length();
            String title = post.name;
            String line = WordUtils.wrap(filterPrintable(HtmlUtils.htmlClean(title)), iLen, "\r", true);
            println(line.replaceAll("\r", "\r " + repeat(" ", 37-iLen)));
        }
        newline();
    }

    private Map<Integer, Entry> getPosts(List<Entry> entries, int page, int perPage) throws Exception {
        if (page < 1 || perPage < 1) return null;

        Map<Integer, Entry> result = new LinkedHashMap<>();
        for (int i=(page-1)*perPage; i<page*perPage; ++i)
            if (i<entries.size()) result.put(i+1, entries.get(i));
        return result;
    }

    public static List<Entry> getUrls(Document webpage) throws Exception {
        List<Entry> urls = new ArrayList<>(); //why
        String title = webpage.title();
        Elements links = webpage.select("a[href]");
        Element link;

        for(int j=0; j < links.size(); j++){
            link=links.get(j);

            String label = "Empty";
            if (!StringUtils.isBlank(link.text())){
                label = link.text();
            } else {
                try {
                    label = link.attr("href").split("url=")[1];
                } catch (ArrayIndexOutOfBoundsException e){
                    label = link.attr("href");
                }
            }
            
            urls.add(new Entry(link.attr("href"), label));

        }
        return urls;
    }

    public static Document getWebpage(String url) throws Exception {
        Document doc = null;
        try{    
            doc = Jsoup.connect(url).get();
        } 
        catch (Exception ex){     
            System.out.println("Couldn't connect with the website."); 
        }
        return doc;
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

    private void waitOn() {
        print("LOADING...");
        flush();
    }

    private void waitOff() {
        for (int i=0; i<14; ++i) {
            write(DEL);
        }
        flush();
    }

    private void help() throws Exception {
        logo();
        println();
        println();
        println("Press any key to go back");
        readKey();
    }

    private void logo() throws Exception {
        write(CLR, LOWERCASE, CASE_LOCK);
        // write(TheOldNet.LOGO);
        write(LOGO);
        write(GREY3); gotoXY(0,5);
    }

    private final static byte[] LOGO = {
        -102, 32, 18, 32, 30, 32, -104, -110, 32, 32, 18, 32, -102, -110, 32, -104,
        32, 32, 18, 32, -110, 32, 18, 32, -110, 32, 32, 32, 18, 32, -110, 32,
        18, 32, -110, 32, 32, 32, 18, 32, -110, 32, 32, 32, 32, 32, -102, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 18, 32, 30, 32,
        32, -102, 32, -104, -110, 32, 18, 32, -110, 32, 18, 32, -110, 32, 18, 32,
        -110, 32, 18, 32, -110, 32, 18, 32, -110, 32, 18, 32, -110, 32, 18, 32,
        -110, 32, 18, 32, -110, 32, 18, 32, -110, 32, 32, 32, -102, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 18, 32, 30, 32,
        -102, 32, 32, -104, -110, 32, 32, 18, 32, -110, 32, 18, 32, -110, 32, 32,
        32, 18, 32, -110, 32, 18, 32, -110, 32, 32, 32, 18, 32, -110, 32, 18,
        32, -110, 32, 32, 32, 32, -102, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, -104, 32, -102, 18, 32, 30, 32, -104, -110, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, -102, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, -104, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, -102, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, -104,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, -102, 32, -104, 32, 32, -102, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, -104, 32, 32, 32, 32, -102, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, -104, 32, -102,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32,
        13
    };
}
