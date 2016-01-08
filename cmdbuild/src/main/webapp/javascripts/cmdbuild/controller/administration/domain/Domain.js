(function() {

	Ext.define('CMDBuild.controller.administration.domain.Domain', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.domain.Domain',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'domainSelectedDomainGet',
			'domainSelectedDomainIsEmpty',
			'domainSelectedDomainSet',
			'onDomainAbortButtonClick',
			'onDomainAddButtonClick',
			'onDomainModifyButtonClick',
			'onDomainRemoveButtonClick',
			'onDomainSaveButtonClick',
			'onDomainSelected -> controllerProperties, controllerEnabledClasses, controllerAttributes'
		],

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @property {CMDBuild.controller.administration.domain.Attributes}
		 */
		controllerAttributes: undefined,

		/**
		 * @property {CMDBuild.controller.administration.domain.EnabledClasses}
		 */
		controllerEnabledClasses: undefined,

		/**
		 * @property {CMDBuild.controller.administration.domain.Properties}
		 */
		controllerProperties: undefined,

		/**
		 * @property {CMDBuild.model.domain.Domain}
		 */
		selectedDomain: null,

		/**
		 * @cfg {CMDBuild.view.administration.domain.DomainView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.domain.DomainView} view
		 */
		constructor: function(view) {
			this.callParent(arguments);

			// Controller build
			this.controllerAttributes = Ext.create('CMDBuild.controller.administration.domain.Attributes', { parentDelegate: this });
			this.controllerEnabledClasses = Ext.create('CMDBuild.controller.administration.domain.EnabledClasses', { parentDelegate: this });
			this.controllerProperties = Ext.create('CMDBuild.controller.administration.domain.Properties', { parentDelegate: this });

			// Inject tabs
			this.view.tabPanel.add(this.controllerProperties.getView());
			this.view.tabPanel.add(this.controllerEnabledClasses.getView());
			this.view.tabPanel.add(this.controllerAttributes.getView());
		},

		/**
		 * Method forwarder
		 */
		onDomainAbortButtonClick: function() {
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesAbortButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesAbortButtonClick');
		},

		onDomainAddButtonClick: function() {
			_CMMainViewportController.deselectAccordionByName(this.cmName);

			this.setViewTitle();

			this.controllerAttributes.cmfg('onDomainAddButtonClick');
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesAddButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesAddButtonClick');

			this.view.tabPanel.setActiveTab(0);
		},

		/**
		 * Method forwarder
		 */
		onDomainModifyButtonClick: function() {
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesModifyButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesModifyButtonClick');
		},

		onDomainRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.removeDomain,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function(buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onDomainSaveButtonClick: function() {
			if (this.validate(this.controllerProperties.getView().form)) {
				var params = {};

				params = Ext.Object.merge(params, this.controllerEnabledClasses.getData());
				params = Ext.Object.merge(params, this.controllerProperties.getData());

				if (Ext.isEmpty(params[CMDBuild.core.constants.Proxy.ID])) {
					CMDBuild.core.proxy.domain.Domain.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.domain.Domain.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * @param {CMDBuild.model.common.accordion.Generic} parameters
		 *
		 * TODO: waiting for refactor (crud)
		 */
		onViewOnFront: function(parameters) {
			if (!Ext.isEmpty(parameters)) {
				var params = {};

				CMDBuild.core.proxy.domain.Domain.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

						this.domainSelectedDomainSet(
							Ext.Array.findBy(decodedResponse, function(item, i) {
								return parameters.get(CMDBuild.core.constants.Proxy.ENTITY_ID) == item[CMDBuild.core.constants.Proxy.ID_DOMAIN];
							}, this)
						);

						this.cmfg('onDomainSelected');

						this.setViewTitle(parameters.get(CMDBuild.core.constants.Proxy.TEXT));

						if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
							this.view.tabPanel.setActiveTab(0);
					}
				});
			}
		},

		removeItem: function() {
			if (!this.domainSelectedDomainIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = this.domainSelectedDomainGet(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.core.proxy.domain.Domain.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.controllerProperties.getView().form.reset();
						this.controllerProperties.getView().form.setDisabledModify(true);

						_CMCache.onDomainDeleted(this.domainSelectedDomainGet(CMDBuild.core.constants.Proxy.ID));
						_CMMainViewportController.findAccordionByCMName(this.cmName).updateStore();
					}
				});
			}
		},

		success: function(response, options, decodedResponse) {
			this.view.tabPanel.setActiveTab(0);
			this.view.tabPanel.getActiveTab().form.setDisabledModify(true);

			_CMCache.onDomainSaved(decodedResponse.domain);
			_CMMainViewportController.findAccordionByCMName(this.cmName).updateStore(decodedResponse[CMDBuild.core.constants.Proxy.DOMAIN][CMDBuild.core.constants.Proxy.ID_DOMAIN]);

			CMDBuild.view.common.field.translatable.Utils.commit(this.controllerProperties.getView().form);
		},

		// SelectedDomain property methods
			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.domain.Domain} or Mixed
			 */
			domainSelectedDomainGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.selectedDomain.get(parameterName);

				return this.selectedDomain;
			},

			/**
			 * @returns {Boolean}
			 */
			domainSelectedDomainIsEmpty: function() {
				return Ext.isEmpty(this.selectedDomain);
			},

			/**
			 * @param {Object} selectedDomainObject
			 */
			domainSelectedDomainSet: function(selectedDomainObject) {
				this.selectedDomain = null;

				if (!Ext.isEmpty(selectedDomainObject) && Ext.isObject(selectedDomainObject)) {
					if (Ext.getClassName(selectedDomainObject) == 'CMDBuild.model.domain.Domain') {
						this.selectedDomain = selectedDomainObject;
					} else {
						this.selectedDomain = Ext.create('CMDBuild.model.domain.Domain', selectedDomainObject);
					}
				}
			}
	});

})();