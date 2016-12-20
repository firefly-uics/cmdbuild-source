(function () {

	/**
	 * Required managed functions:
	 * 	- panelGridAndFromFullScreenUiSetup
	 *
	 * NOTE: "form" and "grid" (or "tree") pointers are required to work with UI state module
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

		// FullScreen UI manage methods
			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			panelGridAndFromFullScreenDisplayBoth: function () {
				var topPanel = Ext.isEmpty(this.grid) ? this.tree : this.grid;

				// Error handling
					if (!Ext.isObject(this.form) || Ext.Object.isEmpty(this.form) || !this.form instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenDisplayBoth(): unmanaged form property', this, this.form);

					if (!Ext.isObject(topPanel) || Ext.Object.isEmpty(topPanel) || !topPanel instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenDisplayBoth(): unmanaged topPanel property', this, topPanel);
				// END: Error handling

				Ext.suspendLayouts();

				// Show top panel to center
				topPanel.show();
				topPanel.region = 'center';

				// Show bottom panel to south
				this.form.show();
				this.form.region = 'south';

				this.panelGridAndFromFullScreenStyleClsAdd(); // Add style classes to panels

				Ext.resumeLayouts(true);
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			panelGridAndFromFullScreenMaximizeBottom: function () {
				var topPanel = Ext.isEmpty(this.grid) ? this.tree : this.grid;

				// Error handling
					if (!Ext.isObject(this.form) || Ext.Object.isEmpty(this.form) || !this.form instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenMaximizeBottom(): unmanaged form property', this, this.form);

					if (!Ext.isObject(topPanel) || Ext.Object.isEmpty(topPanel) || !topPanel instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenMaximizeBottom(): unmanaged topPanel property', this, topPanel);
				// END: Error handling

				Ext.suspendLayouts();

				// Hide top panel
				topPanel.hide();
				topPanel.region = '';

				// Show and maximize bottom panel
				this.form.show();
				this.form.region = 'center';

				this.panelGridAndFromFullScreenStyleClsRemove(); // Remove style classes from panels

				Ext.resumeLayouts(true);
			},

			/**
			 * @returns {Void}
			 *
			 * @private
			 */
			panelGridAndFromFullScreenMaximizeTop: function () {
				var topPanel = Ext.isEmpty(this.grid) ? this.tree : this.grid;

				// Error handling
					if (!Ext.isObject(this.form) || Ext.Object.isEmpty(this.form) || !this.form instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenMaximizeTop(): unmanaged form property', this, this.form);

					if (!Ext.isObject(topPanel) || Ext.Object.isEmpty(topPanel) || !topPanel instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenMaximizeTop(): unmanaged topPanel property', this, topPanel);
				// END: Error handling

				Ext.suspendLayouts();

				// Show and maximize top panel
				topPanel.show();
				topPanel.region = 'center';

				// Hide bottom panel
				this.form.hide();
				this.form.region = '';

				this.panelGridAndFromFullScreenStyleClsRemove(); // Remove style classes from panels

				Ext.resumeLayouts(true);
			},

			/**
			 * @param {Ext.panel.Panel} panel
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			panelGridAndFromFullScreenStyleClsAdd: function () {
				var topPanel = Ext.isEmpty(this.grid) ? this.tree : this.grid;

				// Error handling
					if (!Ext.isObject(this.form) || Ext.Object.isEmpty(this.form) || !this.form instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenRemoveStyleCls(): unmanaged form property', this, this.form);

					if (!Ext.isObject(topPanel) || Ext.Object.isEmpty(topPanel) || !topPanel instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenRemoveStyleCls(): unmanaged topPanel property', this, topPanel);
				// END: Error handling

				if (!topPanel.hasCls('cmdb-border-bottom'))
					topPanel.addCls('cmdb-border-bottom');

				if (!this.form.hasCls('cmdb-border-top'))
					this.form.addCls('cmdb-border-top');
			},

			/**
			 * @param {Ext.panel.Panel} panel
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			panelGridAndFromFullScreenStyleClsRemove: function () {
				var topPanel = Ext.isEmpty(this.grid) ? this.tree : this.grid;

				// Error handling
					if (!Ext.isObject(this.form) || Ext.Object.isEmpty(this.form) || !this.form instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenRemoveStyleCls(): unmanaged form property', this, this.form);

					if (!Ext.isObject(topPanel) || Ext.Object.isEmpty(topPanel) || !topPanel instanceof Ext.panel.Panel)
						return _error('panelGridAndFromFullScreenRemoveStyleCls(): unmanaged topPanel property', this, topPanel);
				// END: Error handling

				if (topPanel.hasCls('cmdb-border-bottom'))
					topPanel.removeCls('cmdb-border-bottom');

				if (this.form.hasCls('cmdb-border-top'))
					this.form.removeCls('cmdb-border-top');
			},

			/**
			 * @param {Object} parameters
			 * @param {String} parameters.force
			 * @param {String} parameters.maximize
			 *
			 * @returns {Void}
			 */
			panelGridAndFromFullScreenUiSetup: function (parameters) {
				parameters = Ext.isObject(parameters) ? parameters : {};
				parameters.force = Ext.isBoolean(parameters.force) ? parameters.force : false;

				// Error handling
					if (!Ext.isString(parameters.maximize) || Ext.isEmpty(parameters.maximize))
						return _error('panelGridAndFromFullScreenUiSetup(): unmanaged maximize parameter', this, parameters.maximize);
				// END: Error handling

				if (CMDBuild.configuration.userInterface.get(CMDBuild.core.constants.Proxy.FULL_SCREEN_MODE) || parameters.force)
					switch (parameters.maximize) {
						case 'bottom':
							return this.panelGridAndFromFullScreenMaximizeBottom();

						case 'top':
							return this.panelGridAndFromFullScreenMaximizeTop();

						case 'both':
						default:
							return this.panelGridAndFromFullScreenDisplayBoth();
					}
			}
	});

})();
