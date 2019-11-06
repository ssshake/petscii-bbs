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
public class Chat extends PetsciiThread {

    static String HR_TOP = StringUtils.repeat(chr(163), 39);

    protected String domain = "https://theoldnet.com";
    protected byte[] logo = LOGO;


    public Chat() {
        // Mandatory
    }

    public Chat(String domain) {
        this.domain = domain;
    }

    public Chat(String domain, byte[] logo) {
        this.domain = domain;
        this.logo = logo;
    }

    @Override
    public void doLoop() throws Exception {
        write(LOWERCASE, CASE_LOCK);
        cls();
        logo();
        help();
        println();
        log("Chat");
        while (true) {

            log("Chat waiting for input");
            
            write(ORANGE);
            print(">> ");
            write(GREY3);
            resetInput();
            flush(); String inputRaw = readLine();
            String input = lowerCase(trim(inputRaw));


            if (".".equals(input) || "exit".equals(input) || "quit".equals(input) || "q".equals(input)) {
                break;

            } else if ("help".equals(input) || "h".equals(input)) {
                help();
                continue;

            } else if ("users".equals(input)) {
                listClients();
                continue;

            } else if (substring(input,0,5).equalsIgnoreCase("send ")) {

                long client = toLong(input.replaceAll("^send ([0-9]+).*$", "$1"));

                String message = input.replaceAll("^send [0-9]+ (.*)$", "$1");

                sendToClient(client, message);


            } else if (substring(input,0,5).equalsIgnoreCase("name ")) {
                String newName = defaultString(input.replaceAll("^name ([^\\s]+).*$", "$1"));
                changeClientName(newName);
                println("name changed to " + newName);


            } else {
                log("Sending to all");
                sendToAllClients(input);
            }
        }
        flush();
    }

    protected void help() throws Exception {
        println("Valid Commands Are:");
        println("quit, help, users, send, name");
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
        -102, 32, 30, 18, 32, 32, 32, 32, -102, -110, 32, -97, 18, 32, -102, -110,
        32, 32, -97, 18, 32, -102, -110, 32, 32, -100, 18, 32, 32, -102, -110, 32,
        32, -127, 18, 32, 32, 32, 32, 32, -102, -110, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 30, 18, 32,
        -102, -110, 32, 32, 32, 32, 32, -97, 18, 32, -102, -110, 32, 32, -97, 18,
        32, -102, -110, 32, -100, 18, 32, -110, 32, -102, 32, -100, 18, 32, -102, -110,
        32, 32, 32, -127, 18, 32, -102, -110, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 30, 18, 32,
        -102, -110, 32, 32, 32, 32, 32, -97, 18, 32, 32, 32, 32, -102, -110, 32,
        -100, 18, 32, 32, 32, 32, -110, 32, -102, 32, 32, -127, 18, 32, -102, -110,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 30, 18, 32, -102, -110, 32, 32, 32, 32, 32, -97,
        18, 32, -110, 32, 32, 18, 32, -102, -110, 32, -100, 18, 32, -97, -110, 32,
        32, -100, 18, 32, -110, 32, -102, 32, 32, -127, 18, 32, -102, -110, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 30, 18, 32, 32, 32, 32, -102, -110, 32, -97, 18, 32,
        -102, -110, 32, 32, -97, 18, 32, -102, -110, 32, -100, 18, 32, -102, -110, 32,
        32, -100, 18, 32, -110, 32, -102, 32, 32, -127, 18, 32, -102, -110, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, -97, 32, -102, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,

        32, 32, 32, 32, 32,

        13
    };

    protected void logo() throws IOException {
        write(logo);
        write(GREY3);
    }


    //CHAT
    protected void listClients() throws Exception {
        println("You are #" + getClientId() + ": "+getClientName() + " [" + getClientClass().getSimpleName() + "]");
        newline();
        for (Map.Entry<Long, PetsciiThread> entry: clients.entrySet())
            if (entry.getKey() != getClientId())
                println("#" + entry.getKey() +": "+entry.getValue().getClientName() + " [" + entry.getValue().getClientClass().getSimpleName() + "]");
        println();
    }

    protected void sendToClient(long client, String message) throws Exception{
        if (getClients().containsKey(client) && isNotBlank(message)) {
            System.out.println("Sending '"+message+"' to #"+client);
            int exitCode = send(client, message);
            System.out.println("Message sent, exitCode="+exitCode+".");
        }
    }

    protected void sendToAllClients(String message) throws Exception {

        for (Map.Entry<Long, PetsciiThread> entry: clients.entrySet())
            if (entry.getKey() != getClientId()) {
                sendToClient(entry.getKey(), message);
            }
        println();
    }    


    @Override
    public void receive(long sender, Object message) {
        log("--------------------------------");
        log("From "+getClients().get(sender).getClientName()+": " +message);
        log("--------------------------------");
        println();
        
        write(LIGHT_BLUE);
        print(getClients().get(sender).getClientName() + " says: ");
  
        write(GREY3);
        println(message.toString());

        write(ORANGE);
        print(">> ");
        write(GREY3);      
    }

}