package es.ugr.murat.agent;

import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.CrossroadConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.util.Logger;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.time.Duration;

/**
 * Clase representando al agente cruce (Crossroad).
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class Crossroad extends MURATBaseAgent {

    private Integer status;
    Duration minimumStateTime; // Mínima duración de tiempo que puede permanecer el cruce en el mismo estado
    Duration cycleTime; // Duración de tiempo que tarda el cruce en pasar por todos sus estados de forma completa

    @Override
    protected void setup() {
        super.setup();
        this.status = CrossroadConstant.STATUS;
        this.minimumStateTime = null;
        this.cycleTime = null;
        Logger.info(ActionConstant.LAUNCHED_AGENT, this.getClass().getSimpleName(), this.getLocalName());
    }

    @Override
    protected void execute() {
        switch (status) {
            case CrossroadConstant.LOAD_DATA -> loadData();
            case CrossroadConstant.CONTROL_TRAFFIC -> controlTraffic();
            case CrossroadConstant.EXIT -> exit();
        }
    }

    protected void loadData() {
        Logger.info(ActionConstant.LOADING_DATA, this.getClass().getSimpleName(), this.getLocalName());
        // TODO: Cargar datos del cruce
        Logger.info(ActionConstant.LOADED_DATA, this.getClass().getSimpleName(), this.getLocalName());
        status = CrossroadConstant.CONTROL_TRAFFIC;
    }


    protected void controlTraffic() {
        outgoingMessage = new ACLMessage(ACLMessage.REQUEST);
        outgoingMessage.setSender(this.getAID());
        outgoingMessage.addReceiver(new AID("HOLA", AID.ISLOCALNAME));
        outgoingMessage.setContent(MessageConstant.CHANGE_LIGHT);
        this.send(outgoingMessage);
        status = CrossroadConstant.EXIT;
    }

    protected void exit() {
        exit = true;
    }

}
