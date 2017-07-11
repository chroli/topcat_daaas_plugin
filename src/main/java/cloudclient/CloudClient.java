/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import java.util.Map;
import java.util.HashMap;
import java.util.Map;

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
import javax.ejb.DependsOn;
import javax.ejb.Schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcatdaaasplugin.cloudclient.entities.*;
import org.icatproject.topcatdaaasplugin.exceptions.*;
import org.icatproject.topcatdaaasplugin.EntityList;

/**
 *
 * @author elz24996
 */
@DependsOn("TrustManagerInstaller")
@Singleton
@Startup
public class CloudClient {

    private static final Logger logger = LoggerFactory.getLogger(CloudClient.class);
    
    private String authToken;
    private String project;
    private HttpClient identityHttpClient;
    private HttpClient computeHttpClient;
    private HttpClient imageHttpClient;
    
    @Schedule(hour="*", minute="*")
    public void createSession() throws Exception {

        try {
            Properties properties = new Properties();

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
            auth.add("scope", scope);

            String data = Json.createObjectBuilder().add("auth", auth).build().toString();

            this.authToken = identityHttpClient.post("auth/tokens", headers, data).getHeader("X-Subject-Token");

        } catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        try {
            Properties properties = new Properties();
            project = properties.getProperty("project");
            identityHttpClient = new HttpClient(properties.getProperty("identityEndpoint") + "/v3");
            computeHttpClient = new HttpClient(properties.getProperty("computeEndpoint") + "/v2/" + project);
            imageHttpClient = new HttpClient(properties.getProperty("imageEndpoint") + "/v2");

            createSession();
        } catch(Exception e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    public EntityList<Flavor> getFlavors()  throws DaaasException {
        EntityList<Flavor> out = new EntityList<Flavor>();
        
        try {
            Response response = computeHttpClient.get("flavors/detail", generateStandardHeaders());
            if(response.getCode() == 200){
                JsonObject results = parseJson(response.toString());
                for(JsonValue flavorValue : results.getJsonArray("flavors")){
                    JsonObject cloudFlavor = (JsonObject) flavorValue;
                    Flavor flavor = new Flavor();
                    flavor.setId(cloudFlavor.getString("id"));
                    flavor.setName(cloudFlavor.getString("name"));
                    flavor.setCpus(cloudFlavor.getInt("vcpus"));
                    flavor.setRam(cloudFlavor.getInt("ram"));
                    flavor.setDisk(cloudFlavor.getInt("disk"));

                    out.add(flavor);
                }
            } else {
                throw new BadRequestException(response.toString());
            }
        } catch(DaaasException e){
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

    public Flavor getFlavor(String id) throws DaaasException {
        Flavor out = new Flavor();

        try {
            Response response = computeHttpClient.get("flavors/" + id, generateStandardHeaders());
            if(response.getCode() == 200){
                JsonObject results = parseJson(response.toString());
                JsonObject cloudFlavor = results.getJsonObject("flavor");
                out.setId(cloudFlavor.getString("id"));
                out.setName(cloudFlavor.getString("name"));
                out.setCpus(cloudFlavor.getInt("vcpus"));
                out.setRam(cloudFlavor.getInt("ram"));
                out.setDisk(cloudFlavor.getInt("disk"));
            } else {
                throw new BadRequestException(response.toString());
            }
        } catch(DaaasException e){
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

    public EntityList<Image> getImages()  throws DaaasException {
        EntityList<Image> out = new EntityList<Image>();
        
        try {
            Response response = imageHttpClient.get("images", generateStandardHeaders());
            if(response.getCode() == 200){
                JsonObject results = parseJson(response.toString());
                for(JsonValue imageValue : results.getJsonArray("images")){
                    JsonObject cloudImage = (JsonObject) imageValue;
                    Image image = new Image();
                    image.setId(cloudImage.getString("id"));
                    image.setName(cloudImage.getString("name"));
                    image.setSize(cloudImage.getInt("size"));

                    out.add(image);
                }
            } else {
                throw new BadRequestException(response.toString());
            }
        } catch(DaaasException e){
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

    public EntityList<AvailabilityZone> getAvailabilityZones()  throws DaaasException {
        EntityList<AvailabilityZone> out = new EntityList<AvailabilityZone>();
        
        try {
            Response response = computeHttpClient.get("os-availability-zone", generateStandardHeaders());
            if(response.getCode() == 200){
                JsonObject results = parseJson(response.toString());
                for(JsonValue availabilityZoneInfoValue : results.getJsonArray("availabilityZoneInfo")){
                    JsonObject availabilityZoneInfo = (JsonObject) availabilityZoneInfoValue;
                    AvailabilityZone availabilityZone = new AvailabilityZone();
                    availabilityZone.setName(availabilityZoneInfo.getString("zoneName"));
                    availabilityZone.setIsAvailable(availabilityZoneInfo.getJsonObject("zoneState").getBoolean("available"));

                    out.add(availabilityZone);
                }
            } else {
                throw new BadRequestException(response.toString());
            }
        } catch(DaaasException e){
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

    public Server createServer(String name, String imageRef, String flavorRef, String availabilityZone, Map<String, String> metadata) throws DaaasException {
        try {
            logger.info("createServer: " + name + ", " + imageRef + ", " + flavorRef + ", " + availabilityZone);
            // {
            //     "server" => {
            //         "name" => "auto-allocate-network",
            //         "imageRef" => "ba123970-efbd-4a91-885a-b069e03e003d",
            //         "flavorRef" => "8a34f302-4cdc-459c-9e45-c5655c94382f",
            //         "availability_zone" => "ceph"
            //         "metadata" => {
            //             "AQ_ARCHETYPE" => "ral-tier1",
            //             "AQ_DOMAIN" => "",
            //             "AQ_PERSONALITY" => "daaas-common",
            //             "AQ_SANDBOX" => "sap86629/daas-excitations",
            //             "AQ_OSVERSION" => "7x-x86_6"
            //         }
            //     }
            // }
            JsonObjectBuilder server = Json.createObjectBuilder();
            server.add("name", name);
            server.add("imageRef", imageRef);
            server.add("flavorRef", flavorRef);
            server.add("availability_zone", availabilityZone);
            JsonObjectBuilder metadataNode = Json.createObjectBuilder();
            for(Map.Entry<String, String> entry : metadata.entrySet()) {
                metadataNode.add(entry.getKey(), entry.getValue());
            }
            server.add("metadata",  metadataNode);
            Properties properties = new Properties();
            server.add("key_name", properties.getProperty("sshKeyPairName"));

            String data = Json.createObjectBuilder().add("server", server).build().toString();

            Response response = computeHttpClient.post("servers", generateStandardHeaders(), data);
            if(response.getCode() >= 400){
                throw new BadRequestException(response.toString());
            }

            Server out = new Server();
            out.setId(parseJson(response.toString()).getJsonObject("server").getString("id"));
            return out;
        } catch(DaaasException e){
            throw e;
        } catch(Exception e){
            String message = e.getMessage();
            if(message == null){
                message = e.toString();
            }
            throw new UnexpectedException(message);
        }
    }

    public Server getServer(String id) throws DaaasException {
        try {
            Server out = new Server();
            out.setId(id);
            Response response = computeHttpClient.get("servers/" + id, generateStandardHeaders());
            if(response.getCode() == 200){
                JsonObject metadata = parseJson(response.toString()).getJsonObject("server").getJsonObject("metadata");
                

                try {
                    out.setStatus(metadata.getString("AQ_STATUS"));

                    if(out.getStatus().equals("FAILED")){
                       logger.info("getServer: server has failed: AQ_STATUS = 'FAILED': " + id); 
                    } else {
                        String hostnames[] = metadata.getString("HOSTNAMES").split("[ ]*,[ ]*");
                        if(hostnames.length == 1){
                            out.setHost(hostnames[0]);
                        } else {
                            out.setStatus("FAILED");
                            logger.info("getServer: server has failed: more than one host name has been specified: " + metadata.getString("HOSTNAMES") + ": " + id); 
                        }
                    }
                } catch(NullPointerException e){}

            } else {
                throw new BadRequestException(response.toString());
            }
            return out;
        } catch(DaaasException e){
            throw e;
        } catch(Exception e){
            String message = e.getMessage();
            if(message == null){
                message = e.toString();
            }
            throw new UnexpectedException(message);
        }
    }

    public void deleteServer(String id) throws DaaasException {
        try {
            Response response = computeHttpClient.delete("servers/" + id, generateStandardHeaders());
            if(response.getCode() >= 400){
                throw new BadRequestException(response.toString());
            }
        } catch(DaaasException e){
            throw e;
        } catch(Exception e){
            String message = e.getMessage();
            if(message == null){
                message = e.toString();
            }
            throw new UnexpectedException(message);
        }
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
