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
				this.panelControllers[panel.cmName] = new panel.cmControllerType(panel);
			} else {
				this.panelControllers[panel.cmName] = new ns.CMBasePanelController(panel);
			}
		}, this);

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
		try {
			return this.viewport.bringTofrontPanelByCmName(cmName, params);
		} catch (e) {
			_debug("Cannot bring to front the panel " + cmName, e);
		}
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
		if (hdInstanceName) {
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
})();