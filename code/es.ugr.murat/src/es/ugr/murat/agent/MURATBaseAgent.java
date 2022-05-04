package es.ugr.murat.agent;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * Basado en LARVABaseAgent.java por Anatoli-Grishenko.
 * https://github.com/Anatoli-Grishenko/es.ugr.larva.core/blob/master/src/agents/LARVABaseAgent.java
 */
public class MURATBaseAgent extends Agent {

    protected Behaviour defaultBehaviour;
    protected Boolean exit;
    protected ACLMessage message;

    @Override
    protected void setup() {
        super.setup();
        exit = false;
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
        System.out.println("Eliminando agente " + this.getName());
    }

}
