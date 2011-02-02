package org.cmdbuild.portlet.configuration;


public class LinkCardItem {

    private String classname;
    private int singleSelect;
    private int noSelect;
    private int required;
    private String filter;
    private String identifier;
    private String label;

    public LinkCardItem() {}

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getNoSelect() {
        return noSelect;
    }

    public void setNoSelect(int noSelect) {
        this.noSelect = noSelect;
    }

    public int getRequired() {
        return required;
    }

    public void setRequired(int required) {
        this.required = required;
    }

    public int getSingleSelect() {
        return singleSelect;
    }

    public void setSingleSelect(int singleSelect) {
        this.singleSelect = singleSelect;
    }
}
