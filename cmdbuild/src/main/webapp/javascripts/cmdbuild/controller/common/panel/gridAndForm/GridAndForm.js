(function () {

	/**
	 * NOTE: form and grid pointers are required to work with UI state module
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.panel.gridAndForm.GridAndForm', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Object}
		 */
		form: undefined,

		/**
		 * @property {Object}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.common.panel.gridAndForm.GridAndFormView}
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

			if (!Ext.isEmpty(_CMUIState))
				_CMUIState.addDelegate(this);
		},

		// _CMUIState methods
			/**
			 * @returns {Void}
			 */
			onFullScreenChangeToFormOnly: function () {
				if (
					!Ext.isEmpty(this.form)
					&& (!Ext.isEmpty(this.grid) || !Ext.isEmpty(this.tree))
				) {
					var topPanel = Ext.isEmpty(this.grid) ? this.tree : this.grid;

					Ext.suspendLayouts();

					topPanel.hide();
					topPanel.region = '';

					this.form.show();
					this.form.region = 'center';

					Ext.resumeLayouts(true);
				} else {
					_warning('onFullScreenChangeToFormOnly(): form or grid property missing', this);
				}
			},

			/**
			 * @returns {Void}
			 */
			onFullScreenChangeToGridOnly: function () {
				if (
					!Ext.isEmpty(this.form)
					&& (!Ext.isEmpty(this.grid) || !Ext.isEmpty(this.tree))
				) {
					var topPanel = Ext.isEmpty(this.grid) ? this.tree : this.grid;

					Ext.suspendLayouts();

					topPanel.show();
					topPanel.region = 'center';

					this.form.hide();
					this.form.region = '';

					Ext.resumeLayouts(true);
				} else {
					_warning('onFullScreenChangeToGridOnly(): form or grid property missing', this);
				}
			},

			/**
			 * @returns {Void}
			 */
			onFullScreenChangeToOff: function () {
				if (
					!Ext.isEmpty(this.form)
					&& (!Ext.isEmpty(this.grid) || !Ext.isEmpty(this.tree))
				) {
					var topPanel = Ext.isEmpty(this.grid) ? this.tree : this.grid;

					Ext.suspendLayouts();

					topPanel.show();
					topPanel.region = 'center';

					this.form.show();
					this.form.region = 'south';

					Ext.resumeLayouts(true);
				} else {
					_warning('onFullScreenChangeToOff(): form or grid property missing', this);
				}
			}
	});

})();
