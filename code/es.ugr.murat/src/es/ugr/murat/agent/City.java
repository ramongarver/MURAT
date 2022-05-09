package es.ugr.murat.agent;

import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.util.Logger;
import jade.core.Agent;

/**
 * Clase representando al agente ciudad (City).
 *
 * @author Ramón García Verjaga
 */
public class City extends Agent {

    @Override
    protected void setup() {
        super.setup();
        Logger.info(ActionConstant.LAUNCHED_AGENT, this.getClass().getSimpleName(), this.getLocalName());
    }

}
