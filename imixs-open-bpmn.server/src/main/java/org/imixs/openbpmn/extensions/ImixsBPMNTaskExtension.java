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

import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNTypes;
import org.openbpmn.bpmn.elements.Activity;
import org.openbpmn.bpmn.elements.core.BPMNElement;
import org.openbpmn.glsp.bpmn.BPMNGNode;
import org.openbpmn.glsp.jsonforms.DataBuilder;
import org.openbpmn.glsp.jsonforms.SchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder.Layout;
import org.openbpmn.glsp.utils.BPMNGModelUtil;
import org.w3c.dom.Element;

/**
 * This is the Default BPMNEvent extension providing the JSONForms schemata.
 *
 * @author rsoika
 *
 */
public class ImixsBPMNTaskExtension extends ImixsBPMNExtension {

    private static Logger logger = Logger.getLogger(ImixsBPMNTaskExtension.class.getName());

    public ImixsBPMNTaskExtension() {
        super();
    }

    @Override
    public int getPriority() {
        return 1101;
    }

    /**
     * The ImixsBPMNTaskExtension can only be applied to a BPMN Task element
     */
    @Override
    public boolean handlesElementTypeId(final String elementTypeId) {
        return BPMNTypes.TASK.equals(elementTypeId);
    }

    /**
     * This Extension is for BPMN Task Elements only
     * <p>
     * The method also verifies if the element has a imixs:processid attribute. This
     * attribute is added in the 'addExtesnion' method call
     */
    @Override
    public boolean handlesBPMNElement(final BPMNElement bpmnElement) {

        if (bpmnElement instanceof Activity) {
            Activity task = (Activity) bpmnElement;
            if (task.getType().equals(BPMNTypes.TASK)) {
                // next check the extension attribute imixs:processid
                if (task.hasAttribute(getNamespace() + ":processid")) {
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
        if (bpmnElement instanceof Activity) {
            bpmnElement.setExtensionAttribute(getNamespace(), "processid", "100");

        }

        // if (bpmnElement instanceof Event) {
        // bpmnElement.setExtensionAttribute(getNamespace(), "activityid", "10");
        // }
    }

    /**
     * Return the task id
     */
    @Override
    public String getInfo(final BPMNElement bpmnElement) {
        return "Id: " + bpmnElement.getExtensionAttribute(getNamespace(), "processid");
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

        /***********
         * Data
         */
        dataBuilder //

                .addData("processid", bpmnElement.getExtensionAttribute(getNamespace(), "processid")) //
                .addData("txttype",
                        ImixsExtensionUtil.getItemValueString(model, elementNode, "txttype")) //
                .addData("txtimageurl",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "txtimageurl")) //
                .addData("txteditorid",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "txteditorid")) //
                .addData("form_definition",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "form.definition")) //
                .addData("txtworkflowsummary",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "txtworkflowsummary")) //
                .addData("txtworkflowabstract",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "txtworkflowabstract"));

        /***********
         * Schema
         */
        schemaBuilder. //
                addProperty("processid", "string", null). //
                addProperty("txteditorid", "string",
                        "The 'Form ID' defines an application form element to be displayed within this task.")
                . //
                addProperty("txttype", "string", null). //
                addProperty("txtimageurl", "string", null). //
                addProperty("form_definition", "string",
                        "An optional 'Form Definition' describe custom form sections and elements.")
                . //
                addProperty("txtworkflowsummary", "string", null). //
                addProperty("txtworkflowabstract", "string", null);

        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");

        /***********
         * UISchema
         */
        uiSchemaBuilder. //
                addCategory("Workflow"). //
                addLayout(Layout.HORIZONTAL). //
                addElement("processid", "Process ID", null). //
                addElement("txttype", "Type", null). //
                addElement("txtimageurl", "Symbol", null). //
                addLayout(Layout.HORIZONTAL). //

                addLayout(Layout.VERTICAL). //
                addElement("txtworkflowsummary", "Summary", null). //
                addElement("txtworkflowabstract", "Abstract", multilineOption). //
                addCategory("App"). //
                addElement("txteditorid", "Input Form ID", null). //
                addElement("form_definition", "Input Form Definition", this.getFileEditorOption());

    }

    /**
     * This method updates the BPMN properties and also the imixs processid.
     * The processID is also updated for the frontend.
     */
    @Override
    public void updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        // we are only interested in category Workflow and App
        if (!"Workflow".equals(category) && !"App".equals(category)) {
            return;
        }
        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();

        String oldTaskId = bpmnElement.getExtensionAttribute(getNamespace(), "processid");
        String newTaskId = json.getString("processid", "0");
        if (gNodeElement instanceof BPMNGNode && !newTaskId.equals(oldTaskId)) {
            bpmnElement.setExtensionAttribute(getNamespace(), "processid",
                    json.getString("processid", "0"));
            // update gNode...
            GLabel label = BPMNGModelUtil.findExtensionLabel((BPMNGNode) gNodeElement);
            if (label != null) {
                label.setText("ID: " + newTaskId);
            }
        }

        ImixsExtensionUtil.setItemValue(model, elementNode, "txttype", "xs:string",
                json.getString("txttype", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "txtimageurl", "xs:string",
                json.getString("txtimageurl", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "txtworkflowsummary", "xs:string",
                json.getString("txtworkflowsummary", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "txtworkflowabstract", "xs:string",
                json.getString("txtworkflowabstract", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "txteditorid", "xs:string",
                json.getString("txteditorid", ""));
        ImixsExtensionUtil.setItemValue(model, elementNode, "form.definition", "xs:string",
                json.getString("form_definition", ""));

    }

}
