(function () {

	/**
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

					this.panelHide(topPanel);
					this.panelShowToCenter(this.form);

					// CSS style setup
					this.removeStyleClsFromPanel(this.form);

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

					this.panelHide(this.form);
					this.panelShowToCenter(topPanel);

					// CSS style setup
					this.removeStyleClsFromPanel(topPanel);

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

					this.panelShowToCenter(topPanel);
					this.panelShowToSouth(this.form);

					// CSS style setup
					this.addStyleClsToPanel(topPanel, 'bottom');
					this.addStyleClsToPanel(this.form, 'top');

					Ext.resumeLayouts(true);
				} else {
					_warning('onFullScreenChangeToOff(): form or grid property missing', this);
				}
			},

			// Service methods
				/**
				 * @param {Ext.panel.Panel} panel
				 * @param {String} mode - ['top' || 'bottom']
				 *
				 * @returns {Void}
				 *
				 * @private
				 */
				addStyleClsToPanel: function (panel, mode) {
					if (
						Ext.isObject(panel) && !Ext.Object.isEmpty(panel)
						&& Ext.isFunction(panel.addCls)
					) {
						switch (mode) {
							case 'bottom': {
								if (Ext.isFunction(panel.hasCls) && !panel.hasCls('cmdb-border-bottom'))
									panel.addCls('cmdb-border-bottom');
							} break;

							case 'top': {
								if (Ext.isFunction(panel.hasCls) && !panel.hasCls('cmdb-border-top'))
									panel.addCls('cmdb-border-top');
							} break;

							default: {
								_error('addStyleClsToPanel(): unmanaged type parameter', this, type);
							}
						}
					} else {
						_error('addStyleClsToPanel(): unmanaged panel parameter', this, panel);
					}
				},

				/**
				 * @param {Ext.panel.Panel} panel
				 *
				 * @returns {Void}
				 *
				 * @private
				 */
				panelHide: function (panel) {
					if (Ext.isObject(panel) && !Ext.Object.isEmpty(panel)) {
						panel.hide();
						panel.region = '';
					} else {
						_error('panelHide(): unmanaged panel parameter', this, panel);
					}
				},

				/**
				 * @param {Ext.panel.Panel} panel
				 *
				 * @returns {Void}
				 *
				 * @private
				 */
				panelShowToCenter: function (panel) {
					if (Ext.isObject(panel) && !Ext.Object.isEmpty(panel)) {
						panel.show();
						panel.region = 'center';
					} else {
						_error('panelHide(): unmanaged panel parameter', this, panel);
					}
				},

				/**
				 * @param {Ext.panel.Panel} panel
				 *
				 * @returns {Void}
				 *
				 * @private
				 */
				panelShowToSouth: function (panel) {
					if (Ext.isObject(panel) && !Ext.Object.isEmpty(panel)) {
						panel.show();
						panel.region = 'south';
					} else {
						_error('panelHide(): unmanaged panel parameter', this, panel);
					}
				},

				/**
				 * @param {Ext.panel.Panel} panel
				 *
				 * @returns {Void}
				 *
				 * @private
				 */
				removeStyleClsFromPanel: function (panel) {
					if (
						Ext.isObject(panel) && !Ext.Object.isEmpty(panel)
						&& Ext.isFunction(panel.removeCls)
					) {
						if (Ext.isFunction(panel.hasCls) && panel.hasCls('cmdb-border-bottom'))
							panel.removeCls('cmdb-border-bottom');

						if (Ext.isFunction(panel.hasCls) && panel.hasCls('cmdb-border-top'))
							panel.removeCls('cmdb-border-top');
					} else {
						_error('removeStyleClsFromPanel(): unmanaged panel parameter', this, panel);
					}
				}
	});

})();
