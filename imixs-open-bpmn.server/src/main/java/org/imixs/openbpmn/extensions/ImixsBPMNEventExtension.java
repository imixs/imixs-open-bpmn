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
import java.util.Arrays;
import java.util.HashMap;
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
public class ImixsBPMNEventExtension extends ImixsBPMNExtension {

    private static Logger logger = Logger.getLogger(ImixsBPMNEventExtension.class.getName());

    public ImixsBPMNEventExtension() {
        super();
    }

    @Override
    public int getPriority() {
        return 1110;
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
     * This method adds a unique identifier to the corresponding BPMNElement
     */
    @Override
    public void addExtension(final BPMNElement bpmnElement) {

        if (bpmnElement instanceof Event) {
            bpmnElement.setExtensionAttribute(getNamespace(), "activityid", "10");
        }
    }

    /**
     * This Helper Method generates a JSON Object with the BPMNElement properties.
     * <p>
     * This json object is used on the GLSP Client to generate the EMF JsonForms
     */
    @Override
    public void buildPropertiesForm(final BPMNElement bpmnElement, final DataBuilder dataBuilder,
            final SchemaBuilder schemaBuilder, final UISchemaBuilder uiSchemaBuilder) {

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();
        ImixsItemNameMapper actorFieldMapper = new ImixsItemNameMapper(model, "txtfieldmapping");

        /***********
         * Data
         */
        dataBuilder //
                .addData("activityid", bpmnElement.getExtensionAttribute(getNamespace(), "activityid")) //
                .addData("txtactivityresult",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "txtactivityresult")) //
                .addData("keypublicresult",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "keypublicresult", "1")) //
                .addDataList("keyrestrictedvisibility",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keyrestrictedvisibility",
                                actorFieldMapper.getValues())) //
                .addData("$readaccess", String.join(System.lineSeparator(),
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "$readaccess")));

        /***********
         * Schema
         */
        // fetch the actorItem definitions from the model definition
        Element definitionsElementNode = model.getDefinitions();
        List<String> actorItemDefs = ImixsExtensionUtil.getItemValueList(model, definitionsElementNode,
                "txtfieldmapping");
        String[] actorItemDefsArray = actorItemDefs.toArray(String[]::new);
        String[] publicEventOptions = { "Yes|1", "No|0" };
        schemaBuilder //
                .addProperty("activityid", "string", null) //
                .addProperty("txtactivityresult", "string",
                        "Optional Execution Result. Additional item values can be defined here. ") //
                .addProperty("keypublicresult", "string",
                        "Show Event as an Action in the Application UI",
                        publicEventOptions) //
                .addProperty("keyrestrictedvisibility", "string", "", actorItemDefsArray) //
                .addProperty("$readaccess", "string", "Add multiple entries in separate lines.");

        /***********
         * UISchema
         */
        Map<String, String> selectItemOption = new HashMap<>();
        selectItemOption.put("format", "selectitem");
        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");
        Map<String, String> selectVertical = new HashMap<>();
        selectVertical.put("format", "selectitem");
        selectVertical.put("orientation", "vertical");
        uiSchemaBuilder //
                .addCategory("Workflow") //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("activityid", "Event ID", null) //
                .addElement("keypublicresult", "Pubilc Event", selectItemOption) //
                .addLayout(Layout.VERTICAL) //
                .addElement("txtactivityresult", "Workflow Result", multilineOption);

        uiSchemaBuilder.addLayout(Layout.HORIZONTAL); //
        if (actorItemDefs != null && actorItemDefs.size() > 0) {
            uiSchemaBuilder.addElement("keyrestrictedvisibility", "Restrict Visibility to Actors",
                    selectVertical);
        }

        uiSchemaBuilder.addElement("$readaccess", "Restrict Read Access", multilineOption);

    }

    /**
     * This method updates the BPMN properties and also the imixs processid.
     * The processID is also updated for the frontend.
     */
    @Override
    public void updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        // we are only interested in category Workflow and History
        if (!"Workflow".equals(category)) {
            return;
        }

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();
        ImixsItemNameMapper actorFieldMapper = new ImixsItemNameMapper(model, "txtfieldmapping");

        bpmnElement.setExtensionAttribute(getNamespace(), "activityid",
                json.getString("activityid", "0"));
        ImixsExtensionUtil.setItemValue(model, elementNode, "txtactivityresult", "xs:string",
                json.getString("txtactivityresult", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "keypublicresult", "xs:string",
                json.getString("keypublicresult", "1"));

        JsonArray valueArray = json.getJsonArray("keyrestrictedvisibility");
        List<String> keyBaseObject = new ArrayList<>(valueArray.size());
        for (JsonValue value : valueArray) {
            String jsonStringValue = ((JsonString) value).getString();
            keyBaseObject.add(jsonStringValue);
        }
        ImixsExtensionUtil.setItemValueList(model, elementNode, "keyrestrictedvisibility", "xs:string",
                keyBaseObject,
                actorFieldMapper.getValues());

        // $readAccess
        String otherValue = json.getString("$readaccess", "");
        if (otherValue.trim().isEmpty()) {
            // remove $readaccess (see https://github.com/imixs/imixs-workflow/issues/832)
            ImixsExtensionUtil.removeItemValue(model, elementNode, "$readaccess");
        } else {
            String[] lines = otherValue.split(System.lineSeparator());
            ImixsExtensionUtil.setItemValueList(model, elementNode, "$readaccess", "xs:string",
                    Arrays.asList(lines), null);
        }

    }

}
