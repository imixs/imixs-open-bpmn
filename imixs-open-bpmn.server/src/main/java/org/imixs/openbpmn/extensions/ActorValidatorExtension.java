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

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.ModelNotification;
import org.openbpmn.bpmn.elements.core.BPMNElementNode;
import org.openbpmn.extensions.BPMNModelExtension;

/**
 * The ActorTimeFieldValidatorExtension validates all actor fieldmappings and
 * in acl and mail message nodes on the load and save event.
 *
 * The validator automatically fixes wrong mappings and sets the isDirty flag on
 * load to indicate the user the situation.
 * 
 * @author rsoika
 */
public class ActorValidatorExtension implements BPMNModelExtension {
    protected static Logger logger = Logger.getLogger(ActorValidatorExtension.class.getName());

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

}
