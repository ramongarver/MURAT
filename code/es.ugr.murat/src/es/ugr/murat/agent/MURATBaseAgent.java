package es.ugr.murat.agent;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

/**
 * Based on LARVABaseAgent.java by Anatoli-Grishenko
 * https://github.com/Anatoli-Grishenko/es.ugr.larva.core/blob/master/src/agents/LARVABaseAgent.java
 */
public class MURATBaseAgent extends Agent {

    public Behaviour defaultBehaviour;
    protected Boolean exit;

    @Override
    protected void setup() {
        super.setup();
        exit = true;
        this.behaviourDefaultSetup();
    }

    protected void behaviourDefaultSetup() {
        defaultBehaviour = new Behaviour() {
            @Override
            public void action() {
                execute();
                if (exit) {
                    doDelete();
                }
            }

            @Override
            public boolean done() {
                return exit;
            }

        };
        this.addBehaviour(defaultBehaviour);
    }

    protected void execute() {

    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }

}
