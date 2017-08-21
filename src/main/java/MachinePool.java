package org.icatproject.topcatdaaasplugin;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Base64;
import java.util.Date;

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
    private AtomicBoolean managePoolBusy = new AtomicBoolean(false);
    private AtomicBoolean checkToSeeIfMachinesHaveFinishedPreparingBusy = new AtomicBoolean(false);
    private AtomicBoolean getScreenShotsBusy = new AtomicBoolean(false);
    private AtomicBoolean cleanUpFailedMachinesBusy = new AtomicBoolean(false);

	@EJB
    CloudClient cloudClient;

    @EJB
    Database database;

	@PostConstruct
    public void init() {
        
    }

    @Schedule(hour="*", minute="*", second="*")
    public void managePool(){
        if(!managePoolBusy.compareAndSet(false, true)){
            return;
        }

        try {
            EntityList<Entity> machineTypes = database.query("select machineType from MachineType machineType");

            for(Entity machineTypeEntity : machineTypes){
                MachineType machineType = (MachineType) machineTypeEntity;

                Map<String, String> params = new HashMap<String, String>();

                EntityList<Entity> nonAquiredMachines = database.query("select machine from Machine machine, machine.machineType as machineType where machine.state != 'aquired' and machine.state not like 'failed%' and machineType.id = " + machineType.getId());
                int diff = machineType.getPoolSize() - nonAquiredMachines.size();
                if(diff > 0){
                    for(int i = 0; i < diff; i++){
                        createMachine(machineType);
                    }
                } else {
                    for(int i = 0; i < (diff * -1); i++){
                        Machine machine = aquireMachine(machineType.getId());
                        if(machine != null){
                            cloudClient.deleteServer(machine.getId());
                            database.remove(machine);
                            logger.info("managePool: pruned machine with id " + machine.getId());
                        }
                    }
                }
            }
        } catch(Exception e){
            logger.error("managePool: " + e.getMessage());
        }

        managePoolBusy.set(false);
    }

    @Schedule(hour="*", minute="*", second="*")
    public void checkToSeeIfMachinesHaveFinishedPreparing(){
        if(!checkToSeeIfMachinesHaveFinishedPreparingBusy.compareAndSet(false, true)){
            return;
        }

        try {
            EntityList<Entity> machineTypes = database.query("select machineType from MachineType machineType");

            for(Entity machineTypeEntity : machineTypes){
                MachineType machineType = (MachineType) machineTypeEntity;

                EntityList<Entity> preparingMachines = database.query("select machine from Machine machine, machine.machineType as machineType where machine.state = 'preparing' and machineType.id = " + machineType.getId());
                for(Entity machineEntity : preparingMachines){
                    Machine machine = (Machine) machineEntity;

                    Server server = cloudClient.getServer(machine.getId());

                    if(server.getHost() != null){
                        machine.setHost(server.getHost());
                    }

                    try { 
                        if(server.getStatus().equals("SUCCESS")){
                            SshClient sshClient = new SshClient(machine.getHost());
                            if(sshClient.exec("is_ready").equals("1\n")){
                                machine.setState("vacant");
                            }
                        } else if(server.getStatus().equals("FAILED")){
                            machine.setState("failed");
                        }
                    } catch(NullPointerException e){}


                    Properties properties = new Properties();
                    int maxPrepareSeconds = Integer.valueOf(properties.getProperty("maxPrepareSeconds", "600"));
                    Date now = new Date();
                    long createdSecondsAgo = (now.getTime() - machine.getCreatedAt().getTime()) / 1000;
                    if(createdSecondsAgo > maxPrepareSeconds){
                        machine.setState("failed");
                        logger.info("checkToSeeIfMachinesHaveFinishedPreparing: machine with id " + machine.getId() + " has taken too long to prepare i.e. > " + maxPrepareSeconds + " seconds");
                    }

                    database.persist(machine);
                }
            }
        } catch(Exception e){
            logger.error("checkToSeeIfMachinesHaveFinishedPreparing: " + e.getMessage());
        }

        checkToSeeIfMachinesHaveFinishedPreparingBusy.set(false);
    }

    @Schedule(hour="*", minute="*", second="0")
    public void getScreenShots(){
        if(!getScreenShotsBusy.compareAndSet(false, true)){
            return;
        }
        
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

        getScreenShotsBusy.set(false);
    }

    @Schedule(hour="*", minute="*", second="*")
    public void cleanUpFailedMachines(){
        if(!cleanUpFailedMachinesBusy.compareAndSet(false, true)){
            return;
        }

        try {
            EntityList<Entity> failedMachines =  database.query("select machine from Machine machine where machine.state = 'failed'");
            for(Entity machineEntity : failedMachines){
                Machine machine = (Machine) machineEntity;
                cloudClient.deleteServer(machine.getId());
                machine.setState("failed:cleaned_up");
                database.persist(machine);
                logger.info("cleanUpFailedMachines: cleaned up failed machine with id " + machine.getId());
            }
        } catch(Exception e){
            logger.error("cleanUpFailedMachines: " + e.getMessage());
        }

        cleanUpFailedMachinesBusy.set(false);
    }

    public synchronized Machine aquireMachine(Long machineTypeId) throws DaaasException {
        try {
            String query = "select machine from Machine machine, machine.machineType as machineType where machine.state = 'vacant' and machineType.id = " + machineTypeId;
            EntityList<Entity> vacantMachines = database.query(query);
            if(vacantMachines.size() < 1){
                return null;
            }
            Machine out = (Machine) vacantMachines.get(0);
            out.setState("aquired");
            database.persist(out);
            logger.info("aquireMachine: aquired machine with id " + out.getId());
            return out;
        } catch(Exception e){
            throw new UnexpectedException(e.getMessage());
        }
    }

    private void createMachine(MachineType machineType){
        try {
            logger.info("createMachine: creating machine with machineType: " + machineType.toJsonObjectBuilder().build().toString());
            Machine machine = new Machine();
            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("AQ_ARCHETYPE", machineType.getAquilonArchetype());
            metadata.put("AQ_DOMAIN", machineType.getAquilonDomain());
            metadata.put("AQ_PERSONALITY", machineType.getAquilonPersonality());
            metadata.put("AQ_SANDBOX", machineType.getAquilonSandbox());
            metadata.put("AQ_OSVERSION", machineType.getAquilonOSVersion());

            Server server = cloudClient.createServer(machineType.getName(), machineType.getImageId(), machineType.getFlavorId(), machineType.getAvailabilityZone(), metadata);
            
            machine.setId(server.getId());
            machine.setName(machineType.getName());
            machine.setState("preparing");
            machine.setMachineType(machineType);

            database.persist(machine);
            logger.info("createMachine: created machine with id " + machine.getId());
        } catch(Exception e) {
            logger.error("createMachine: " + e.getMessage());
        }
    }

}