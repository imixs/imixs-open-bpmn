package org.imixs.openbpmn.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.imixs.openbpmn.extensions.ImixsExtensionUtil;
import org.openbpmn.bpmn.BPMNTypes;
import org.openbpmn.bpmn.elements.Activity;
import org.openbpmn.bpmn.elements.Event;
import org.openbpmn.bpmn.elements.SequenceFlow;
import org.openbpmn.bpmn.elements.core.BPMNElementNode;
import org.w3c.dom.Element;

public class ImixsBPMNUtil {

    /**
     * Returns true if the given BPMNElement is a BPMN TROW_EVENT with a Link
     * definition
     * 
     * <pre>{@code<bpmn2:intermediateThrowEvent id="event_ounTaA" name="HOLD">
     *   <bpmn2:linkEventDefinition id="linkEventDefinition_343OGA"/>
     *   ....
     * </bpmn2:intermediateCatchEvent>}</pre>
     * 
     * @return
     */
    public static boolean isLinkCatchEventElement(BPMNElementNode element) {
        if (element instanceof Event && BPMNTypes.THROW_EVENT.equals(element.getType())) {
            // test if we find a Link definition
            Set<Element> linkDefinitions = ((Event) element).getEventDefinitionsByType(BPMNTypes.EVENT_DEFINITION_LINK);
            if (linkDefinitions != null && linkDefinitions.size() > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the given BPMNElement is a Imixs Task element
     * 
     * <bpmn2:task id="Task_2" imixs:processid="1000" name="Task 1">
     * 
     * @return
     */
    public static boolean isImixsTaskElement(BPMNElementNode element) {
        return (element instanceof Activity && element.hasAttribute(ImixsExtensionUtil.getNamespace() + ":processid"));

    }

    /**
     * Returns true if the given BPMNElement is a Imixs Event element
     * 
     * <bpmn2:intermediateCatchEvent id="CatchEvent_2" imixs:activityid="20" >
     * 
     * @return
     */
    public static boolean isImixsEventElement(BPMNElementNode element) {
        return (element instanceof Event && element.hasAttribute(ImixsExtensionUtil.getNamespace() + ":activityid"));
    }

    /**
     * Returns true if the given node is a an ImixsEvent node with no incoming
     * nodes or with one incoming node that comes from a Start event.
     * 
     */
    public static boolean isInitEventNode(BPMNElementNode eventNode) {
        if (isImixsEventElement(eventNode)) {
            Set<SequenceFlow> flowSet = eventNode.getIngoingSequenceFlows();
            if (flowSet.isEmpty()) {
                // no incoming flows - match!
                return true;
            } else if (flowSet.size() == 1) {
                // is the incoming flow coming from a bpmn2:startEvent?
                BPMNElementNode sourceElement = flowSet.iterator().next().getSourceElement();
                return BPMNTypes.START_EVENT.equals(sourceElement.getType());
            } else {
                // undefined - more than one incoming flow
            }
        }
        return false;
    }

    /**
     * Iterates tough all ingoing sequence flows and tests if the source element is
     * a so called Init-Event. An Init-Event is an Imixs Event with no incoming
     * nodes or with one incoming node that comes direct from a Start event.
     * <p>
     * If a source element is an Event and has a predecessor event the method calls
     * itself recursive.
     * 
     * @param currentNode
     */
    public static List<BPMNElementNode> findInitEventNodes(BPMNElementNode currentNode) {
        List<BPMNElementNode> collector = new ArrayList<>();
        // logger.info("findInitEventNodes for element " + currentNode.getId() + "
        // type=" + currentNode.getType());
        Set<SequenceFlow> flowSet = currentNode.getIngoingSequenceFlows();
        for (SequenceFlow flow : flowSet) {
            BPMNElementNode element = flow.getSourceElement();
            // logger.info("verify element " + element.getId() + " type=" +
            // element.getType());
            if (isInitEventNode(element)) {
                collector.add(element);
            } else if (element != null && isImixsEventElement(element)) {
                // is the source an Imixs event node?
                // recursive call....
                List<BPMNElementNode> subResult = findInitEventNodes(element);
                collector.addAll(subResult);
            }
        }
        return collector;
    }
}
