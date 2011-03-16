package org.cmdbuild.portlet.configuration;

import org.cmdbuild.portlet.ws.SOAPClient;

public class GridConfiguration {

	private int page;
	private int maxResult;
	private int startIndex;
	private boolean showEmailColumn;
	private boolean advanceProcess;
	private boolean displayDetailColumn;
	private boolean displayOnlyBaseDSP;
	private boolean displayHistory;
	private boolean displayAttachmentList;
	private String query;
	private String fullTextQuery;
	private String sortname;
	private String sortorder;
	private SOAPClient client;

	public boolean isAdvanceProcess() {
		return advanceProcess;
	}

	public void setAdvanceProcess(final boolean advanceProcess) {
		this.advanceProcess = advanceProcess;
	}

	public boolean isDisplayDetailColumn() {
		return displayDetailColumn;
	}

	public void setDisplayDetailColumn(final boolean displayDetailColumn) {
		this.displayDetailColumn = displayDetailColumn;
	}

	public int getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(final int maxResult) {
		this.maxResult = maxResult;
	}

	public int getPage() {
		return page;
	}

	public void setPage(final int page) {
		this.page = page;
	}

	public boolean isDisplayOnlyBaseDSP() {
		return displayOnlyBaseDSP;
	}

	public void setDisplayOnlyBaseDSP(final boolean displayOnlyBaseDSP) {
		this.displayOnlyBaseDSP = displayOnlyBaseDSP;
	}

	public String getFullTextQuery() {
		return fullTextQuery;
	}

	public void setFullTextQuery(final String fullTextQuery) {
		this.fullTextQuery = fullTextQuery;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	public boolean isShowEmailColumn() {
		return showEmailColumn;
	}

	public void setShowEmailColumn(final boolean showEmailColumn) {
		this.showEmailColumn = showEmailColumn;
	}

	public boolean isDisplayHistory() {
		return displayHistory;
	}

	public void setDisplayHistory(final boolean displayHistory) {
		this.displayHistory = displayHistory;
	}

	public String getSortname() {
		return sortname;
	}

	public void setSortname(final String sortname) {
		this.sortname = sortname;
	}

	public String getSortorder() {
		return sortorder;
	}

	public void setSortorder(final String sortorder) {
		this.sortorder = sortorder;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(final int startIndex) {
		this.startIndex = startIndex;
	}

	public boolean isDisplayAttachmentList() {
		return displayAttachmentList;
	}

	public void setDisplayAttachmentList(final boolean displayAttachmentList) {
		this.displayAttachmentList = displayAttachmentList;
	}

	public SOAPClient getClient() {
		return client;
	}

	public void setClient(final SOAPClient client) {
		this.client = client;
	}
}
