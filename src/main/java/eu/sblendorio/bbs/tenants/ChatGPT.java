package eu.sblendorio.bbs.tenants;
import eu.sblendorio.bbs.core.PetsciiThread;

import static eu.sblendorio.bbs.core.Colors.WHITE;
import static eu.sblendorio.bbs.core.Keys.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatGPT extends PetsciiThread {

    final String API_URL ="https://api.openai.com/v1/chat/completions";
    String API_KEY = System.getenv("OPEN_AI_API_KEY");
    HttpClient httpClient;

    public ChatGPT() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void doLoop() throws Exception {
        String input = "";
        write(LOWERCASE, CASE_LOCK);
        while(true) {
            if(input.isEmpty()) {
                write(CLR);
            }
            baseUI();
            input = readLine();

            if(input.equals("exit")) {
                break;
            } else {
                write(CLR);
                baseUI();
                gotoXY(1, 3); write(WHITE); print("You asked: " + input);
                gotoXY(1, 4); write(WHITE); print("ChatGPT says:");
                String response = postRequest(input);
                gotoXY(1, 5); write(WHITE); print(response);

                flush();
            }
        }
    }

    private void baseUI() throws IOException {
        gotoXY(1, 1); write(WHITE); print("ChatGPT v0.1 (exit to quit)");
        resetInput();
        gotoXY(0, 23); write(WHITE); print(">"); 
    }
    private String postRequest(String userInput ) throws IOException, InterruptedException {
        String payload = "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"system\",\"content\": \"You are a helpful assistant.\"},{\"role\": \"user\",\"content\": \"" + userInput +"\"}]}";

        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
            .header("content-type", "application/json")
            .header("accept", "application/json")
            .header("authorization", "Bearer " + API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
    
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        String content = response.body();
        System.out.println(content);

        Map<String, Object> json = parseJsonToMap(content);
        String result = (String)json.get("content");
        return result;
    }

    private Map<String, Object> parseJsonToMap(String jsonString) {
        Map<String, Object> resultMap = new HashMap<>();
        Pattern pattern = Pattern.compile("\"(.*?)\"\\s*:\\s*(\".*?\"|\\d+|true|false|null|\\{.*?\\})");
        Matcher matcher = pattern.matcher(jsonString);

        while (matcher.find()) {
            String key = matcher.group(1);
            String valueString = matcher.group(2);
            Object value;

            if (valueString.startsWith("\"") && valueString.endsWith("\"")) {
                value = valueString.substring(1, valueString.length() - 1);
            } else if (valueString.equals("null")) {
                value = null;
            } else if (valueString.equals("true") || valueString.equals("false")) {
                value = Boolean.valueOf(valueString);
            } else if (valueString.startsWith("{") && valueString.endsWith("}")) {
                value = parseJsonToMap(valueString);
            } else {
                value = Integer.parseInt(valueString);
            }

            resultMap.put(key, value);
        }

        return resultMap;
    }
}