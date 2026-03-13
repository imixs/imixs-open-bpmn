/********************************************************************************
 * Copyright (c) 2022 Imixs Software Solutions GmbH and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
package org.imixs.openbpmn.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.server.features.validation.Marker;
import org.eclipse.glsp.server.features.validation.MarkersReason;
import org.imixs.openbpmn.extensions.ImixsExtensionUtil;
import org.imixs.openbpmn.util.BPMNLinkedFlowIterator;
import org.imixs.openbpmn.util.ImixsBPMNUtil;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.elements.Activity;
import org.openbpmn.bpmn.elements.BPMNProcess;
import org.openbpmn.bpmn.elements.Event;
import org.openbpmn.bpmn.elements.core.BPMNElementNode;
import org.openbpmn.bpmn.exceptions.BPMNModelException;
import org.openbpmn.bpmn.validation.BPMNValidationMarker;
import org.openbpmn.glsp.validators.BPMNGLSPValidator;

/**
 * The ImixsBPMNValidator extends the BPMNGLSPValidator to validate the
 * imixs workflow element ids of task and event elements.
 *
 * @See BPMNGLSPValidator
 * @see: https://www.eclipse.org/glsp/documentation/validation/
 * @author rsoika
 *
 */
public class ImixsBPMNValidator extends BPMNGLSPValidator {
    private static Logger logger = Logger.getLogger(ImixsBPMNValidator.class.getName());

    /**
     * The method validates a model in BATCH mode to find duplicate event or task
     * ids
     */
    @Override
    public List<Marker> validate(final List<GModelElement> elements, final String reason) {
        long l = System.currentTimeMillis();
        List<BPMNValidationMarker> result = new ArrayList<>();
        List<Marker> markers = super.validate(elements, reason);

        if (MarkersReason.BATCH.equals(reason)) {
            logger.fine("├── Imixs Validator doBatch ....");
            try {
                BPMNModel model = modelState.getBpmnModel();
                logger.fine("│   ├── validate " + model.getBpmnProcessList().size() + " processes...");

                List<BPMNProcess> processes = model.getBpmnProcessList();
                for (BPMNProcess _process : processes) {
                    result.addAll(validateProcess(_process, true));
                }
            } catch (BPMNModelException e) {
                logger.warning("Failed to validate : " + e.getMessage());
            }
        }

        markers.addAll(this.convertBPMNValidationMarkers(result));

        logger.info("├── Finished Imixs batch validation in " + (System.currentTimeMillis() - l) + "ms...");
        return markers;
    }

    /**
     * This method validates the elements of a single BPMNProcess and returns a list
     * ob BPMNValidationMarkers.
     * 
     *
     * 
     * @return
     * @throws BPMNModelException
     */
    public List<BPMNValidationMarker> validateProcess(BPMNProcess process, boolean forceValidation)
            throws BPMNModelException {
        List<BPMNValidationMarker> result = new ArrayList<>();
        // make sure that the process is initialized
        process.init();

        // first we collect all Tasks
        Set<Activity> tasks = process.getActivities();
        List<String> uniqueTaskIds = new ArrayList<String>();
        for (Activity task : tasks) {

            task.resetValidation();
            if (!forceValidation && task.isValidated()) {
                // skip
                continue;
            }

            String taskID = task.getExtensionAttribute(ImixsExtensionUtil.getNamespace(), "processid");
            if (uniqueTaskIds.contains(taskID)) {
                // create Marker!
                task.addValidationMarker(new BPMNValidationMarker("Task",
                        "The Task ID " + taskID + " is already used within this process.",
                        task.getId(),
                        BPMNValidationMarker.ErrorType.ERROR));
            }
            task.setValidated(true);
            result.addAll(task.getValidationMarkers());
            uniqueTaskIds.add(taskID);

            // No validate the events
            List<BPMNValidationMarker> eventMarkers = validateEventsByTask(task);
            result.addAll(eventMarkers);

        }

        // finally return the list of all Validation marker of this process
        return result;
    }

    /**
     * Validates if all events for a task have a unique event id
     *
     * @param task
     */
    public List<BPMNValidationMarker> validateEventsByTask(Activity task) {
        List<BPMNValidationMarker> result = new ArrayList<>();
        List<String> uniqueEventIds = new ArrayList<>();
        String taskID = task.getExtensionAttribute(ImixsExtensionUtil.getNamespace(), "processid");

        // Collect all events: directly associated events + incoming init events
        List<Event> allEvents = new ArrayList<>();
        // find associated events
        BPMNLinkedFlowIterator<Event> eventNavigator = new BPMNLinkedFlowIterator<>(task,
                node -> ImixsBPMNUtil.isImixsEventElement(node));
        while (eventNavigator.hasNext()) {
            allEvents.add((Event) eventNavigator.next());
        }
        // find init events
        for (BPMNElementNode initEvent : ImixsBPMNUtil.findInitEventNodes(task)) {
            allEvents.add((Event) initEvent);
        }

        // Validate unique event IDs across all collected events
        for (Event event : allEvents) {
            event.resetValidation();
            String eventID = event.getExtensionAttribute(ImixsExtensionUtil.getNamespace(), "activityid");
            if (uniqueEventIds.contains(eventID)) {
                event.addValidationMarker(new BPMNValidationMarker("Task",
                        "The Event ID " + eventID + " is already associated with the Task " + taskID + ".",
                        event.getId(),
                        BPMNValidationMarker.ErrorType.ERROR));
            }
            event.setValidated(true);
            result.addAll(event.getValidationMarkers());
            uniqueEventIds.add(eventID);
        }

        return result;
    }
}