package org.icatproject.topcatdaaasplugin;

import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.ejb.Stateless;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
 


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcatdaaasplugin.exceptions.*;
import org.icatproject.topcatdaaasplugin.httpclient.HttpClient;
import org.icatproject.topcatdaaasplugin.httpclient.Response;
import org.icatproject.topcatdaaasplugin.Properties;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonReader;


public class IcatClient {

	private HttpClient httpClient;
    private String sessionId;

    public void IcatClient(String icatUrl, String sessionId) {
        try {
            this.httpClient = new HttpClient(icatUrl + "/icat");
            this.sessionId = sessionId;
        } catch(Exception e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    public JsonArray query(String query) throws Exception {
    	String url = "entityManager?sessionId=" + URLEncoder.encode(sessionId, "UTF8") + "&query=" + URLEncoder.encode(query, "UTF8");
    	return parseJsonArray(httpClient.get(url, generateStandardHeaders()).toString());
    }


    private Map<String, String> generateStandardHeaders(){
        Map<String, String> out = new HashMap<String, String>();
        out.put("Content-Type", "application/json");
        return out;
    }

    private JsonArray parseJsonArray(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonArray out = jsonReader.readArray();
        jsonReader.close();
        return out;
    }

}