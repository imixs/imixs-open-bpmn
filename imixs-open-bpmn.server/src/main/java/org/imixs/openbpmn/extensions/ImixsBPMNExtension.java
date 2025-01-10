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

import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.elements.core.BPMNElement;
import org.openbpmn.extensions.BPMNElementExtension;
import org.w3c.dom.Element;

/**
 * This is the Default BPMNEvent extension providing the JSONForms shemata.
 *
 * @author rsoika
 *
 */
public abstract class ImixsBPMNExtension implements BPMNElementExtension {

    private static Logger logger = Logger.getLogger(ImixsBPMNExtension.class.getName());

    public ImixsBPMNExtension() {
        super();
    }

    @Override
    public String getNamespace() {
        return ImixsExtensionUtil.getNamespace();
    }

    @Override
    public String getNamespaceURI() {
        return ImixsExtensionUtil.getNamespaceURI();
    }

    @Override
    public String getLabel() {
        return "Imixs-Workflow";
    }

    /**
     * This method adds a new Imixs Extension. The methoe is overwritten by the
     * specific element extensions (Task and Event).
     * 
     * The method verifies if we already have default imixs properties for the
     * process. If not this method will generate a default setup
     * 
     */
    @Override
    public void addExtension(final BPMNElement bpmnElement) {
        BPMNModel model = bpmnElement.getModel();
        Element definitionsElement = model.getDefinitions();
        if (definitionsElement != null) {

            // Add a default model version if not yet set
            String imixsModelVersion = ImixsExtensionUtil.getItemValueString(model, definitionsElement,
                    "txtworkflowmodelversion");
            if (imixsModelVersion.isEmpty()) {
                ImixsExtensionUtil.setItemValue(model, definitionsElement, "txtworkflowmodelversion", "xs:string",
                        "default-en-1.0");
            }

            // add default plugin list if not yet set
            List<String> plugins = ImixsExtensionUtil.getItemValueList(model, definitionsElement, "txtplugins");
            if (plugins.isEmpty()) {
                plugins = new ArrayList<>();
                plugins.add("org.imixs.workflow.engine.plugins.RulePlugin");
                plugins.add("org.imixs.workflow.engine.plugins.SplitAndJoinPlugin");
                plugins.add("org.imixs.workflow.engine.plugins.OwnerPlugin");
                plugins.add("org.imixs.workflow.engine.plugins.ApproverPlugin");
                plugins.add("org.imixs.workflow.engine.plugins.HistoryPlugin");
                plugins.add("org.imixs.workflow.engine.plugins.ApplicationPlugin");
                plugins.add("org.imixs.workflow.engine.plugins.IntervalPlugin");
                plugins.add("org.imixs.workflow.engine.plugins.MailPlugin");
                plugins.add("org.imixs.workflow.engine.plugins.ResultPlugin");
                ImixsExtensionUtil.setItemValueList(model, definitionsElement, "txtplugins",
                        "xs:string", plugins, null);
            }

            // add default txtfieldmapping if not yet set
            List<String> fieldMappings = ImixsExtensionUtil.getItemValueList(model, definitionsElement,
                    "txtfieldmapping");
            if (fieldMappings.isEmpty()) {
                fieldMappings = new ArrayList<>();
                fieldMappings.add("Creator|$creator");
                fieldMappings.add("Owner|$owner");
                fieldMappings.add("Editor|$editor");
                ImixsExtensionUtil.setItemValueList(model, definitionsElement, "txtfieldmapping",
                        "xs:string", fieldMappings, null);
            }
        }

    }

    /**
     * Helper method that returns a Multiline Option for the JSONForms UI Schema
     * 
     * @return
     */
    Map<String, String> getMultilineOption() {
        Map<String, String> multilineOption = new HashMap<>();
        multilineOption.put("multi", "true");
        return multilineOption;
    }

    /**
     * Helper method that returns a textFileEditor Option for the JSONForms UI
     * Schema
     * 
     * @return
     */
    Map<String, String> getFileEditorOption() {
        // custom fileEditor...
        Map<String, String> fileEditor = new HashMap<>();
        fileEditor.put("format", "textFileEditor");
        return fileEditor;
    }
}
