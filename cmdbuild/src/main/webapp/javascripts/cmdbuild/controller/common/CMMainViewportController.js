(function() {

	Ext.ns("CMDBuild.controller");
	var ns = CMDBuild.controller;

	ns.CMMainViewportController = function(viewport) {
		this.viewport = viewport;

		this.panelControllers = {};

		this.viewport.foreachPanel(function(panel) {
			buildPanelController(this, panel);
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
		if (hdInstanceName) {
			try {
				hdInstanceName.setHTML(name);
			} catch (e) {
				// Sometimes Explorer does not like it...
			}
		}
	};

	ns.CMMainViewportController.prototype.selectStartingClass = function() {
		var startingClassId = (
			CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.STARTING_CLASS_ID) // Group's starting class
			|| CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.STARTING_CLASS) // Main configuration's starting class
		);
		var accordionWithNode = Ext.isEmpty(startingClassId) ? undefined : this.getFirstAccordionWithANodeWithGivenId(startingClassId);

		if (!Ext.isEmpty(accordionWithNode)) {
			accordionWithNode.expand();
			accordionWithNode.selectNodeById(startingClassId);
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

	/**
	 * @param {Object} parameters
	 * @param {Boolean or Object} parameters.activateFirstTab - if object selects object as tab otherwise selects first one
	 * @param {Number} parameters.Id - card id
	 * @param {Number} parameters.IdClass
	 */
	ns.CMMainViewportController.prototype.openCard = function(parameters) {
		if (
			Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
			&& !Ext.isEmpty(parameters['Id'])
			&& !Ext.isEmpty(parameters['IdClass'])
		) {
			parameters.activateFirstTab = Ext.isEmpty(parameters.activateFirstTab) ? true : parameters.activateFirstTab;

			var accordion = this.getFirstAccordionWithANodeWithGivenId(parameters['IdClass']);

			this.setDanglingCard(parameters);

			if (!Ext.isEmpty(accordion) && Ext.isFunction(accordion.selectNodeById)) {
				accordion.deselect(); // Required or selection doesn't work if exists another selection
				accordion.selectNodeById(parameters['IdClass']);
			}
		} else {
			_error('malformed parameters in openCard method', this);
		}
	};

	/**
	 * @param {Object} me
	 * @param {Object} panel
	 */
	function buildPanelController(me, panel) {
		if (Ext.isFunction(panel.cmControllerType)) {
			// We start to use the cmcreate factory method to have the possibility to inject the sub-controllers in tests
			if (Ext.isFunction(panel.cmControllerType.cmcreate)) {
				me.panelControllers[panel.cmName] = new panel.cmControllerType.cmcreate(panel);
			} else {
				me.panelControllers[panel.cmName] = new panel.cmControllerType(panel);
			}
		} else if (Ext.isString(panel.cmControllerType)) { // To use Ext.loader to asynchronous load also controllers
			me.panelControllers[panel.cmName] = Ext.create(panel.cmControllerType, panel);
		} else {
			me.panelControllers[panel.cmName] = new ns.CMBasePanelController(panel);
		}
	}

})();