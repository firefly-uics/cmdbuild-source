(function () {

	/**
	 * Class to be extended in widget controllers to adapt AbstractController functionalities
	 *
	 * @abstract
	 */
	Ext.define('CMDBuild.controller.common.AbstractBaseWidgetController', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		statics: {
			/**
			 * Old implementation to be used in new widgets
			 *
			 * @param {Object} model
			 *
			 * @return {Object} out
			 */
			getTemplateResolverServerVars: function(model) {
				var out = {};
				var pi = null;

				if (!Ext.isEmpty(model)) {
					if (Ext.getClassName(model) == 'CMDBuild.model.CMActivityInstance') {
						// Retrieve the process instance because it stores the data. this.card has only the varibles to show in this step (is the activity instance)
						pi = _CMWFState.getProcessInstance();
					} else if (Ext.getClassName(model) == 'CMDBuild.model.CMProcessInstance') {
						pi = model;
					}

					if (!Ext.isEmpty(pi) && Ext.isFunction(pi.getValues)) { // The processes use a new serialization. Add backward compatibility attributes to the card values
						out = Ext.apply({
							'Id': pi.get('Id'),
							'IdClass': pi.get('IdClass'),
							'IdClass_value': pi.get('IdClass_value')
						}, pi.getValues());
					} else {
						out = model.raw || model.data;
					}
				}

				return out;
			}
		},

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.model.CMActivityInstance}
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
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {Object}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMWidgetManager} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			if (!Ext.isEmpty(view) && !Ext.Object.isEmpty(widgetConf)) {
				this.callParent([{
					view: view,
					parentDelegate: ownerController,
					widgetConf: widgetConf,
					clientForm: clientForm,
					card: card
				}]);

				this.view.delegate = this; // Apply delegate to view
			} else {
				_error('Wrong or empty widget view or configuration objects', this);
			}
		},

		/**
		 * @abstract
		 */
		beforeActiveView: Ext.emptyFn,

		/**
		 * Executed before window hide perform
		 *
		 * @abstract
		 */
		beforeHideView: Ext.emptyFn,

		/**
		 * @return {Object or null}
		 */
		getData: function() {
			return null;
		},

		/**
		 * @param {String} variableName
		 *
		 * @return {Mixed}
		 */
		getVariable: function(variableName) {
			if (!Ext.isEmpty(this.templateResolver) && Ext.isFunction(this.templateResolver.getVariable))
				return this.templateResolver.getVariable(variableName);

			_warning('No configured templateResolver instance', this);

			return undefined;
		},

		/**
		 * @return {Object}
		 */
		getTemplateResolverServerVars: function() {
			if (!Ext.isEmpty(this.card))
				return this.statics().getTemplateResolverServerVars(this.card);

			return {};
		},

		/**
		 * @return {Number}
		 */
		getWidgetId: function() {
			return this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.ID];
		},

		/**
		 * @param {String}
		 */
		getWidgetLabel: function() {
			return this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.LABEL];
		},

		/**
		 * @return {Boolean}
		 */
		isBusy: function() {
			return false;
		},

		/**
		 * @return {Boolean}
		 */
		isValid: function() {
			return true;
		},

		/**
		 * @param {Array} callbackChainArray
		 */
		onBeforeSave: function(callbackChainArray, i) {
			if (!Ext.isEmpty(callbackChainArray[i])) {
				var callbackObject = callbackChainArray[i];

				Ext.callback(callbackObject.fn, callbackObject.scope, [callbackChainArray, i + 1]);
			}
		},

		/**
		 * @abstract
		 */
		onEditMode: Ext.emptyFn,

		// Compatibility with old implementations
		/**
		 * @return {Number}
		 *
		 * @deprecated
		 */
		getId: function() {
			_deprecated('getId', this);

			return this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.ID];
		},

		/**
		 * @param {String}
		 *
		 * @deprecated
		 */
		getLabel: function() {
			_deprecated('getLabel', this);

			return this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.LABEL];
		}
	});

})();