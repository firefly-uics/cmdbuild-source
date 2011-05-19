package org.cmdbuild.elements.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.TableImpl.OrderEntry;

public interface ITable extends BaseSchema {

	public static final String BaseTable = "Class";

	public void save();
	public void delete();
	public boolean isNew();
	public void setDescription(String description);
	public String getDescription();
	public void setSuperClass(boolean isSuperClass);
	public boolean isSuperClass();
	public void setParent(String parent);
	public void setParent(Integer parent);
	public List<OrderEntry> getOrdering();
	public String toString();
	public boolean isActivity();
	public boolean 	isAllowedOnTrees();
	public CardFactory cards();
	public ITable getParent();	
	public void setParent(ITable parent);
	public TableTree treeBranch();
	public ArrayList<ITable> getChildren();
	public boolean hasChild();
	public boolean isTheTableClass();
	public boolean isTheTableActivity();

	public Iterable<IAttribute> fkDetails();
}
