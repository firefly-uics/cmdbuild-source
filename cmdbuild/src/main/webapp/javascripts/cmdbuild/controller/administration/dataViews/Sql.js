(function() {

	Ext.define('CMDBuild.controller.administration.dataViews.Sql', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.dataViews.Filter',
			'CMDBuild.model.DataViews'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.dataViews.DataViews}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onDataViewSqlAbortButtonClick',
			'onDataViewSqlAddButtonClick',
			'onDataViewSqlModifyButtonClick = onDataViewSqlItemDoubleClick',
			'onDataViewSqlRemoveButtonClick',
			'onDataViewSqlRowSelected',
			'onDataViewSqlSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.dataViews.sql.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataViews.sql.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.DataViews.sql}
		 */
		selectedView: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataViews.sql.SqlView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.dataViews.DataViews} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.dataViews.sql.SqlView', {
				delegate: this
			});

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onDataViewSqlAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedView)) {
				this.onDataViewSqlRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onDataViewSqlAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedView = null;

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.DataViews.sql'));
		},

		onDataViewSqlModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onDataViewSqlRemoveButtonClick: function() {
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

		/**
		 * TODO: server implementation to get a single view data
		 */
		onDataViewSqlRowSelected: function() {
			this.selectedView = this.grid.getSelectionModel().getSelection()[0];

			this.form.loadRecord(this.selectedView);

			// Translation setup
			Ext.apply(this.form.descriptionTextField, {
				translationsKeyName: this.selectedView.get(CMDBuild.core.proxy.CMProxyConstants.NAME)
			});

			this.form.setDisabledModify(true, true);
		},

		onDataViewSqlSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = Ext.create('CMDBuild.model.DataViews.sql',this.form.getData(true));

				if (Ext.isEmpty(formData.get(CMDBuild.core.proxy.CMProxyConstants.ID))) {
					CMDBuild.core.proxy.dataViews.Sql.create({
						params: formData.getData(),
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.dataViews.Sql.update({
						params: formData.getData(),
						scope: this,
						success: this.success
					});
				}
			}
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedView)) {
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.ID] = this.selectedView.get(CMDBuild.core.proxy.CMProxyConstants.ID);

				CMDBuild.core.proxy.dataViews.Sql.remove({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						this.form.reset();

						this.grid.getStore().load({
							scope: this,
							callback: function(records, operation, success) {
								this.grid.getSelectionModel().select(0, true);

								// If no selections disable all UI
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

			_CMCache.flushTranslationsToSave(options.params[CMDBuild.core.proxy.CMProxyConstants.NAME]);

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
		}
	});

})();