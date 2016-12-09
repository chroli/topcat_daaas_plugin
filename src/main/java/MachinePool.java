package org.icatproject.topcatdaaasplugin;

import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Schedule;
 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcatdaaasplugin.exceptions.*;
import org.icatproject.topcatdaaasplugin.httpclient.HttpClient;
import org.icatproject.topcatdaaasplugin.httpclient.Response;
import org.icatproject.topcatdaaasplugin.Properties;
import org.icatproject.topcatdaaasplugin.database.entities.*;


@Singleton
@Startup
public class MachinePool {

	HttpClient httpClient;

	@PostConstruct
    public void init() {
        
    }

    @Schedule(hour="*", minute="*", second="*")
    public void topupPool(){
        //make machines are fully booted before persisting them (i.e. the ssh port can be accessed)
    }

    public synchronized Machine aquireMachine(MachineType machineType){

    }

}