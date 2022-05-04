package es.ugr.murat.agent;

import jade.core.Agent;

/**
 * Clase representando al agente ciudad (City).
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class City extends Agent {

    @Override
    protected void setup() {
        super.setup();
        System.out.println("||Launched||" + this.getClass().getSimpleName() + "::" + this.getAID().getLocalName());
    }

}
