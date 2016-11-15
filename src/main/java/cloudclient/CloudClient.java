/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonReader;


import org.icatproject.topcatdaaasplugin.httpclient.HttpClient;
import org.icatproject.topcatdaaasplugin.httpclient.Response;
import org.icatproject.topcatdaaasplugin.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author elz24996
 */
@Singleton
@Startup
public class CloudClient {

    private static final Logger logger = LoggerFactory.getLogger(CloudClient.class);
    
    private static String authToken;
    private static String project;
    private static HttpClient identityHttpClient;
    private static HttpClient computeHttpClient;
    private static HttpClient imageHttpClient;
    

    @PostConstruct
    public void init() {
        try {
            Properties properties = new Properties();
            project = properties.getProperty("project");
            identityHttpClient = new HttpClient(properties.getProperty("identityEndpoint") + "/v3");
            computeHttpClient = new HttpClient(properties.getProperty("computeEndpoint") + "/v2/" + project);
            imageHttpClient = new HttpClient(properties.getProperty("imageEndpoint") + "/v2");


            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            
            // {
            //     "auth" => {
            //         "identity" => {
            //             "methods" => [
            //                 "password"
            //             ],
            //             "password" => {
            //                 "user" => {
            //                     "name" => "elz24996",
            //                     "domain" => {
            //                         "name" => "stfc"
            //                     },
            //                     "password" => "saadsad"
            //                 }
            //             }
            //         },
            //         "scope" => {
            //             "project" => {
            //                 "id" => "3242342342344ada"
            //             }
            //         }
            //     }
            // }

            JsonObjectBuilder auth = Json.createObjectBuilder();
            JsonObjectBuilder identity = Json.createObjectBuilder();
            JsonArrayBuilder methods = Json.createArrayBuilder();
            methods.add("password");
            identity.add("methods", methods);
            JsonObjectBuilder password = Json.createObjectBuilder();
            JsonObjectBuilder user = Json.createObjectBuilder();
            user.add("name", properties.getProperty("username"));
            JsonObjectBuilder domain = Json.createObjectBuilder();
            domain.add("name", properties.getProperty("domain"));
            user.add("domain", domain);
            user.add("password", properties.getProperty("password"));
            password.add("user", user);
            identity.add("password", password);
            auth.add("identity", identity);
            JsonObjectBuilder scope = Json.createObjectBuilder();
            JsonObjectBuilder project = Json.createObjectBuilder();
            project.add("id", properties.getProperty("project"));
            scope.add("project", project);
            auth.add("identity", identity);
            auth.add("scope", scope);

            String data = Json.createObjectBuilder().add("auth", auth).build().toString();

            this.authToken = identityHttpClient.post("auth/tokens", headers, data).getHeader("X-Subject-Token");

            logger.info("created authToken " + this.authToken);

        } catch(Exception e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    public EntityList<Machine> getMachines()  throws CloudClientException {
        EntityList<Machine> out = new EntityList<Machine>();
        
        try {
            Response response = computeHttpClient.get("servers/detail", generateStandardHeaders());
            if(response.getCode() == 200){
                JsonObject results = parseJson(response.toString());
                for(JsonValue serverValue : results.getJsonArray("servers")){
                    JsonObject server = (JsonObject) serverValue;
                    Machine machine = new Machine(this);
                    machine.setId(server.getString("id"));
                    machine.setName(server.getString("name"));
                    machine.setState(server.getString("status"));
                    JsonArray addresses = server.getJsonObject("addresses").getJsonArray("public");
                    if(addresses != null){
                        machine.setHost(addresses.getJsonObject(0).getString("addr"));
                    } else {
                        machine.setHost("");
                    }

                    out.add(machine);
                }
            } else {
                throw new BadRequestException(response.toString());
            }
        } catch(CloudClientException e){
            throw e;
        } catch(Exception e){
            String message = e.getMessage();
            if(message == null){
                message = e.toString();
            }
            throw new UnexpectedException(message);
        }

        return out;
    }

    private Map<String, String> generateStandardHeaders(){
        Map<String, String> out = new HashMap<String, String>();
        out.put("Content-Type", "application/json");
        out.put("X-Auth-Token", authToken);
        return out;
    }

    private JsonObject parseJson(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonObject out = jsonReader.readObject();
        jsonReader.close();
        return out;
    }
    
}
