(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.ManageEmail', {
		extend: 'CMDBuild.controller.common.AbstractBaseWidgetController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * Widget before save callback loop object
		 *
		 * @property {Object}
		 */
		beforeSaveCallbackObject: undefined,

		/**
		 * @property {CMDBuild.model.CMActivityInstance or Ext.data.Model}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'widgetConfigurationGet = widgetManageEmailConfigurationGet'
		],

		/**
		 * Disable delegate apply to avoid to set this class as delegate of email tab
		 *
		 * @cfg {Boolean}
		 *
		 * @override
		 */
		enableDelegateApply: false,

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		ownerController: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		tabDelegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailView}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.manageEmail.Configuration',

		/**
		 * @param {CMDBuild.view.management.common.widgets.customForm.CustomFormView} configurationObject.view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {Object} configurationObject.widgetConfiguration
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {CMDBuild.model.CMActivityInstance or Ext.data.Model} configurationObject.card
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			// Shorthands
			this.tabDelegate = this.view.delegate;

			this.tabDelegate.cmfg('tabEmailConfigurationSet', { value: this.widgetConfiguration });
			this.tabDelegate.cmfg('configurationTemplatesSet', this.cmfg('widgetManageEmailConfigurationGet', CMDBuild.core.constants.Proxy.TEMPLATES));

			// Build bottom toolbar
			this.buildBottomToolbar();
		},

		/**
		 * Create event manager and show toolbar
		 */
		buildBottomToolbar: function() {
			this.tabDelegate.getView().on('show', this.widgetEmailShowEventManager, this);

			// Border manage
			if (!this.tabDelegate.grid.hasCls('cmborderbottom'))
				this.tabDelegate.grid.addCls('cmborderbottom');

			this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).removeAll();
			this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).add(
				Ext.create('CMDBuild.core.buttons.text.Back', {
					scope: this,

					handler: function(button, e) {
						this.parentDelegate.activateFirstTab();
					}
				})
			);
			this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).show();
		},

		/**
		 * Delete event and hide toolbar on widget destroy
		 */
		destroy: function() {
			this.tabDelegate.getView().un('show', this.widgetEmailShowEventManager, this);
			this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).hide();

			// Border manage
			if (this.tabDelegate.grid.hasCls('cmborderbottom'))
				this.tabDelegate.grid.removeCls('cmborderbottom');
		},

		/**
		 * @returns {Object}
		 *
		 * @override
		 */
		getData: function() {
			var out = {};
			out[CMDBuild.core.constants.Proxy.OUTPUT] = this.tabDelegate.cmfg('selectedEntityIdGet');

			return out;
		},

		/**
		 * Used to mark widget as busy during regenerations, especially useful for getData() regeneration
		 *
		 * @returns {Boolean}
		 *
		 * @override
		 */
		isBusy: function() {
			return this.tabDelegate.cmfg('busyStateGet');
		},

		/**
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			if (
				Ext.isBoolean(this.cmfg('widgetManageEmailConfigurationGet', CMDBuild.core.constants.Proxy.REQUIRED))
				&& this.cmfg('widgetManageEmailConfigurationGet', CMDBuild.core.constants.Proxy.REQUIRED)
			) {
				return this.tabDelegate.controllerGrid.getDraftEmails().length > 0;
			}

			return this.callParent(arguments);
		},

		/**
		 * @param {Array} callbackChainArray
		 *
		 * @override
		 */
		onBeforeSave: function(callbackChainArray, i) {
			if (!Ext.isEmpty(callbackChainArray[i])) {
				var me = this;

				this.tabDelegate.globalLoadMask = false;

				this.beforeSaveCallbackObject = {
					array: callbackChainArray,
					index: i
				};

				// Setup end-point callback to close widget save callback loop
				this.tabDelegate.cmfg('regenerationEndPointCallbackSet', function() {
					if (!Ext.Object.isEmpty(me.beforeSaveCallbackObject)) {
						var index = me.beforeSaveCallbackObject.index;

						Ext.callback(
							me.beforeSaveCallbackObject.array[index].fn,
							me.beforeSaveCallbackObject.array[index].scope,
							[
								me.beforeSaveCallbackObject.array,
								index + 1
							]
						);
					}

					me.tabDelegate.cmfg('regenerationEndPointCallbackSet'); // Reset callback function
				});

				this.tabDelegate.cmfg('regenerateAllEmailsSet', true);
				this.tabDelegate.cmfg('storeLoad');
			}
		},

		/**
		 * @param {CMDBuild.view.management.common.tabs.email.EmailView} panel
		 * @param {Object} eOpts
		 */
		widgetEmailShowEventManager: function(panel, eOpts) {
			var cardWidgetTypes = [];

			if (Ext.isArray(this.parentDelegate.takeWidgetFromCard(this.card)))
				Ext.Array.forEach(this.parentDelegate.takeWidgetFromCard(this.card), function(widgetObject, i, allWidgetObjects) {
					if (!Ext.isEmpty(widgetObject))
						cardWidgetTypes.push(widgetObject[CMDBuild.core.constants.Proxy.TYPE]);
				}, this);

			if (Ext.Array.contains(cardWidgetTypes, this.cmfg('widgetManageEmailConfigurationGet', CMDBuild.core.constants.Proxy.TYPE)))
				this.tabDelegate.getView().getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM).show();
		}
	});

})();