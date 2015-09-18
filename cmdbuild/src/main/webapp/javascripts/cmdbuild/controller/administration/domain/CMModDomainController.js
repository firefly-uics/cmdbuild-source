(function() {

	Ext.define("CMDBuild.controller.administration.domain.CMModDomainController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @property {CMDBuild.controller.administration.domain.EnabledClasses}
		 */
		controllerEnabledClasses: undefined,

		/**
		 * @property {CMDBuild.cache.CMDomainModel}
		 */
		selectedDomain: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.domain.CMModDomain}
		 */
		view: undefined,

		constructor: function(view) {
			this.callParent(arguments);

			this.domain = null;
			this.formController = new CMDBuild.controller.administration.domain.CMDomainFormController(this.view.domainForm);
			this.attributesController = new CMDBuild.controller.administration.domain.CMDomainAttributesController(this.view.domainAttributes);

			// Temporary delegate bind, will be refactored
			this.view.delegate = this;
			this.formController.parentDelegate = this;
			this.attributesController.parentDelegate = this;
			this.view.domainForm.delegate = this.formController;
			this.view.domainAttributes.delegate = this.attributesController;

			// Controller build
			this.controllerEnabledClasses = Ext.create('CMDBuild.controller.administration.domain.EnabledClasses', { parentDelegate: this });

			// Inject tabs
			this.view.tabPanel.add(this.controllerEnabledClasses.getView());

			this.view.addButton.on("click", this.onAddDomainButtonClick, this);
			_CMCache.on("cm_domain_deleted", this.view.onDomainDeleted, this.view);
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onEnabledClassesSaveButtonClick':
				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onAbortButtonClick: function() {
			this.formController.onAbortButtonClick();
			this.controllerEnabledClasses.onAbortButtonClick();
		},

		onModifyButtonClick: function() {
			this.formController.view.enableModify();
			this.controllerEnabledClasses.onModifyButtonClick();
		},

		onViewOnFront: function(selection) {
			if (selection) {
				this.selectedDomain = _CMCache.getDomainById(selection.get(CMDBuild.core.proxy.CMProxyConstants.ID)); // TODO: use proxy to read class

				this.controllerEnabledClasses.onDomainSelected();

				this.domain = _CMCache.getDomainById(selection.get("id")); // TODO: delete and keep selectedDomain
				this.formController.onDomainSelected(this.domain);
				this.attributesController.onDomainSelected(this.domain);
				this.view.setTitleSuffix(this.domain.get("description"));
			}
		},

		onAddDomainButtonClick: function() {
			_CMMainViewportController.deselectAccordionByName("domain");
			this.view.selectPropertiesTab();
			this.formController.onAddButtonClick();
			this.controllerEnabledClasses.onAddButtonClick();
			this.attributesController.onAddButtonClick();
		},

		onSaveButtonClick: function() {
			var originDisabledClasses = [];
			var destinationDisabledClasses = [];

			// Get origin disabled classes
			this.controllerEnabledClasses.getEnabledTreeVisit(this.controllerEnabledClasses.getView().originTree.getStore().getRootNode(), originDisabledClasses);

			// Get destination disabled classes
			this.controllerEnabledClasses.getEnabledTreeVisit(this.controllerEnabledClasses.getView().destinationTree.getStore().getRootNode(), destinationDisabledClasses);

			var invalidFields = this.view.domainForm.getNonValidFields();
			if (invalidFields.length == 0) {
				CMDBuild.LoadMask.get().show();
				var withDisabled = true;
				var data = this.view.domainForm.getData(withDisabled);
				if (this.formController.currentDomain == null) {
					data.id = -1;
				} else {
					data.id = this.formController.currentDomain.get(CMDBuild.core.proxy.CMProxyConstants.ID);

					data.disabled1 = Ext.encode(originDisabledClasses); // TODO proxy constants
					data.disabled2 = Ext.encode(destinationDisabledClasses); // TODO proxy constants
				}

				CMDBuild.ServiceProxy.administration.domain.save({
					params: data,
					scope: this,
					success: function(req, res, decoded) {
						this.view.domainForm.disableModify();
						_CMCache.onDomainSaved(decoded.domain);
						_CMCache.flushTranslationsToSave(decoded.domain.name);
					},
					callback: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});

			} else {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			}
		},
	});

	function onAttributeSaved(jsonAttribute) {
		if (this.domain != null) {
			try {
				var attributeLibary = this.domain.getAttributeLibrary();
				var newAttribute = CMDBuild.core.model.CMAttributeModel.buildFromJson(jsonAttribute);
				var oldAttribute = attributeLibary.get(newAttribute.getname());

				if (oldAttribute == null) {
					this.attributesController.beforeAddAttributeToLibrary(newAttribute);
					attributeLibary.add(newAttribute);
				} else {
					oldAttribute.update(newAttribute);
				}
			} catch (e) {
				_debug(e);
			}
		}
	};

})();