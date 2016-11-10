(function () {

	/**
	 * @link CMDBuild.controller.common.field.filter.advanced.window.Window
	 * @link CMDBuild.controller.management.workflow.panel.tree.filter.advanced.FilterEditor
	 */
	Ext.define('CMDBuild.controller.common.panel.gridAndForm.panel.common.filter.advanced.FilterEditor', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.panel.common.filter.advanced.Manager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onPanelGridAndFormFilterAdvancedFilterEditorAbortButtonClick',
			'onPanelGridAndFormFilterAdvancedFilterEditorApplyButtonClick',
			'onPanelGridAndFormFilterAdvancedFilterEditorSaveAndApplyButtonClick',
			'onPanelGridAndFormFilterAdvancedFilterEditorViewHide',
			'onPanelGridAndFormFilterAdvancedFilterEditorViewShow'
		],

		/**
		 * @property {CMDBuild.view.common.panel.gridAndForm.panel.common.filter.advanced.FilterEditorWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.panel.gridAndForm.panel.common.filter.advanced.Manager} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.panel.gridAndForm.panel.common.filter.advanced.FilterEditorWindow', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedFilterEditorAbortButtonClick: function () {
			this.view.close();
		},

		/**
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedFilterEditorApplyButtonClick: function () {
			var filterModelObject = this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterGet').getData();
			filterModelObject[CMDBuild.core.constants.Proxy.CONFIGURATION] = this.view.fieldFilter.getValue();

			// If new filter model
			if (Ext.isEmpty(filterModelObject[CMDBuild.core.constants.Proxy.ID])) {
				filterModelObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = CMDBuild.Translation.newSearchFilter;
				filterModelObject[CMDBuild.core.constants.Proxy.NAME] = CMDBuild.Translation.newSearchFilter;
			}

			this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterSet', { value: filterModelObject });

			// Save filter in local storage
			if (this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterIsEmpty', CMDBuild.core.constants.Proxy.ID))
				this.cmfg('panelGridAndFormFilterAdvancedLocalFilterAdd', this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterGet'));

			this.cmfg('onPanelGridAndFormFilterAdvancedFilterEditorAbortButtonClick'); // Close filter editor view
			this.cmfg('panelGridAndFormFilterAdvancedManagerViewClose'); // Close manager view
			this.cmfg('onPanelGridAndFormFilterAdvancedFilterSelect', this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterGet')); // Apply filter to store
		},

		/**
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedFilterEditorSaveAndApplyButtonClick: function () {
			var filterModelObject = this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterGet').getData();
			filterModelObject[CMDBuild.core.constants.Proxy.CONFIGURATION] = this.view.fieldFilter.getValue();

			this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterSet', { value: filterModelObject });
			this.cmfg('panelGridAndFormFilterAdvancedManagerSave', { enableApply: true });
		},

		/**
		 * Reset manage toggle button on window hide with no filters in manager store
		 *
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedFilterEditorViewHide: function () {
			if (this.cmfg('panelGridAndFormFilterAdvancedManagerStoreIsEmpty'))
				this.cmfg('panelGridAndFormFilterAdvancedManageToggleStateReset');
		},

		/**
		 * Show event forwarder method
		 *
		 * @returns {Void}
		 */
		onPanelGridAndFormFilterAdvancedFilterEditorViewShow: function () {
			this.setViewTitle([
				this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterGet', CMDBuild.core.constants.Proxy.NAME),
				this.cmfg('panelGridAndFormFilterAdvancedEntryTypeGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
			]);

			this.view.fieldFilter.entryTypeSelect({
				className: this.cmfg('panelGridAndFormFilterAdvancedEntryTypeGet', CMDBuild.core.constants.Proxy.NAME),
				disabledPanels: ['functions'],
				scope: this,
				callback: function () {
					this.view.fieldFilter.setValue(this.cmfg('panelGridAndFormFilterAdvancedManagerSelectedFilterGet', CMDBuild.core.constants.Proxy.CONFIGURATION));
				}
			});
		}
	});

})();
