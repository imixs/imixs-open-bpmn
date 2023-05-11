package org.imixs.workflow.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNTypes;
import org.openbpmn.bpmn.elements.BPMNProcess;
import org.openbpmn.bpmn.elements.Event;
import org.openbpmn.bpmn.elements.SequenceFlow;
import org.openbpmn.bpmn.elements.core.BPMNElementNode;
import org.openbpmn.bpmn.exceptions.BPMNModelException;
import org.openbpmn.bpmn.exceptions.BPMNValidationException;
import org.openbpmn.bpmn.util.BPMNModelFactory;

/**
 * This is a preview for the new Imixs-Workflow Kernel implementation based on
 * Open-BPMN
 * 
 * The Test simulates some scenarios from the Imixs-Workflow engine.
 * 
 */
public class TestImixsKernelSimulator {
    private static Logger logger = Logger.getLogger(TestImixsKernelSimulator.class.getName());

    /**
     * Simulates refmodel-1
     * 
     * Simulate processing event case3 with a follow-up event.
     * 
     * @throws BPMNModelException
     * 
     */
    @Test
    public void testProcessEventCase3() throws BPMNModelException {

        logger.info("...read model");
        BPMNModel model = BPMNModelFactory.read("/imixs-refmodel-1.bpmn");

        assertEquals(1, model.getProcesses().size());
        BPMNProcess process = model.openDefaultProces();

        // fetch event case3
        Event event = (Event) process.findElementById("event_zLUTkA");
        assertNotNull(event);

        // processing simulation
        ImixsBPMNEventIterator eventIterator = new ImixsBPMNEventIterator(event);
        // we expect two process iterations
        // case3 and followup
        while (eventIterator.hasNext()) {
            Event nextEvent = eventIterator.next();
            logger.info("...processing event " + nextEvent.getId());

        }

        logger.info("...processing completed ");

    }

    /**
     * A helper navigator to find the target task
     * 
     */
    class ImixsBPMNEventIterator implements Iterator<Event> {

        private int index;
        Event lastEvent = null;
        List<SequenceFlow> outgoingFlows;

        public ImixsBPMNEventIterator(Event event) throws BPMNValidationException {
            this.lastEvent = event;
        }

        /**
         * Returns {@code true} if the iteration has still an event to be
         * processed.
         * (In other words, returns {@code true} if {@link #next} would
         * return a valid event rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more events
         */
        @Override
        public boolean hasNext() {

            // do we need to find the next event?
            if (lastEvent == null) {
                // fetch next element from the outgoing flow
                // we are sure that we have exactly one outgoing flow (Rule for Imixs-Workflow)
                SequenceFlow flow = outgoingFlows.get(0);
                BPMNElementNode targetNode = flow.getTargetElement();
                String targetType = targetNode.getType();

                // Event?
                if (BPMNTypes.CATCH_EVENT.equals(targetType)) {
                    // no more events!
                    lastEvent = (Event) targetNode;
                }

                // Task?
                if (BPMNTypes.BPMN_TASKS.contains(targetType)) {
                    // no more events!
                    lastEvent = null;
                }

                if (BPMNTypes.EXCLUSIVE_GATEWAY.equals(targetType) //
                        || BPMNTypes.EVENTBASED_GATEWAY.equals(targetType) //
                        || BPMNTypes.COMPLEX_GATEWAY.equals(targetType)) {
                    // handle gateway case...
                    throw new IllegalArgumentException("Gateways not yet supported by ImixsBPMNEventIterator!");
                }

            }

            // do we have a new lastEvent?
            return (lastEvent != null);
        }

        /**
         * Returns the next event in the iteration.
         */
        @Override
        public Event next() {
            if (!hasNext()) {
                throw new IllegalStateException("No more Events defined!");
            }

            // find outgoing flows
            outgoingFlows = new ArrayList<>(lastEvent.getOutgoingSequenceFlows());

            // An imixs event must not have more than one outgoing flows
            if (outgoingFlows.size() == 0) {
                throw new IllegalStateException(
                        "An Imixs Event '" + lastEvent.getId()
                                + "' must not have one outgoing SequenceFlow!");
            }
            if (outgoingFlows.size() > 1) {
                throw new IllegalStateException(
                        "An Imixs Event '" + lastEvent.getId()
                                + "' must not have more than one outgoing SequenceFlows!");
            }

            // set last event null!
            Event nextEvent = lastEvent;
            lastEvent = null;
            return nextEvent;
        }
    }

}
