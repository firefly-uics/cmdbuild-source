(function () {

	Ext.define('CMDBuild.controller.management.workflow.panel.tree.filter.advanced.FilterEditor', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.workflow.panel.tree.filter.advanced.Manager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowTreeFilterAdvancedFilterEditorAbortButtonClick',
			'onWorkflowTreeFilterAdvancedFilterEditorApplyButtonClick',
			'onWorkflowTreeFilterAdvancedFilterEditorSaveAndApplyButtonClick',
			'onWorkflowTreeFilterAdvancedFilterEditorViewHide',
			'onWorkflowTreeFilterAdvancedFilterEditorViewShow'
		],

		/**
		 * @property {CMDBuild.view.management.workflow.panel.tree.filter.advanced.FilterEditorWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.workflow.panel.tree.filter.advanced.Manager} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.workflow.panel.tree.filter.advanced.FilterEditorWindow', { delegate: this });
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTreeFilterAdvancedFilterEditorAbortButtonClick: function () {
			this.view.close();
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTreeFilterAdvancedFilterEditorApplyButtonClick: function () {
			var filterModelObject = this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterGet').getData();
			filterModelObject[CMDBuild.core.constants.Proxy.CONFIGURATION] = this.view.fieldFilter.getValue();

			// If new filter model
			if (Ext.isEmpty(filterModelObject[CMDBuild.core.constants.Proxy.ID])) {
				filterModelObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = CMDBuild.Translation.newSearchFilter;
				filterModelObject[CMDBuild.core.constants.Proxy.NAME] = CMDBuild.Translation.newSearchFilter;
			}

			this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterSet', { value: filterModelObject });

			// Save filter in local storage
			if (this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterIsEmpty', CMDBuild.core.constants.Proxy.ID))
				this.cmfg('workflowTreeFilterAdvancedLocalFilterAdd', this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterGet'));

			this.cmfg('onWorkflowTreeFilterAdvancedFilterEditorAbortButtonClick'); // Close filter editor view

			this.cmfg('workflowTreeFilterAdvancedManagerViewClose'); // Close manager view

			this.cmfg('onWorkflowTreeFilterAdvancedFilterSelect', this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterGet')); // Apply filter to store
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTreeFilterAdvancedFilterEditorSaveAndApplyButtonClick: function () {
			var filterModelObject = this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterGet').getData();
			filterModelObject[CMDBuild.core.constants.Proxy.CONFIGURATION] = this.view.fieldFilter.getValue();

			this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterSet', { value: filterModelObject });
			this.cmfg('workflowTreeFilterAdvancedManagerSave', { enableApply: true });
		},

		/**
		 * Reset manage toggle button on window hide with no filters in manager store
		 *
		 * @returns {Void}
		 */
		onWorkflowTreeFilterAdvancedFilterEditorViewHide: function () {
			if (this.cmfg('workflowTreeFilterAdvancedManagerStoreIsEmpty'))
				this.cmfg('workflowTreeFilterAdvancedManageToggleStateReset');
		},

		/**
		 * Show event forwarder method
		 *
		 * @returns {Void}
		 */
		onWorkflowTreeFilterAdvancedFilterEditorViewShow: function () {
			this.setViewTitle([
				this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterGet', CMDBuild.core.constants.Proxy.NAME),
				this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
			]);

			this.view.fieldFilter.entryTypeSelect({
				className: this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME),
				disabledPanels: ['functions'],
				scope: this,
				callback: function () {
					this.view.fieldFilter.setValue(this.cmfg('workflowTreeFilterAdvancedManagerSelectedFilterGet', CMDBuild.core.constants.Proxy.CONFIGURATION));
				}
			});
		}
	});

})();
