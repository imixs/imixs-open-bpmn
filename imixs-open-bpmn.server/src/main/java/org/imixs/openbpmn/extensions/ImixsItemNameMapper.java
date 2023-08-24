package org.imixs.openbpmn.extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.openbpmn.bpmn.BPMNModel;
import org.w3c.dom.Element;

/**
 * This is a helper class to deal with label value pairs defined in the Imixs
 * Workflow Definitions Extension
 * <p>
 * The mapper resolves label values pairs of the following format
 * 
 * {@code Label|value}
 * 
 * and provides methods to resolve a value by a label and vice versar.
 * 
 */
public class ImixsItemNameMapper {
    List<String> labels = null;
    List<String> values = null;
    List<String> itemDefinitions = null;

    private static Logger logger = Logger.getLogger(ImixsBPMNTaskExtension.class.getName());

    String[] validFieldMappings = new String[] { "txttimefieldmapping", "txtfieldmapping" };

    /**
     * Construct a new ImixsItemMapping based on a given fieldMapping name stored in
     * the Imixs BPMN Definitions Extension.
     * 
     * Supported filedMappings are: txttimefieldmapping | txtfieldmapping
     * 
     * @param model
     * @param fieldMapping
     */
    public ImixsItemNameMapper(final BPMNModel model, final String fieldMapping) {

        if (!Arrays.asList(validFieldMappings).contains(fieldMapping)) {
            logger.severe("Unsupported field mapping - '" + fieldMapping + "'");
        }

        // resolve Item References and store the parts in object lists
        Element definitionsElementNode = model.getDefinitions();
        itemDefinitions = ImixsExtensionUtil.getItemValueList(model, definitionsElementNode, fieldMapping);
        labels = new ArrayList<String>();
        values = new ArrayList<String>();
        for (String _itemDef : itemDefinitions) {
            String[] defParts = _itemDef.split("\\|");
            if (defParts.length > 1) {
                labels.add(defParts[0].trim());
                values.add(defParts[1].trim());
            } else {
                labels.add(_itemDef.trim());
                values.add(_itemDef.trim());
            }
        }

    }

    /**
     * Returns the label list
     * 
     * @return
     */
    public List<String> getLabels() {
        return labels;
    }

    /**
     * Returns the value list
     * 
     * @return
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Returns the ItemDefinition list containing label|value
     * 
     * @return
     */
    public List<String> getItemDefinitions() {
        return itemDefinitions;
    }

    /**
     * Resolves the label for a given value
     * 
     * @param _ref
     * @return
     */
    public String resolveLabel(String _ref) {
        int refIndex = values.indexOf(_ref);
        if (refIndex > -1) {
            return labels.get(refIndex);
        }
        // not found
        return "";
    }

    /**
     * Resolves the value for a given label
     * 
     * @param _ref
     * @return
     */
    public String resolveValue(String _ref) {
        int refIndex = labels.indexOf(_ref);
        if (refIndex > -1) {
            return values.get(refIndex);
        }
        // not found
        return "";
    }

    /**
     * Returns the Labels as a String Array
     * 
     * @return String array with all labels
     */
    public String[] getLabelsArray() {
        if (labels != null) {
            return labels.toArray(String[]::new);
        }
        // no defintion
        return new String[] { "" };
    }
}
