(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.ManageEmail', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		mixins: {
			observable: 'Ext.util.Observable'
		},

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
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		ownerController: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.tabs.email.Email}
		 */
		tabController: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.EmailView}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.tabs.email.EmailView} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance or Ext.data.Model} card
		 *
		 * @override
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			this.mixins.observable.constructor.call(this);

			this.callParent(arguments);

			this.tabController = this.view.delegate;

			this.tabController.cmfg('configurationSet', widgetConf);

			// Converts configuration templates to templates model objects
			this.configurationTemplates = []; // Reset variable

			Ext.Array.forEach(widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES], function(item, index, allItems) {
				this.configurationTemplates.push(this.configurationTemplatesToModel(item));
			}, this);

			this.tabController.cmfg('configurationTemplatesSet', this.configurationTemplates);

			this.buildBottomToolbar();
		},

		buildBottomToolbar: function() {
			this.tabController.grid.addCls('cmborderbottom');
			this.tabController.getView().removeDocked(this.tabController.getView().getDockedComponent('bottom'));
			this.tabController.getView().addDocked(
				Ext.create('Ext.toolbar.Toolbar', {
					dock: 'bottom',
					itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
					ui: 'footer',

					layout: {
						type: 'hbox',
						align: 'middle',
						pack: 'center'
					},

					items: [
						Ext.create('CMDBuild.core.buttons.Back', {
							scope: this,

							handler: function(button, e) {
								this.ownerController.activateFirstTab();
							}
						})
					]
				})
			);
		},

		/**
		 * Translates some properties to fix some server side names problems
		 *
		 * @param {Object} template
		 *
		 * @return {CMDBuild.model.common.tabs.email.Template} or null
		 */
		configurationTemplatesToModel: function(template) {
			if (Ext.isObject(template) && !Ext.Object.isEmpty(template)) {
				var model = Ext.create('CMDBuild.model.common.tabs.email.Template', template);
				model.set(CMDBuild.core.proxy.CMProxyConstants.BCC, template[CMDBuild.core.proxy.CMProxyConstants.BCC_ADDRESSES]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.BODY, template[CMDBuild.core.proxy.CMProxyConstants.CONTENT]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.CC, template[CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.FROM, template[CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS]);
				model.set(CMDBuild.core.proxy.CMProxyConstants.TO, template[CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES]);

				return model;
			}

			return null;
		},

		/**
		 * @return {Object}
		 *
		 * @override
		 */
		getData: function() {
			var out = {};
			out[CMDBuild.core.proxy.CMProxyConstants.OUTPUT] = this.tabController.cmfg('selectedEntityIdGet');

			return out;
		},

		/**
		 * Used to mark widget as busy during regenerations, especially useful for getData() regeneration
		 *
		 * @return {Boolean}
		 *
		 * @override
		 */
		isBusy: function() {
			return this.tabController.cmfg('busyStateGet');
		},

		/**
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			if (Ext.isBoolean(this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REQUIRED]) && this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REQUIRED])
				return this.controllerGrid.getDraftEmails().length > 0;

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

				this.tabController.globalLoadMask = false;

				this.beforeSaveCallbackObject = {
					array: callbackChainArray,
					index: i
				};

				// Setup end-point callback to close widget save callback loop
				this.tabController.cmfg('regenerationEndPointCallbackSet', function() {
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

					me.tabController.cmfg('regenerationEndPointCallbackSet'); // Reset callback function
				});

				this.tabController.cmfg('regenerateAllEmailsSet', true);
				this.tabController.cmfg('storeLoad');
			}
		}
	});

})();