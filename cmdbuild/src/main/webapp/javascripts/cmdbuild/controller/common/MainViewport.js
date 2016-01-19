(function() {

	Ext.define('CMDBuild.controller.common.MainViewport', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'mainViewportAccordionContainerIsCollapsed',
			'mainViewportAccordionControllerExists',
			'mainViewportAccordionControllerExpand',
			'mainViewportAccordionControllerGet',
			'mainViewportAccordionControllerUpdateStore',
			'mainViewportAccordionDeselect',
			'mainViewportAccordionSetDisabled',
			'mainViewportCardSelect',
			'mainViewportDanglingCardGet',
			'mainViewportInstanceNameSet',
			'mainViewportModuleControllerExists',
			'mainViewportModuleControllerGet',
			'mainViewportModuleShow',
			'mainViewportSelectFirstExpandedAccordionSelectableNode',
			'mainViewportStartingEntitySelect',
			'onMainViewportCreditsClick'
		],

		/**
		 * All accordions views
		 *
		 * @cfg {Array}
		 */
		accordion: [],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		accordionControllers: {},

		/**
		 * @property {Ext.panel.Panel}
		 */
		accordionContainer: undefined,

		/**
		 * The danglig card is used to open a card from a panel to another (something called follow the relations between cards)
		 *
		 * @property {Object}
		 *
		 * @private
		 */
		danglingCard: null,

		/**
		 * @cfg {Boolean}
		 */
		hideAccordions: false,

		/**
		 * All module views
		 *
		 * @cfg {Array}
		 */
		module: [],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		moduleControllers: [],

		/**
		 * @property {Ext.panel.Panel}
		 */
		moduleContainer: undefined,

		/**
		 * @property {CMDBuild.view.common.MainViewport}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 *
		 * @override
		 */
		constructor: function(configObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.MainViewport', { delegate: this });

			// Shorthands
			this.accordionContainer = this.view.accordionContainer;
			this.moduleContainer = this.view.moduleContainer;

			this.accordionControllerBuild();
			this.moduleControllerBuild();
		},

		// Accordion manage methods
			/**
			 * Accordions doen't uses standard CMDBuild logic (controller builds view) but view creates controller
			 *
			 * @private
			 */
			accordionControllerBuild: function() {
				if (!Ext.isEmpty(this.accordion) && Ext.isArray(this.accordion)) {
					Ext.Array.forEach(this.accordion, function(accordionController, i, allAccordionControllers) {
						if (!Ext.isEmpty(accordionController)) {
							if (Ext.isFunction(accordionController.cmfg) && !Ext.isEmpty(accordionController.cmfg('accordionIdentifierGet'))) {
								accordionController.parentDelegate = this; // Inject as parentDelegate in accordion controllers

								this.accordionControllers[accordionController.cmfg('accordionIdentifierGet')] = accordionController;

								this.accordionContainer.add(accordionController.getView());
							} else {
								_warning('identifier not found in accordion object', this, accordionController);
							}
						}
					}, this);
				}
			},

			/**
			 * @returns {Boolean}
			 */
			mainViewportAccordionContainerIsCollapsed: function() {
				return this.hideAccordions;
			},

			/**
			 * @param {String} identifier
			 *
			 * @returns {Boolean} accordionExists
			 */
			mainViewportAccordionControllerExists: function(identifier) {
				var accordionControllerExists = (
					!Ext.isEmpty(identifier) && Ext.isString(identifier)
					&& !Ext.isEmpty(this.accordionControllers[identifier])
				);

				if (!accordionControllerExists)
					_error('accordion controller with identifier "' + identifier + '" not found', this);

				return accordionControllerExists;
			},

			/**
			 * Forwarder method
			 *
			 * @param {Object} parameters
			 */
			mainViewportAccordionControllerExpand: function(identifier) {
				if (this.cmfg('mainViewportAccordionControllerExists', identifier))
					this.cmfg('mainViewportAccordionControllerGet', identifier).cmfg('accordionExpand');
			},

			/**
			 * @param {String} identifier
			 *
			 * @returns {Mixed} or null
			 */
			mainViewportAccordionControllerGet: function(identifier) {
				if (this.cmfg('mainViewportAccordionControllerExists', identifier))
					return this.accordionControllers[identifier];

				return null;
			},

			/**
			 * Forwarder method
			 *
			 * @param {Object} parameters
			 * @param {String} parameters.identifier
			 * @param {Number} parameters.nodeIdToSelect
			 */
			mainViewportAccordionControllerUpdateStore: function(parameters) {
				if (
					Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
					&& this.cmfg('mainViewportAccordionControllerExists', parameters.identifier)
				) {
					parameters.nodeIdToSelect = Ext.isEmpty(parameters.nodeIdToSelect) ? null : parameters.nodeIdToSelect;

					this.cmfg('mainViewportAccordionControllerGet', parameters.identifier).cmfg('accordionUpdateStore', parameters.nodeIdToSelect);
				}
			},

			/**
			 * Forwarder method
			 *
			 * @param {String} identifier
			 */
			mainViewportAccordionDeselect: function(identifier) {
				if (this.cmfg('mainViewportAccordionControllerExists', identifier))
					this.cmfg('mainViewportAccordionControllerGet', identifier).cmfg('accordionDeselect');
			},

			/**
			 * @private
			 */
			accordionSelectFirstSelectableLeaf: function() {
				var accordionController = this.accordionControllerWithSelectableNodeGet();

				if (!Ext.isEmpty(accordionController))
					accordionController.cmfg('accordionSelectFirstSelectableNode');
			},

			/**
			 * Forwarder method
			 *
			 * @param {Object} parameters
			 * @param {String} parameters.identifier
			 * @param {Boolean} parameters.state
			 */
			mainViewportAccordionSetDisabled: function(parameters) {
				if (
					Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
					&& this.cmfg('mainViewportAccordionControllerExists', parameters.identifier)
				) {
					parameters.state = Ext.isBoolean(parameters.state) ? parameters.state : true;

					this.cmfg('mainViewportAccordionControllerGet', parameters.identifier).getView().setDisabled(parameters.state);
				}
			},

			/**
			 * Returns first accordion witch contains a node with give id
			 *
			 * @param {String} id
			 *
			 * @return {Mixed or null} searchedAccordionController
			 *
			 * @private
			 */
			accordionControllerWithNodeWithIdGet: function(id) {
				var searchedAccordionController = this.accordionControllerExpandedGet();

				// First search in expanded accordion
				if (!Ext.isEmpty(searchedAccordionController) && !Ext.isEmpty(searchedAccordionController.cmfg('accordionNodeByIdGet', id)))
					return searchedAccordionController;

				// Then in other ones
				searchedAccordionController = null;

				Ext.Object.each(this.accordionControllers, function(identifier, accordionController, myself) {
					if (!Ext.isEmpty(accordionController) && !Ext.isEmpty(accordionController.cmfg('accordionNodeByIdGet', id))) {
						searchedAccordionController = accordionController;

						return false;
					}
				}, this);

				return searchedAccordionController;
			},

			/**
			 * Returns expanded accordion's controller
			 *
			 * @returns {Mixed or null} expandedAccordionController
			 *
			 * @private
			 */
			accordionControllerExpandedGet: function() {
				var expandedAccordionController = null;

				Ext.Object.each(this.accordionControllers, function(identifier, accordionController, myself) {
					if (!Ext.isEmpty(accordionController) && !accordionController.getView().getCollapsed()) {
						expandedAccordionController = accordionController;

						return false;
					}
				}, this);

				return expandedAccordionController;
			},

			/**
			 * @returns {Mixed or null} searchedAccordionController
			 *
			 * @private
			 */
			accordionControllerWithSelectableNodeGet: function() {
				var searchedAccordionController = null;

				Ext.Object.each(this.accordionControllers, function(identifier, accordionController, myself) {
					if (!Ext.isEmpty(accordionController) && !Ext.isEmpty(accordionController.cmfg('accordionFirtsSelectableNodeGet'))) {
						searchedAccordionController = accordionController;

						return false;
					}
				}, this);

				return searchedAccordionController;
			},

		// DanglingCard property methods
			/**
			 * @returns {Object}
			 */
			mainViewportDanglingCardGet: function() {
				var danglingCard = Ext.clone(this.danglingCard);

				this.danglingCardReset();

				return danglingCard;
			},

			/**
			 * @private
			 */
			danglingCardReset: function() {
				this.danglingCard = null;
			},

			/**
			 * @param {Object} danglingCard
			 *
			 * @private
			 */
			danglingCardSet: function(danglingCard) {
				this.danglingCard = danglingCard;
			},

		/**
		 * @param {Object} parameters
		 * @param {Boolean or Object} parameters.activateFirstTab - if object selects object as tab otherwise selects first one
		 * @param {Number} parameters.Id - card id
		 * @param {Number} parameters.IdClass
		 */
		mainViewportCardSelect: function(parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters['Id'])
				&& !Ext.isEmpty(parameters['IdClass'])
			) {
				parameters.activateFirstTab = Ext.isEmpty(parameters.activateFirstTab) ? true : parameters.activateFirstTab;

				var accordionController = this.accordionControllerWithNodeWithIdGet(parameters['IdClass']);

				this.danglingCardSet(parameters);

				if (!Ext.isEmpty(accordionController)) {
					accordionController.cmfg('accordionDeselect'); // Instruction required or selection doesn't work if exists another selection
					accordionController.cmfg('accordionSelectNodeById', parameters['IdClass']);
				}
			} else {
				_error('malformed parameters in openCard method', this);
			}
		},

		/**
		 * @param {String} name
		 */
		mainViewportInstanceNameSet: function(name) {
			name = Ext.isString(name) ? name : '';

			var instanceNameContainer = Ext.get('instance-name');

			if (!Ext.isEmpty(instanceNameContainer)) {
				try {
					instanceNameContainer.setHTML(name);
				} catch (e) {
					// Prevents some Explorer error
				}
			}
		},

		/**
		 * @administration
		 */
		mainViewportSelectFirstExpandedAccordionSelectableNode: function() {
			var expandedAccordionController = this.accordionControllerExpandedGet();

			if (!Ext.isEmpty(expandedAccordionController)) {
				this.cmfg('mainViewportModuleShow', { identifier: expandedAccordionController.cmfg('accordionIdentifierGet') });

				expandedAccordionController.cmfg('accordionSelectFirstSelectableNode');
			}
		},

		/**
		 * Select selected entity at first page load
		 *
		 * @management
		 */
		mainViewportStartingEntitySelect: function() {
			var startingClassId = (
				CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.STARTING_CLASS_ID) // Group's starting class
				|| CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.STARTING_CLASS) // Main configuration's starting class
			);
			var accordionWithNodeController = Ext.isEmpty(startingClassId) ? null : this.accordionControllerWithNodeWithIdGet(startingClassId);

			if (!Ext.isEmpty(accordionWithNodeController)) {
				accordionWithNodeController.cmfg('accordionExpand');
				accordionWithNodeController.cmfg('accordionSelectNodeById', startingClassId);
			} else {
				this.accordionSelectFirstSelectableLeaf();
			}
		},

		// Module manage methods
			/**
			 * @private
			 */
			moduleControllerBuild: function() {
				if (!Ext.isEmpty(this.module) && Ext.isArray(this.module)) {
					Ext.Array.forEach(this.module, function(moduleController, i, allModuleViews) {
						if (!Ext.isEmpty(moduleController)) {
							if (Ext.isFunction(moduleController.cmfg)) {
								if (!Ext.isEmpty(moduleController.cmfg('identifierGet'))) {
									moduleController.parentDelegate = this; // Inject as parentDelegate in module controllers

									this.moduleControllers[moduleController.cmfg('identifierGet')] = moduleController;

									this.moduleContainer.add(moduleController.getView());
								} else {
									_warning('identifier not found in accordion object', this, moduleController);
								}
							} else if (!Ext.isEmpty(moduleController.cmName)) { // TODO: legacy mode manage
								this.moduleControllers[moduleController.cmName] = moduleController.delegate;

								if (Ext.isFunction(moduleController.cmControllerType)) {
									// We start to use the cmcreate factory method to have the possibility to inject the sub-controllers in tests
									if (Ext.isFunction(moduleController.cmControllerType.cmcreate)) {
										this.moduleControllers[moduleController.cmName] = new moduleController.cmControllerType.cmcreate(moduleController);
									} else {
										this.moduleControllers[moduleController.cmName] = new moduleController.cmControllerType(moduleController);
									}
								} else if (Ext.isString(moduleController.cmControllerType)) { // To use Ext.loader to asynchronous load also controllers
									this.moduleControllers[moduleController.cmName] = Ext.create(moduleController.cmControllerType, moduleController);
								} else {
									this.moduleControllers[moduleController.cmName] = new CMDBuild.controller.CMBasePanelController(moduleController);
								}

								this.moduleContainer.add(moduleController);
							}
						}
					}, this);
				}
			},

			/**
			 * @param {String} identifier
			 *
			 * @returns {Boolean} accordionExists
			 */
			mainViewportModuleControllerExists: function(identifier) {
				var moduleControllerExists = (
					!Ext.isEmpty(identifier) && Ext.isString(identifier)
					&& !Ext.isEmpty(this.moduleControllers[identifier])
				);

				if (!moduleControllerExists)
					_error('module controller with identifier "' + identifier + '" not found', this);

				return moduleControllerExists;
			},

			/**
			 * @param {String} identifier
			 *
			 * @returns {Mixed} or null
			 */
			mainViewportModuleControllerGet: function(identifier) {
				if (this.cmfg('mainViewportModuleControllerExists', identifier))
					return this.moduleControllers[identifier];

				return null;
			},

			/**
			 * Show module view
			 *
			 * @param {Object} parameters
			 * @param {String} parameters.identifier
			 * @param {Object} parameters.parameters
			 *
			 * @returns {Boolean} toShow
			 */
			mainViewportModuleShow: function(parameters) {
				var toShow = false;

				if (
					!Ext.Object.isEmpty(parameters)
					&& this.cmfg('mainViewportModuleControllerExists', parameters.identifier)
				) {
					parameters.parameters = Ext.isEmpty(parameters.parameters) ? null : parameters.parameters;

					var modulePanel = this.cmfg('mainViewportModuleControllerGet', parameters.identifier);

					if (!Ext.isEmpty(modulePanel) && Ext.isFunction(modulePanel.getView)) {
						modulePanel = modulePanel.getView();
					} else if (!Ext.isEmpty(modulePanel) && !Ext.isEmpty(modulePanel.view)) { // TODO: legacy
						modulePanel = modulePanel.view;
					}

					toShow = !Ext.isFunction(modulePanel.beforeBringToFront) || modulePanel.beforeBringToFront(parameters.parameters) !== false; // TODO: legacy

					if (!Ext.isEmpty(modulePanel)) {
						if (toShow)
							this.moduleContainer.layout.setActiveItem(modulePanel.getId());

						/**
						 * Legacy event
						 *
						 * @deprecated
						 */
						modulePanel.fireEvent('CM_iamtofront', parameters.parameters);

						// FireEvent not used because of problems to pass right parameters to cmfg() function
						if (!Ext.isEmpty(modulePanel.delegate) && Ext.isFunction(modulePanel.delegate.cmfg))
							modulePanel.delegate.cmfg('onModuleInit', parameters.parameters);
					}

					return toShow;
				}

				return toShow;
			},

		/**
		 * Manages footer credits link click action
		 */
		onMainViewportCreditsClick: function() {
			Ext.create('CMDBuild.core.window.Credits').show();
		}
	});

})();