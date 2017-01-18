package org.icatproject.topcatdaaasplugin;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;


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

                EntityList<Entity> vacantMachines = database.query("select machine from Machine machine, machine.machineType as machineType where machine.state = 'vacant' and machineType.id = " + machineType.getId());
                int diff = machineType.getPoolSize() - vacantMachines.size();
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

            }
        } catch(Exception e){
            logger.error("managePool: " + e.getMessage());
        }

        busy.set(false);
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
            Server server = cloudClient.createServer(machineType.getName(), machineType.getImageId(), machineType.getFlavorId(), machineType.getAvailabilityZone());
            while(server.getHost() == null){
                Thread.sleep(1000);
                server = cloudClient.getServer(server.getId());
            }

            machine.setId(server.getId());
            machine.setName(machineType.getName());
            machine.setState("vacant");
            machine.setHost(server.getHost());
            machine.setWebsockifyToken("token");
            machine.setMachineType(machineType);
            database.persist(machine);
            logger.info("created machine: " + machine.getId());
        } catch(Exception e) {
            logger.error("createMachine: " + e.getMessage());
        }
    }

}