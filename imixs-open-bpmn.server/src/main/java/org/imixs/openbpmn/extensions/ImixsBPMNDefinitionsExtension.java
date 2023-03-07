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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.eclipse.glsp.graph.GModelElement;
import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNTypes;
import org.openbpmn.bpmn.elements.BPMNProcess;
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
public class ImixsBPMNDefinitionsExtension extends ImixsBPMNExtension {

    private static Logger logger = Logger.getLogger(ImixsBPMNTaskExtension.class.getName());

    public ImixsBPMNDefinitionsExtension() {
        super();
    }

    @Override
    public int getPriority() {
        return 1101;
    }

    @Override
    public boolean handlesElementTypeId(final String elementTypeId) {
        return BPMNTypes.PROCESS_TYPE_PUBLIC.equals(elementTypeId);
    }

    /**
     * This Extension is for the default public process only
     */
    @Override
    public boolean handlesBPMNElement(final BPMNElement bpmnElement) {
        if (bpmnElement instanceof BPMNProcess) {
            return ((BPMNProcess) bpmnElement).isPublicProcess();
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

        // find the definitions element
        BPMNModel model = bpmnElement.getModel();
        Element elementNode = model.getDefinitions();
        dataBuilder //
                .addData("txtworkflowmodelversion",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "txtworkflowmodelversion"));

        // add Date Objects
        ImixsItemNameMapper timeFieldMapper = new ImixsItemNameMapper(model, "txttimefieldmapping");
        dataBuilder.addArray("dateobjects");
        for (int i = 0; i < timeFieldMapper.values.size(); i++) {
            dataBuilder.addObject();
            dataBuilder.addData("date", timeFieldMapper.labels.get(i));
            dataBuilder.addData("item", timeFieldMapper.values.get(i));
        }
        dataBuilder.closeArray();

        // add Field Mapping
        dataBuilder.addArray("actors");
        ImixsItemNameMapper actorFieldMapper = new ImixsItemNameMapper(model, "txtfieldmapping");
        for (int i = 0; i < actorFieldMapper.values.size(); i++) {
            dataBuilder.addObject();
            dataBuilder.addData("actor", actorFieldMapper.labels.get(i));
            dataBuilder.addData("item", actorFieldMapper.values.get(i));
        }
        dataBuilder.closeArray();

        // add Plugin list
        List<String> plugins = ImixsExtensionUtil.getItemValueList(model, elementNode, "txtplugins");
        dataBuilder.addArray("plugins");
        for (String _plugin : plugins) {
            dataBuilder.addObject();
            dataBuilder.addData("classname",
                    _plugin);
        }
        dataBuilder.closeArray();

        /*
         * *****************
         * Schema *
         *******************/
        schemaBuilder. //
                addProperty("txtworkflowmodelversion", "string", null);

        schemaBuilder.addArray("dateobjects");
        schemaBuilder.addProperty("date", "string", null, null);
        schemaBuilder.addProperty("item", "string", null, null);

        schemaBuilder.addArray("actors");
        schemaBuilder.addProperty("actor", "string", null, null);
        schemaBuilder.addProperty("item", "string", null, null);

        schemaBuilder.addArray("plugins");
        schemaBuilder.addProperty("classname", "string", null, null);

        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");

        /***********
         * UISchema
         ***********/
        uiSchemaBuilder. //
                addCategory("Workflow"). //
                addLayout(Layout.VERTICAL). //
                addElement("txtworkflowmodelversion", "Model Version", null). //
                addElement("dateobjects", "Date Objects", null). //
                addElement("actors", "Actors", null). //
                addElement("plugins", "Plugins", null);

    }

    @Override
    public void updatePropertiesData(final JsonObject json, final String category, final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        // we are only interested in category Workflow
        if (!"Workflow".equals(category)) {
            return;
        }

        long l = System.currentTimeMillis();
        // find the definitions element
        BPMNModel model = bpmnElement.getModel();
        Element elementNode = model.getDefinitions();
        ImixsExtensionUtil.setItemValue(model, elementNode, "txtworkflowmodelversion", "xs:string",
                json.getString("txtworkflowmodelversion", ""));

        // Update dateobjects
        JsonArray dataList = json.getJsonArray("dateobjects");
        List<String> valueList = new ArrayList<>();
        if (dataList != null) {
            for (JsonValue jsonValue : dataList) {
                JsonObject jsonData = (JsonObject) jsonValue;
                // JsonObject jsonData = (JsonObject) iter.next();
                if (jsonData != null) {
                    String date = jsonData.getString("date", "");
                    String item = jsonData.getString("item", "");
                    if (!item.isEmpty()) {
                        if (date.isEmpty()) {
                            valueList.add(item);
                        } else {
                            valueList.add(date + "|" + item);
                        }
                    }
                }
            }
        }
        ImixsExtensionUtil.setItemValueList(model, elementNode, "txttimefieldmapping", "xs:string", valueList);

        // Update actors
        valueList = new ArrayList<>();
        dataList = json.getJsonArray("actors");
        if (dataList != null) {
            for (JsonValue jsonValue : dataList) {
                JsonObject jsonData = (JsonObject) jsonValue;
                // JsonObject jsonData = (JsonObject) iter.next();

                if (jsonData != null) {
                    String actor = jsonData.getString("actor", "");
                    String item = jsonData.getString("item", "");
                    if (!item.isEmpty()) {
                        if (actor.isEmpty()) {
                            valueList.add(item);
                        } else {
                            valueList.add(actor + "|" + item);
                        }
                    }
                }
            }
        }
        ImixsExtensionUtil.setItemValueList(model, elementNode, "txtfieldmapping",
                "xs:string", valueList);

        // Update Plugin list
        valueList = new ArrayList<>();
        dataList = json.getJsonArray("plugins");
        if (dataList != null) {
            for (JsonValue jsonValue : dataList) {
                JsonObject jsonData = (JsonObject) jsonValue;
                // JsonObject jsonData = (JsonObject) iter.next();
                if (jsonData != null) {
                    valueList.add(jsonData.getString("classname", ""));
                }
            }
        }
        ImixsExtensionUtil.setItemValueList(model, elementNode, "txtplugins",
                "xs:string", valueList);
        // update completed
    }

}
