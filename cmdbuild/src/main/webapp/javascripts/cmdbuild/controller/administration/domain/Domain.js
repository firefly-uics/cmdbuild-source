(function() {

	Ext.define('CMDBuild.controller.administration.domain.Domain', {
		extend: 'CMDBuild.controller.common.AbstractBasePanelController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Domain'
		],

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
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onDomainAbortButtonClick',
			'onDomainAddButtonClick',
			'onDomainModifyButtonClick',
			'onDomainRemoveButtonClick',
			'onDomainSaveButtonClick',
			'onDomainSelected -> controllerAttributes, controllerEnabledClasses, controllerProperties',
			'selectedDomainGet',
			'selectedDomainSet',
		],

		/**
		 * @property {CMDBuild.cache.CMDomainModel}
		 */
		selectedDomain: null,

		/**
		 * @cfg {String}
		 */
		titleSeparator: ' - ',

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

		onDomainAbortButtonClick: function() {
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesAbortButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesAbortButtonClick');
		},

		onDomainAddButtonClick: function() {
			_CMMainViewportController.deselectAccordionByName('domain');

			this.controllerAttributes.cmfg('onDomainAddButtonClick');
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesAddButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesAddButtonClick');

			this.view.tabPanel.setActiveTab(0);
		},

		onDomainModifyButtonClick: function() {
			this.controllerEnabledClasses.cmfg('onDomainEnabledClassesModifyButtonClick');
			this.controllerProperties.cmfg('onDomainPropertiesModifyButtonClick');
		},

		onDomainRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title, // TODO delete domain
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * TODO: should be separated in two server calls (create, update) as usual. Needed server refactor.
		 */
		onDomainSaveButtonClick: function() {
			if (this.validate(this.controllerProperties.getView())) {
				var originDisabledClasses = [];
				var destinationDisabledClasses = [];
				var data = this.controllerProperties.getView().getData(true);

				// Get origin disabled classes
				this.controllerEnabledClasses.getEnabledTreeVisit(this.controllerEnabledClasses.getView().originTree.getStore().getRootNode(), originDisabledClasses);

				// Get destination disabled classes
				this.controllerEnabledClasses.getEnabledTreeVisit(this.controllerEnabledClasses.getView().destinationTree.getStore().getRootNode(), destinationDisabledClasses);

				if (Ext.isEmpty(this.selectedDomain)) {
					data[CMDBuild.core.proxy.CMProxyConstants.ID] = -1;
				} else {
					data[CMDBuild.core.proxy.CMProxyConstants.ID] = this.selectedDomain.get(CMDBuild.core.proxy.CMProxyConstants.ID);

					data['disabled1'] = Ext.encode(originDisabledClasses);
					data['disabled2'] = Ext.encode(destinationDisabledClasses);
				}

				CMDBuild.core.proxy.Domain.update({
					params: data,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.view.tabPanel.getActiveTab().setDisabledModify(true);

						_CMCache.onDomainSaved(decodedResponse.domain);
						_CMCache.flushTranslationsToSave(decodedResponse.domain.name);
					}
				});

			} else {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			}
		},

		/**
		 * @param {CMDBuild.view.common.CMAccordionStoreModel} parameters
		 */
		onViewOnFront: function(parameters) {
			if (!Ext.isEmpty(parameters)) {
				this.selectedDomain = _CMCache.getDomainById(parameters.get(CMDBuild.core.proxy.CMProxyConstants.ID)); // TODO: use proxy to read domain

				this.cmfg('onDomainSelected');

				this.setViewTitle(parameters.get(CMDBuild.core.proxy.CMProxyConstants.TEXT));

				if (Ext.isEmpty(this.view.tabPanel.getActiveTab()))
					this.view.tabPanel.setActiveTab(0);
			}
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedDomain)) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME] = this.selectedDomain.get(CMDBuild.core.proxy.CMProxyConstants.NAME);

				CMDBuild.core.proxy.Domain.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.controllerProperties.getView().reset();

						this.controllerProperties.getView().setDisabledModify(true);

						// TODO: to delete when old cache system will be disabled
						_CMCache.onDomainDeleted(this.selectedDomain.get(CMDBuild.core.proxy.CMProxyConstants.ID));
					}
				});
			}
		},

		/**
		 * @return {CMDBuild.cache.CMDomainModel}
		 */
		selectedDomainGet: function() {
			return this.selectedDomain;
		},

		/**
		 * @param {CMDBuild.cache.CMDomainModel} selectedDomain
		 */
		selectedDomainSet: function(selectedDomain) {
			if (Ext.isEmpty(selectedDomain)) {
				this.selectedDomain = null;
			} else {
				this.selectedDomain = selectedDomain;
			}
		},

		/**
		 * Setup view panel title as a breadcrumbs component
		 *
		 * @param {String} titlePart
		 */
		setViewTitle: function(titlePart) {
			if (Ext.isEmpty(titlePart)) {
				this.view.setTitle(this.view.baseTitle);
			} else {
				this.view.setTitle(this.view.baseTitle + this.titleSeparator + titlePart);
			}
		}
	});

})();