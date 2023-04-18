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
import java.util.List;
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

        /***********
         * Data
         */
        dataBuilder //
                .addData("txtmailsubject",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "txtmailsubject")) //
                .addData("rtfmailbody",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "rtfmailbody")) //
                .addDataList("keymailreceiverfields",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keymailreceiverfields")) //
                .addDataList("keymailreceiverfieldscc",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keymailreceiverfieldscc")) //
                .addDataList("keymailreceiverfieldsbcc",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keymailreceiverfieldsbcc")) //
                .addData("nammailreceiver",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "nammailreceiver")) //
                .addData("nammailreceivercc",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "nammailreceivercc")) //
                .addData("nammailreceiverbcc",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "nammailreceiverbcc")) //

        ;

        /***********
         * Schema
         */
        // fetch the actorItem definitions from the model definition
        Element definitionsElementNode = model.getDefinitions();
        List<String> actorItemDefs = ImixsExtensionUtil.getItemValueList(model, definitionsElementNode,
                "txtfieldmapping");

        String[] actorItemDefsArray = actorItemDefs.toArray(String[]::new);
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
                .addElement("rtfmailbody", "Body", multilineOption);

        if (actorItemDefs != null && actorItemDefs.size() > 0) {
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
    public void updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        // we are only interested in category Workflow and History
        if ("Mail".equals(category)) {

        }

    }

}
