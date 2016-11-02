/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import javax.ws.rs.core.Response;

/**
 *
 * @author elz24996
 */
public class Void implements ResponseProducer {
    
    public Response toResponse(){
        return Response.ok().build();
    }
    
}
