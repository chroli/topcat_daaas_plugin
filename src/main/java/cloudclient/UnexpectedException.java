/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

/**
 *
 * @author elz24996
 */
public class UnexpectedException extends CloudClientException {
    
    public UnexpectedException(String message){
        super(message);
        this.status = 500;
    }
    
}
