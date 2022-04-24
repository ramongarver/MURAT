package es.ugr.murat.agent;

import jade.core.Agent;

public class City extends Agent {

    @Override
    protected void setup() {
        super.setup();
        System.out.println("Created||" + this.getClass().getSimpleName() + "::" + this.getAID().getName());
    }

}
