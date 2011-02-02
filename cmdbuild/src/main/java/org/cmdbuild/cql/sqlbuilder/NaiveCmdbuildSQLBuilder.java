package org.cmdbuild.cql.sqlbuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.cql.CQLBuilderListener.FieldInputValue;
import org.cmdbuild.cql.CQLBuilderListener.FieldOperator;
import org.cmdbuild.cql.CQLBuilderListener.FieldValueType;
import org.cmdbuild.cql.CQLBuilderListener.WhereType;
import org.cmdbuild.cql.compiler.impl.ClassDeclarationImpl;
import org.cmdbuild.cql.compiler.impl.ClassSelectImpl;
import org.cmdbuild.cql.compiler.impl.DomainDeclarationImpl;
import org.cmdbuild.cql.compiler.impl.DomainObjectsReferenceImpl;
import org.cmdbuild.cql.compiler.impl.FieldImpl;
import org.cmdbuild.cql.compiler.impl.FieldSelectImpl;
import org.cmdbuild.cql.compiler.impl.GroupImpl;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.cql.compiler.impl.SelectImpl;
import org.cmdbuild.cql.compiler.impl.WhereImpl;
import org.cmdbuild.cql.compiler.select.SelectElement;
import org.cmdbuild.cql.compiler.select.SelectItem;
import org.cmdbuild.cql.compiler.where.DomainObjectsReference;
import org.cmdbuild.cql.compiler.where.WhereElement;
import org.cmdbuild.cql.compiler.where.Field.FieldValue;
import org.cmdbuild.cql.compiler.where.fieldid.LookupFieldId;
import org.cmdbuild.cql.compiler.where.fieldid.SimpleFieldId;
import org.cmdbuild.cql.compiler.where.fieldid.LookupFieldId.LookupOperatorTree;
import org.cmdbuild.dao.type.SQLQuery;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.filters.AbstractFilter;
import org.cmdbuild.elements.filters.AttributeFilter;
import org.cmdbuild.elements.filters.CompositeFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.SchemaCache;
import org.cmdbuild.services.auth.UserContext;

public class NaiveCmdbuildSQLBuilder {
	private static final String ReferenceSelectTmpl = "(SELECT \"Id\" FROM \"%1\" WHERE %2 and %1.\"Status\"='A')";

	Map<String,CardQuery> cardQueries = new HashMap<String,CardQuery>();
	
	private CardQuery get(String ref) {
		return cardQueries.get(ref);
	}
	private CardQuery getOrCreate(ClassDeclarationImpl cdecl, UserContext userCtx) {
		if(null==get( cdecl.getAs() == null ? cdecl.getName() : cdecl.getAs() )	) {
			String key = (cdecl.getAs() == null) ? cdecl.getName() : cdecl.getAs();
			CardQuery cardQuery = cdecl.getClassTable(userCtx).cards().list();
			cardQueries.put(key, cardQuery);
		}
		return get( cdecl.getAs() == null ? cdecl.getName() : cdecl.getAs() ); //cdecl.getName(),cdecl.getAs());
	}
	private CardQuery getOrCreate(DomainDeclarationImpl ddecl, UserContext userCtx) {
		if(null==get(ddecl.getAs())) {
			CardQuery parent = null;
			if(ddecl.parent() instanceof ClassDeclarationImpl) {
				parent = getOrCreate((ClassDeclarationImpl)ddecl.parent(), userCtx);
			} else {
				parent = getOrCreate((DomainDeclarationImpl)ddecl.parent(), userCtx);
			}
			CardQuery cardQuery = ddecl.getEndClassTable(userCtx).cards().list();
			parent.cardInRelation(ddecl.getDirectedDomain(userCtx), cardQuery);
			cardQueries.put(ddecl.getAs(), cardQuery);
		}
		return cardQueries.get(ddecl.getAs());
	}

	public CardQuery build(QueryImpl q, Map<String,Object> vars,
			UserContext userCtx) {
		return build(q, vars, null, userCtx);
	}
	@SuppressWarnings("unchecked")
	public CardQuery build(QueryImpl q, Map<String,Object> vars,
			CardQuery root, UserContext userCtx) {
		CardQuery out = null;
		if(root == null) {
			out = getOrCreate(q.getFrom().mainClass(), userCtx);
		} else {
			ClassDeclarationImpl cdecl = q.getFrom().mainClass();
			String key = (cdecl.getAs() != null) ? cdecl.getAs() : cdecl.getName();
			cardQueries.put(key, root);
			out = root;
		}
		ITable table = q.getFrom().mainClass().getClassTable(userCtx);
		CompositeFilter flt = new CompositeFilter();
		flt.setGroup(false);
		WhereImpl w = q.getWhere();
		for(WhereElement el : w.getElements()) {
			handleWhereElement(el, flt, table, vars, userCtx);
		}
		
		if(flt.hasItems()) {
			out.filter(flt);
		}
		SelectImpl sel = q.getSelect();
		if(!sel.isDefault()) {
			for(SelectElement elm : sel.getElements()) {
				if(elm instanceof ClassSelectImpl) {
					ClassSelectImpl classSel = (ClassSelectImpl)elm;
					for(SelectItem item : classSel.getElements()) {
						if(item instanceof FieldSelectImpl) {
							FieldSelectImpl field = (FieldSelectImpl)item;
							out.getAttributes().add(field.getName());
						} else {
							Log.WORKFLOW.warn("unsupported select item: " + elm.getClass().getSimpleName());
						}
					}
				} else {
					Log.WORKFLOW.warn("unsupported select element: " + elm.getClass().getSimpleName());
				}
			}
		}
		
		out.subset((Integer)q.getOffsetValue(),(Integer)q.getLimitValue());
		return out;
	}
	
	private void handleWhereElement(WhereElement el,
			CompositeFilter flt, ITable table, Map<String,Object> vars,
			UserContext userCtx) {
		if(el instanceof FieldImpl) {
			Log.WORKFLOW.debug("add field");
			handleField((FieldImpl)el,vars,table,flt);
		} else if(el instanceof DomainObjectsReference) {
			Log.WORKFLOW.debug("add domain objs");
			DomainObjectsReferenceImpl d = (DomainObjectsReferenceImpl)el;
			DomainDeclarationImpl ddecl = (DomainDeclarationImpl)d.getScope();

			ITable domTable = ddecl.getEndClassTable(userCtx);
			CardQuery domFltMngr = getOrCreate(ddecl, userCtx);
			CompositeFilter domFlt = new CompositeFilter();
			CompositeFilter domObjsFlt = null;
			if(d.isNot()) {
				Log.WORKFLOW.debug("domain objs is not");
				domObjsFlt = new CompositeFilter();
				domFlt.first(true, domObjsFlt);
			} else {
				domObjsFlt = domFlt;
			}
			
			for(WhereElement domel : d.getElements()) {
				handleWhereElement(domel, domObjsFlt, domTable, vars, userCtx);
			}
			
			if(domFlt.hasItems()) {
				domFltMngr.filter(domFlt);
			}
		} else if(el instanceof GroupImpl) {
			Log.WORKFLOW.debug("add group");
			GroupImpl g = (GroupImpl)el;
			CompositeFilter grpFlt = new CompositeFilter();
			add(g.getType(),flt,grpFlt,g.isNot());
			for(WhereElement gEl : g.getElements()) {
				handleWhereElement(gEl, /*out,*/ grpFlt, table, vars, userCtx);
			}
			
		} else {
			Log.WORKFLOW.warn("unsupported type: " + el.getClass());
		}
	}
	
	private void add(WhereType type, CompositeFilter to, AbstractFilter child, boolean not) {
		switch(type) {
		case FIRST:	to.first(not, child);
			break;
		case AND:	to.and(not, child);
			break;
		case OR:	to.or(not, child);
			break;
		}
	}
	private void handleField(FieldImpl f, Map<String,Object> vars, ITable table, CompositeFilter flt) {
		if(f.getId() instanceof SimpleFieldId) {
			handleSimpleField((SimpleFieldId)f.getId(),f,vars,table,flt);
		} else if(f.getId() instanceof LookupFieldId) {
			handleLookupField((LookupFieldId)f.getId(),f,vars,table,flt);
		} else {
			throw new RuntimeException("Complex field ids are not supported!");
		}
	}

	private void handleLookupField(LookupFieldId fid,FieldImpl f, Map<String,Object> vars, ITable table, CompositeFilter flt) {
		String id = fid.getId();
		IAttribute attr = table.getAttribute(id);
		
		StringBuffer sbuf = new StringBuffer();
		LookupOperatorTree node = fid.getTree();
		
		createLookupQuery(node,sbuf,f,vars,table,flt);
		String query = sbuf.toString();
		Log.WORKFLOW.debug("lookup query: " + query);
		
		AttributeFilter attrFlt = new AttributeFilter(attr,AttributeFilterType.EQUALS,new Object[]{ query });
		add(f.getType(),flt,attrFlt,f.isNot());
	}

	private void createLookupQuery(LookupOperatorTree node, StringBuffer sbuf, FieldImpl f, Map<String,Object> vars, ITable table, CompositeFilter flt) {
		if(node.getOperator().equalsIgnoreCase("parent")) {
			sbuf.append("(select \"Id\" from \"LookUp\" where \"ParentId\" = ");
			if(node.hasChild()) {
				createLookupQuery(node.getChild(),sbuf,f,vars,table,flt);
			} else {
				FieldValue value = f.getValues().iterator().next();
				if(node.getAttributeName() == null) {
					if(value.getType() == FieldValueType.INT) {
						sbuf.append(value.getValue());
					} else if(value.getType() == FieldValueType.STRING) {
						sbuf.append("(select \"Id\" from \"LookUp\" where \"Description\" = '"+value.getValue().toString()+"')");
					}
				} else {
					sbuf.append("(select \"Id\" from \"LookUp\" where \""+node.getAttributeName()+"\" = '"+value.getValue().toString()+"')");
				}
			}
			sbuf.append(")");
		} else {
			throw new RuntimeException("unsupported lookup operator: " + node.getOperator());
		}
	}

	private void handleSimpleField(SimpleFieldId fid, FieldImpl f, Map<String,Object> vars, ITable table, CompositeFilter flt) {
		AttributeFilter.AttributeFilterType type = convert(f.getOperator(),f.isNot());
		IAttribute attr = table.getAttribute(fid.getId());

		AttributeFilter attrFlt = new AttributeFilter(attr, type, extractValuesForFilter(f, table, attr, type, vars));
		boolean not = (type == AttributeFilterType.NULL) ? f.isNot() : false;
		add(f.getType(),flt,attrFlt,not);
	}

	private Object[] extractValuesForFilter(FieldImpl f, ITable table, IAttribute attr, AttributeFilterType type, Map<String, Object> vars) {
		List<Object> values = new ArrayList<Object>();

		for(FieldValue v : f.getValues()) {
			values.add(convert(attr,v,vars));
		}
		
		Object firstValue = values.get(0);
		String firstStringValue = null;
		if (firstValue instanceof String) {
			firstStringValue = (String) firstValue;
		}

		if (firstStringValue != null) {
			if (attr.getType() == IAttribute.AttributeType.LOOKUP) {
				//check int
				if (f.getValues().iterator().next().getType() != FieldValueType.NATIVE) {
					try {
						Integer.getInteger(firstStringValue);
					} catch(NumberFormatException nfe) {
						LookupType lkpType = attr.getLookupType();
						Lookup lkp = SchemaCache.getInstance().getLookup(lkpType.getType(), firstStringValue);
						if (lkp == null) {
							throw new RuntimeException("Invalid lookup value '" + values.get(0) + "' for type: " + lkpType.getType());
						}
						values.clear();
						values.add(lkp.getId());
					}
				}
			} else if (attr.getType() == IAttribute.AttributeType.REFERENCE) {
				//check int
				AttributeFilterType tmp = type;
				if (f.getValues().iterator().next().getType() != FieldValueType.NATIVE) {
					try {
						Integer.parseInt(firstStringValue);
						type = AttributeFilterType.EQUALS;
					} catch(NumberFormatException nfe) {
						IDomain dom = attr.getReferenceDomain();
						ITable otbl;
						if (dom.getDirectionFrom(table)) {
							otbl = dom.getClass2();
						} else {
							otbl = dom.getClass1();
						}
	
						IAttribute descrAttr = otbl.getAttribute("Description");
						AttributeFilter tmpflt = new AttributeFilter(descrAttr, tmp, values.toArray(new Object[values.size()]));
						values.clear();
						values.add(String.format(ReferenceSelectTmpl, otbl.getDBName(), tmpflt.toString()));
	
						type = AttributeFilterType.IN;
					}
				}
			}
		}

		return values.toArray(new Object[values.size()]);
	}
	
	AttributeFilterType convert(FieldOperator op,boolean not) {
		switch(op){
		case BGN: return not ? AttributeFilterType.DONTBEGIN : AttributeFilterType.BEGIN;
		case BTW: return not ? AttributeFilterType.NOTBETWEEN : AttributeFilterType.BETWEEN;//throw new RuntimeException("unsupported operator BETWEEN");
		case CONT: return not ? AttributeFilterType.DONTCONTAINS : AttributeFilterType.CONTAINS;
		case END: return not ? AttributeFilterType.DONTEND : AttributeFilterType.END;
		case EQ: return not ? AttributeFilterType.DIFFERENT : AttributeFilterType.EQUALS;
		case GT: return AttributeFilterType.STRICT_MAJOR;
		case GTEQ: return AttributeFilterType.MAJOR;
		case IN: return AttributeFilterType.IN;
		case ISNULL: return AttributeFilterType.NULL;
		case LT: return AttributeFilterType.STRICT_MINOR;
		case LTEQ: return AttributeFilterType.MINOR;
		}
		throw new RuntimeException("unsupported operator " + op.name());
	}
	
	Object convert(IAttribute attr,FieldValue v, Map<String,Object> context) {
		Object out = null;
		switch(v.getType()) {
		case BOOL:
		case FLOAT:
		case INT:
		case STRING:
			out = v.getValue().toString();
			break;
		case NATIVE:
			out = new SQLQuery(v.getValue().toString());
			break;
		case DATE:
		case TIMESTAMP:
			String frmt = (attr.getType() == AttributeType.DATE) ? "dd/MM/yy" : "dd/MM/yy HH:mm:ss";
			out = new SimpleDateFormat(frmt).format((Date)v.getValue());
			break;
		case INPUT:
			out = convert(context.get(((FieldInputValue)v.getValue()).getVariableName()).toString());
			break;
		case SUBEXPR:
			throw new RuntimeException("subqueries are not supported");
		}
		if(out == null){
			throw new RuntimeException("cannot convert value " + v.getType().name() + ": " + v.getValue() + " to string!");
		}
		Log.WORKFLOW.debug("converted value: " + out);
		return out;
	}
	String convert(Object o) {
		if(o instanceof java.util.Date) {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Date)o);
		}
		return o.toString();
	}
}
