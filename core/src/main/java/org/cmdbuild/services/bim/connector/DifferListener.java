package org.cmdbuild.services.bim.connector;

import java.util.EventListener;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.dao.entry.CMCard;

public interface DifferListener extends EventListener {
	
	/**
	 * @param source: the entity to be created in CMDBuild 
	 * */
	void createTarget(Entity source);
	
	/**
	 * @param target: the entity to be removed in CMDBuild
	 * */
	void deleteTarget(CMCard target);
	
	/**
	 * @param source: the entity from which the data has to be copied
	 * @param target: the entity to be updated in CMDBuild
	 * */
	void updateTarget(Entity source, CMCard target);

}
