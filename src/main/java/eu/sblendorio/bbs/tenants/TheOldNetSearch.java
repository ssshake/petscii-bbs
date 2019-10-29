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

public class TheOldNetSearch extends PetsciiThread {

    public static final String URL_TEMPLATE = "http://theoldnet.com/get?url=";

    protected int currentPage = 1;
    protected int pageSize = 10;
    protected int screenRows = 18;

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


    @Override
    public void doLoop() throws Exception {
        do {
            currentPage = 1;
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
            resetInput();
            String search = readLine();
            if (defaultString(search).trim().equals(".") || isBlank(search))
                return;
            println();
            println();
            waitOn();


            String url = URL_TEMPLATE + URLEncoder.encode(search, "UTF-8");
            displayPage(url);
            List<Entry> entries = getUrls(url);
            waitOff();
            if (isEmpty(entries)) {
                write(RED); println("Zero result page - press any key");
                flush(); resetInput(); readKey();
                continue;
            }
            displayLinksOnPage(entries);
        } while (true);
    }
    private void logo() throws Exception {
        write(CLR, LOWERCASE, CASE_LOCK);
        // write(TheOldNet.LOGO);
        write(LOGO);
        write(GREY3); gotoXY(0,5);
    }

    public void getAndDisplayLinksOnPage(String url) throws Exception{
        waitOn();
        List<Entry> entries = getUrls(url);
        waitOff();
        if (isEmpty(entries)) {
            write(RED); println("Zero result page - press any key");
            flush(); resetInput(); readKey();
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
                ++currentPage;
                posts = null;
                try {
                    listPosts(entries);
                } catch (NullPointerException e) {
                    --currentPage;
                    posts = null;
                    listPosts(entries);
                }
            } else if ("-".equals(input) && currentPage > 1) {
                --currentPage;
                posts = null;
                listPosts(entries);
            } else if ("--".equals(input) && currentPage > 1) {
                currentPage = 1;
                posts = null;
                listPosts(entries);
            } else if ("r".equals(input) || "reload".equals(input) || "refresh".equals(input)) {
                posts = null;
                listPosts(entries);
            } else if (posts.containsKey(toInt(input))) {
                // displayPost(toInt(input));
                final Entry p = posts.get(toInt(input));
                displayPage(p.url);
                getAndDisplayLinksOnPage(p.url);
                listPosts(entries);
            } else if ("".equals(input)) {
                listPosts(entries);
            }
        }
        flush();
    }

    protected void displayPage(String url) throws Exception {
        int i = 3;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        cls();
        logo();
        waitOn();

        Document doc = null;
        String title = url;

        String response = httpGet(url);

        final String content = response
                .replaceAll("<img.[^>]*>", " [IMAGE] ")
                .replaceAll("<a.[^>]*>", " [LINK] ")
                .replaceAll("&quot;", "\"")
                .replaceAll("&apos;", "'")
                .replaceAll("&#xA0;", " ")
                .replaceAll("(?is)<style>.*</style>", EMPTY)
                .replaceAll("(?is)<script .*</script>", EMPTY)
                .replaceAll("(?is)^[\\s\\n\\r]+|^\\s*(</?(br|div|figure|iframe|img|p|h[0-9])[^>]*>\\s*)+", EMPTY)
                .replaceAll("(?is)^(<[^>]+>(\\s|\n|\r)*)+", EMPTY);

        
        final String head = title;

        List<String> rows = wordWrap(""); //head removed because dups
        List<String> article = wordWrap(content);
        
        rows.addAll(article);
        waitOff();
        int page = 1;
        int j = 0;
        boolean forward = true;
        while (j < rows.size()) {
            if (j>0 && j % screenRows == 0 && forward) {
                println();
                write(WHITE);
                print("-PAGE " + page + "-  SPACE=NEXT  -=PREV  .=LINKS");
                write(GREY3);

                resetInput(); int ch = readKey();
                if (ch == '.') {
                    //listPosts(); //should we show the list of new links on page upon this?
                    return;
                } else if (ch == '-' && page > 1) {
                    j -= (screenRows *2);
                    --page;
                    forward = false;
                    cls();
                    logo();
                    continue;
                } else {
                    ++page;
                }
                cls();
                logo();
            }
            String row = rows.get(j);
            println(row);
            forward = true;
            ++j;
        }
        println();
        println("-- End of Page --");
        readKey();
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

    public static String getSite(String url) throws Exception {
        String response = httpGet(url);
        response = response.replaceAll("<img.[^>]*>", " [IMAGE] ");
        response = response.replaceAll("<a.[^>]*>", " [LINK]==> ");
        response = response.replaceAll("&quot;", "\"");
        response = response.replaceAll("&apos;", "'");
        response = response.replaceAll("&#xA0;", " ");
        
        response = response.replaceAll("<[^>]*>", " ");
        
        Pattern ptn = Pattern.compile("\\s{3,}");
        Matcher mtch = ptn.matcher(response);
        response = mtch.replaceAll("\n\r\n\r");
        return response;
    }

    private void listPosts(List<Entry> entries) throws Exception {
        logo();
        println("Links On Page:");
        println();
        posts = getPosts(entries, currentPage, pageSize);
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


    public static void main(String[] args) throws Exception {
        List<Entry> urls = getUrls(URL_TEMPLATE + URLEncoder.encode("super", "UTF-8"));

        int c = 0;
        for (Entry url: urls)
            System.out.println((++c)+"* "+url.name);
    }


    public static List<Entry> getUrls(String url) throws Exception {
        Document doc = null; 
        List<Entry> urls = new ArrayList<>(); //why
        System.out.println(url);
        try{     
            doc = Jsoup.connect(url).get();
            String title = doc.title();
            Elements links = doc.select("a[href]");
            Element link;
            System.out.println("hi");

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
        } 
        catch (Exception ex){     
            System.out.println("Couldn't connect with the website."); 
        }
        return urls;
    }

    private void waitOn() {
        print("PLEASE WAIT...");
        flush();
    }

    private void waitOff() {
        for (int i=0; i<14; ++i) write(DEL);
        flush();
    }

    private void help() throws Exception {
        logo();
        println();
        println();
        println("Press any key to go back");
        readKey();
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

 13};
}
