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
 * This is the ACL extension providing the JSONForms schemata.
 *
 * @author rsoika
 *
 */
public class ImixsBPMNEventACLExtension extends ImixsBPMNExtension {

    private static Logger logger = Logger.getLogger(ImixsBPMNTaskExtension.class.getName());

    public ImixsBPMNEventACLExtension() {
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
     * This json object is used on the GLSP Client to generate the JsonForms
     * <p>
     * keyupdateacl, keyownershipfields, keyaddwritefields, keyaddreadfields
     * ,"namownershipnames", namaddreadaccess, namaddwriteaccess
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
                .addData("keyupdateacl",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "keyupdateacl", "false")) //
                .addDataList("keyownershipfields",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keyownershipfields")) //
                .addDataList("keyaddreadfields",
                        ImixsExtensionUtil.getItemValueList(model, elementNode, "keyaddreadfields")) //
                .addDataList("keyaddwritefields",
                        ImixsExtensionUtil.getItemValueList(model, elementNode, "keyaddwritefields")) //
                .addData("namownershipnames",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "namownershipnames")) //
                .addData("namaddreadaccess",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "namaddreadaccess")) //
                .addData("namaddwriteaccess",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "namaddwriteaccess")) //
        ; //

        // fetch the actorItem definitions from the model definition
        Element definitionsElementNode = model.getDefinitions();
        List<String> actorItemDefs = ImixsExtensionUtil.getItemValueList(model, definitionsElementNode,
                "txtfieldmapping");

        /***********
         * Schema
         */
        String[] enabledOption = { "Yes|true", "No|false" };

        String[] actorItemDefsArray = actorItemDefs.toArray(String[]::new);
        schemaBuilder //
                .addProperty("keyupdateacl", "string", "", enabledOption) //
                .addProperty("keyownershipfields", "string", "", actorItemDefsArray) //
                .addProperty("keyaddreadfields", "string", "", actorItemDefsArray) //
                .addProperty("keyaddwritefields", "string", "", actorItemDefsArray) //

                .addProperty("namownershipnames", "string", "", null) //
                .addProperty("namaddreadaccess", "string", "") //
                .addProperty("namaddwriteaccess", "string", "") //
        ;

        /***********
         * UISchema
         */
        Map<String, String> selectVertical = new HashMap<>();
        selectVertical.put("format", "selectitem");
        selectVertical.put("orientation", "vertical");
        Map<String, String> selectHorizontal = new HashMap<>();
        selectHorizontal.put("format", "selectitem");
        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");
        uiSchemaBuilder //
                .addCategory("ACL") //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("keyupdateacl", "Active", selectHorizontal) //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("keyownershipfields", "Owner", selectVertical)
                .addElement("keyaddreadfields", "Read Access", selectVertical)
                .addElement("keyaddwritefields", "Write Access", selectVertical)
                .addLayout(Layout.HORIZONTAL) //
                .addElement("namownershipnames", "Others", multilineOption)
                .addElement("namaddreadaccess", "Others", multilineOption)
                .addElement("namaddwriteaccess", "Others", multilineOption)

        ;
    }

    /**
     * This method updates the BPMN properties
     */
    @Override
    public void updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        // we are only interested in category Workflow and History
        if (!"ACL".equals(category)) {
            return;
        }

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();

        // base settings

        ImixsExtensionUtil.setItemValue(model, elementNode, "keyupdateacl", "xs:string",
                json.getString("keyupdateacl", "false"));

        // set keyscheduledbaseobject
        JsonArray valueArray = json.getJsonArray("keyownershipfields");
        Iterator<JsonValue> iter = valueArray.iterator();
        List<String> keyBaseObject = new ArrayList<String>();
        while (iter.hasNext()) {
            JsonValue nextValue = iter.next();
            String jsonStringValue = ((JsonString) nextValue).getString();
            keyBaseObject.add(jsonStringValue);
        }
        ImixsExtensionUtil.setItemValueList(model, elementNode, "keyownershipfields", "xs:string",
                keyBaseObject);

        valueArray = json.getJsonArray("keyaddreadfields");
        iter = valueArray.iterator();
        keyBaseObject = new ArrayList<String>();
        while (iter.hasNext()) {
            JsonValue nextValue = iter.next();
            String jsonStringValue = ((JsonString) nextValue).getString();
            keyBaseObject.add(jsonStringValue);
        }
        ImixsExtensionUtil.setItemValueList(model, elementNode, "keyaddreadfields", "xs:string",
                keyBaseObject);

        valueArray = json.getJsonArray("keyaddwritefields");
        iter = valueArray.iterator();
        keyBaseObject = new ArrayList<String>();
        while (iter.hasNext()) {
            JsonValue nextValue = iter.next();
            String jsonStringValue = ((JsonString) nextValue).getString();
            keyBaseObject.add(jsonStringValue);
        }
        ImixsExtensionUtil.setItemValueList(model, elementNode, "keyaddwritefields", "xs:string",
                keyBaseObject);

        ImixsExtensionUtil.setItemValue(model, elementNode, "namownershipnames", "xs:string",
                json.getString("namownershipnames", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "namaddreadaccess", "xs:string",
                json.getString("namaddreadaccess", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "namaddwriteaccess", "xs:string",
                json.getString("namaddwriteaccess", ""));

    }

}
