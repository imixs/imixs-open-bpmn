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
package org.imixs.openbpmn;

import java.util.logging.Logger;

import org.imixs.openbpmn.extensions.ImixsBPMNDefinitionsExtension;
import org.imixs.openbpmn.extensions.ImixsBPMNEventExtension;
import org.imixs.openbpmn.extensions.ImixsBPMNTaskExtension;
import org.openbpmn.extension.BPMNExtension;
import org.openbpmn.glsp.BPMNDiagramModule;

import com.google.inject.multibindings.Multibinder;

/**
 * The DiagramModule contains the bindings in dedicated methods. Imixs BPMN
 * extends this module and customize it by overriding dedicated binding methods.
 *
 *
 * @author rsoika
 *
 */
public class ImixsBPMNDiagramModule extends BPMNDiagramModule {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(ImixsBPMNDiagramModule.class.getName());

    /**
     * This method adds the custom Imixs BPMN Extensions
     *
     * @param binding
     */
    public void configureBPMNExtensions(final Multibinder<BPMNExtension> binding) {
        // bind BPMN default extensions
        super.configureBPMNExtensions(binding);

        // Imixs Extensions (just for testing)
        binding.addBinding().to(ImixsBPMNDefinitionsExtension.class);
        binding.addBinding().to(ImixsBPMNTaskExtension.class);
        binding.addBinding().to(ImixsBPMNEventExtension.class);
    }
}
