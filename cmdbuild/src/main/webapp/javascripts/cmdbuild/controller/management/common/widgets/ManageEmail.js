(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.ManageEmail', {
		extend: 'CMDBuild.controller.common.AbstractBaseWidgetController',

		requires: ['CMDBuild.core.constants.Proxy'],

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
		 * @cfg {Boolean}
		 *
		 * @override
		 */
		enableViewDelegateInject: false,

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
		widgetConfiguration: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.manageEmail.Configuration',

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMWidgetManager} configurationObject.view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} configurationObject.parentDelegate
		 * @param {Object} configurationObject.widgetConfiguration
		 * @param {Ext.form.Basic} configurationObject.clientForm
		 * @param {CMDBuild.model.CMActivityInstance} configurationObject.card
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			// Shorthands
			this.tabController = this.view.delegate;

			this.tabController.cmfg('configurationSet', this.widgetConfiguration);

			// Converts configuration templates to templates model objects
			this.configurationTemplates = []; // Reset variable

			Ext.Array.forEach(this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.TEMPLATES), function(templateObject, i, allTemplateObjects) {
				this.configurationTemplates.push(this.configurationTemplatesToModel(templateObject));
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
					itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
					ui: 'footer',

					layout: {
						type: 'hbox',
						align: 'middle',
						pack: 'center'
					},

					items: [
						Ext.create('CMDBuild.core.buttons.text.Back', {
							scope: this,

							handler: function(button, e) {
								this.parentDelegate.activateFirstTab(); // TODO: needs refactor of CMWidgetManagerController to use cmfg()
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
				model.set(CMDBuild.core.constants.Proxy.BCC, template[CMDBuild.core.constants.Proxy.BCC_ADDRESSES]);
				model.set(CMDBuild.core.constants.Proxy.BODY, template[CMDBuild.core.constants.Proxy.CONTENT]);
				model.set(CMDBuild.core.constants.Proxy.CC, template[CMDBuild.core.constants.Proxy.CC_ADDRESSES]);
				model.set(CMDBuild.core.constants.Proxy.FROM, template[CMDBuild.core.constants.Proxy.FROM_ADDRESS]);
				model.set(CMDBuild.core.constants.Proxy.TO, template[CMDBuild.core.constants.Proxy.TO_ADDRESSES]);

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
			out[CMDBuild.core.constants.Proxy.OUTPUT] = this.tabController.cmfg('selectedEntityIdGet');

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
			if (this.widgetConfigurationGet(CMDBuild.core.constants.Proxy.REQUIRED))
				return this.tabController.controllerGrid.getDraftEmails().length > 0;

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