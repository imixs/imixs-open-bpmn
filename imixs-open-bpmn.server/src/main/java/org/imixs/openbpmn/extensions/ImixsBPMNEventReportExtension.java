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
 * This is the Report BPMNEvent extension providing the JSONForms shemata.
 * 
 * An Imixs BPMN event provide the following properties:
 * <p>
 * <ul>
 * <li>txtReportName=Name of the Report to be processed
 * <li>txtReportFilePath= optional filename or file path the result will be
 * saved
 * <li>txtReportOptions= optional params
 * <li>txtReportTarget = where the result is saved (0=workitem, 2= disk)
 * </ul>
 *
 * @author rsoika
 *
 */
public class ImixsBPMNEventReportExtension extends ImixsBPMNElementExtension {

        private static Logger logger = Logger.getLogger(ImixsBPMNEventRuleExtension.class.getName());

        public ImixsBPMNEventReportExtension() {
                super();
        }

        @Override
        public int getPriority() {
                return 1170;
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
                                .addData("txtreportname",
                                                ImixsExtensionUtil.getItemValueString(model, elementNode,
                                                                "txtreportname"))//
                                .addData("txtreportfilepath",
                                                ImixsExtensionUtil.getItemValueString(model, elementNode,
                                                                "txtreportfilepath")) //
                                .addData("txtreportoptions",
                                                ImixsExtensionUtil.getItemValueString(model, elementNode,
                                                                "txtreportoptions")) //
                                .addData("txtreporttarget",
                                                ImixsExtensionUtil.getItemValueString(model, elementNode,
                                                                "txtreporttarget", "0"));
                /***********
                 * Schema
                 */
                String[] targetOptions = { "Attach to Workitem|0", "External|2" };
                schemaBuilder //
                                .addProperty("txtreportname", "string",
                                                "Define an optional report definition. A report definition can be processed by a plugin or adapter class. ")//
                                .addProperty("txtreportfilepath", "string",
                                                "Optional path to store the report result.  ") //
                                .addProperty("txtreportoptions", "string", null) //
                                .addProperty("txtreporttarget", "string",
                                                "Target to store the report result object.", targetOptions);

                /***********
                 * UISchema
                 */
                Map<String, String> selectItemOption = new HashMap<>();
                selectItemOption.put("format", "selectitem");
                Map<String, String> multilineOption = new HashMap<>();
                multilineOption.put("multi", "true");
                uiSchemaBuilder //
                                .addCategory("Report") //

                                .addLayout(Layout.VERTICAL) //
                                .addElement("txtreportname", "Report Name", null) //
                                .addElement("txtreportfilepath", "Filename", null) //
                                .addElement("txtreporttarget", "Target", selectItemOption) //
                                .addElement("txtreportoptions", "Options", multilineOption);

        }

        /**
         * This method updates the BPMN properties and also the imixs processid.
         * The processID is also updated for the frontend.
         */
        @Override
        public boolean updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
                        final GModelElement gNodeElement) {

                // we are only interested in category Workflow and History
                if ("Report".equals(category)) {
                        BPMNModel model = bpmnElement.getModel();
                        Element elementNode = bpmnElement.getElementNode();

                        // Report
                        ImixsExtensionUtil.setItemValue(model, elementNode, "txtreportname", "xs:string",
                                        json.getString("txtreportname", ""));
                        ImixsExtensionUtil.setItemValue(model, elementNode, "txtreportfilepath", "xs:string",
                                        json.getString("txtreportfilepath", ""));
                        ImixsExtensionUtil.setItemValue(model, elementNode, "txtreportoptions", "xs:string",
                                        json.getString("txtreportoptions", ""));
                        ImixsExtensionUtil.setItemValue(model, elementNode, "txtreporttarget", "xs:string",
                                        json.getString("txtreporttarget", ""));
                }
                return false;
        }

}
