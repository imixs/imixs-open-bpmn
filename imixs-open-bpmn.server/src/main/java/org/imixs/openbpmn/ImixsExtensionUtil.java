package org.imixs.openbpmn;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openbpmn.bpmn.BPMNNS;
import org.openbpmn.bpmn.elements.core.BPMNElement;
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
    public static void setItemValue(final BPMNElement bpmnElement, final String itemName, final String type,
            final String value) {

        Element extensionElement = bpmnElement.getModel().findChildNodeByName(bpmnElement.getElementNode(),
                BPMNNS.BPMN2, "extensionElements");

        boolean isNew = false;
        // if no extensionElement exists we create one
        if (extensionElement == null) {
            extensionElement = bpmnElement.getModel().createElement(BPMNNS.BPMN2, "extensionElements");
            isNew = true;

            // reload
            // extensionElement =
            // BPMNModel.findChildNodeByName(bpmnElement.getElementNode(),
            // "extensionElements");
        }

        // now search for the matching item....
        Element item = findItemByName(extensionElement, itemName);
        // iterate through set and verify the name attribute
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
                item = bpmnElement.getModel().getDoc().createElementNS(getNamespaceURI(), getNamespace() + ":item");
                extensionElement.appendChild(item);
            }
        }

        // update item...
        if (item != null) {
            item.setAttribute("name", itemName);
            item.setAttribute("type", type);
            // update the item content
            item.setTextContent(value);
        }

        if (isNew) {
            // lazy creation
            bpmnElement.getElementNode().appendChild(extensionElement);
        }
    }

    /**
     * This helper method returns a value list of all imixs:value elements of an
     * imixs:item by a given name.
     * If no extensionElement exists, or no imxis:item with the itemName exists,
     * than the method returns an empty List
     * 
     * @param itemName
     * @return the itemValue list.
     */
    public static List<String> getItemValueList(final BPMNElement bpmnElement, String itemName) {
        Element extensionElement = bpmnElement.getModel().findChildNodeByName(bpmnElement.getElementNode(),
                BPMNNS.BPMN2, "extensionElements");

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
                        // we expect a CDATA, bu we can not be sure
                        Node cdata = findCDATA(imixsItemValue);
                        if (cdata != null) {
                            String cdValue = cdata.getNodeValue();
                            if (cdValue != null) {
                                result.add(cdValue);
                            }
                        } else {
                            // normal text node
                            result.add(imixsItemValue.getTextContent());
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
     * This is just a convenience method to avoid validating the value list
     * 
     * @param bpmnElement
     * @param itemName
     * @return string value of the first imixs:value in a imixs:item
     */
    public static String getItemValueString(final BPMNElement bpmnElement, String itemName) {
        List<String> valueList = getItemValueList(bpmnElement, itemName);
        if (valueList != null && valueList.size() > 0) {
            return valueList.get(0);
        }
        return "";
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
