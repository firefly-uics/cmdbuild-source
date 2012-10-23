(function() {
	Ext.ns("CMDBuild.controller");
	var ns = CMDBuild.controller;

	ns.CMMainViewportController = function(viewport) {
		this.viewport = viewport;

		this.accordionControllers = {};
		this.panelControllers = {};

		this.viewport.foreachAccordion(function(accordion) {
			if (typeof accordion.cmControllerType == "function") {
				this.accordionControllers[accordion.cmName] = new accordion.cmControllerType(accordion);
			} else {
				this.accordionControllers[accordion.cmName] = new ns.accordion.CMBaseAccordionController(accordion);
			}
		}, this);

		this.viewport.foreachPanel(function(panel) {
			if (typeof panel.cmControllerType == "function") {
				// We start to use the cmcreate factory method to have the possibility
				// to ignet the subcontrollers in tests
				if (typeof panel.cmControllerType.cmcreate == "function") {
					this.panelControllers[panel.cmName] = new panel.cmControllerType.cmcreate(panel);
				} else {
					this.panelControllers[panel.cmName] = new panel.cmControllerType(panel);
				}
			} else {
				this.panelControllers[panel.cmName] = new ns.CMBasePanelController(panel);
			}
		}, this);

		// the danglig card is used to open a card
		// from a panel to another (something called follow the relations
		// between cards)
		var danglingCard = null;

		this.getDanglingCard = function() {
			var b = danglingCard;
			danglingCard = null;
			return b;
		};

		this.setDanglingCard = function(dc) {
			danglingCard = dc;
		};
	};

	ns.CMMainViewportController.prototype.bringTofrontPanelByCmName = function(cmName, params) {
//		try {
			return this.viewport.bringTofrontPanelByCmName(cmName, params);
//		} catch (e) {
//			_debug("Cannot bring to front the panel " + cmName, e);
//		}
	};

	ns.CMMainViewportController.prototype.deselectAccordionByName = function(cmName) {
		try {
			this.viewport.deselectAccordionByName(cmName);
		} catch (e) {
			_debug("Cannot unselect the accordion " + cmName, e);
		}
	};

	ns.CMMainViewportController.prototype.disableAccordionByName = function(cmName) {
		try {
			this.viewport.disableAccordionByName(cmName);
		} catch (e) {
			_debug("Cannot disable the accordion " + cmName, e);
		}
	};

	ns.CMMainViewportController.prototype.enableAccordionByName = function(cmName) {
		try {
			this.viewport.enableAccordionByName(cmName);
		} catch (e) {
			_debug("Cannot enable the accordion " + cmName, e);
		}
	};

	ns.CMMainViewportController.prototype.findAccordionByCMName = function(cmName) {
		return this.viewport.findAccordionByCMName(cmName);
	};

	ns.CMMainViewportController.prototype.findModuleByCMName = function(cmName) {
		return this.viewport.findModuleByCMName(cmName);
	};

	ns.CMMainViewportController.prototype.getFirstAccordionWithANodeWithGivenId = function(id) {
		return this.viewport.getFirstAccordionWithANodeWithGivenId(id);
	};

	ns.CMMainViewportController.prototype.setInstanceName = function(name) {
		var hdInstanceName = Ext.get('instance_name');
		if (hdInstanceName 
				&& hdInstanceName.dom
				&& hdInstanceName.dom.innerHTML) {

			hdInstanceName.dom.innerHTML = name;
		}
	};

	ns.CMMainViewportController.prototype.selectStartingClass = function() {
		var startingClass = CMDBuild.Runtime.StartingClassId || CMDBuild.Config.cmdbuild.startingclass, // TODO check also the group starting class
		a = startingClass ? this.getFirstAccordionWithANodeWithGivenId(startingClass) : undefined;

		if (a) {
			a.expandSilently();
			a.selectNodeById(startingClass);
		} else {
			this.selectFirstSelectableLeaf();
		}
	};

	ns.CMMainViewportController.prototype.selectFirstSelectableLeaf = function() {
		var a = this.viewport.getFirstAccordionWithASelectableNode();
		if (a) {
			a.selectFirstSelectableNode();
		}
	};

	ns.CMMainViewportController.prototype.selectFirstSelectableLeafOfOpenedAccordion = function() {
		var a = this.viewport.getExpansedAccordion();
		if (a) {
			this.bringTofrontPanelByCmName(a.cmName);
			a.selectFirstSelectableNode();
		}
	};

	/*
	 * p = {
			Id: the id of the card
			IdClass: the id of the class which the card belongs,
			activateFirstTab: true to force the tab panel to return to the first tab 
		}
	*/
	ns.CMMainViewportController.prototype.openCard = function(p) {
		var accordion = this.getFirstAccordionWithANodeWithGivenId(p.IdClass);

		this.setDanglingCard(p);

		if (accordion.collapsed) {
			// waiting for the rendering for select the node
			accordion.mon(accordion, "afterlayout", function() {
				accordion.deselect();
				accordion.selectNodeById(p.IdClass);
			}, null, {single: true});

			accordion.expandSilently();
		} else {
			accordion.deselect();
			accordion.selectNodeById(p.IdClass);
		}
	};

})();