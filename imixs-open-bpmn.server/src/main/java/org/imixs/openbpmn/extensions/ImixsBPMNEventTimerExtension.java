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
package org.imixs.openbpmn.extensions;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.JsonObject;

import org.eclipse.glsp.graph.GModelElement;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNTypes;
import org.openbpmn.bpmn.elements.Event;
import org.openbpmn.bpmn.elements.core.BPMNElement;
import org.openbpmn.glsp.jsonforms.DataBuilder;
import org.openbpmn.glsp.jsonforms.SchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder.Layout;
import org.w3c.dom.Element;

/**
 * This is the Default BPMNEvent extension providing the JSONForms shemata.
 *
 * @author rsoika
 *
 */
public class ImixsBPMNEventTimerExtension extends ImixsBPMNExtension {

    private static Logger logger = Logger.getLogger(ImixsBPMNTaskExtension.class.getName());

    public ImixsBPMNEventTimerExtension() {
        super();
    }

    @Override
    public int getPriority() {
        return 103;
    }

    /**
     * The ImixsBPMNTaskExtension can only be applied to a BPMN Task element
     */
    @Override
    public boolean handlesElementTypeId(final String elementTypeId) {
        return BPMNTypes.CATCH_EVENT.equals(elementTypeId);
    }

    /**
     * This Extension is for BPMN Task Elements only
     * <p>
     * The method also verifies if the element has a imixs:processid attribute. This
     * attribute is added in the 'addExtesnion' method call
     */
    @Override
    public boolean handlesBPMNElement(final BPMNElement bpmnElement) {

        if (bpmnElement instanceof Event) {
            Event event = (Event) bpmnElement;
            if (event.getType().equals(BPMNTypes.CATCH_EVENT)) {
                if (event.hasAttribute(getNamespace() + ":activityid")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This Helper Method generates a JSON Object with the BPMNElement properties.
     * <p>
     * This json object is used on the GLSP Client to generate the EMF JsonForms
     * <p>
     * keyscheduledactivity, txtscheduledview, numactivitydelay
     * ,"keyscheduledbaseobject", keytimecomparefield, keyactivitydelayunit
     */
    @Override
    public void buildPropertiesForm(final BPMNElement bpmnElement, final DataBuilder dataBuilder,
            final SchemaBuilder schemaBuilder, final UISchemaBuilder uiSchemaBuilder) {

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();

        /***********
         * Data
         */
        dataBuilder //
                .addData("txtscheduledview",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "txtscheduledview")) //
                .addData("numactivitydelay",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "numactivitydelay"));

        // set enabled yes|no
        String keyEnabled = ImixsExtensionUtil.getItemValueString(model, elementNode, "keyscheduledactivity");
        if ("1".equals(keyEnabled)) {
            keyEnabled = "Yes";
        } else {
            keyEnabled = "No";
        }
        dataBuilder.addData("keyscheduledactivity", keyEnabled); //

        // set keyscheduledbaseobject
        String keyBaseObject = ImixsExtensionUtil.getItemValueString(model, elementNode, "keyscheduledbaseobject");
        switch (keyBaseObject) {
            case "4":
                keyBaseObject = "Reference";
                break;
            case "3":
                keyBaseObject = "Creation Date";
                break;
            case "2":
                keyBaseObject = "Last Modified";
                break;
            default:
                keyBaseObject = "Last Event";
                break;
        }
        dataBuilder.addData("keyscheduledbaseobject", keyBaseObject); //

        // set keyactivitydelayunit
        String keyUnit = ImixsExtensionUtil.getItemValueString(model, elementNode, "keyactivitydelayunit");
        switch (keyUnit) {
            case "4":
                keyUnit = "Workdays";
                break;
            case "3":
                keyUnit = "Days";
                break;
            case "2":
                keyUnit = "Hours";
                break;
            default:
                keyUnit = "Minutes";
                break;
        }
        dataBuilder.addData("keyactivitydelayunit", keyUnit); //

        // resolve Item References and set the label as the value...
        ImixsItemNameMapper itemTimeMapping = new ImixsItemNameMapper(model, "txttimefieldmapping");
        String refTimeItem = ImixsExtensionUtil.getItemValueString(model, elementNode, "keytimecomparefield");
        dataBuilder.addData("keytimecomparefield", itemTimeMapping.resolveLabel(refTimeItem)); //

        /***********
         * Schema
         */
        String[] enabledOption = { "Yes", "No" };
        String[] refOption = { "Last Event", "Last Modified", "Creation Date", "Reference" };
        String[] keyUnits = { "Minutes", "Hours", "Days", "Workdays" };
        String[] timeFields = itemTimeMapping.getLabelsArray();
        schemaBuilder //
                .addProperty("txtscheduledview", "string", "") //
                .addProperty("keyscheduledbaseobject", "string", "", refOption) //
                .addProperty("keytimecomparefield", "string", "", timeFields) //
                .addProperty("numactivitydelay", "string", "") //
                .addProperty("keyactivitydelayunit", "string", "", keyUnits) //
                .addProperty("keyscheduledactivity", "string", "", enabledOption);

        /***********
         * UISchema
         */
        Map<String, String> radioOption = new HashMap<>();
        radioOption.put("format", "radio");
        Map<String, String> comboOption = new HashMap<>();
        comboOption.put("format", "combo");
        uiSchemaBuilder //
                .addCategory("Scheduler") //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("keyscheduledactivity", "Enabled", radioOption)
                .addElement("numactivitydelay", "Delay", null)
                .addElement("keyactivitydelayunit", "Unit", comboOption)
                .addLayout(Layout.HORIZONTAL) //
                .addElement("keyscheduledbaseobject", "Time Base Object", radioOption) //
                .addElement("keytimecomparefield", "Item Reference", comboOption) //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("txtscheduledview", "Selection", null);
    }

    /**
     * This method updates the BPMN properties
     */
    @Override
    public void updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        // we are only interested in category Workflow and History
        if (!"Scheduler".equals(category)) {
            return;
        }

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();

        // base settings

        ImixsExtensionUtil.setItemValue(model, elementNode, "txtscheduledview", "xs:string",
                json.getString("txtscheduledview", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "numactivitydelay", "xs:string",
                json.getString("numactivitydelay", ""));

        // set enabled yes|no
        String keyEnabled = json.getString("keyscheduledactivity", "Yes");
        if ("Yes".equals(keyEnabled)) {
            keyEnabled = "1";
        } else {
            keyEnabled = "0";
        }
        ImixsExtensionUtil.setItemValue(model, elementNode, "keyscheduledactivity", "xs:string", keyEnabled);

        // set keyscheduledbaseobject
        String keyBaseObject = json.getString("keyscheduledbaseobject", "");
        switch (keyBaseObject) {
            case "Reference":
                keyBaseObject = "4";
                break;
            case "Creation Date":
                keyBaseObject = "3";
                break;
            case "Last Modified":
                keyBaseObject = "2";
                break;
            default:
                keyBaseObject = "1";
                break;
        }
        ImixsExtensionUtil.setItemValue(model, elementNode, "keyscheduledbaseobject", "xs:string", keyBaseObject);

        // set keyactivitydelayunit
        String keyDelayUnit = json.getString("keyactivitydelayunit", "");
        switch (keyDelayUnit) {
            case "Workdays":
                keyDelayUnit = "4";
                break;
            case "Days":
                keyDelayUnit = "3";
                break;
            case "Hours":
                keyDelayUnit = "2";
                break;
            default:
                keyDelayUnit = "1";
                break;
        }
        ImixsExtensionUtil.setItemValue(model, elementNode, "keyactivitydelayunit", "xs:string", keyDelayUnit);

        // set timeCompare field
        ImixsItemNameMapper timeFieldMapper = new ImixsItemNameMapper(model, "txttimefieldmapping");
        ImixsExtensionUtil.setItemValue(model, elementNode, "keytimecomparefield", "xs:string",
                timeFieldMapper.resolveValue(json.getString("keytimecomparefield", "")));

    }

}