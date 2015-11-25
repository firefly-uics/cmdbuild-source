(function() {

	Ext.define('CMDBuild.controller.administration.email.templates.Templates', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.email.Templates',
			'CMDBuild.model.email.Templates'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.Email}
		 */
		parentDelegate: undefined,

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
		 * @property {CMDBuild.model.email.Templates.singleTemplate}
		 */
		selectedTemplate: undefined,

		/**
		 * Values windows grid data
		 *
		 * @property {Array}
		 */
		valuesData: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.templates.TemplatesView}
		 */
		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.email.templates.TemplatesView} view
		 *
		 * @override
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.templates.TemplatesView', {
				delegate: this
			});

			// Shorthands
			this.grid = this.view.grid;
			this.form = this.view.form;
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
			this.form.loadRecord(Ext.create('CMDBuild.model.email.Templates.singleTemplate'));
		},

		onEmailTemplatesModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onEmailTemplatesRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.common.confirmpopup.title,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function(buttonId, text, opt) {
					if (buttonId == 'yes')
						this.removeItem();
				}
			});
		},

		onEmailTemplatesRowSelected: function() {
			if (this.grid.getSelectionModel().hasSelection()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.grid.getSelectionModel().getSelection()[0].get(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.core.proxy.email.Templates.get({
					params: params,
					scope: this,
					failure: function(response, options, decodedResponse) {
						CMDBuild.Msg.error(
							CMDBuild.Translation.common.failure,
							Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure, this.selectedTemplate.get(CMDBuild.core.constants.Proxy.NAME)),
							false
						);
					},
					success: function(response, options, decodedResponse) {
						this.selectedTemplate = Ext.create('CMDBuild.model.email.Templates.singleTemplate', decodedResponse.response);

						this.form.loadRecord(this.selectedTemplate);
						this.form.delayField.setValue(this.selectedTemplate.get(CMDBuild.core.constants.Proxy.DELAY)); // Manual setup to avoid load record bug
						this.valuesData = this.selectedTemplate.get(CMDBuild.core.constants.Proxy.VARIABLES);
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
				formData[CMDBuild.core.constants.Proxy.VARIABLES] = Ext.encode(this.valuesData);

				if (Ext.isEmpty(formData.id)) {
					CMDBuild.core.proxy.email.Templates.create({
						params: formData,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.email.Templates.update({
						params: formData,
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
				var params = {};
				params[CMDBuild.core.constants.Proxy.NAME] = this.selectedTemplate.get(CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.core.proxy.email.Templates.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function(records, operation, success) {
								this.grid.getSelectionModel().select(0, true);

								if (!this.grid.getSelectionModel().hasSelection())
									this.form.setDisabledModify(true, true, true);
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
						CMDBuild.core.constants.Proxy.NAME,
						me.form.getForm().findField(CMDBuild.core.constants.Proxy.NAME).getValue()
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