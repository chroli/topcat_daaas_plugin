package org.icatproject.topcatdaaasplugin;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Schedule;
import javax.ejb.EJB;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcatdaaasplugin.exceptions.*;
import org.icatproject.topcatdaaasplugin.cloudclient.CloudClient;
import org.icatproject.topcatdaaasplugin.cloudclient.entities.*;
import org.icatproject.topcatdaaasplugin.database.Database;
import org.icatproject.topcatdaaasplugin.database.entities.*;
import org.icatproject.topcatdaaasplugin.Entity;
import org.icatproject.topcatdaaasplugin.EntityList;
import org.icatproject.topcatdaaasplugin.Properties;

@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@Startup
public class MachinePool {

    private static final Logger logger = LoggerFactory.getLogger(MachinePool.class);
    private AtomicBoolean busy = new AtomicBoolean(false);

	@EJB
    CloudClient cloudClient;

    @EJB
    Database database;

	@PostConstruct
    public void init() {
        
    }

    @Schedule(hour="*", minute="*", second="*")
    public void managePool(){
        if(!busy.compareAndSet(false, true)){
            return;
        }

        try {
            EntityList<Entity> machineTypes = database.query("select machineType from MachineType machineType");

            for(Entity machineTypeEntity : machineTypes){
                MachineType machineType = (MachineType) machineTypeEntity;

                Map<String, String> params = new HashMap<String, String>();

                EntityList<Entity> nonAquiredMachines = database.query("select machine from Machine machine, machine.machineType as machineType where machine.state != 'aquired' and machineType.id = " + machineType.getId());
                int diff = machineType.getPoolSize() - nonAquiredMachines.size();
                if(diff > 0){
                    for(int i = 0; i < diff; i++){
                        createMachine(machineType);
                    }
                } else {
                    for(int i = 0; i < (diff * -1); i++){
                        Machine machine = aquireMachine(machineType.getId());
                        logger.info("pruned machine: " + machine.getId());
                        cloudClient.deleteMachine(machine.getId());
                        database.remove(machine);
                    }
                }

                EntityList<Entity> preparingMachines = database.query("select machine from Machine machine, machine.machineType as machineType where machine.state = 'preparing' and machineType.id = " + machineType.getId());
                for(Entity machineEntity : preparingMachines){
                    Machine machine = (Machine) machineEntity;
                    SshClient sshClient = new SshClient(machine.getHost());
                    if(sshClient.exec("is_ready").equals("1\n")){
                        machine.setState("vacant");
                        database.persist(machine);
                    }
                }
            }
        } catch(Exception e){
            logger.error("managePool: " + e.getMessage());
        }

        busy.set(false);
    }


    @Schedule(hour="*", minute="*", second="*")
    public void getScreenShots(){
        try {
            EntityList<Entity> aquiredMachines =  database.query("select machine from Machine machine where machine.state = 'aquired'");
            for(Entity machineEntity : aquiredMachines){
                Machine machine = (Machine) machineEntity;
                machine.setScreenshot(Base64.getMimeDecoder().decode(new SshClient(machine.getHost()).exec("get_screenshot")));
                database.persist(machine);
            }
        } catch(Exception e){
            logger.error("getScreenShots: " + e.getMessage());
        }
    }

    public synchronized Machine aquireMachine(Long machineTypeId) throws DaaasException {
        try {
            String query = "select machine from Machine machine, machine.machineType as machineType where machine.state = 'vacant' and machineType.id = " + machineTypeId;
            EntityList<Entity> vacantMachines = database.query(query);
            while(vacantMachines.size() < 1){
                vacantMachines = database.query(query);
                Thread.sleep(100);
            }
            Machine out = (Machine) vacantMachines.get(0);
            out.setState("aquired");
            database.persist(out);
            logger.info("aquired machine: " + out.getId());
            return out;
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }
    }

    private void createMachine(MachineType machineType){
        try {
            Machine machine = new Machine();
            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("AQ_ARCHETYPE", machineType.getAquilonArchetype());
            metadata.put("AQ_DOMAIN", machineType.getAquilonDomain());
            metadata.put("AQ_PERSONALITY", machineType.getAquilonPersonality());
            metadata.put("AQ_SANDBOX", machineType.getAquilonSandbox());
            metadata.put("AQ_OSVERSION", machineType.getAquilonOSVersion());

            Server server = cloudClient.createServer(machineType.getName(), machineType.getImageId(), machineType.getFlavorId(), machineType.getAvailabilityZone(), metadata);
            while(server.getHost() == null){
                Thread.sleep(1000);
                server = cloudClient.getServer(server.getId());
            }

            Process process = Runtime.getRuntime().exec(new String[] {
                "/usr/bin/nslookup", server.getHost()
            });

            process.waitFor();

            String host = IOUtils.toString(process.getInputStream(), StandardCharsets.US_ASCII);
            host = host.replaceAll("(?s).*name\\s+=\\s+", "").replaceAll("(?s)\\.\\s.*", "");

            machine.setId(server.getId());
            machine.setName(machineType.getName());
            machine.setState("preparing");
            machine.setHost(host);
            machine.setMachineType(machineType);
            

            //Properties properties = new Properties();
            //String command = properties.getProperty("ssh_init_command");
            //SshClient sshClient = new SshClient(machine.getHost());

            //sshClient.exec(command);
            database.persist(machine);
            logger.info("created machine: " + machine.getId());
        } catch(Exception e) {
            logger.error("createMachine: " + e.getMessage());
        }
    }

}