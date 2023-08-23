package org.imixs.openbpmn.extensions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openbpmn.bpmn.BPMNModel;
import org.openbpmn.bpmn.BPMNNS;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This helper class sets extension tags within a BPMN Element
 * 
 * Example:
 * 
 * <pre>{@code  
 * <bpmn2:task id="Task_2" imixs:processid="1900" name="Approve">
      <bpmn2:extensionElements>
        <imixs:item name="user.name" type="xs:string">John</imixs:item>
        ....
      </bpmn2:extensionElements>
        }</pre>
 * 
 */
public class ImixsExtensionUtil {

    public static String getNamespace() {
        return "imixs";
    }

    public static String getNamespaceURI() {
        return "http://www.imixs.org/bpmn2";
    }

    /**
     * This method returns a Imixs ItemValue List from the Model Definition.
     * Such a list can contains label|value pairs.
     * 
     * IF the boolean 'stripLabels' is true than only the value part will be given.
     * 
     * @param model
     * @param itemName
     * @return
     */
    public static List<String> getDefinitionsElementList(BPMNModel model, String itemName, boolean stripLabels) {

        // fetch the actorItem definitions from the model definition
        Element definitionsElementNode = model.getDefinitions();
        List<String> itemDefValues = ImixsExtensionUtil.getItemValueList(model, definitionsElementNode,
                itemName);

        if (stripLabels == false) {
            return itemDefValues;
        }

        // strip the label part....
        List<String> stripedList = new ArrayList<String>();
        for (String value : itemDefValues) {
            if (value.contains("|")) {
                value = value.substring(value.indexOf("|") + 1).trim();
            }
            stripedList.add(value);
        }
        return stripedList;
    }

    /**
     * This method sets a imixs:item extension element.
     * <p>
     * If the bpmnElement node not yet have any extension, the the method will
     * create one.
     * <p>
     * If no item extension with the given name exists, the method will generate
     * one.
     * <p
     * If the itemName is null or empty an existing item extension node will be
     * removed.
     * 
     * @param bpmnElement
     * @param itemName
     * @param type
     * @param value
     */
    public static void setItemValue(final BPMNModel model, final Element elementNode, final String itemName,
            final String type,
            final String value) {

        Element extensionElement = model.findChildNodeByName(elementNode,
                BPMNNS.BPMN2, "extensionElements");

        boolean isNew = false;
        // if no extensionElement exists we create one
        if (extensionElement == null) {
            extensionElement = model.createElement(BPMNNS.BPMN2, "extensionElements");
            isNew = true;
        }

        // now search for the matching item....
        Element item = findItemByName(extensionElement, itemName);
        if (item != null) {
            // if the value is null or empty the item node will be removed
            if (value == null || value.isEmpty()) {
                extensionElement.removeChild(item);
            }
        } else {
            // item does not exits
            // we only create one if a value is given
            if (value != null && !value.isEmpty()) {
                // <imixs:item name="user.name" type="xs:string">John</imixs:item>
                item = model.getDoc().createElementNS(getNamespaceURI(), getNamespace() + ":item");
                extensionElement.appendChild(item);
            }
        }

        // update the item...
        if (item != null) {
            item.setAttribute("name", itemName);
            item.setAttribute("type", type);

            // remove all childs
            while (item.hasChildNodes()) {
                item.removeChild(item.getFirstChild());
            }

            Element valueElement = createItemValueElement(model);
            // update the item content
            CDATASection cdataSection = model.getDoc().createCDATASection(value);
            valueElement.appendChild(cdataSection);
            item.appendChild(valueElement);

            // if we have a file:// link than we create an additional open-bpmn attribute
            if (value.startsWith("file://")) {
                valueElement.setAttribute("open-bpmn:file-link", value);
            } else {
                valueElement.removeAttribute("open-bpmn:file-link");
            }

        }

        if (isNew) {
            // lazy creation
            // elementNode.appendChild(extensionElement);

            elementNode.insertBefore(extensionElement, elementNode.getFirstChild());
        }
    }

    /**
     * This method sets a imixs:item extension element with a Value List. The method
     * creates an imixs:value entry for each list entry.
     * <p>
     * If the bpmnElement node not yet have any extension, the the method will
     * create one.
     * <p>
     * If no item extension with the given name exists, the method will generate
     * one.
     * <p
     * If the itemName is null or empty an existing item extension node will be
     * removed.
     *
     * An optional 'referenceList' can be provided (e.g. the Actor Mapping List). If
     * an value is not part of the referenceList, than the value will not
     * be set! This is to avoid holding old field mappings. See Issue #18
     * 
     * 
     * @param elementNode
     * @param itemName
     * @param type
     * @param valueList     - new valueList
     * @param referenceList - optional list of allowed values
     */
    public static void setItemValueList(final BPMNModel model, final Element elementNode, final String itemName,
            final String type, final List<String> valueList, List<String> referenceList) {

        Element extensionElement = model.findChildNodeByName(elementNode,
                BPMNNS.BPMN2, "extensionElements");

        boolean isNew = false;
        // if no extensionElement exists we create one
        if (extensionElement == null) {
            extensionElement = model.createElement(BPMNNS.BPMN2, "extensionElements");
            isNew = true;
        }

        // now search for the matching item....
        Element item = findItemByName(extensionElement, itemName);
        if (item != null) {
            // if the value is null or empty the item node will be removed
            if (valueList == null || valueList.isEmpty()) {
                extensionElement.removeChild(item);
            }
        } else {
            // item does not exits
            // we only create one if a value is given
            if (valueList != null && !valueList.isEmpty()) {
                // <imixs:item name="user.name" type="xs:string">John</imixs:item>
                item = model.getDoc().createElementNS(getNamespaceURI(), getNamespace() + ":item");
                extensionElement.appendChild(item);
            }
        }

        // update the item...
        if (item != null) {
            item.setAttribute("name", itemName);
            item.setAttribute("type", type);

            // remove all childs
            while (item.hasChildNodes()) {
                item.removeChild(item.getFirstChild());
            }

            // create a imixs:value tag for each value in the list
            for (String value : valueList) {
                if (referenceList != null && !referenceList.contains(value)) {
                    // not in our reference list!
                    continue;
                }
                Element valueElement = createItemValueElement(model);
                // update the item content
                CDATASection cdataSection = model.getDoc().createCDATASection(value);
                valueElement.appendChild(cdataSection);
                // valueElement.setTextContent(value);
                item.appendChild(valueElement);
            }
        }

        if (isNew) {
            // lazy creation
            // elementNode.appendChild(extensionElement);

            elementNode.insertBefore(extensionElement, elementNode.getFirstChild());
        }
    }

    /**
     * This method removes a imixs:item extension element if it exits.
     * <p>
     * If the bpmnElement node not yet have any extension, the the method will
     * change nothing.
     * 
     * @param bpmnElement
     * @param itemName
     * @param type
     * @param value
     */
    public static void removeItemValue(final BPMNModel model, final Element elementNode, final String itemName) {

        Element extensionElement = model.findChildNodeByName(elementNode,
                BPMNNS.BPMN2, "extensionElements");

        // if no extensionElement exists exit
        if (extensionElement == null) {
            return;
        }

        // now search for the matching item....
        Element item = findItemByName(extensionElement, itemName);
        if (item != null) {
            // remove the item node
            extensionElement.removeChild(item);
        }
    }

    /**
     * Creates an imixs:value element
     * 
     * <pre>{@code<imixs:value><![CDATA[model-1.0]]></imixs:value>}</pre>
     */
    public static Element createItemValueElement(BPMNModel model) {
        Element element = model.getDoc().createElementNS(getNamespaceURI(), getNamespace() + ":value");
        return element;
    }

    public static List<String> getItemValueList(final BPMNModel model, final Element elementNode, String itemName) {
        return getItemValueList(model, elementNode, itemName, null);
    }

    /**
     * This helper method returns a value list of all imixs:value elements of an
     * imixs:item by a given name.
     * If no extensionElement exists, or no imxis:item with the itemName exists,
     * than the method returns an empty List
     * 
     * The method also avoids duplicates as this can of course not be handled by the
     * react component.
     * 
     * An optional 'referenceList' can be provided (e.g. the Actor Mapping List). If
     * an existing value is not part of the referenceList, than the value will not
     * be set! This is to avoid holding old field mappings. See Issue #18
     * 
     * @param itemName      - name of the item
     * @param referenceList - optional list of allowed values
     * @return the itemValue list.
     */
    public static List<String> getItemValueList(final BPMNModel model, final Element elementNode, String itemName,
            List<String> referenceList) {
        Element extensionElement = model.findChildNodeByName(elementNode, BPMNNS.BPMN2, "extensionElements");
        List<String> uniqueValueList = new ArrayList<>();

        List<String> result = new ArrayList<>();
        if (extensionElement != null) {
            // first find the matching imixs:item
            Element imixsItemElement = findItemByName(extensionElement, itemName);
            if (imixsItemElement != null) {
                // now iterate over all item:values and add each value into the list
                // <imixs:value><![CDATA[form_basic]]></imixs:value>
                Set<Element> imixsValueElements = findAllImixsElements(imixsItemElement, "value");
                if (imixsValueElements != null) {
                    for (Element imixsItemValue : imixsValueElements) {
                        String value = null;
                        // we expect a CDATA, bu we can not be sure
                        Node cdata = findCDATA(imixsItemValue);
                        if (cdata != null) {
                            String cdValue = cdata.getNodeValue();
                            if (cdValue != null) {
                                value = cdValue;
                            }
                        } else {
                            // normal text node
                            value = imixsItemValue.getTextContent();
                        }

                        // avoid duplicates
                        if (value.contains("|")) {
                            String valuePart = value.substring(value.indexOf("|") + 1).trim();
                            if (uniqueValueList.contains(valuePart)) {
                                continue;
                            }
                            uniqueValueList.add(valuePart);
                        } else {
                            if (uniqueValueList.contains(value)) {
                                continue;
                            }
                            uniqueValueList.add(value);
                        }

                        // add value - it is now unique!
                        if (referenceList == null || referenceList.contains(value)) {
                            result.add(value);
                        }
                    }

                }

            }
        }
        // no item found with this item name - return an empty list
        return result;
    }

    /**
     * This helper method returns the first imixs:value within a imixs:item as a
     * String. If no item with the given name exists, or the item has no values, the
     * method returns an empty string.
     * <p>
     * 
     * @param bpmnElement
     * @param itemName
     * @return string value of the first imixs:value in a imixs:item
     */
    public static String getItemValueString(final BPMNModel model, final Element elementNode, String itemName) {

        return getItemValueString(model, elementNode, itemName, "");
    }

    /**
     * This helper method returns the first imixs:value within a imixs:item as a
     * String. If no item with the given name exists, or the item has no values, the
     * method returns the given default string.
     * <p>
     * 
     * @param bpmnElement
     * @param itemName
     * @param defaultValue - optional default value
     * @return string value of the first imixs:value in a imixs:item
     */
    public static String getItemValueString(final BPMNModel model, final Element elementNode, String itemName,
            String defaultValue) {
        List<String> valueList = getItemValueList(model, elementNode, itemName, null);
        if (valueList != null && valueList.size() > 0) {
            return valueList.get(0);
        }
        return defaultValue;
    }

    public static Boolean getItemValueBoolean(final BPMNModel model, final Element elementNode, String itemName) {

        return Boolean.parseBoolean(getItemValueString(model, elementNode, itemName, "true"));
    }

    /**
     * Helper method that finds a extension item by name
     * <p>
     * 
     * <pre>{@code<imixs:item name="ITEMNAME" type="xs:string">}</pre>
     * 
     * @param extensionElement
     * @param itemName
     * @return
     */
    private static Element findItemByName(Element extensionElement, String itemName) {

        Set<Element> itemElements = findAllImixsElements(extensionElement, "item");
        // iterate through set and verify the name attribute
        for (Element item : itemElements) {
            if (itemName.equals(item.getAttribute("name"))) {
                // match!
                return item;
            }
        }
        return null;
    }

    /**
     * This helper method returns a set of all imixs:ELEMENTS for the given parent
     * node. If no nodes were found, the method returns an empty list.
     * <p>
     * The type can be either 'item' or 'value'
     * 
     * Example:
     * 
     * <pre>{@code<imixs:item name="user.name" type=
     * "xs:string">John</imixs:item>}</pre>
     * 
     * @param parent
     * @param nodeName
     * @return - list of nodes. If no nodes were found, the method returns an empty
     *         list
     */
    private static Set<Element> findAllImixsElements(Element parent, String type) {
        Set<Element> result = new LinkedHashSet<Element>();
        // resolve the tag name
        String tagName = getNamespace() + ":" + type;
        if (parent != null) {
            NodeList childs = parent.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node childNode = childs.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE && tagName.equals(childNode.getNodeName())) {
                    result.add((Element) childNode);
                }
            }
        }
        return result;
    }

    /**
     * Helper method that finds an optional CDATA node within the current element
     * content.
     * 
     * @param element
     * @return
     */
    private static Node findCDATA(Element element) {
        // search CDATA node
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof CDATASection) {
                return (CDATASection) node;
            }
        }
        return null;
    }

}
