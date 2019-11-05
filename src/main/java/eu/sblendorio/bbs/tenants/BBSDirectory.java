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
    protected byte[] logo = LOGO;
    protected int pageSize = 10;
    protected int screenRows = 19;
    protected boolean showAuthor = false;

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
                bbses = null;
                try {
                    listBBSes();
                } catch (NullPointerException e) {
                    --currentPage;
                    bbses = null;
                    listBBSes();
                    continue;
                }
                continue;
            } else if ("-".equals(input) && currentPage > 1) {
                --currentPage;
                bbses = null;
                listBBSes();
                continue;
            } else if ("--".equals(input) && currentPage > 1) {
                currentPage = 1;
                bbses = null;
                listBBSes();
                continue;
            } else if ("r".equals(input) || "reload".equals(input) || "refresh".equals(input)) {
                bbses = null;
                listBBSes();
                continue;
            } else if (bbses.containsKey(toInt(input))) {
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
                bbses = null;
                currentPage = 1;
                try {
                    listBBSes();
                } catch (Exception e) {
                    log("WORDPRESS FAILED: " + e.getClass().getName() + ": " + e.getMessage());
                    logo = oldLogo;
                    domain = oldDomain;
                    bbses = null;
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
        log("LISTING OF BBSes NOW");
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
        println("Press any key to go back to bbses");
        readKey();
    }

    protected void displayPost(int n) throws Exception {
        int i = 3;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        cls();
        logo();
        waitOn();

        String author = null;
        final BBS p = bbses.get(n);

        try {
            if (showAuthor) {
                author = p.sysop;
            }
        } catch (Exception e) {
            log("Error during retrieving author");
            e.printStackTrace();
        }
        final String content = p.comment
                .replaceAll("(?is)<style>.*</style>", EMPTY)
                .replaceAll("(?is)<script .*</script>", EMPTY)
                .replaceAll("(?is)^[\\s\\n\\r]+|^\\s*(</?(br|div|figure|iframe|img|p|h[0-9])[^>]*>\\s*)+", EMPTY)
                .replaceAll("(?is)^(<[^>]+>(\\s|\n|\r)*)+", EMPTY);
        final String head = p.name + (isNotBlank(author) ? " - di " + author : EMPTY) + "<br>" + HR_TOP ;
        List<String> rows = wordWrap(head);

        List<String> article = wordWrap(p.update.replaceAll("^(\\d\\d\\d\\d).(\\d\\d).(\\d\\d).*","$3/$2/$1") +
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

    public final static byte[] LOGO = new byte[] {
        -102, 18, -87, 32, 32, 32, -110, 32, 32, 32, 5, 18, 32, 32, -110, 32,
        32, 18, 32, 32, -102, -110, 32, 32, 32, 5, 18, 32, 32, -102, -110, 32,
        5, 32, 32, 32, 32, -102, 32, 5, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, -102, 32, 32, 32, 32, 18, 32, -110, -87, 32, 32, 18,
        32, -110, -87, 32, 5, 18, 32, -110, 32, 18, 32, -110, 32, 18, 32, -110,
        32, 18, 32, -102, -110, 32, 5, 18, 32, -102, -110, 32, 32, 32, 5, 67,
        66, 66, 83, 79, 85, 84, 80, 79, 83, 84, 46, -102, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 18, 32, -110, 32, 32, 32, 32, 32, 32, 5, 18,
        32, 32, -110, 32, 32, 18, 32, 32, -102, -110, 32, 32, 32, 5, 18, 32,
        -102, -110, 32, 32, 5, 83, 69, 82, 86, 69, 66, 66, 83, 46, 32, 32,
        32, 32, -102, 32, 32, 32, 32, 32, 32, 32, 32, 18, 32, -33, -110, 32,
        32, 28, 18, 32, -33, -102, -110, 32, 5, 18, 32, -102, -110, 32, 5, 18,
        32, -110, 32, 18, 32, -102, -110, 32, 5, 18, 32, -102, -110, 32, 32, 32,
        5, 18, 32, -102, -110, 32, 5, 67, 79, 77, 32, -102, 32, 5, 32, -102,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, -33,
        18, 32, 32, 32, -110, 32, 32, 32, 5, 18, 32, 32, -110, 32, 32, 18,
        32, 32, -102, -110, 32, 32, 5, 18, 32, 32, -102, -110, 32, 32, 5, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, -102, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 5, 32, 32, -102,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,

        32, 32, 32, 32, 32, 32, 32,

        13
    };

    protected void logo() throws IOException {
        write(LOGO);
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