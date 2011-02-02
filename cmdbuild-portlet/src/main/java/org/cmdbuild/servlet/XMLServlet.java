package org.cmdbuild.servlet;

import org.cmdbuild.portlet.operation.ServletOperation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.cmdbuild.portlet.configuration.CardConfiguration;
import org.cmdbuild.portlet.configuration.GridConfiguration;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.operation.CardOperation;
import org.cmdbuild.portlet.operation.GridOperation;
import org.cmdbuild.portlet.operation.LookupOperation;
import org.cmdbuild.portlet.utils.CardUtils;
import org.cmdbuild.portlet.utils.GridUtils;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.FilterOperator;
import org.cmdbuild.services.soap.Lookup;
import org.cmdbuild.services.soap.Order;
import org.cmdbuild.services.soap.Query;

public class XMLServlet extends HttpServlet {

    private GridOperation goperation;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletOperation operations = new ServletOperation();
        HttpSession session = request.getSession();
        SOAPClient client = operations.getClient(session);
        Log.PORTLET.debug("Context path XML servlet: " + request.getContextPath());
        goperation = new GridOperation((String) session.getAttribute("contextPath"));
        CardUtils cardUtils = new CardUtils();
        CardConfiguration cardConfig = cardUtils.getCardConfiguration(request);
        GridUtils gridUtils = new GridUtils();
        GridConfiguration gridConfig = gridUtils.getGridConfiguration(request);
        response.setContentType("text/xml");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        PrintWriter out = response.getWriter();
        StringBuilder xmlResponse = new StringBuilder();
        xmlResponse.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<rows>")
                .append("<page>" + gridConfig.getPage() + "</page>");
        CardList cardlist;
        LookupOperation operation = new LookupOperation(client);
        List<AttributeSchema> schema = operation.getAttributeList(cardConfig.getClassname());
        List<Lookup> processLookup = null;
        if (cardConfig.getType().equals(GridOperation.PROCESS_CLASS)) {
            processLookup = operation.getLookupList(GridOperation.PROCESS_FLOW_STATUS);
            cardlist = getProcessInfo(cardConfig, gridConfig, schema, processLookup);
        } else {
            cardlist = goperation.getCardInfo(cardConfig, gridConfig, null);
        }
        if (cardlist != null && cardlist.getCards() != null && cardlist.getTotalRows() > 0) {
            xmlResponse.append(goperation.getXML(cardlist, schema, cardConfig.getType(), processLookup, gridConfig));
        } else {
            xmlResponse.append("<total>0</total>\n");
        }
        xmlResponse.append("</rows>");
        out.print(xmlResponse.toString());
        response.getWriter().flush();
        response.getWriter().close();
    }

    private CardList getProcessInfo(CardConfiguration cardConfig, GridConfiguration gridConfig, List<AttributeSchema> schema, List<Lookup> processLookup) {
        CardOperation operation = new CardOperation(gridConfig.getClient());
        int lookupid = 0;
        for (Lookup l : processLookup) {
            if (l.getId() > 0) {
                if (l.getCode().equals(cardConfig.getFlowstatus())) {
                    lookupid = l.getId();
                }
            } else {
                lookupid = -1;
            }
        }

        Query query = generateProcessQuery(processLookup, lookupid);
        List<Order> orders = generateProcessOrder(gridConfig, schema);

        CardList cards;
        if (orders!= null && orders.size() > 0) {
            cards = operation.getCardList(cardConfig.getClassname(), null, query, orders, gridConfig.getMaxResult(), gridConfig.getStartIndex(), gridConfig.getFullTextQuery(), null);
        } else {
            cards = operation.getCardList(cardConfig.getClassname(), null, query, null, gridConfig.getMaxResult(), gridConfig.getStartIndex(), gridConfig.getFullTextQuery(), null);
        }

        return cards;

    }

    private List<Order> generateProcessOrder(GridConfiguration gridConfig, List<AttributeSchema> schema) {
        List<Order> orders = new LinkedList<Order>();

        if (gridConfig.getSortname().equals("")) {
            orders = goperation.generateOrder(schema);
        } else {
            Order order = new Order();
            order.setColumnName(gridConfig.getSortname());
            if (gridConfig.getSortorder().equalsIgnoreCase("ASC")) {
                order.setType("ASC");
            } else {
                order.setType("DESC");
            }
            orders.add(order);
        }
        return orders;
    }

    private Query generateProcessQuery(List<Lookup> lookups, int lookupid) {
        Query processQuery = getProcessQuery(lookups, lookupid);
        Query query = new Query();
        FilterOperator fo = new FilterOperator();
        fo.getSubquery().add(processQuery);
        fo.setOperator("AND");
        query.setFilterOperator(fo);
        return query;
    }

    private Query getProcessQuery(List<Lookup> lookups, int lookupid) {

        FilterOperator operator;
        Query flowfilterquery = new Query();
        Filter flowfilter = generateFlowStatusFilter(lookups, lookupid);
        flowfilterquery.setFilter(flowfilter);

        Query flowstatusquery = new Query();
        Filter status = new Filter();
        status.getValue().add("A");
        status.setOperator("EQUALS");
        status.setName("Status");
        flowstatusquery.setFilter(status);

        operator = new FilterOperator();
        operator.setOperator("AND");
        operator.getSubquery().add(flowstatusquery);
        operator.getSubquery().add(flowfilterquery);

        Query query = new Query();
        query.setFilterOperator(operator);
        return query;

    }

    private Filter generateFlowStatusFilter(List<Lookup> lookups, int lookupid) {
        Filter flowStatusFilter = new Filter();
        if (lookupid > 0) {
            flowStatusFilter.getValue().add(String.valueOf(lookupid));
            flowStatusFilter.setOperator("EQUALS");
            flowStatusFilter.setName(GridOperation.PROCESS_FLOW_STATUS);
        } else {
            for (Lookup lookup : lookups) {
                if ("closed.terminated".equals(lookup.getCode()) || "closed.aborted".equals(lookup.getCode())) {
                    continue;
                } else {
                    flowStatusFilter.getValue().add(String.valueOf(lookup.getId()));
                }
            }
            flowStatusFilter.setOperator("EQUALS");
            flowStatusFilter.setName(GridOperation.PROCESS_FLOW_STATUS);
        }
        return flowStatusFilter;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
