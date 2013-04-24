package org.cmdbuild.cql;

/**
 * Adaptor class for CQLBuilderListener interface. All method are empty.
 */
public class CQLBuilderAdaptor implements CQLBuilderListener {

	public void addFromClass(String className, String classAs) {
	}

	public void addFromClass(int classId, String classAs) {
	}

	public void addGroupByElement(String classDomainReference,
			String attributeName) {
	}

	public void addOrderByElement(String classDomainReference,
			String attributeName, OrderByType type) {
	}

	public void addSelectAttribute(String attributeName, String attributeAs,
			String classNameOrReference) {
	}

	public void defaultGroupBy() {
	}

	public void defaultLimit() {
	}

	public void defaultOffset() {
	}

	public void defaultOrderBy() {
	}

	public void defaultSelect() {
	}

	public void defaultWhere() {
	}

	public void endDomain() {
	}

	public void endDomainMeta() {
	}

	public void endDomainObjects() {
	}

	public void endDomainRef() {
	}

	public void endExpression() {
	}

	public void endField() {
	}

	public void endFrom() {
	}

	public void endFromDomain() {
	}

	public void endGroup() {
	}

	public void endGroupBy() {
	}

	public void endOrderBy() {
	}

	public void endSelect() {
	}

	public void endSelectFromClass() {
	}

	public void endSelectFromDomain() {
	}

	public void endSelectFromDomainMeta() {
	}

	public void endSelectFromDomainObjects() {
	}

	public void endSelectFunction() {
	}

	public void endValue() {
	}

	public void endWhere() {
	}

	public void globalEnd() {
	}

	public void globalStart() {
	}

	public void selectAll() {
	}

	public void setLimit(int limit) {
	}

	public void setLimit(FieldInputValue limit) {
	}

	public void setOffset(int offset) {
	}

	public void setOffset(FieldInputValue offset) {
	}

	public void startComplexField(WhereType type, boolean isNot,
			String classOrDomainNameOrRef, String[] fieldPath,
			FieldOperator operator) {
	}

	public void startDomain(WhereType type, String scopeReference,
			String domainName, DomainDirection direction, boolean isNot) {
	}

	public void startDomain(WhereType type, String scopeReference,
			int domainId, DomainDirection direction, boolean isNot) {
	}

	public void startDomainMeta() {
	}

	public void startDomainObjects() {
	}

	public void startDomainRef(WhereType type, String domainReference,
			boolean isNot) {
	}

	public void startExpression() {
	}

	public void startFrom(boolean history) {
	}

	public void startFromDomain(String scopeReference, String domainName,
			String domainAs, DomainDirection direction) {
	}

	public void startFromDomain(String scopeReference, int domainId,
			String domainas, DomainDirection direction) {
	}

	public void startGroup(WhereType type, boolean isNot) {
	}

	public void startGroupBy() {
	}

	public void startLookupField(WhereType type, boolean isNot,
			String classOrDomainNameOrRef, String fieldId,
			LookupOperator[] lookupPath, FieldOperator operator) {
	}

	public void startOrderBy() {
	}

	public void startSelect() {
	}

	public void startSelectFromClass(String classNameOrReference) {
	}

	public void startSelectFromDomain(String domainNameOrReference) {
	}

	public void startSelectFromDomainMeta() {
	}

	public void startSelectFromDomainObjects() {
	}

	public void startSelectFunction(String functionName, String functionAs) {
	}

	public void startSimpleField(WhereType type, boolean isNot,
			String classOrDomainNameOrRef, String fieldId,
			FieldOperator operator) {
	}

	public void startValue(FieldValueType type) {
	}

	public void startWhere() {
	}

	public void value(Object o) {
	}

}
