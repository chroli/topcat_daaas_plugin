/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Images
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.exceptions;

import org.icatproject.topcatdaaasplugin.rest.ResponseProducer;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;

/**
 * @author elz24996
 */
public class DaaasException extends Exception implements ResponseProducer {

    private String message;
    protected int status;

    public DaaasException(String message) {
        this.status = 400;
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public JsonObjectBuilder toJsonObjectBuilder() {
        return Json.createObjectBuilder().add("message", (String) getMessage());
    }

    public String toString() {
        return toJsonObjectBuilder().build().toString();
    }

    public Response toResponse() {
        return Response.status(status).entity(toString()).build();
    }

}
