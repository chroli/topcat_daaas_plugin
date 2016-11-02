/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icatproject.topcatdaaasplugin.cloudclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.XmlRpcException;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;

/**
 *
 * @author elz24996
 */

public class CloudClient {
    
    private String sessionId;
    private XmlRpcClient client;
    
    public CloudClient() throws CloudClientException {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("https://hn1.nubes.rl.ac.uk/RPC2"));
            this.client = new XmlRpcClient();
            client.setConfig(config);
        } catch(MalformedURLException e){
            throw new UnexpectedException(e.getMessage());
        }
    }
    
    public CloudClient(String sessionId) throws CloudClientException {
        this();
        this.sessionId = sessionId;
    }

    public Session login(String username, String password)
            throws CloudClientException {
        
        Object[] params = new Object[]{username + ":" + password, username, "", -1};
        try {
            Object[] result = (Object[]) client.execute("one.user.login", params);

            boolean isSuccess = (boolean) result[0];

            if(isSuccess){
                Session out = new Session(this);
                out.setSessionId(username + ":" + (String) result[1]);
                return out;
            }
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }
        
        throw new BadRequestException("Either your username or password is incorrect");
    }
    
    public User getUser()
            throws CloudClientException {
        
        return getUser(-1);
    }
    
    public User getUser(Integer id)
            throws CloudClientException {
        
        Object[] params = new Object[]{
            //auth token
            sessionId,
            //object id
            id
        };
        
        try {
            Object[] result = (Object[]) client.execute("one.user.info", params);

            boolean isSuccess = (boolean) result[0];

            if(isSuccess){
                User out = new User(this);
                Document document = createDocument((String) result[1]);
                XPath xPath =  XPathFactory.newInstance().newXPath();
                out.setUsername((String) xPath.compile("USER/NAME").evaluate(document));
                return out;
            } else if((int) result[2] == 0x0100){
                throw new AuthenticationException((String) result[1]);
            } else {
                throw new BadRequestException((String) result[1]);
            }
        } catch(CloudClientException e){
            throw e;
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }
        
    }

    private static String[] machineStates = {
        "LCM_INIT",
        "PROLOG",
        "BOOT",
        "RUNNING",
        "MIGRATE",
        "SAVE_STOP",
        "SAVE_SUSPEND",
        "SAVE_MIGRATE",
        "PROLOG_MIGRATE",
        "PROLOG_RESUME",
        "EPILOG_STOP",
        "EPILOG",
        "SHUTDOWN",
        "CANCEL",
        "FAILURE",
        "CLEANUP_RESUBMIT",
        "UNKNOWN",
        "HOTPLUG",
        "SHUTDOWN_POWEROFF",
        "BOOT_UNKNOWN",
        "BOOT_POWEROFF",
        "BOOT_SUSPENDED",
        "BOOT_STOPPED",
        "CLEANUP_DELETE",
        "HOTPLUG_SNAPSHOT",
        "HOTPLUG_NIC",
        "HOTPLUG_SAVEAS",
        "HOTPLUG_SAVEAS_POWEROFF",
        "HOTPLUG_SAVEAS_SUSPENDED",
        "SHUTDOWN_UNDEPLOY",
        "EPILOG_UNDEPLOY",
        "PROLOG_UNDEPLOY",
        "BOOT_UNDEPLOY",
        "HOTPLUG_PROLOG_POWEROFF",
        "HOTPLUG_EPILOG_POWEROFF",
        "BOOT_MIGRATE",
        "BOOT_FAILURE",
        "BOOT_MIGRATE_FAILURE",
        "PROLOG_MIGRATE_FAILURE",
        "PROLOG_FAILURE",
        "EPILOG_FAILURE",
        "EPILOG_STOP_FAILURE",
        "EPILOG_UNDEPLOY_FAILURE",
        "PROLOG_MIGRATE_POWEROFF",
        "PROLOG_MIGRATE_POWEROFF_FAILURE",
        "PROLOG_MIGRATE_SUSPEND",
        "PROLOG_MIGRATE_SUSPEND_FAILURE",
        "BOOT_UNDEPLOY_FAILURE",
        "BOOT_STOPPED_FAILURE",
        "PROLOG_RESUME_FAILURE",
        "PROLOG_UNDEPLOY_FAILURE",
        "DISK_SNAPSHOT_POWEROFF",
        "DISK_SNAPSHOT_REVERT_POWEROFF",
        "DISK_SNAPSHOT_DELETE_POWEROFF",
        "DISK_SNAPSHOT_SUSPENDED",
        "DISK_SNAPSHOT_REVERT_SUSPENDED",
        "DISK_SNAPSHOT_DELETE_SUSPENDED",
        "DISK_SNAPSHOT",
        "DISK_SNAPSHOT_REVERT",
        "DISK_SNAPSHOT_DELETE"
    };

    public EntityList<Machine> getMachines()
            throws CloudClientException {
        
        Object[] params = new Object[]{
            //auth token
            sessionId,
            //show only user's VMs & groups ??
            -1,
            //offset for pagination	
            0,
            //number of entries to return
            -1,
            //VM state to filter by.
            -1
        };
        
        try {
            Object[] result = (Object[]) client.execute("one.vmpool.info", params);

            boolean isSuccess = (boolean) result[0];

            if(isSuccess){
                EntityList<Machine> out = new EntityList<Machine>();
                
                StringBuilder query = new StringBuilder();
                query.append("VM_POOL/VM");
                
                EntityList<Template> templates = getTemplates();
                if(templates.size() > 0){
                    query.append("[");
                }
                for(int i = 0; i < templates.size(); i++){
                    Template template = templates.get(i);
                    query.append("TEMPLATE/CONTEXT/BASETEMPLATE=");
                    query.append(template.getId());
                    if(i < templates.size() - 1){
                        query.append(" or ");
                    }
                }
                if(templates.size() > 0){
                    query.append("]");
                }
                
                Document document = createDocument((String) result[1]);
                XPath xPath =  XPathFactory.newInstance().newXPath();
                NodeList machines = (NodeList) xPath.compile(query.toString()).evaluate(document, XPathConstants.NODESET);
                
                for(int i = 0; i < machines.getLength(); i++){
                    Machine machine = new Machine(this);
                    Node machineNode = machines.item(i);
                    machine.setId(Integer.parseInt(xPath.compile("ID").evaluate(machineNode)));
                    machine.setName(xPath.compile("NAME").evaluate(machineNode));
                    machine.setGroupName(xPath.compile("GNAME").evaluate(machineNode));
                    machine.setState(machineStates[Integer.parseInt(xPath.compile("STATE").evaluate(machineNode))]);
                    String ip = xPath.compile("TEMPLATE/CONTEXT/ETH0_IP").evaluate(machineNode);
                    machine.setHost(InetAddress.getByName(ip).getHostName());
                    out.add(machine);
                }
                return out;
            } else if((int) result[2] == 0x0100){
                throw new AuthenticationException((String) result[1]);
            } else {
                throw new BadRequestException((String) result[1]);
            }
        } catch(CloudClientException e){
            throw e;
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }
        
    }
    
    public Machine createMachine(Integer templateId, String name)
            throws CloudClientException {
        
        Object[] params = new Object[]{
            //auth token
            sessionId,
            //which template to base vm on
            templateId,
            //name of vm
            name,
            //start normally
            false,
            //extra content variables
            ""
        };
        
        try {
            Object[] result = (Object[]) client.execute("one.template.instantiate", params);

            boolean isSuccess = (boolean) result[0];

            if(isSuccess){
                Integer machineId = (Integer) result[1];
                return getMachine(machineId);
            } else if((int) result[2] == 0x0100){
                throw new AuthenticationException((String) result[1]);
            } else {
                throw new BadRequestException((String) result[1]);
            }
        } catch(CloudClientException e){
            throw e;
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }
    }

    public Machine getMachine(Integer machineId)
            throws CloudClientException {
        Machine out = new Machine(this);

        Object[] params = new Object[]{
            //auth token
            sessionId,
            //The object ID
            machineId
        };

        try {
            Object[] result = (Object[]) client.execute("one.vm.info", params);

            boolean isSuccess = (boolean) result[0];

            if(isSuccess){
                Document document = createDocument((String) result[1]);
                XPath xPath =  XPathFactory.newInstance().newXPath();

                out.setId(Integer.parseInt(xPath.compile("VM/ID").evaluate(document)));
                out.setName(xPath.compile("VM/NAME").evaluate(document));
                out.setGroupName(xPath.compile("VM/GNAME").evaluate(document));
                out.setState(machineStates[Integer.parseInt(xPath.compile("VM/STATE").evaluate(document))]);
                String ip = xPath.compile("VM/TEMPLATE/CONTEXT/ETH0_IP").evaluate(document);
                out.setHost(InetAddress.getByName(ip).getHostName());
            } else {
                throw new BadRequestException((String) result[1]);
            }
        } catch(CloudClientException e){
            throw e;
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }

        return out;
    }

    public Void deleteMachine(Integer machineId)
            throws CloudClientException {
        Machine machine = getMachine(machineId);
        
        
        Object[] params = null;

        if(machine.getState().equals("FAILURE")){
            params = new Object[]{
                //auth token
                sessionId,
                //the action
                "delete",
                //The object ID
                machineId
            };
        } else {
            params = new Object[]{
                //auth token
                sessionId,
                //the action
                "shutdown-hard",
                //The object ID
                machineId
            };
        }

        try {
            Object[] result = (Object[]) client.execute("one.vm.action", params);

            boolean isSuccess = (boolean) result[0];

            if(isSuccess){
                return new Void();
            } else {
                throw new BadRequestException((String) result[1]);
            }
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }

    }
    
    public EntityList<Template> getTemplates()
            throws CloudClientException {
        
        Object[] params = new Object[]{
            //auth token
            sessionId,
            //show only connected user’s and his group’s resources
            -1,
            //offset for pagination	
            -1,
            //number of entries to return
            -1
        };
        
        try {
            Object[] result = (Object[]) client.execute("one.templatepool.info", params);

            boolean isSuccess = (boolean) result[0];

            if(isSuccess){
                EntityList<Template> out = new EntityList<Template>();
                
                Document document = createDocument((String) result[1]);
                XPath xPath =  XPathFactory.newInstance().newXPath();
                NodeList templates = (NodeList) xPath.compile("VMTEMPLATE_POOL/VMTEMPLATE[GNAME='CCP4Users']").evaluate(document, XPathConstants.NODESET);
                
                for(int i = 0; i < templates.getLength(); i++){
                    Template template = new Template(this);
                    Node templateNode = templates.item(i);
                    template.setId(Integer.parseInt(xPath.compile("ID").evaluate(templateNode)));
                    template.setName(xPath.compile("NAME").evaluate(templateNode));
                    template.setDescription(xPath.compile("TEMPLATE/DESCRIPTION").evaluate(templateNode));
                    template.setCpuCount(Integer.parseInt(xPath.compile("TEMPLATE/CPU").evaluate(templateNode)));
                    template.setMemoryAllocation(Integer.parseInt(xPath.compile("TEMPLATE/MEMORY").evaluate(templateNode)));
                    out.add(template);
                }

                return out;
            } else if((int) result[2] == 0x0100){
                throw new AuthenticationException((String) result[1]);
            } else {
                throw new BadRequestException((String) result[1]);
            }
        } catch(CloudClientException e){
            throw e;
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }
        
    }
    
    private Document createDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        return builder.parse(input);
    }
}
