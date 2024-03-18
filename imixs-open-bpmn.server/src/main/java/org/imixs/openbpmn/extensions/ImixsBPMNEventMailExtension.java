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
 * This is the Mail extension providing the JSONForms schemata.
 *
 * @author rsoika
 *
 */
public class ImixsBPMNEventMailExtension extends ImixsBPMNExtension {

    private static Logger logger = Logger.getLogger(ImixsBPMNEventMailExtension.class.getName());

    public ImixsBPMNEventMailExtension() {
        super();
    }

    @Override
    public int getPriority() {
        return 1140;
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

        // generate Mail panel
        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();

        ImixsItemNameMapper actorFieldMapper = new ImixsItemNameMapper(model, "txtfieldmapping");

        /***********
         * Data
         */
        dataBuilder //
                .addData("txtmailsubject",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "txtmailsubject")) //
                .addData("rtfmailbody",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "rtfmailbody")) //
                .addDataList("keymailreceiverfields",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keymailreceiverfields", actorFieldMapper.getValues())) //
                .addDataList("keymailreceiverfieldscc",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keymailreceiverfieldscc",
                                actorFieldMapper.getValues())) //
                .addDataList("keymailreceiverfieldsbcc",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keymailreceiverfieldsbcc",
                                actorFieldMapper.getValues())) //
                .addData("nammailreceiver", String.join(System.lineSeparator(),
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "nammailreceiver"))) //
                .addData("nammailreceivercc", String.join(System.lineSeparator(),
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "nammailreceivercc"))) //
                .addData("nammailreceiverbcc", String.join(System.lineSeparator(),
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "nammailreceiverbcc"))) //
        ;

        /***********
         * Schema
         */

        String[] actorItemDefsArray = actorFieldMapper.getItemDefinitions().toArray(String[]::new);
        schemaBuilder //
                .addProperty("txtmailsubject", "string", "") //
                .addProperty("rtfmailbody", "string",
                        "Mail body can be plain text, HTML or a XSL Template.") //
                .addProperty("keymailreceiverfields", "string", "", actorItemDefsArray) //
                .addProperty("keymailreceiverfieldscc", "string", "", actorItemDefsArray) //
                .addProperty("keymailreceiverfieldsbcc", "string", "", actorItemDefsArray) //
                .addProperty("nammailreceiver", "string", "Add multiple entries in separate lines.") //
                .addProperty("nammailreceivercc", "string", "Add multiple entries in separate lines.") //
                .addProperty("nammailreceiverbcc", "string", "Add multiple entries in separate lines.") //
        ;

        /***********
         * UISchema
         */
        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");
        Map<String, String> selectVertical = new HashMap<>();
        selectVertical.put("format", "selectitem");
        selectVertical.put("orientation", "vertical");

        uiSchemaBuilder //
                .addCategory("Message") //
                .addElement("txtmailsubject", "Subject", null) //
                .addElement("rtfmailbody", "Body", this.getFileEditorOption());

        if (actorFieldMapper.getItemDefinitions() != null && actorFieldMapper.getItemDefinitions().size() > 0) {
            uiSchemaBuilder //
                    .addLayout(Layout.HORIZONTAL) //
                    .addElement("keymailreceiverfields", "To", selectVertical)
                    .addElement("keymailreceiverfieldscc", "CC", selectVertical)
                    .addElement("keymailreceiverfieldsbcc", "BCC", selectVertical);
        }

        uiSchemaBuilder //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("nammailreceiver", "Others", multilineOption) //
                .addElement("nammailreceivercc", "Others", multilineOption) //
                .addElement("nammailreceiverbcc", "Others", multilineOption);

    }

    /**
     * This method updates the BPMN properties
     */
    @Override
    public boolean updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        // we are only interested in category Message
        if ("Message".equals(category)) {

            BPMNModel model = bpmnElement.getModel();
            Element elementNode = bpmnElement.getElementNode();

            ImixsItemNameMapper actorFieldMapper = new ImixsItemNameMapper(model, "txtfieldmapping");

            // subject / body
            ImixsExtensionUtil.setItemValue(model, elementNode, "txtmailsubject", "xs:string",
                    json.getString("txtmailsubject", ""));
            ImixsExtensionUtil.setItemValue(model, elementNode, "rtfmailbody", "xs:string",
                    json.getString("rtfmailbody", ""));

            // set the Checkbox Key Properties.
            // For each property a for-each loop is used to iterate over the JsonValue
            // objects in the JsonArray, and the getString() method is called to retrieve
            // the string value of each JsonString object. Finally, the
            // ImixsExtensionUtil.setItemValueList() method is called to set the value list
            // for the property.
            String[] keyProperties = { "keymailreceiverfields", "keymailreceiverfieldscc",
                    "keymailreceiverfieldsbcc" };
            for (String property : keyProperties) {
                JsonArray valueArray = json.getJsonArray(property);
                List<String> keyBaseObject = new ArrayList<>(valueArray.size());
                for (JsonValue value : valueArray) {
                    String jsonStringValue = ((JsonString) value).getString();
                    keyBaseObject.add(jsonStringValue);
                }
                ImixsExtensionUtil.setItemValueList(model, elementNode, property, "xs:string",
                        keyBaseObject,
                        actorFieldMapper.getValues());
            }

            // Set the other names.
            // For each property, the getString() method is called to retrieve the
            // property value from the json object. The split() method is called to split
            // the value into lines, and we set a value list for each property.
            String[] nameProperties = { "nammailreceiver", "nammailreceivercc", "nammailreceiverbcc" };
            for (String property : nameProperties) {
                String otherValue = json.getString(property, "");
                // String[] lines = otherValue.split(System.lineSeparator());
                // See: https://github.com/imixs/imixs-open-bpmn/issues/24
                String[] lines = otherValue.split("\\R");
                ImixsExtensionUtil.setItemValueList(model, elementNode, property, "xs:string",
                        Arrays.asList(lines), null);
            }
        }
        return false;

    }

}
