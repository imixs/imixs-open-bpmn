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
import org.openbpmn.bpmn.elements.core.BPMNElement;
import org.openbpmn.glsp.jsonforms.DataBuilder;
import org.openbpmn.glsp.jsonforms.SchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder;
import org.openbpmn.glsp.jsonforms.UISchemaBuilder.Layout;
import org.w3c.dom.Element;

/**
 * This helper class for the ACL extension tags within a BPMN Element
 * 
 * 
 */
public class ImixsExtensionACLHelper {

    private static Logger logger = Logger.getLogger(ImixsExtensionACLHelper.class.getName());

    /**
     * Helper Method to generate the ACL Property panel for Events and Tasks
     */
    public static void generateACLSchemata(final BPMNElement bpmnElement, final DataBuilder dataBuilder,
            final SchemaBuilder schemaBuilder, final UISchemaBuilder uiSchemaBuilder) {

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();
        ImixsItemNameMapper actorFieldMapper = new ImixsItemNameMapper(model, "txtfieldmapping");

        /***********
         * Data
         */
        // fetch the allowed actor mappings
        // ImixsExtensionAC

        dataBuilder //
                .addData("keyupdateacl",
                        ImixsExtensionUtil.getItemValueString(model, elementNode,
                                "keyupdateacl", "false")) //
                .addDataList("keyownershipfields",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keyownershipfields", actorFieldMapper.getValues())) //
                .addDataList("keyaddreadfields", ImixsExtensionUtil.getItemValueList(model, elementNode,
                        "keyaddreadfields", actorFieldMapper.getValues()))
                .addDataList("keyaddwritefields",
                        ImixsExtensionUtil.getItemValueList(model, elementNode,
                                "keyaddwritefields", actorFieldMapper.getValues())) //
                .addData("namownershipnames",
                        String.join(System.lineSeparator(),
                                ImixsExtensionUtil.getItemValueList(model, elementNode,
                                        "namownershipnames", null)))
                .addData("namaddreadaccess",
                        String.join(System.lineSeparator(),
                                ImixsExtensionUtil.getItemValueList(model, elementNode,
                                        "namaddreadaccess", null)))
                .addData("namaddwriteaccess",
                        String.join(System.lineSeparator(),
                                ImixsExtensionUtil.getItemValueList(model, elementNode,
                                        "namaddwriteaccess", null)));

        /***********
         * Schema
         */
        String[] enabledOption = { "Yes|true", "No|false" };

        String[] actorItemDefsArray = actorFieldMapper.getItemDefinitions().toArray(String[]::new);
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
                .addElement("keyupdateacl", "Active", selectHorizontal);
        if (actorFieldMapper.getItemDefinitions() != null && actorFieldMapper.getItemDefinitions().size() > 0) {
            uiSchemaBuilder //
                    .addLayout(Layout.HORIZONTAL) //
                    .addElement("keyownershipfields", "Owner", selectVertical)
                    .addElement("keyaddreadfields", "Read Access", selectVertical)
                    .addElement("keyaddwritefields", "Write Access", selectVertical);
        }
        uiSchemaBuilder //
                .addLayout(Layout.HORIZONTAL) //
                .addElement("namownershipnames", "Others", multilineOption)
                .addElement("namaddreadaccess", "Others", multilineOption)
                .addElement("namaddwriteaccess", "Others", multilineOption);
    }

    /**
     * Helper Method to update a BPMN element with new data form the ACL Property
     * panel
     */
    public static void updatePropertiesData(final JsonObject json, final String category,
            final BPMNElement bpmnElement,
            final GModelElement gNodeElement) {

        BPMNModel model = bpmnElement.getModel();
        Element elementNode = bpmnElement.getElementNode();

        ImixsItemNameMapper actorFieldMapper = new ImixsItemNameMapper(model, "txtfieldmapping");

        // base settings
        ImixsExtensionUtil.setItemValue(model, elementNode, "keyupdateacl", "xs:string",
                json.getString("keyupdateacl", "false"));

        // set the Checkbox Key Properties.
        // For each property a for-each loop is used to iterate over the JsonValue
        // objects in the JsonArray, and the getString() method is called to retrieve
        // the string value of each JsonString object. Finally, the
        // ImixsExtensionUtil.setItemValueList() method is called to set the value list
        // for the property.
        String[] keyProperties = { "keyownershipfields", "keyaddreadfields", "keyaddwritefields" };
        for (String property : keyProperties) {
            JsonArray valueArray = json.getJsonArray(property);
            List<String> keyBaseObject = new ArrayList<>(valueArray.size());
            for (JsonValue value : valueArray) {
                String jsonStringValue = ((JsonString) value).getString();
                logger.fine("..add " + property + " -> " + jsonStringValue);
                keyBaseObject.add(jsonStringValue);
            }
            ImixsExtensionUtil.setItemValueList(model, elementNode, property, "xs:string", keyBaseObject,
                    actorFieldMapper.getValues());
        }

        // Set the other names.
        // For each property, the getString() method is called to retrieve the
        // property value from the json object. The split() method is called to split
        // the value into lines, and we set a value list for each property.
        String[] nameProperties = { "namownershipnames", "namaddreadaccess", "namaddwriteaccess" };
        for (String property : nameProperties) {
            String otherValue = json.getString(property, "");
            // String[] lines = otherValue.split(System.lineSeparator());
            // See: https://github.com/imixs/imixs-open-bpmn/issues/24
            String[] lines = otherValue.split("\\R");
            ImixsExtensionUtil.setItemValueList(model, elementNode, property, "xs:string",
                    Arrays.asList(lines), null);
        }

    }
}
