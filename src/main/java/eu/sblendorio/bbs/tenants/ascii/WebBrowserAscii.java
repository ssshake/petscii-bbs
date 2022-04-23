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

import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.repeat;
import java.util.LinkedHashMap;
import java.util.Map;

import eu.sblendorio.bbs.core.WebBrowserLink;

public class WebBrowserAscii extends AsciiThread{

    protected int __currentPage = 1;
    protected int __pageSize = 10;
    protected int __screenRows = 18;
    protected int __screenCols = 79;

    static class Pager {
        public boolean forward;
        public int page;
        public int currentRow;

        public Pager(boolean forward, int page, int currentRow) throws Exception {
            this.forward = forward;
            this.page = page;
            this.currentRow = currentRow;
        }
    }

    protected Map<Integer, WebBrowserLink> links = emptyMap();

    public WebBrowserAscii() {
        super();
    }

    @Override
    public void doLoop() throws Exception {
        cls();
        println("The Old Net");
        println("Internet Services Access Terminal");
        newline();
        // newline();
        // println("Recommended Sites:");
        // newline();
        // println("[1] The Old Net [2] 68k.news [3] Old'a Vista [4] Wikipedia");
        // newline();
        // println("Enter [U]RL");
        newline();
        newline();

        try {
            do {
               enterAddress();
            } while (true);

        } catch (UnsupportedOperationException ex) {
            log("Exit browser");
        }
    }

    void loadWebPage(String url) throws Exception{
        Document webpage;
        try {
            webpage = WebBrowser.getWebpage(WebBrowser.makeUrl(url));
            println(">> Website Found");
        } catch (HttpStatusException | UnknownHostException ex) {
            println(">> Error Getting Website");
            webpage = Jsoup.parseBodyFragment("HTTP connection error");
        }

        displayPage(webpage, url);
    }

    protected void displayPage(Document webpage, String url) throws Exception {
        __currentPage = 1;

        Pager pager = new Pager(true, 1, 0);

        final String content = WebBrowser.formattedWebpage(webpage);

        //should update address bar?

        List<String> rows = new ArrayList<>();
        rows.addAll(wordWrap(content));

        while (pager.currentRow < rows.size() + 1) {

            boolean startOfDocument = pager.page <= 1;
            boolean endOfDocument = pager.currentRow == rows.size();

            boolean startOfPage = pager.currentRow % __screenRows == 1;
            boolean endOfPage = pager.currentRow > 0 && pager.currentRow % __screenRows == 0 && pager.forward;

            // if (startOfPage){
            //     printPageNumber(pager.page);
            // }

            if (endOfPage || endOfDocument) {
                writeFooter(startOfDocument, endOfDocument, pager.page);

                String nextStep = promptForUserInput(pager, webpage, url, startOfDocument, endOfDocument);
                switch (nextStep){
                    case "skip":
                        continue;
                    case "exit":
                        return;
                    default:
                        break;
                }
            }

            if (!endOfDocument){
                printRow(pager, rows);
                pager.forward = true;
                ++pager.currentRow;
            }
        }        
    }

    String promptForUserInput(Pager pager, Document webpage, String currentAddress, boolean startOfDocument, boolean endOfDocument) throws Exception {
        String instruction = "";
        switch(getInputKey()){ 
            case 'e':
            case 'E':
                enterAddress();
                break;  
            case '.':
            case 'q':
            case 'Q':
                throw new UnsupportedOperationException();
            case 'b':
            case 'B':
                instruction = "exit";
                break;
            case 'l':
            case 'L':
                listLinksForPage(pager, webpage, currentAddress);
                break;

            case 'u':
            case 'U':
                if (startOfDocument){
                    instruction = "skip";
                    break;
                }

                loadPreviousPage(pager, currentAddress);
                instruction = "skip";
                break;

            case 'd':
            case 'D':
                if (endOfDocument){
                    instruction = "skip";
                    break;
                }
                loadNextPage(pager, currentAddress);
                break;

            default:
                instruction = "skip";
        }
        return instruction;
    }

    void printPageNumber(int page) {
        println(">> PAGE " + page + repeat(' ', 3-String.valueOf(page).length()));
    } 

    void printRow(Pager pager, List<String> rows){
        String row = rows.get(pager.currentRow);
        println(row);
    }

    int getInputKey() throws Exception {
        resetInput();
        return readKey();
    }

    void enterAddress() throws Exception {
        newline();
        print("Enter a website address http://");

        String url = readLine(setOfChars(STR_ALPHANUMERIC, "."));
        resetInput();

        println(">> Going to " + url);

        //todo not sure if this is right
        if ("_quit_program".equalsIgnoreCase(url)) {
            throw new UnsupportedOperationException();
        }

        loadWebPage(url);

    }
    
    void writeFooter(Boolean startOfDocument, Boolean endOfDocument, int page){
        newline();
        if (startOfDocument){            
            print("PAGE " + page + " | [D]OWN | [B]ACK | LIST [L]INKS | [Q]UIT | [E]NTER URL > ");
        } else if (endOfDocument){
            print("PAGE " + page + " | [U]P | [B]ACK | LIST [L]INKS | [Q]UIT | [E]NTER URL > ");
        } else {
            print("PAGE " + page + " | [U]P | [D]OWN | [B]ACK | LIST [L]INKS | [Q]UIT | [E]NTER URL > ");
        }
    }

    void loadPreviousPage(Pager pager, String head){
        --pager.page;
        pager.currentRow = ( pager.page -1 ) * __screenRows;
        pager.forward = false;
    }

    void loadNextPage(Pager pager, String head){
        ++pager.page;
        pager.forward = true;
    }

    void listLinksForPage(Pager pager, Document webpage, String currentAddress) throws Exception {
        getAndDisplayLinksOnPage(webpage, currentAddress);
        cls();
        pager.currentRow = 0;
        pager.page = 0;
    }

    void getAndDisplayLinksOnPage(Document webpage, String currentAddress) throws Exception{
        // loading();
        while (true) {

            // writeAddressBar(currentAddress);
            listLinks(webpage);
            print("Enter Link # or [B]ack, [P]rev, [N]ext> ");

            resetInput();
            flush();

            String inputRaw = readLine();
            String input = lowerCase(trim(inputRaw));

            //QUIT
            if ("b".equalsIgnoreCase(input)
                    || ".".equals(input)
                    || "exit".equalsIgnoreCase(input)
                    || "quit".equalsIgnoreCase(input)
                    || "q".equalsIgnoreCase(input)) {
                break;
            }

            //NEXT PAGE
            else if ("n".equalsIgnoreCase(input)) {
                ++__currentPage;
                links = null;
            }

            //PREVIOUS PAGE
            else if ("p".equalsIgnoreCase(input) && __currentPage > 1) {
                --__currentPage;
                links = null;
            }

            //SUCCESS PATH
            //DO THE THING WHERE YOU LOAD A NEW PAGE
            else if (links != null && input != null && links.containsKey(toInt(input))) {
                final WebBrowserLink link = links.get(toInt(input));
                loadWebPage(link.url);
            }
        }
    }
    
    private void listLinks(Document webpage) throws Exception {
        cls();
        println("Links On Page:");
        println();

        List<WebBrowserLink> entries = WebBrowser.getAllLinks(webpage);

        if (isEmpty(entries)) {
            println("Zero result page - press any key");
            flush();
            resetInput();
            readKey();
            return;
        }

        links = getLinksForPage(entries, __currentPage, __pageSize);

        for (Map.Entry<Integer, WebBrowserLink> entry: links.entrySet()) {
            int i = entry.getKey();
            WebBrowserLink post = entry.getValue();

            print(i + ".");

            final int iLen = __screenCols - String.valueOf(i).length(); //I'm guessing something to do with the row width

            String title = post.name;
            String line = WordUtils.wrap(filterPrintable(HtmlUtils.htmlClean(title)), iLen, "\r", true);

            println(line.replaceAll("\r", "\r " + repeat(" ", __screenCols-iLen)));
        }
        newline();
    }

    private Map<Integer, WebBrowserLink> getLinksForPage(List<WebBrowserLink> entries, int page, int perPage) throws Exception {
        if (page < 1 || perPage < 1) {
            return null;
        };

        Map<Integer, WebBrowserLink> result = new LinkedHashMap<>();

        for ( int i = ( page - 1 ) * perPage; i < page * perPage; ++i ){
            if (i<entries.size()) {
                result.put( i + 1, entries.get(i));
            }
        }
        return result;
    }    

    protected List<String> wordWrap(String s) {
        String[] cleaned = filterPrintableWithNewline(HtmlUtils.htmlClean(s)).split("\n");
        List<String> result = new ArrayList<>();
        for (String item: cleaned) {
            String[] wrappedLine = WordUtils
                    .wrap(item, __screenCols, "\n", true)
                    .split("\n");
            result.addAll(asList(wrappedLine));
        }
        return result;
    }

}
