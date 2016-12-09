package org.icatproject.topcatdaaasplugin;

import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;

import javax.ejb.Stateless;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
 


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcatdaaasplugin.exceptions.*;
import org.icatproject.topcatdaaasplugin.httpclient.HttpClient;
import org.icatproject.topcatdaaasplugin.httpclient.Response;
import org.icatproject.topcatdaaasplugin.Properties;




@DependsOn("TrustManagerInstaller")
@Stateless
public class TopcatClient {

	HttpClient httpClient;

	@PostConstruct
    public void init() {
        try {
            Properties properties = new Properties();
            httpClient = new HttpClient(properties.getProperty("topcatUrl") + "/topcat");
        } catch(Exception e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    public Boolean isAdmin(String icatUrl, String sessionId) throws Exception {
    	String url = "admin/isValidSession?icatUrl=" + URLEncoder.encode(icatUrl, "UTF8") + "&sessionId=" + URLEncoder.encode(sessionId, "UTF8");
    	return httpClient.get(url, generateStandardHeaders()).toString().equals("true");
    }


    private Map<String, String> generateStandardHeaders(){
        Map<String, String> out = new HashMap<String, String>();
        out.put("Content-Type", "application/json");
        return out;
    }

}