/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin;

import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author elz24996
 */
public class Properties extends java.util.Properties {
    
    private static Properties instance = null;

    public synchronized static Properties getInstance() {
       if(instance == null) {
          instance = new Properties();
       }
       return instance;
    }
    
    public Properties(){
        super();
        try {
            load(new FileInputStream("topcat_daaas_plugin.properties"));
        } catch(IOException e){
            
        }
    }
    
}
