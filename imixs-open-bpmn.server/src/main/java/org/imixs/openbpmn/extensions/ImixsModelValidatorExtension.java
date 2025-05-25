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

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNNS;
import org.openbpmn.bpmn.ModelNotification;
import org.openbpmn.bpmn.elements.BPMNProcess;
import org.openbpmn.bpmn.elements.DataObject;
import org.openbpmn.bpmn.elements.core.BPMNElementNode;
import org.openbpmn.bpmn.exceptions.BPMNModelException;
import org.openbpmn.extensions.BPMNModelExtension;
import org.openbpmn.extensions.model.FileLinkExtension;
import org.w3c.dom.Element;

/**
 * The ImixsModelValidatorExtension validate ACL Actor names and DataObject
 * Types.
 * <p>
 * The Extension scans all actor fieldMappings in acl and mail message nodes on
 * the
 * load and save event and automatically fixes wrong mappings.
 * The Extension sets the isDirty flag on load if the actor fields changed.
 * <p>
 * The Extension also verifies DataObjects if the content is stored in an
 * external file and optional update the dataType flag if necessary.
 * 
 * @author rsoika
 */
public class ImixsModelValidatorExtension implements BPMNModelExtension {
    protected static Logger logger = Logger.getLogger(ImixsModelValidatorExtension.class.getName());

    @Override
    public int getPriority() {
        return 102;
    }

    /**
     * This method verifies all Task and Event elements for valid ACL Actor field
     * mappings.
     * 
     */
    @Override
    public void onSave(BPMNModel model, final Path path) {
        validateACL(model);
    }

    /**
     * This method validates the ACL mappings and sets the isDirty flag if an
     * invalid value was found
     * 
     * @param path - file path
     * @return boolean - true if the linked file content has changed.
     */

    @Override
    public void onLoad(BPMNModel model, Path path) {

        if (!validateACL(model)) {
            // mark model as dirty
            model.setDirty(true);
            model.getNotifications().add(new ModelNotification(ModelNotification.Severity.WARNING,
                    "ACL Settings updated!", "Invalid ACL Settings found - fixed automatically!"));
        }

        if (!validateDataObjects(model, path)) {
            // mark model as dirty
            model.setDirty(true);
            model.getNotifications().add(new ModelNotification(ModelNotification.Severity.WARNING,
                    "Data Objects updated!", "Data Objects updated."));
        }

    }

    /**
     * Helper method validates all acl field mappings in events and tasks and
     * automatically removes invalid values.
     * 
     * The method returns false if values were updated
     * 
     * @param model
     * @return
     */
    private boolean validateACL(BPMNModel model) {
        boolean result = true;
        ImixsItemNameMapper actorFieldMapper = new ImixsItemNameMapper(model, "txtfieldmapping");
        String[] keyProperties = { "keyownershipfields", "keyaddreadfields", "keyaddwritefields",
                "keymailreceiverfields", "keymailreceiverfieldscc", "keymailreceiverfieldsbcc" };

        // Iterate over all Events and Task and call verify the ACL settings
        LinkedHashSet<BPMNElementNode> allACLElements = new LinkedHashSet<BPMNElementNode>();
        allACLElements.addAll(model.findAllEvents());
        allACLElements.addAll(model.findAllActivities());

        for (BPMNElementNode aclElement : allACLElements) {

            for (String property : keyProperties) {

                List<String> valueList = ImixsExtensionUtil.getItemValueList(model, aclElement.getElementNode(),
                        property);

                // validate value list just to give out a message
                for (String _value : valueList) {
                    if (!actorFieldMapper.getValues().contains(_value)) {
                        result = false;
                        logger.warning(property + " contains invalid value '" + _value
                                + "' - value will be automatically removed!");

                        ImixsExtensionUtil.setItemValueList(model, aclElement.getElementNode(), property, "xs:string",
                                valueList,
                                actorFieldMapper.getValues());
                    }
                }

            }

        }
        return result;
    }

    /**
     * Helper method validates all dataObjects with external fileData and tests the
     * dataType update which is used to display the correct icon on the bpmn
     * element.
     * 
     * The method returns false if values were updated
     * 
     * Note: The method only validates DataObjects with a file link!
     * 
     * @param model
     * @return
     */
    private boolean validateDataObjects(BPMNModel model, Path path) {

        boolean valid = true;
        // test only if the imixs-extension was applied to the model!
        if (!model.hasNamespace(ImixsExtensionUtil.getNamespace())) {
            return valid;
        }
        Set<BPMNProcess> processList = model.getProcesses();
        for (BPMNProcess process : processList) {
            try {
                process.init();
                Set<DataObject> objects = process.getDataObjects();
                for (DataObject dataObject : objects) {
                    if (dataObject.hasExtensionAttribute(ImixsExtensionUtil.getNamespace(),
                            ImixsBPMNDataObjectExtension.IMIXS_DATATYPE)) {
                        // do not validate
                        continue;
                    }
                    // compute data type...
                    String oldDataType = dataObject.getExtensionAttribute(ImixsExtensionUtil.getNamespace(),
                            ImixsBPMNDataObjectExtension.IMIXS_DATATYPE);
                    String dataType = "";
                    Element element = dataObject.getChildNode(BPMNNS.BPMN2, "documentation");
                    String data;
                    try {
                        data = FileLinkExtension.readFileContent(element, path);
                        if (data == null) {
                            // no external file data - so we try real content
                            data = dataObject.getDocumentation();
                        }
                        if (data == null || data.isEmpty()) {
                            continue;
                        }
                        if (data.contains("<imixs-form")) {
                            dataType = ImixsBPMNDataObjectExtension.IMIXS_DATATYPE_FORM;
                        }
                        if (data.contains("<PromptDefinition")) {
                            dataType = ImixsBPMNDataObjectExtension.IMIXS_DATATYPE_AI;
                        }
                        if (!dataType.isEmpty()) {
                            dataObject.setExtensionAttribute(ImixsExtensionUtil.getNamespace(),
                                    ImixsBPMNDataObjectExtension.IMIXS_DATATYPE, dataType);
                        } else {
                            // remove deprecated type
                            dataObject.removeExtensionAttribute(ImixsExtensionUtil.getNamespace(),
                                    ImixsBPMNDataObjectExtension.IMIXS_DATATYPE);
                        }
                        // If data type changed, set flag
                        if (!dataType.equals(oldDataType)) {
                            valid = false;
                        }
                    } catch (IOException e) {
                        logger.fine("Failed to validate extenral file data");
                    }

                }
            } catch (BPMNModelException e) {
                logger.warning("Failed to open process: " + e.getMessage());
            }

        }
        return valid;
    }
}
