package es.ugr.murat.agent;

import es.ugr.murat.constant.CrossroadConstant;
import es.ugr.murat.constant.TrafficLightConstant;
import jade.core.Agent;

/**
 * Class representing a Crossroad agent.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class Crossroad extends Agent {

    /**
     * Status of the Crossroad agent.
     * The following values are possible:
     * TODO...
     */
    Integer status;

    @Override
    protected void setup() {
        super.setup();
        this.status = CrossroadConstant.STATUS;
        System.out.println("Created||" + this.getClass().getSimpleName() + "::" + this.getAID().getName());
    }

}
