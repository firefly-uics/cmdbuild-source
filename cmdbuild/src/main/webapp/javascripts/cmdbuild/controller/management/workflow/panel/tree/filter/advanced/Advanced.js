(function () {

	/**
	 * @link CMDBuild.controller.common.panel.gridAndForm.filter.advanced.Advanced
	 */
	Ext.define('CMDBuild.controller.management.workflow.panel.tree.filter.advanced.Advanced', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'panelGridAndFormFilterAdvancedLocalFilterAdd',
			'panelGridAndFormFilterAdvancedLocalFilterGet',
			'panelGridAndFormFilterAdvancedLocalFilterIsEmpty',
			'panelGridAndFormFilterAdvancedLocalFilterRemove',
			'panelGridAndFormFilterAdvancedManageToggleButtonLabelSet',
			'panelGridAndFormFilterAdvancedManageToggleStateReset',
			'getView = panelGridAndFormFilterAdvancedViewGet',
			'onPanelGridAndFormFilterAdvancedClearButtonClick',
			'onPanelGridAndFormFilterAdvancedDisable',
			'onPanelGridAndFormFilterAdvancedEnable',
			'onPanelGridAndFormFilterAdvancedFilterSelect',
			'onPanelGridAndFormFilterAdvancedManageToggleButtonClick'
		],

		/**
		 * @property {CMDBuild.controller.management.workflow.panel.tree.filter.advanced.Manager}
		 */
		controllerManager: undefined,

		/**
		 * @property {Object}
		 *
		 * @private
		 */
		localFilterCache: {},

		/**
		 * @property {CMDBuild.model.common.panel.gridAndForm.filter.advanced.SelectedEntryType}
		 *
		 * @private
		 */
		selectedEntryType: undefined,

		/**
		 * @property {CMDBuild.view.common.panel.gridAndForm.filter.advanced.AdvancedView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.panel.gridAndForm.filter.advanced.AdvancedView', { delegate: this });

			// Shorthands
			this.grid = this.cmfg('panelGridAndFormGridGet');

			// Build sub controllers
			this.controllerManager = Ext.create('CMDBuild.controller.management.workflow.panel.tree.filter.advanced.Manager', { parentDelegate: this });
		},

		/**
		 * @param {String} label
		 *
		 * @returns {Void}
		 */
		panelGridAndFormFilterAdvancedManageToggleButtonLabelSet: function (label) {
			this.view.manageToggleButton.setText(Ext.isEmpty(label) ? CMDBuild.Translation.searchFilter : Ext.String.ellipsis(label, 20));
			this.view.manageToggleButton.setTooltip(Ext.isEmpty(label) ? '' : label);
		},

		/**
		 * @returns {Void}
		 */
		panelGridAndFormFilterAdvancedManageToggleStateReset: function () {
			this.view.manageToggleButton.toggle(false);
		},

		// LocalFilterCache property functions
			/**
			 * @param {CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter} filterModel
			 *
			 * @returns {Void}
			 */
			panelGridAndFormFilterAdvancedLocalFilterAdd: function (filterModel) {
				if (Ext.isObject(filterModel) && !Ext.Object.isEmpty(filterModel)) {
					var filterIdentifier = filterModel.get(CMDBuild.core.constants.Proxy.ID);

					if (Ext.isEmpty(filterIdentifier))
						filterIdentifier = new Date().valueOf(); // Compatibility mode with IE older than IE 9 (Date.now())

					this.localFilterCache[filterIdentifier] = filterModel;
				}
			},

			/**
			 * @returns {Array}
			 */
			panelGridAndFormFilterAdvancedLocalFilterGet: function () {
				return Ext.Object.getValues(this.localFilterCache);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			panelGridAndFormFilterAdvancedLocalFilterIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'localFilterCache';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter} filterModel
			 *
			 * @returns {Void}
			 */
			panelGridAndFormFilterAdvancedLocalFilterRemove: function (filterModel) {
				if (Ext.isObject(filterModel) && !Ext.Object.isEmpty(filterModel)) {
					var identifierToDelete = null;

					// Search for filter
					Ext.Object.each(this.localFilterCache, function (id, object, myself) {
						var filterModelObject = filterModel.getData();
						var localFilterObject = object.getData();

						if (Ext.Object.equals(filterModelObject, localFilterObject))
							identifierToDelete = id;
					}, this);

					if (!Ext.isEmpty(identifierToDelete))
						delete this.localFilterCache[identifierToDelete];
				}
			},

		/**
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedClearButtonClick: function () {
			if (Ext.isObject(this.grid) && !Ext.Object.isEmpty(this.grid)) {
				if (this.grid.getSelectionModel().hasSelection())
					this.grid.getSelectionModel().deselectAll();

				this.cmfg('panelGridAndFormFilterAdvancedManageToggleButtonLabelSet');

				this.view.clearButton.disable();

				this.cmfg('panelGridAndFormGridFilterClear');
			} else {
				_error('onPanelGridAndFormFilterAdvancedClearButtonClick(): grid not found', this, this.grid);
			}
		},

		/**
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedDisable: function () {
			this.view.clearButton.disable();
			this.view.manageToggleButton.disable();
		},

		/**
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedEnable: function () {
			this.view.clearButton.enable();
			this.view.manageToggleButton.enable();
		},

		/**
		 * Apply selected filter to store or clear grid and buttons state
		 *
		 * @param {CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter} filter
		 *
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedFilterSelect: function (filter) {
			if (Ext.isObject(this.grid) && !Ext.Object.isEmpty(this.grid)) {
				this.controllerManager.cmfg('panelGridAndFormFilterAdvancedManagerViewClose');

				this.view.clearButton.enable();

				if (
					Ext.isObject(filter) && !Ext.Object.isEmpty(filter)
					&& Ext.getClassName(filter) == 'CMDBuild.model.common.panel.gridAndForm.filter.advanced.Filter'
				) {
					this.cmfg('panelGridAndFormFilterAdvancedManageToggleButtonLabelSet', filter.get(CMDBuild.core.constants.Proxy.DESCRIPTION));
					this.cmfg('panelGridAndFormGridFilterApply', filter);
				} else {
					this.cmfg('onPanelGridAndFormFilterAdvancedClearButtonClick');
				}
			} else {
				_error('onPanelGridAndFormFilterAdvancedFilterSelect(): grid not found', this, this.grid);
			}
		},

		/**
		 * @param {Boolean} buttonState
		 *
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedManageToggleButtonClick: function (buttonState) {
			if (buttonState) {
				this.controllerManager.cmfg('panelGridAndFormFilterAdvancedManagerViewShow');
			} else {
				this.controllerManager.cmfg('panelGridAndFormFilterAdvancedManagerViewClose');
			}
		}
	});

})();
