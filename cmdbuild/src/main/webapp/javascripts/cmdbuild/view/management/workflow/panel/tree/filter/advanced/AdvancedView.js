(function () {

	/**
	 * @link CMDBuild.view.common.panel.gridAndForm.filter.advanced.AdvancedView
	 */
	Ext.define('CMDBuild.view.management.workflow.panel.tree.filter.advanced.AdvancedView', {
		extend: 'Ext.container.ButtonGroup',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.filter.advanced.Advanced}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.filter.SearchClear}
		 */
		clearButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.filter.SearchSet}
		 */
		manageToggleButton: undefined,

		border: false,
		frame: false,
		shadow: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.manageToggleButton = Ext.create('CMDBuild.core.buttons.iconized.filter.SearchSet', {
						enableToggle: true,
						scope: this,

						toggleHandler: function (button, state) {
							this.delegate.cmfg('onPanelGridAndFormFilterAdvancedManageToggleButtonClick', state);
						}
					}),
					this.clearButton = Ext.create('CMDBuild.core.buttons.iconized.filter.SearchClear', {
						disabled: true,
						scope: this,

						handler: function (button, e) {
							this.delegate.cmfg('onPanelGridAndFormFilterAdvancedClearButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * Override to avoid full button group container disabled
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		disable: function () {
			this.delegate.cmfg('onPanelGridAndFormFilterAdvancedDisable');
		},

		/**
		 * Override to avoid full button group container disabled
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		enable: function () {
			this.delegate.cmfg('onPanelGridAndFormFilterAdvancedEnable');
		}
	});

})();
