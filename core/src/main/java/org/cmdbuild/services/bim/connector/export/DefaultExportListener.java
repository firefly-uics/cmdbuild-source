package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.services.bim.connector.DefaultBimDataView.CONTAINER_GUID;

import java.util.List;
import java.util.Map;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.connector.Output;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultExportListener implements Output {

		private final BimFacade serviceFacade;
		Map<String, Map<String, List<String>>> relationsMap = Maps.newHashMap();
		
		public DefaultExportListener(BimFacade bimFacade){
			this.serviceFacade = bimFacade;
		}
		
		@Override
		public void createTarget(Entity entityToCreate, String targetProjectId) {
			final String objectOid = serviceFacade.createCard(entityToCreate, targetProjectId);
			System.out.println("object '" + objectOid +"' created");
			final String spaceGuid = entityToCreate.getAttributeByName(CONTAINER_GUID).getValue();
			toAdd(objectOid, spaceGuid);
		}

		@Override
		public void deleteTarget(Entity entityToRemove, String targetProjectId) {
			final String removedObjectOid = serviceFacade.removeCard(entityToRemove, targetProjectId);
			System.out.println("object '" + removedObjectOid +"' removed");
			final String oldContainerOid = entityToRemove.getAttributeByName(CONTAINER_GUID).getValue();
			if (!oldContainerOid.isEmpty()) {
				toRemove(removedObjectOid, oldContainerOid);
			}
		}

		private void toAdd(String objectOid, String spaceGuid) {
			if (relationsMap.containsKey(spaceGuid)) {
				Map<String, List<String>> spaceMap = relationsMap.get(spaceGuid);
				if (spaceMap.containsKey("A")) {
					spaceMap.get("A").add(objectOid);
				} else {
					List<String> listToAdd = Lists.newArrayList(objectOid);
					spaceMap.put("A", listToAdd);
				}
			} else {
				Map<String, List<String>> spaceMap = Maps.newHashMap();
				List<String> listToAdd = Lists.newArrayList(objectOid);
				spaceMap.put("A", listToAdd);
				relationsMap.put(spaceGuid, spaceMap);
			}
		}

		private void toRemove(String objectOid, String spaceGuid) {
			if (relationsMap.containsKey(spaceGuid)) {
				Map<String, List<String>> spaceMap = relationsMap.get(spaceGuid);
				if (spaceMap.containsKey("D")) {
					spaceMap.get("D").add(objectOid);
				} else {
					List<String> listToAdd = Lists.newArrayList(objectOid);
					spaceMap.put("D", listToAdd);
				}
			} else {
				Map<String, List<String>> spaceMap = Maps.newHashMap();
				List<String> listToAdd = Lists.newArrayList(objectOid);
				spaceMap.put("D", listToAdd);
				relationsMap.put(spaceGuid, spaceMap);
			}
		}

		@Override
		public void updateRelations(String targetProjectId) {
			serviceFacade.updateRelations(relationsMap, targetProjectId);
			relationsMap = Maps.newHashMap();
		}
}
