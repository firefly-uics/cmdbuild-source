package org.cmdbuild.portlet.operation;

import java.util.LinkedList;
import java.util.List;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.metadata.CMDBuildTagDictionary;
import org.cmdbuild.portlet.plugin.CMPortletPlugin;
import org.cmdbuild.portlet.plugin.CMPortletPluginLibrary;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.Card;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.CqlQuery;
import org.cmdbuild.services.soap.Lookup;
import org.cmdbuild.services.soap.Metadata;
import org.cmdbuild.services.soap.Order;

public class GridOperation {

    public static final String PROCESS_CLASS = "processclass";
    public static final String PRIVILEGE_READ = "read";
    public static final String PROCESS_FLOW_STATUS = "FlowStatus";
    public static final int DEFAULT_BUTTON_WIDTH = 20;
    private String contextPath;

    public GridOperation(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getXML(CardList list, List<AttributeSchema> schema, String type, List<Lookup> processLookup, GridConfiguration gridConfig) {
        String result = "";
        if (list != null) {
            int total = list.getTotalRows();
            result = "<total>" + total + "</total>\n";
            for (Card card : list.getCards()) {
                StringBuilder cardBuilder = new StringBuilder();
                cardBuilder.append("<row id=\'").append(card.getId()).append("\'>\n");
                cardBuilder.append("<cell><![CDATA[").append(card.getId()).append("]]></cell>\n");
                cardBuilder.append(serializeCell(card.getAttributeList(), schema));
                cardBuilder.append(serializeGridButtons(type, card, processLookup, gridConfig));
                cardBuilder.append("</row>\n");
                result = result + cardBuilder.toString();
            }
        } else {
            result = "<total>" + 0 + "</total>\n";
            result = result + "<row id=\'" + 0 + "\'>\n";
            result = result + "</row>\n";
        }
        return result;
    }

    public String serializeCell(List<Attribute> attrs, List<AttributeSchema> listSchema) {
        String result = "";
        for (AttributeSchema as : listSchema) {
            if ((PortletConfiguration.getInstance().displayOnlyBaseDSP() && as.isBaseDSP())
                    || !PortletConfiguration.getInstance().displayOnlyBaseDSP()) {
                if (attrs != null && attrs.size() > 0) {
                    for (Attribute attribute : attrs) {
                        String name = as.getName();
                        String aname = attribute.getName();
                        String avalue = attribute.getValue();
                        if (aname.equals(name)) {
                            result = result + "<cell><![CDATA[" + avalue + "]]></cell>\n";
                        }
                    }
                }
            }
        }
        return result;
    }

    private String serializeGridButtons(String type, Card card, List<Lookup> processLookup, GridConfiguration gridConfig) {
        StringBuilder buttons = new StringBuilder();
        buttons.append("<cell><![CDATA[");
        for (CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
            String customButtons = plugin.serializeGridButtons(type, card, processLookup, gridConfig, contextPath);
            if (customButtons != null && customButtons.length() > 0) {
                buttons.append(customButtons);
            }
        }
        buttons.append(serializeDefaultGridButtons(type, card, processLookup, gridConfig));
        buttons.append("]]></cell>\n");
        return buttons.toString();
    }

    private String serializeDefaultGridButtons(String type, Card card, List<Lookup> processLookup, GridConfiguration gridConfig) {
        StringBuilder buttons = new StringBuilder();
        if (type.contains("process")) {
            buttons.append(generateProcessXml(card, processLookup, gridConfig));
        }
        buttons.append(" <img class=\"CMDBuildGridButton\" src=\"").append(contextPath).append("/css/images/hourglass.png\"").append(" alt=\"Storia\" title=\"Storia\" ").append(String.format("onclick=\"CMDBuildShowHistory('%s', '%d', '%s')\" ", card.getClassName(), card.getId(), type)).append("/> ");
        return buttons.toString();
    }

    private String generateProcessXml(Card card, List<Lookup> processLookup, GridConfiguration gridConfig) {
        String privilege = getPrivilegeMetadata(card.getMetadata());
        StringBuilder result = new StringBuilder();

        if (gridConfig.isShowEmailColumn()) {
            result.append(serializeEmailCell(card, processLookup));
        }
        if (gridConfig.isAdvanceProcess() && PortletConfiguration.getInstance().displayStartProcess()) {
            result.append(serializeAdvanceProcessCell(card, processLookup, privilege));
        }
        if (gridConfig.isDisplayDetailColumn()) {
            result.append(serializeDetailCell(card));
        }
        if (gridConfig.isDisplayAttachmentList()) {
            result.append(serializeAttachmentList(card));
        }
        return result.toString();
    }

    private String getPrivilegeMetadata(List<Metadata> metadata) {
        String privilege = null;
        if (metadata != null) {
            for (Metadata meta : metadata) {
                if (meta != null) {
                    if (meta.getKey().equals(CMDBuildTagDictionary.PRIVILEGES)) {
                        privilege = meta.getValue();
                    }
                }
            }
        }
        return privilege;
    }

    public String serializeEmailCell(Card card, List<Lookup> processLookup) {
        if (!isProcessCompleted(card, getCompletedProcessLookupId(processLookup))) {
            return String.format("<span class=\"CMDBuildGridButton\"><img src=\"%s/css/images/email.png\" alt=\"Email\" title=\"Email\" onclick=\"CMDBuildShowEmail(\'%s\',\'%s\')\"/></span> ", contextPath, card.getClassName(), card.getId());
        } else {
            return String.format("<img src=\"%s/css/images/email.png\" alt=\"Email\" title=\"Email\" class=\"CMDBuildDisabled\"/> ", contextPath);
        }
    }

    public String serializeAdvanceProcessCell(Card card, List<Lookup> processLookup, String privilege) {
        int processCompletedId = getCompletedProcessLookupId(processLookup);
        if (!isProcessCompleted(card, processCompletedId) && privilege != null && !privilege.equals(PRIVILEGE_READ)) {
            return String.format(" <span class=\"CMDBuildGridButton\"><img src=\"%s/css/images/pencil_go.png\" alt=\"Avanza processo\" title=\"Avanza processo\" onclick=\"CMDBuildAdvanceProcess(\'%s\',\'%s\')\"/></span> ", contextPath, card.getClassName(), card.getId());
        } else {
            return String.format(" <img src=\"%s/css/images/pencil_go.png\" alt=\"Avanza processo\" title=\"Avanza processo\" class=\"CMDBuildDisabled\"/> ", contextPath);
        }
    }

    public String serializeDetailCell(Card card) {
        return String.format(" <span class=\"CMDBuildGridButton\"><img src=\"%s/css/images/zoom.png\" "
                + "alt=\"Dettaglio\" title=\"Dettaglio\" "
                + "onclick=\"CMDBuildShowElementDetail(\'0\', \'%s\',\'%s\', \'%s\')\"/></span> ", contextPath, card.getClassName(), card.getId(), PROCESS_CLASS);
    }

    public String serializeAttachmentList(Card card) {
        return String.format(" <span class=\"CMDBuildGridButton\"><img src=\"%s/css/images/attach.png\" "
                + "alt=\"Allegati\" title=\"Allegati\" "
                + "onclick=\"CMDBuildShowAttachmentList(\'%s\', \'%s\')\"/></span> ", contextPath, card.getClassName(), card.getId());
    }

    private int getCompletedProcessLookupId(List<Lookup> processLookup) {
        int processCompletedLookupId = 0;
        if (processLookup != null) {
            for (Lookup lookup : processLookup) {
                if ("closed.completed".equals(lookup.getCode())) {
                    processCompletedLookupId = lookup.getId();
                }
            }
        }
        return processCompletedLookupId;
    }

    public String generateGridHeaders(AttributeSchema as, String colModel, String index) {
        boolean visible = as.isBaseDSP();
        boolean displayOnlyBaseDSP = PortletConfiguration.getInstance().displayOnlyBaseDSP();
        int size = getSpecificColumnSize(as);
        FieldType fieldType = FieldType.valueOf(as.getType());
        boolean isFixed = fieldType.isFixed();
        if (!visible && displayOnlyBaseDSP) {
            return colModel;
        }
        if (!visible && !displayOnlyBaseDSP) {
            index = "{display:\'" + as.getDescription() + "\', name:\'" + as.getName() + "\', index:\'" + as.getName() + "\', width:\'" + size + "\', fixed: " + isFixed + ", sortable: true, hide : true}";
        } else if (visible) {
            index = "{display:\'" + as.getDescription() + "\', name:\'" + as.getName() + "\', index:\'" + as.getName() + "\', width:\'" + size + "\', fixed: " + isFixed + ", sortable: true}";
        }
        if (colModel.equals("")) {
            colModel = colModel + index;
        } else {
            colModel = colModel + "," + index;
        }
        return colModel;
    }

    private int getSpecificColumnSize(AttributeSchema as) {
        try {
            FieldType fieldType = FieldType.valueOf(as.getType());
            return fieldType.getHeaderLength(as);
        } catch (IllegalArgumentException e) {
            return 40;
        }
    }

    public String generateButtonHeaders(String colModel) {
        int width = getButtonAreaWidth();
        return colModel = colModel + ", " + String.format("{display: \'\', name: \'\', width:%d, fixed: true, sortable: false}", width);
    }

    private int getButtonAreaWidth() {
        int width = 0;
        width = PortletConfiguration.getInstance().displayAdvanceProcess() ? (width += DEFAULT_BUTTON_WIDTH) : (width += 0);
        width = PortletConfiguration.getInstance().displayDetailColumn() ? (width += DEFAULT_BUTTON_WIDTH) : (width += 0);
        width = PortletConfiguration.getInstance().displayEmailColumn() ? (width += DEFAULT_BUTTON_WIDTH) : (width += 0);
        width = PortletConfiguration.getInstance().displayAttachmentList() ? (width += DEFAULT_BUTTON_WIDTH) : (width += 0);
        width = PortletConfiguration.getInstance().displayHistory() ? (width += DEFAULT_BUTTON_WIDTH) : (width += 0);
        for (CMPortletPlugin plugin : CMPortletPluginLibrary.getPlugins()) {
            for (int i = 0; i < plugin.customGridButtonsLength(); i++) {
                width += DEFAULT_BUTTON_WIDTH;
            }
        }
        return width;
    }

    public CardList getCardInfo(CardConfiguration cardConfig, GridConfiguration gridConfig, CqlQuery cqlQuery) {
        CardOperation operation = new CardOperation(gridConfig.getClient());
        List<AttributeSchema> schema = operation.getAttributeList(cardConfig.getClassname());
        List<Order> orders = generateCardOrder(gridConfig.getSortname(), schema, gridConfig.getSortorder());

        CardList cards;
        if (orders!= null && orders.size() > 0) {
            cards = operation.getCardList(cardConfig.getClassname(), null, null, orders, gridConfig.getMaxResult(), gridConfig.getStartIndex(), gridConfig.getFullTextQuery(), cqlQuery);
        } else {
            cards = operation.getCardList(cardConfig.getClassname(), null, null, null, gridConfig.getMaxResult(), gridConfig.getMaxResult(), gridConfig.getFullTextQuery(), cqlQuery);
        }
        return cards;
    }

    private List<Order> generateCardOrder(String sortname, List<AttributeSchema> schema, String sortorder) {
        List<Order> orders = new LinkedList<Order>();
        if (sortname.equals("")) {
            orders = generateOrder(schema);
        } else {
            Order order = new Order();
            order.setColumnName(sortname);
            if (sortorder.equalsIgnoreCase("ASC")) {
                order.setType("ASC");
            } else {
                order.setType("DESC");
            }
            Log.PORTLET.debug("Ordering colum " + order.getColumnName() + " with order " + order.getType().toString());
            orders.add(order);
        }
        return orders;
    }

    public List<Order> generateOrder(List<AttributeSchema> schema) {
        List<Order> orderList = new LinkedList<Order>();
        for (AttributeSchema attribute : schema) {
            int classorder = attribute.getClassorder();
            Order order = new Order();
            order.setColumnName(attribute.getName());
            if (classorder > 0) {
                order.setType("ASC");
            } else if (classorder < 0) {
                order.setType("DESC");
            } else {
                continue;
            }
            Log.PORTLET.debug("Ordering colum " + order.getColumnName() + " with order " + order.getType().toString());
            orderList.add(order);
        }
        return orderList;
    }

    private boolean isProcessCompleted(Card card, int processCompletedLookupId) {
        int cardFlowStatus = 0;
        if (card != null && processCompletedLookupId > 0) {
            for (Attribute attribute : card.getAttributeList()) {
                if (PROCESS_FLOW_STATUS.equals(attribute.getName())) {
                    cardFlowStatus = Integer.valueOf(attribute.getCode());
                }
            }
        }
        if (cardFlowStatus == processCompletedLookupId) {
            return true;
        } else {
            return false;
        }
    }
}
