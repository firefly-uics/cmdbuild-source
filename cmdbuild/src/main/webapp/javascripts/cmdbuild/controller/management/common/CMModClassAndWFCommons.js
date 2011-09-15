Ext.define("CMDBuild.controller.management.common.CMModClassAndWFCommons", {
	/*
	 * p = {
			Id: the id of the card
			IdClass: the id of the class which the card belongs,
			activateFirstTab: true to force the tab panel to return to the first tab 
		}
	 */
	openCard: function(p) {
		var entryType = _CMCache.getEntryTypeById(p.IdClass),
			accordion = _CMMainViewportController.getFirstAccordionWithANodeWithGivenId(p.IdClass),
			modPanel = _CMMainViewportController.findModuleByCMName(entryType.get("type"));

		_CMMainViewportController.setDanglingCard(p);

		if (accordion.collapsed) {
			// waiting for the rendering for select the node
			accordion.mon(accordion, "afterlayout", function() {
				accordion.deselect();
				accordion.selectNodeById(p.IdClass);
			}, {single: true});

			accordion.expandSilently();
		} else {
			accordion.deselect();
			accordion.selectNodeById(p.IdClass);
		}
	}
});