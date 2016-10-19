(function () {

	/**
	 * @link CMDBuild.controller.administration.classes.tabs.Domains
	 */
	Ext.define('CMDBuild.controller.administration.workflow.tabs.Domains', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.administration.workflow.tabs.Domains'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.workflow.Workflow}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWorkflowTabDomainsAddButtonClick',
			'onWorkflowTabDomainsAddWorkflowButtonClick',
			'onWorkflowTabDomainsIncludeInheritedCheck',
			'onWorkflowTabDomainsItemDoubleClick',
			'onWorkflowTabDomainsModifyButtonClick',
			'onWorkflowTabDomainsRemoveButtonClick',
			'onWorkflowTabDomainsRowSelect',
			'onWorkflowTabDomainsShow',
			'onWorkflowTabDomainsStoreLoad',
			'onWorkflowTabDomainsWorkflowSelected'
		],

		/**
		 * @property {CMDBuild.view.administration.workflow.tabs.domains.GridPanel}
		 */
		grid: undefined,

		/**
		 * Just the grid subset of domain properties, not a full domain object
		 *
		 * @property {CMDBuild.model.administration.workflow.tabs.domains.Grid}
		 *
		 * @private
		 */
		selectedDomain: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.workflow.tabs.domains.DomainsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.workflow.Workflow} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.workflow.tabs.domains.DomainsView', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
			this.includeInheritedCheckbox = this.view.includeInheritedCheckbox;
		},

		/**
		 * @returns {Void}
		 *
		 * FIXME: refactor with external services standards
		 */
		onWorkflowTabDomainsAddButtonClick: function () {
			if (
				this.cmfg('mainViewportAccordionControllerExists', CMDBuild.core.constants.ModuleIdentifiers.getDomain())
				&& this.cmfg('mainViewportModuleControllerExists', CMDBuild.core.constants.ModuleIdentifiers.getDomain())
			) {
				var accordionController = this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()),
					moduleController = this.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain());

				Ext.apply(accordionController, {
					disableSelection: true,
					scope: this,
					callback: function () {
						accordionController.cmfg('accordionDeselect');

						moduleController.cmfg('onDomainAddButtonClick');
					}
				});

				accordionController.cmfg('accordionExpand');
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsAddWorkflowButtonClick: function () {
			this.view.disable();
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsIncludeInheritedCheck: function () {
			if (this.includeInheritedCheckbox.getValue()) {
				this.grid.getStore().clearFilter();
			} else {
				this.grid.getStore().filterBy(function (record) {
					return !record.get(CMDBuild.core.constants.Proxy.INHERITED);
				});
			}
		},

		/**
		 * @returns {Void}
		 *
		 * FIXME: refactor with external services standards
		 */
		onWorkflowTabDomainsItemDoubleClick: function () {
			if (
				!this.selectedDomainIsEmpty()
				&& this.cmfg('mainViewportAccordionControllerExists', CMDBuild.core.constants.ModuleIdentifiers.getDomain())
			) {
				var accordionController = this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain());

				Ext.apply(accordionController, {
					disableSelection: true,
					scope: this,
					callback: function () {
						accordionController.cmfg('accordionDeselect');
						accordionController.cmfg('accordionNodeByIdSelect', { id: this.selectedDomainGet(CMDBuild.core.constants.Proxy.ID_DOMAIN) });
					}
				});

				accordionController.cmfg('accordionExpand');
			}
		},

		/**
		 * @returns {Void}
		 *
		 * FIXME: refactor with external services standards
		 */
		onWorkflowTabDomainsModifyButtonClick: function () {
			if (
				!this.selectedDomainIsEmpty()
				&& this.cmfg('mainViewportAccordionControllerExists', CMDBuild.core.constants.ModuleIdentifiers.getDomain())
				&& this.cmfg('mainViewportModuleControllerExists', CMDBuild.core.constants.ModuleIdentifiers.getDomain())
			) {
				var accordionController = this.cmfg('mainViewportAccordionControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain()),
					moduleController = this.cmfg('mainViewportModuleControllerGet', CMDBuild.core.constants.ModuleIdentifiers.getDomain());

				Ext.apply(accordionController, {
					disableSelection: true,
					scope: this,
					callback: function () {
						accordionController.cmfg('accordionDeselect');
						accordionController.cmfg('accordionNodeByIdSelect', { id: this.selectedDomainGet(CMDBuild.core.constants.Proxy.ID_DOMAIN) });

						Ext.Function.createDelayed(function () { // FIXME: fix me avoid delay
							moduleController.cmfg('onDomainModifyButtonClick');
						}, 100, this)();
					}
				});

				accordionController.cmfg('accordionExpand');
			}
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsRemoveButtonClick: function () {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsRowSelect: function () {
			if (this.grid.getSelectionModel().hasSelection())
				this.selectedDomainSet({ value: this.grid.getSelectionModel().getSelection()[0] });

			this.view.setDisabledTopBar(!this.grid.getSelectionModel().hasSelection());
		},

		/**
		 * @returns {Void}
		 */
		onWorkflowTabDomainsShow: function () {
			if (!this.cmfg('workflowSelectedWorkflowIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.NAME);

				this.grid.getStore().load({
					params: params,
					scope: this,
					callback: function (records, operation, success) {
						if (!this.grid.getSelectionModel().hasSelection())
							this.grid.getSelectionModel().select(0, true);

						this.cmfg('onWorkflowTabDomainsRowSelect');
					}
				});
			}
		},

		/**
		 * Translations of grid records domain's class name to description
		 *
		 * @returns {Void}
		 *
		 * FIXME: waiting for refactor (rename)
		 */
		onWorkflowTabDomainsStoreLoad: function () {
			if (!Ext.isEmpty(this.grid.getStore().getRange()) && Ext.isArray(this.grid.getStore().getRange())) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.administration.workflow.tabs.Domains.readAllEntryTypes({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (Ext.isArray(decodedResponse) && !Ext.isEmpty(decodedResponse))
							Ext.Array.each(this.grid.getStore().getRange(), function (gridRecord, i, allGridRecords) {
								var foundClassObject = undefined;

								// Translate class1 name to description
								foundClassObject = Ext.Array.findBy(decodedResponse, function (record, i) {
									return gridRecord.get('class1') == record[CMDBuild.core.constants.Proxy.NAME];
								}, this);

								if (!Ext.isEmpty(foundClassObject))
									gridRecord.set('class1', foundClassObject[CMDBuild.core.constants.Proxy.TEXT]);

								// Translate class2 name to description
								foundClassObject = Ext.Array.findBy(decodedResponse, function (record, i) {
									return gridRecord.get('class2') == record[CMDBuild.core.constants.Proxy.NAME];
								}, this);

								if (!Ext.isEmpty(foundClassObject))
									gridRecord.set('class2', foundClassObject[CMDBuild.core.constants.Proxy.TEXT]);

								gridRecord.commit();
							}, this);
					}
				});
			}
		},

		/**
		 * Enable/Disable tab on workflow selection
		 *
		 * @returns {Void}
		 */
		onWorkflowTabDomainsWorkflowSelected: function () {
			this.view.setDisabled(
				this.cmfg('workflowSelectedWorkflowIsEmpty')
				|| this.cmfg('workflowSelectedWorkflowGet', CMDBuild.core.constants.Proxy.TABLE_TYPE) == CMDBuild.core.constants.Global.getTableTypeSimpleTable()
			);
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		removeItem: function () {
			if (!this.selectedDomainIsEmpty()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = this.selectedDomainGet(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.administration.workflow.tabs.Domains.remove({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.selectedDomainReset();

						this.cmfg('onWorkflowTabDomainsShow');
					}
				});
			}
		},

		// SelectedDomain property functions
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 *
			 * @private
			 */
			selectedDomainGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 *
			 * @private
			 */
			selectedDomainIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			selectedDomainReset: function (parameters) {
				this.propertyManageReset('selectedDomain');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			selectedDomainSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.administration.workflow.tabs.domains.Grid';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';

					this.propertyManageSet(parameters);
				}
			}
	});

})();
