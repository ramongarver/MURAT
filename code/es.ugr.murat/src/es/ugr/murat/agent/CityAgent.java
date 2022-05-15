package es.ugr.murat.agent;

import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.CityConstant;
import es.ugr.murat.constant.CrossroadConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.model.CrossroadModel;
import es.ugr.murat.simulation.Simulation;
import es.ugr.murat.util.Logger;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Map;

/**
 * Clase representando al agente ciudad (CityAgent).
 *
 * @author Ramón García Verjaga
 */
public class CityAgent extends MURATBaseAgent {

    private Map<Integer, CrossroadModel> crossroads; // Cruces de la ciudad | (crossroadId -> crossroadModel)

    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        status = CityConstant.LOAD_DATA;
        crossroads = null;
        Logger.info(ActionConstant.LAUNCHED_AGENT, this.getClass().getSimpleName(), this.getLocalName());
    }

    @Override
    protected void execute() {
        switch (status) {
            case CityConstant.LOAD_DATA -> loadData();
            case CityConstant.FINALIZE_CROSSROADS -> finalizeCrossroads();
            case CityConstant.EXIT -> exit();
        }
    }

    protected void loadData() {
        Logger.info(ActionConstant.LOADING_DATA, this.getClass().getSimpleName(), this.getLocalName());
        // Cargamos datos la ciudad
            // Obtenemos datos de los cruces de la ciudad
        crossroads = Simulation.simulation.getCityCrossroads();
        Logger.info(ActionConstant.LOADED_DATA, this.getClass().getSimpleName(), this.getLocalName());
        status = CityConstant.FINALIZE_CROSSROADS;
    }

    protected void finalizeCrossroads() {
        try {
            //TODO: Ponemos a "Dormir" el programa durante los ms que queremos
            Thread.sleep(6*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Enviamos un mensaje a cada semáforo para pedirle que finalice
//        crossroads.forEach((crossroadId, crossroadModel) ->
//                this.sendACLMessage(ACLMessage.REQUEST, this.getAID(),
//                        new AID(CrossroadConstant.AGENT_NAME + crossroadId, AID.ISLOCALNAME),
//                        MessageConstant.FINALIZE));
        status = CityConstant.EXIT;
    }

    protected void exit() {
        exit = true;
    }
    //**************************************************//

    //*************** Utilidades y otros ***************//

    //**************************************************//

}
