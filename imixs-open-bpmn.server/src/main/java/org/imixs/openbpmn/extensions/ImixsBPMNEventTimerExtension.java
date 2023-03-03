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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

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
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "numactivitydelay")) //
                .addData("keyscheduledactivity",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "keyscheduledactivity", "0")) //
                .addDataList("keyscheduledbaseobject",
                        ImixsExtensionUtil.getItemValueList(model, elementNode, "keyscheduledbaseobject")) //
                .addData("keyactivitydelayunit",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "keyactivitydelayunit")) //
                .addData("keytimecomparefield",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "keytimecomparefield")); //

        // fetch the timeItem definitions from the model definition
        Element definitionsElementNode = model.getDefinitions();
        List<String> timeItemDefs = ImixsExtensionUtil.getItemValueList(model, definitionsElementNode,
                "txttimefieldmapping");

        /***********
         * Schema
         */
        String[] enabledOption = { "Yes|1", "No|0" };
        String[] refOption = { "Last Event|1", "Last Modified|2", "Creation Date|3", "Reference|4" };
        String[] keyUnits = { "Minutes|1", "Hours|2", "Days|3", "Workdays|4" };
        // String[] timeFields = itemTimeMapping.getLabelsArray();

        String[] timeFields = timeItemDefs.toArray(String[]::new);
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
        Map<String, String> selectVertical = new HashMap<>();
        selectVertical.put("format", "selectitem");
        // selectVertical.put("orientation", "vertical");
        Map<String, String> selectHorizontal = new HashMap<>();
        selectHorizontal.put("format", "selectitem");

        Map<String, String> selectCombo = new HashMap<>();
        selectCombo.put("format", "selectitemcombo");

        // Map<String, String> comboOption = new HashMap<>();
        // comboOption.put("format", "combo");
        uiSchemaBuilder //
                .addCategory("Scheduler") //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("keyscheduledactivity", "Enabled", selectHorizontal)
                .addElement("numactivitydelay", "Delay", null)
                .addElement("keyactivitydelayunit", "Unit", selectCombo)
                .addLayout(Layout.HORIZONTAL) //
                .addElement("keyscheduledbaseobject", "A Time Base Object", selectVertical) //
                .addElement("keytimecomparefield", "Item Reference", selectCombo) //
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

        ImixsExtensionUtil.setItemValue(model, elementNode, "keyscheduledactivity", "xs:string",
                json.getString("keyscheduledactivity", "0"));

        // set keyscheduledbaseobject
        JsonArray valueArray = json.getJsonArray("keyscheduledbaseobject");
        Iterator<JsonValue> iter = valueArray.iterator();
        List<String> keyBaseObject = new ArrayList<String>();
        while (iter.hasNext()) {
            JsonValue nextValue = iter.next();
            String jsonStringValue = ((JsonString) nextValue).getString();
            keyBaseObject.add(jsonStringValue);
        }
        ImixsExtensionUtil.setItemValueList(model, elementNode, "keyscheduledbaseobject", "xs:string", keyBaseObject);

        ImixsExtensionUtil.setItemValue(model, elementNode, "keyactivitydelayunit", "xs:string",
                json.getString("keyactivitydelayunit", "1"));

        // set timeCompare field
        ImixsExtensionUtil.setItemValue(model, elementNode, "keytimecomparefield", "xs:string",
                json.getString("keytimecomparefield", ""));

    }

}
