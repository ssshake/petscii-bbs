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

    protected int __currentPage = 0;
    protected int __screenRows = 15;

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
    protected boolean showAuthor = false;

    protected Map<Integer, BBS> bbses = emptyMap();

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

    void printCommandOptions(){
        write(WHITE);print("#"); write(GREY3);
        print(", [");
        write(WHITE); print("+-"); write(GREY3);
        print("]Page [");
        write(WHITE); print("."); write(GREY3);
        print("]");
        write(WHITE); print("Q"); write(GREY3);
        print("uit> ");
    }

    @Override
    public void doLoop() throws Exception {
        write(LOWERCASE, CASE_LOCK);
        log("BBS List entering (" + domain + ")");
        listBBSes();
        while (true) {

            log("BBS List waiting for input");
            printCommandOptions();
            resetInput();
            flush(); String inputRaw = readLine();
            String input = lowerCase(trim(inputRaw));

            if (".".equals(input) || "exit".equals(input) || "quit".equals(input) || "q".equals(input)) {
                break;

            } else if ("+".equals(input)) {
                ++__currentPage;
            } else if ("-".equals(input)) {
                if (__currentPage > 0){
                    --__currentPage;
                }
            } else if (bbses.containsKey(toInt(input))) {
                displayBBSInfo(toInt(input));
                continue;
            }
            listBBSes();
        }
        flush();
    }

    protected Map<Integer, BBS> getAllBBSes() throws Exception {
        
        JSONObject response = (JSONObject) httpGetJson(getApi());
        
        JSONArray bbsList = (JSONArray) response.get("bbs_list");
        
        Map<Integer, BBS> result = new LinkedHashMap<>();

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
            
            // result.put( i + 1 + ( perPage * ( page - 1 ) ), bbs); //wut
            result.put(i + 1, bbs);
        }
        return result;
    }

    protected Map<Integer, BBS> getBBSesForPage(){
        int minRow = __currentPage * __screenRows;
        int maxRow = minRow + __screenRows;

        Map<Integer, BBS> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, BBS> entry: bbses.entrySet()) {
            int i = entry.getKey();
            BBS bbs = entry.getValue();
            if (i >= minRow && i <= maxRow){
                result.put(i, bbs);
            }
        }        
        return result;
    }

    protected void listBBSes() throws Exception {
        log("LISTING OF BBSes NOW");
        cls();
        logo();

        if (isEmpty(bbses)) {
            waitOn();
            bbses = getAllBBSes();
            waitOff();
        }

        Map<Integer, BBS> bbsForThisPage = getBBSesForPage();



        for (Map.Entry<Integer, BBS> entry: bbsForThisPage.entrySet()) {
            int i = entry.getKey();
            BBS bbs = entry.getValue();
            write(WHITE); print(i + "."); write(GREY3);
            final int iLen = 37-String.valueOf(i).length();
            String line = WordUtils.wrap(filterPrintable(HtmlUtils.htmlClean(bbs.name)), iLen, "\r", true);
            println(line.replaceAll("\r", "\r " + repeat(" ", 37-iLen)));
        }
        newline();
    }
    protected void displayBBSInfo(int n) throws Exception {
        int i = 3;
        cls();
        logo();
        waitOn();

        String author = null;
        final BBS p = bbses.get(n);

        List<String> rows = wordWrap("Name: " + p.name);

        rows.add("Address: " + p.address);
        rows.add("Port: " + p.port);
        rows.add("Sysop: " + p.sysop);
        rows.add("Warez: " + p.warez);
        rows.add("Hardware: " + p.hardware);
        rows.add("Web: " + p.web);
        rows.add("Flashterm: " + p.flashterm);
        rows.add("Comment: " + p.comment);
        rows.add("Location: " + p.location);
        rows.add("Update: " + p.update);

        waitOff();
        int page = 1;
        int j = 0;
        boolean forward = true;

        while (j < rows.size()) {

            String row = rows.get(j);

            if (j % 2 == 0){
                write(CYAN);
            } else {
                write(WHITE);
            }
            
            println(row);
            ++j;
        }

        println();
        write(WHITE);
        print(".=EXIT");
        write(GREY3);

        resetInput(); 
        int ch = readKey();

        if (ch == '.') {
            listBBSes();
            return;
        }
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

}