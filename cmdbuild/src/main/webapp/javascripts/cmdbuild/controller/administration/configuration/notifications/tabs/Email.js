(function () {

	Ext.define('CMDBuild.controller.administration.configuration.notifications.tabs.Email', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.configuration.notifications.tabs.Email'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.notifications.Notifications}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'configurationNotificationsTabEmailDataGet',
			'configurationNotificationsTabEmailIsValid',
			'onConfigurationNotificationsTabEmailEnabledChange',
			'onConfigurationNotificationsTabEmailShow',
			'onConfigurationNotificationsTabEmailTemplateSelect'
		],

		/**
		 * @property {CMDBuild.view.administration.configuration.notifications.tabs.EmailPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.configuration.notifications.Notifications} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.configuration.notifications.tabs.EmailPanel', { delegate: this });

			this.setupFieldsDefault();
		},

		/**
		 * @returns {Object}
		 */
		configurationNotificationsTabEmailDataGet: function () {
			return this.view.panelFunctionDataGet({ includeDisabled: true });
		},

		/**
		 * @returns {Boolean}
		 */
		configurationNotificationsTabEmailIsValid: function () {
			if (this.view.panelFunctionValueGet({ propertyName: CMDBuild.core.constants.Proxy.ENABLED }))
				return this.validate(this.view);

			return true;
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationNotificationsTabEmailEnabledChange: function () {
			this.view.fieldTemplate.allowBlank = !this.view.panelFunctionValueGet({ propertyName: CMDBuild.core.constants.Proxy.ENABLED });
		},

		/**
		 * @returns {Void}
		 */
		onConfigurationNotificationsTabEmailShow: function () {
			// Error handling
				if (this.cmfg('configurationNotificationsConfigurationIsEmpty'))
					return _error('onConfigurationNotificationsTabEmailShow(): empty configuration model', this, this.cmfg('configurationNotificationsConfigurationGet'));
			// END: Error handling

			this.view.reset();
			this.view.loadRecord(this.cmfg('configurationNotificationsConfigurationGet'));
		},

		/**
		 * @param {String} name
		 *
		 * @returns {Void}
		 */
		onConfigurationNotificationsTabEmailTemplateSelect: function () {
			var name = this.view.panelFunctionValueGet({ propertyName: CMDBuild.core.constants.Proxy.TEMPLATE });

			if (Ext.isString(name) && !Ext.isEmpty(name)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = name;

				CMDBuild.proxy.administration.configuration.notifications.tabs.Email.readTemplate({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

						if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
							this.setupFieldsAccount(
								Ext.isString(decodedResponse[CMDBuild.core.constants.Proxy.DEFAULT_ACCOUNT]) && !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.DEFAULT_ACCOUNT])
							);

							this.setupFieldsDestination(
								Ext.isString(decodedResponse[CMDBuild.core.constants.Proxy.BCC]) && !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.BCC])
								|| Ext.isString(decodedResponse[CMDBuild.core.constants.Proxy.CC]) && !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.CC])
								|| Ext.isString(decodedResponse[CMDBuild.core.constants.Proxy.TO]) && !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.TO])
							);
						} else {
							_error('onConfigurationNotificationsTabEmailTemplateSelect(): unmanaged response', this, decodedResponse);
						}
					}
				});
			} else {
				this.setupFieldsDefault();
			}
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		setupFieldsAccount: function (state) {
			state = Ext.isBoolean(state) ? state : true;

			this.view.fieldAccount.reset();
			this.view.fieldAccount.allowBlank = state;
			this.view.fieldAccount.setDisabled(state);
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		setupFieldsDefault: function () {
			this.setupFieldsAccount();
			this.setupFieldsDestination();
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		setupFieldsDestination: function (state) {
			state = Ext.isBoolean(state) ? state : true;

			this.view.fieldDestination.reset();
			this.view.fieldDestination.allowBlank = state;
			this.view.fieldDestination.setDisabled(state);
		}
	});

})();
