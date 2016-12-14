(function () {

	Ext.define('CMDBuild.controller.common.panel.gridAndForm.tools.properties.Properties', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.common.panel.gridAndForm.GridAndForm}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'panelGridAndFormtoolsPropertiesUpdateAndShow'
		],

		/**
		 * @property {CMDBuild.controller.common.panel.gridAndForm.tools.properties.NavigationChronology}
		 *
		 * @private
		 */
		controllerNavigationChronology: undefined,

		/**
		 * @property {CMDBuild.view.common.panel.gridAndForm.tools.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * Configuration property di add 5px left margin
		 *
		 * @cfg {Boolean}
		 */
		withSpacer: true,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.panel.gridAndForm.GridAndForm} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.panel.gridAndForm.tools.properties.PropertiesView', { delegate: this });

			// Build sub-controllers
			this.controllerNavigationChronology = Ext.create('CMDBuild.controller.common.panel.gridAndForm.tools.properties.NavigationChronology', { parentDelegate: this});
		},

		/**
		 * @returns {Void}
		 */
		panelGridAndFormtoolsPropertiesUpdateAndShow: function () {
			this.view.menu.removeAll();

			// View build (sorted)
			this.view.menu.add([
				this.controllerNavigationChronology.cmfg('panelGridAndFormtoolsPropertiesNavigationConfigObjectGet')
			]);

			if (Ext.isObject(this.view.menu) && !Ext.Object.isEmpty(this.view.menu) && this.view.menu.isMenu) {
				this.view.menu.showAt(0, 0);
				this.view.menu.showAt(
					this.view.getX() + this.view.getWidth() - this.view.menu.getWidth(),
					this.view.getY() + this.view.getHeight()
				);
			}
		}
	});

})();
