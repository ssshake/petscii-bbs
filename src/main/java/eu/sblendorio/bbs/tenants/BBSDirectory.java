package eu.sblendorio.bbs.tenants;

import eu.sblendorio.bbs.core.Hidden;
import eu.sblendorio.bbs.core.HtmlUtils;
import eu.sblendorio.bbs.core.PetsciiThread;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.sblendorio.bbs.core.Keys.*;
import static eu.sblendorio.bbs.core.Colors.*;
import static eu.sblendorio.bbs.core.Utils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.math.NumberUtils.*;

@Hidden
public class BBSDirectory extends PetsciiThread {

    static String HR_TOP = StringUtils.repeat(chr(163), 39);

    static class Post {
        long id;
        String title;
        String date;
        String content;
        String excerpt;
        Long authorId;
    }

    static class BBS {
        String name;
        String address;
        String port;
        String sysop;
        String warez;
        String hardware;
        String web;
        String flashterm;
        String comment;
        String location;
        String update;
    }

    protected String domain = "http://cbbsoutpost.servebbs.com/api/exportbbslist/service.php?f=json";
    protected byte[] logo = LOGO_WORDPRESS;
    protected int pageSize = 10;
    protected int screenRows = 19;
    protected boolean showAuthor = false;

    protected Map<Integer, Post> posts = emptyMap();
    protected Map<Integer, BBS> bbses = emptyMap();

    protected int currentPage = 1;
    private String originalDomain;

    public BBSDirectory() {
        // Mandatory
    }

    public BBSDirectory(String domain) {
        this.domain = domain;
    }

    public BBSDirectory(String domain, byte[] logo) {
        this.domain = domain;
        this.logo = logo;
    }

    protected final String getApi() { return domain; };

    @Override
    public void doLoop() throws Exception {
        originalDomain = domain;
        write(LOWERCASE, CASE_LOCK);
        log("Wordpress entering (" + domain + ")");
        listBBSes();
        while (true) {
            log("Wordpress waiting for input");
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
            write(WHITE); print("Q"); write(GREY3);
            print("uit> ");
            resetInput();
            flush(); String inputRaw = readLine();
            String input = lowerCase(trim(inputRaw));
            if (".".equals(input) || "exit".equals(input) || "quit".equals(input) || "q".equals(input)) {
                break;
            } else if ("help".equals(input) || "h".equals(input)) {
                help();
                listBBSes();
                continue;
            } else if ("+".equals(input)) {
                ++currentPage;
                posts = null;
                try {
                    listBBSes();
                } catch (NullPointerException e) {
                    --currentPage;
                    posts = null;
                    listBBSes();
                    continue;
                }
                continue;
            } else if ("-".equals(input) && currentPage > 1) {
                --currentPage;
                posts = null;
                listBBSes();
                continue;
            } else if ("--".equals(input) && currentPage > 1) {
                currentPage = 1;
                posts = null;
                listBBSes();
                continue;
            } else if ("r".equals(input) || "reload".equals(input) || "refresh".equals(input)) {
                posts = null;
                listBBSes();
                continue;
            } else if (posts.containsKey(toInt(input))) {
                displayPost(toInt(input));
            } else if ("".equals(input)) {
                listBBSes();
                continue;
            } else if ("clients".equals(input)) {
                listClients();
                continue;
            } else if (substring(input,0,5).equalsIgnoreCase("send ")) {
                long client = toLong(input.replaceAll("^send ([0-9]+).*$", "$1"));
                String message = input.replaceAll("^send [0-9]+ (.*)$", "$1");
                if (getClients().containsKey(client) && isNotBlank(message)) {
                    System.out.println("Sending '"+message+"' to #"+client);
                    int exitCode = send(client, message);
                    System.out.println("Message sent, exitCode="+exitCode+".");
                }
            } else if (substring(input,0,5).equalsIgnoreCase("name ")) {
                String newName = defaultString(input.replaceAll("^name ([^\\s]+).*$", "$1"));
                changeClientName(newName);
            } else if (substring (input, 0, 8).equalsIgnoreCase("connect ")) {
                final String oldDomain = domain;
                final byte[] oldLogo = logo;
                domain = defaultString(input.replaceAll("^connect ([^\\s]+).*$", "$1"));
                if (!domain.matches("(?is)^http.*"))
                    domain = "https://" + domain;
                log("new API: "+getApi());
                posts = null;
                currentPage = 1;
                try {
                    listBBSes();
                } catch (Exception e) {
                    log("WORDPRESS FAILED: " + e.getClass().getName() + ": " + e.getMessage());
                    logo = oldLogo;
                    domain = oldDomain;
                    posts = null;
                    listBBSes();
                }
            }
        }
        flush();
    }

    protected Map<Integer, BBS> getBBSes(int page, int perPage) throws Exception {
        if (page < 1 || perPage < 1) return null;

        Map<Integer, BBS> result = new LinkedHashMap<>();
        
        JSONObject response = (JSONObject) httpGetJson(getApi());
        
        JSONArray bbsList = (JSONArray) response.get("bbs_list");
        
        for (int i=0; i<bbsList.size(); ++i) {
            BBS bbs = new BBS();
            JSONObject bbsJSON = (JSONObject) bbsList.get(i);

            bbs.name = bbsJSON.get("bbs_name").toString();
            bbs.address = bbsJSON.get("bbs_address").toString();
            bbs.port = bbsJSON.get("bbs_port").toString();
            bbs.sysop = bbsJSON.get("bbs_sysop").toString();
            bbs.warez = bbsJSON.get("bbs_warez").toString();
            bbs.hardware = bbsJSON.get("bbs_hardware").toString();
            bbs.web = bbsJSON.get("bbs_web").toString();
            bbs.flashterm = bbsJSON.get("bbs_flashterm").toString();
            bbs.comment = bbsJSON.get("bbs_comment").toString();
            bbs.location = bbsJSON.get("bbs_location").toString();
            bbs.update = bbsJSON.get("bbs_update").toString();
            
            result.put(i+1+(perPage*(page-1)), bbs);
        }
        return result;
    }

    protected void listBBSes() throws Exception {
        log("LISTING OF POSTS NOW");
        cls();
        logo();

        if (isEmpty(bbses)) {
            waitOn();
            bbses = getBBSes(currentPage, pageSize);
            waitOff();
        }

        for (Map.Entry<Integer, BBS> entry: bbses.entrySet()) {
            int i = entry.getKey();
            BBS bbs = entry.getValue();
            write(WHITE); print(i + "."); write(GREY3);
            final int iLen = 37-String.valueOf(i).length();
            String line = WordUtils.wrap(filterPrintable(HtmlUtils.htmlClean(bbs.name)), iLen, "\r", true);
            println(line.replaceAll("\r", "\r " + repeat(" ", 37-iLen)));
        }
        newline();
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

    protected void help() throws Exception {
        cls();
        logo();
        println();
        println();
        println("Press any key to go back to posts");
        readKey();
    }

    protected void displayPost(int n) throws Exception {
        int i = 3;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        cls();
        logo();
        waitOn();

        String author = null;
        final Post p = posts.get(n);

        try {
            if (showAuthor) {
                JSONObject authorJ = (JSONObject) httpGetJson(getApi() + "users/" + p.authorId);
                author = authorJ.get("name").toString();
            }
        } catch (Exception e) {
            log("Error during retrieving author");
            e.printStackTrace();
        }
        final String content = p.content
                .replaceAll("(?is)<style>.*</style>", EMPTY)
                .replaceAll("(?is)<script .*</script>", EMPTY)
                .replaceAll("(?is)^[\\s\\n\\r]+|^\\s*(</?(br|div|figure|iframe|img|p|h[0-9])[^>]*>\\s*)+", EMPTY)
                .replaceAll("(?is)^(<[^>]+>(\\s|\n|\r)*)+", EMPTY);
        final String head = p.title + (isNotBlank(author) ? " - di " + author : EMPTY) + "<br>" + HR_TOP ;
        List<String> rows = wordWrap(head);

        List<String> article = wordWrap(p.date.replaceAll("^(\\d\\d\\d\\d).(\\d\\d).(\\d\\d).*","$3/$2/$1") +
                " - " + content
        );
        rows.addAll(article);
        waitOff();
        int page = 1;
        int j = 0;
        boolean forward = true;
        while (j < rows.size()) {
            if (j>0 && j % screenRows == 0 && forward) {
                println();
                write(WHITE);
                print("-PAGE " + page + "-  SPACE=NEXT  -=PREV  .=EXIT");
                write(GREY3);

                resetInput(); int ch = readKey();
                if (ch == '.') {
                    listBBSes();
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
    }

    protected void waitOn() {
        print("PLEASE WAIT...");
        flush();
    }

    protected void waitOff() {
        for (int i=0; i<14; ++i) write(DEL);
        flush();
    }

    public final static byte[] LOGO_WORDPRESS = new byte[] {
        -104, -84, 32, 32, -84, 32, 32, 32, 32, 32, 32, 32, 32, -84, -94, 13,
        -68, -69, 32, 18, -65, -110, -84, 18, -94, -110, -65, 18, -95, -94, -110, -69,
        18, -84, -110, -65, 18, -95, -110, 32, -95, 18, -84, -110, -65, 18, -95, -94,
        -110, -84, 18, -94, -110, -66, 18, -65, -94, -110, 13, 32, -65, -65, -66, 18,
        -95, -110, 32, 18, -95, -95, -94, -110, -69, -95, 18, -95, -95, -94, -110, 32,
        18, -84, -110, -65, 18, -95, -110, -66, 32, 18, -94, -110, -69, -68, -65, 13,
        32, -68, -68, 32, 32, 18, -94, -110, -66, -68, 32, -66, 18, -94, -110, -66,
        -68, 32, 32, -66, -68, -68, 18, -94, -110, -68, 18, -94, -110, 32, 18, -94,
        -110, -66, 13
    };

    protected void logo() throws IOException {
        if (!equalsDomain(domain, originalDomain)) {
            final String normDomain = normalizeDomain(domain);
            gotoXY(25,1); write(WHITE); print(substring(normDomain, 0, 14));
            if (normDomain.length() > 14) {
                gotoXY(25, 2); print(substring(normDomain, 14, 28));
            }
            gotoXY(0,0);
            write(LOGO_WORDPRESS);
        } else {
            write(logo);
        }
        write(GREY3);
    }

    protected void listClients() throws Exception {
        cls();
        println("You are #" + getClientId() + ": "+getClientName() + " [" + getClientClass().getSimpleName() + "]");
        newline();
        for (Map.Entry<Long, PetsciiThread> entry: clients.entrySet())
            if (entry.getKey() != getClientId())
                println("#" + entry.getKey() +": "+entry.getValue().getClientName() + " [" + entry.getValue().getClientClass().getSimpleName() + "]");
        println();
    }

    @Override
    public void receive(long sender, Object message) {
        log("--------------------------------");
        log("From "+getClients().get(sender).getClientName()+": " +message);
        log("--------------------------------");
        println();
        println("--------------------------------");
        println("From "+getClients().get(sender).getClientName()+": " +message);
        println("--------------------------------");
    }

}