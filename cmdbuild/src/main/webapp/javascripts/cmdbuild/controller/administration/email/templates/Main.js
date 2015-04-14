(function() {

	Ext.define('CMDBuild.controller.administration.email.templates.Main', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEmailTemplatesAbortButtonClick',
			'onEmailTemplatesAddButtonClick',
			'onEmailTemplatesModifyButtonClick = onEmailTemplatesItemDoubleClick',
			'onEmailTemplatesRemoveButtonClick',
			'onEmailTemplatesRowSelected',
			'onEmailTemplatesSaveButtonClick',
			'onEmailTemplatesValuesButtonClick',
			'valuesDataSet',
			'valuesDataGet'
		],

		/**
		 * @property {CMDBuild.view.administration.email.templates.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.templates.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {String}
		 */
		selectedTemplate: undefined,

		/**
		 * Values windows grid data
		 *
		 * @property {Array}
		 */
		valuesData: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.templates.MainPanel}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.email.templates.MainPanel} view
		 *
		 * @override
		 */
		constructor: function(view) {
			// TODO: in future would be nice to implement "this.callParent(arguments);" ... mainwhile i do it manually
			this.stringToFunctionNameMap = {};
			this.decodeCatchedFunctionsArray();
			this.view = view;

			// Handlers exchange
			this.grid = view.grid;
			this.form = view.form;
			this.view.delegate = this;
			this.grid.delegate = this;
			this.form.delegate = this;

		},

		loadFieldsStore: function() {
			this.form.defaultAccountCombo.getStore().load();
		},

		onEmailTemplatesAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedTemplate)) {
				this.onEmailTemplatesRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onEmailTemplatesAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();
			this.selectedTemplate = null;
			this.valuesData = null;
			this.form.reset();
			this.form.setDisabledModify(false, true);

			this.loadFieldsStore();
		},

		onEmailTemplatesModifyButtonClick: function() {
			this.form.setDisabledModify(false);

			this.loadFieldsStore();
		},

		onEmailTemplatesRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes')
						this.removeItem();
				}
			});
		},

		onEmailTemplatesRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				this.selectedTemplate = this.grid.getSelectionModel().getSelection()[0];

				CMDBuild.core.proxy.EmailTemplates.get({
					params: {
						name: this.selectedTemplate.get(CMDBuild.core.proxy.CMProxyConstants.NAME)
					},
					loadMask: true,
					scope: this,
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(
							CMDBuild.Translation.common.failure,
							Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure, this.selectedTemplate.get(CMDBuild.core.proxy.CMProxyConstants.NAME)),
							false
						);
					},
					success: function(response, options, decodedResponse) {
						var templateModel = Ext.create('CMDBuild.model.EmailTemplates.singleTemplate', decodedResponse.response);

						this.form.loadRecord(templateModel);
						this.form.delayField.setValue(templateModel.get(CMDBuild.core.proxy.CMProxyConstants.DELAY)); // Manual setup to avoid load record bug
						this.valuesData = templateModel.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES);
						this.form.setDisabledModify(true, true);
					}
				});
			}
		},

		onEmailTemplatesSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);

				// To put and encode variablesWindow grid values
				formData[CMDBuild.core.proxy.CMProxyConstants.VARIABLES] = Ext.encode(this.valuesData);

				if (Ext.isEmpty(formData.id)) {
					CMDBuild.core.proxy.EmailTemplates.create({
						params: formData,
						loadMask: true,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.EmailTemplates.update({
						params: formData,
						loadMask: true,
						scope: this,
						success: this.success
					});
				}
			}
		},

		onEmailTemplatesValuesButtonClick: function() {
			Ext.create('CMDBuild.controller.administration.email.templates.Values', {
				parentDelegate: this
			});
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedTemplate)) {
				CMDBuild.core.proxy.EmailTemplates.remove({
					params: {
						name: this.selectedTemplate.get(CMDBuild.core.proxy.CMProxyConstants.NAME)
					},
					loadMask: true,
					scope: this,
					success: function() {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function() {
								this.grid.getSelectionModel().select(0, true);

								if (!this.grid.getSelectionModel().hasSelection())
									this.form.setDisabledModify(true);
							}
						});
					}
				});
			}
		},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			var me = this;

			this.grid.getStore().load({
				callback: function(records, operation, success) {
					var rowIndex = this.find(
						CMDBuild.core.proxy.CMProxyConstants.NAME,
						me.form.getForm().findField(CMDBuild.core.proxy.CMProxyConstants.NAME).getValue()
					);

					me.grid.getSelectionModel().select(rowIndex, true);
					me.form.setDisabledModify(true);
				}
			});
		},

		/**
		 * @return {Object}
		 */
		valuesDataGet: function() {
			return this.valuesData;
		},

		/**
		 * @param {Object} dataObject
		 */
		valuesDataSet: function(dataObject) {
			this.valuesData = dataObject || {};
		}
	});

})();