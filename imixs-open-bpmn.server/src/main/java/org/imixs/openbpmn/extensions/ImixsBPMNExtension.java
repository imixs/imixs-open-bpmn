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

import org.openbpmn.bpmn.elements.core.BPMNElement;
import org.openbpmn.extensions.BPMNElementExtension;

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
     * This method adds a unique identifier to the corresponding BPMNElement
     */
    @Override
    public void addExtension(final BPMNElement bpmnElement) {
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
