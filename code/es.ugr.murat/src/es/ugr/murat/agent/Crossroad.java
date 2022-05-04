package es.ugr.murat.agent;

import es.ugr.murat.constant.CrossroadConstant;
import jade.core.Agent;

/**
 * Clase representando al agente cruce (Crossroad).
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class Crossroad extends Agent {

    private Integer status;
    private Integer period;


    @Override
    protected void setup() {
        super.setup();
        this.status = CrossroadConstant.STATUS;
        System.out.println("||Launched||Agent-" + this.getClass().getSimpleName() + "::" + this.getAID().getLocalName());
    }

}
