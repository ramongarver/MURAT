package es.ugr.murat.agent;

import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.util.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 * Basado en LARVABaseAgent.java por Anatoli-Grishenko.
 * https://github.com/Anatoli-Grishenko/es.ugr.larva.core/blob/master/src/agents/LARVABaseAgent.java
 */
public class MURATBaseAgent extends Agent {

    protected Boolean exit;
    protected Behaviour defaultBehaviour;
    protected ACLMessage incomingMessage;
    protected ACLMessage outgoingMessage;

    @Override
    protected void setup() {
        super.setup();
        exit = false;
        this.behaviourDefaultSetup();
        incomingMessage = null;
        outgoingMessage = null;
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
        Logger.info(ActionConstant.REMOVING_AGENT, this.getClass().getSimpleName(), this.getLocalName());
    }

    protected ACLMessage buildACLMessage(int performative, AID sender, AID receiver, String content) {
        ACLMessage message = new ACLMessage(performative);
        message.setSender(sender);
        message.addReceiver(receiver);
        message.setContent(content);
        return message;
    }

    protected final void sendACLMessage(ACLMessage message) {
        this.send(message);
        outgoingMessage = message;
        Logger.info(ActionConstant.MESSAGE_SENT, this.getClass().getSimpleName(), this.getLocalName(), outgoingMessage.toString());
    }

    protected final void sendACLMessage(int performative, AID sender, AID receiver, String content) {
        ACLMessage message = this.buildACLMessage(performative, sender, receiver, content);
        this.sendACLMessage(message);
    }

    protected final void receiveACLMessage() {
        incomingMessage = this.blockingReceive();
        Logger.info(ActionConstant.MESSAGE_RECEIVED, this.getClass().getSimpleName(), this.getLocalName(), incomingMessage.toString());
    }

}
