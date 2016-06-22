(function () {

	Ext.define('CMDBuild.controller.administration.classes.Classes', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.classes.Classes'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'classesSelectedClassGet',
			'classesSelectedClassIsEmpty',
			'classesSelectedClassReset',
			'identifierGet = classesIdentifierGet',
			'onClassesAddButtonClick',
			'onClassesClassSelection',
			'onClassesModuleInit = onModuleInit',
			'onClassesPrintButtonClick'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.controller.administration.classes.tabs.CMAttributes}
		 */
		controllerAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.administration.classes.tabs.GeoAttributes}
		 */
		controllerDomains: undefined,

		/**
		 * @property {CMDBuild.controller.administration.classes.tabs.Domains}
		 */
		controllerGeoAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.administration.classes.tabs.Layers}
		 */
		controllerLayers: undefined,

		/**
		 * @property {CMDBuild.controller.common.panel.gridAndForm.print.Window}
		 */
		controllerPrintWindow: undefined,

		/**
		 * @property {CMDBuild.controller.administration.classes.tabs.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.controller.administration.classes.tabs.widgets.Widgets}
		 */
		controllerWidgets: undefined,

		/**
		 * @property {CMDBuild.model.classes.Class}
		 *
		 * @private
		 */
		selectedClass: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.classes.ClassesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.classes.ClassesView', { delegate: this });

			// Shorthands
			this.tabPanel = this.view.tabPanel;

			this.tabPanel.removeAll();

			// Build sub-controllers
			this.controllerAttributes = Ext.create('CMDBuild.controller.administration.classes.tabs.CMAttributes', { // FIXME: legacy
				parentDelegate: this,
				view: this.view.attributesPanel
			});
			this.controllerDomains = Ext.create('CMDBuild.controller.administration.classes.tabs.Domains', { parentDelegate: this });
			this.controllerGeoAttributes = Ext.create('CMDBuild.controller.administration.classes.tabs.GeoAttributes', { parentDelegate: this });
			this.controllerLayers = Ext.create('CMDBuild.controller.administration.classes.tabs.Layers', { parentDelegate: this });
			this.controllerPrintWindow = Ext.create('CMDBuild.controller.common.panel.gridAndForm.print.Window', { parentDelegate: this });
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.classes.tabs.Properties', { parentDelegate: this });
			this.controllerWidgets = Ext.create('CMDBuild.controller.administration.classes.tabs.widgets.Widgets', { parentDelegate: this });

			// Inject tabs (sorted)
			this.tabPanel.add([
				this.controllerProperties.getView(),
				this.view.attributesPanel, // FIXME: legacy
				this.controllerDomains.getView(),
				this.controllerWidgets.getView(),
				this.controllerLayers.getView(),
				this.controllerGeoAttributes.getView()
			]);
		},

		/**
		 * @returns {Void}
		 *
		 * FIXME: use cmfg redirect functionalities (onClassesTabAddButtonClick)
		 */
		onClassesAddButtonClick: function () {
			this.tabPanel.setActiveTab(0);

			this.cmfg('mainViewportAccordionDeselect', this.cmfg('classesIdentifierGet'));
			this.cmfg('classesSelectedClassReset');

			this.setViewTitle();

			this.controllerAttributes.onAddClassButtonClick(); // FIXME: legacy
			this.controllerDomains.cmfg('onClassesTabDomainsAddClassButtonClick');
			this.controllerGeoAttributes.cmfg('onClassesTabGeoAttributesAddClassButtonClick');
			this.controllerLayers.cmfg('onClassesTabLayersAddClassButtonClick');
			this.controllerProperties.cmfg('onClassesTabPropertiesAddClassButtonClick');
			this.controllerWidgets.cmfg('onClassesTabWidgetsAddClassButtonClick');
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onClassesModuleInit: function (node) {
			if (Ext.isObject(node) && !Ext.Object.isEmpty(node)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

				CMDBuild.proxy.classes.Classes.read({// FIXME: waiting for refactor (server endpoint)
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse)) {
							var selectedClass = Ext.Array.findBy(decodedResponse, function (classObject, i) {
								return node.get(CMDBuild.core.constants.Proxy.ENTITY_ID) == classObject[CMDBuild.core.constants.Proxy.ID];
							}, this);

							if (Ext.isObject(selectedClass) && !Ext.Object.isEmpty(selectedClass)) {
								this.classesSelectedClassSet({ value: selectedClass });

								this.setViewTitle(this.cmfg('classesSelectedClassGet', CMDBuild.core.constants.Proxy.DESCRIPTION));

								this.cmfg('onClassesClassSelection');

								// Manage tab selection
								if (Ext.isEmpty(this.tabPanel.getActiveTab()))
									this.tabPanel.setActiveTab(0);

								this.tabPanel.getActiveTab().fireEvent('show'); // Manual show event fire because was already selected
							} else {
								_error('onClassesModuleInit(): class not found', this, node.get(CMDBuild.core.constants.Proxy.ENTITY_ID));
							}
						}
					}
				});

				this.onModuleInit(node); // Custom callParent() implementation
			}
		},

		/**
		 * @returns {Void}
		 *
		 * FIXME: use cmfg redirect functionalities (onClassesTabClassSelection)
		 */
		onClassesClassSelection: function () {
			this.controllerAttributes.onClassSelected( // FIXME: legacy
				this.cmfg('classesSelectedClassGet', CMDBuild.core.constants.Proxy.ID),
				this.cmfg('classesSelectedClassGet', CMDBuild.core.constants.Proxy.NAME)
			);
			this.controllerDomains.cmfg('onClassesTabDomainsClassSelection');
			this.controllerGeoAttributes.cmfg('onClassesTabGeoAttributesClassSelection');
			this.controllerLayers.cmfg('onClassesTabLayersClassSelection');
			this.controllerProperties.cmfg('onClassesTabPropertiesClassSelection');
			this.controllerWidgets.cmfg('onClassesTabWidgetsClassSelection');
		},

		/**
		 * @param {String} format
		 *
		 * @returns {Void}
		 */
		onClassesPrintButtonClick: function (format) {
			if (Ext.isString(format) && !Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.FORMAT] = format;

				this.controllerPrintWindow.cmfg('panelGridAndFormPrintWindowShow', {
					format: format,
					mode: 'schema',
					params: params
				});
			} else {
				_error('onClassesPrintButtonClick(): unmanaged format property', this, format);
			}
		},

		// SelectedClass property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			classesSelectedClassGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			classesSelectedClassIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			classesSelectedClassReset: function (parameters) {
				this.propertyManageReset('selectedClass');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			classesSelectedClassSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.classes.Class';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedClass';

					this.propertyManageSet(parameters);
				}
			}
	});

})();
