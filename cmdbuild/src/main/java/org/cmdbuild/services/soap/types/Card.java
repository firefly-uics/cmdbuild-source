package org.cmdbuild.services.soap.types;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;

public class Card {
	
	private String className;
	private int id;
	private Calendar beginDate;
	private Calendar endDate;
	private String user;
	private List<Attribute> attributeList;
	private List<Metadata> metadata;
	
	public Card(){ }
	
	public Card(ICard card) {
		setup(card);
		List<Attribute> attrs = new ArrayList<Attribute>();
		for (AttributeValue av : card.getAttributeValueMap().values()) {
			Attribute tmp = new Attribute();
			String name = av.getSchema().getName();
			String value = av.toString();
			tmp.setName(name);
			tmp.setValue(value);
			if (av.getId() != null){
				tmp.setCode(av.getId().toString());
			}
			attrs.add(tmp);
		}
		this.setAttributeList(attrs);
	}
	
	public Card(ICard card, Attribute[] attrs){
		
		AttributeValue value;
		Attribute attribute;
		List<Attribute> list = new ArrayList<Attribute>();
		Log.SOAP.debug("Filtering card with following attributes");
		for (Attribute a : attrs) {
			String name = a.getName();
			if (name != null && (!(name.equals("")))){
				value = card.getAttributeValue(name);
				attribute = new Attribute();
				attribute.setName(name);
				if (value != null){
					attribute.setValue(value.toString());
				}
				if (value.getId() != null){
					attribute.setCode(value.getId().toString());
				}
				Log.SOAP.debug("Attribute name=" + name + ", value="+ value);
				String attributeName = attribute.getName();
				if (!attributeName.equals("Id")
						&& !attributeName.equals("ClassName")
						&& !attributeName.equals("BeginDate")
						&& !attributeName.equals("User")
						&& !attributeName.equals("EndDate")) {
					list.add(attribute);
				}
			}		
			
			this.setAttributeList(list);
			}
		setup(card);
	}
	
	private void setup(ICard card){
		int id = card.getId();
		this.setId(id);
		this.setClassName(card.getSchema().getName());
		this.setUser(card.getUser());

		// Convert Date to Calendar
		Calendar beginDate = Calendar.getInstance();
		beginDate.setTime(card.getBeginDate());
		this.setBeginDate(beginDate);
		try {
			Date endDate = card.getAttributeValue("EndDate").getDate();
			Calendar endDateCalendar = Calendar.getInstance();
			endDateCalendar.setTime(endDate);
			this.setEndDate(endDateCalendar);
		} catch (NotFoundException e) {
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String classname) {
		this.className = classname;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Calendar getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Calendar beginDate) {
		this.beginDate = beginDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<Attribute> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(List<Attribute> attributeList) {
		this.attributeList = attributeList;
	}

	public List<Metadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<Metadata> metadata) {
		this.metadata = metadata;
	}	
	
	public String toString(){
		String attributes= "";
		Iterator<Attribute> itr= attributeList.iterator();
		while(itr.hasNext()){
			attributes +=itr.next().toString();
		}
		return "[className: "+className+" id:"+id+" attributes: "+attributes+"]"; 
	}
}
