(function() {

	Ext.define('CMDBuild.controller.administration.dataView.Filter', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.dataView.Filter',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.DataView}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onDataViewFilterAbortButtonClick',
			'onDataViewFilterAddButtonClick',
			'onDataViewFilterModifyButtonClick = onDataViewFilterItemDoubleClick',
			'onDataViewFilterRemoveButtonClick',
			'onDataViewFilterRowSelected',
			'onDataViewFilterSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.dataView.Filter}
		 */
		selectedView: undefined,

		/**
		 * @property {CMDBuild.view.administration.dataView.filter.FilterView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.dataView.DataView} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.dataView.filter.FilterView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onDataViewFilterAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedView)) {
				this.onDataViewFilterRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onDataViewFilterAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedView = null;

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.dataView.Filter'));
		},

		onDataViewFilterModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onDataViewFilterRemoveButtonClick: function() {
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
		 * TODO: waiting for refactor (crud)
		 */
		onDataViewFilterRowSelected: function() {
			this.selectedView = this.grid.getSelectionModel().getSelection()[0];

			// TODO: waiting for refacotr
			if (Ext.isString(this.selectedView.get(CMDBuild.core.constants.Proxy.FILTER)))
				this.selectedView.set(CMDBuild.core.constants.Proxy.FILTER, Ext.decode(this.selectedView.get(CMDBuild.core.constants.Proxy.FILTER)));

			this.form.loadRecord(this.selectedView);
			this.form.setDisabledModify(true, true);
		},

		onDataViewFilterSaveButtonClick: function() {
			if (this.validate(this.form)) {
				var formDataModel = Ext.create('CMDBuild.model.dataView.Filter', this.form.getData(true));

				var params = formDataModel.getData();
				params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(params[CMDBuild.core.constants.Proxy.FILTER]);

				if (Ext.isEmpty(formDataModel.get(CMDBuild.core.constants.Proxy.ID))) {
					CMDBuild.core.proxy.dataView.Filter.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.dataView.Filter.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedView)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.selectedView.get(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.core.proxy.dataView.Filter.remove({
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
									this.form.setDisabledModify(true, true, true, true);
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

			CMDBuild.view.common.field.translatable.Utils.commit(this.form);

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
		}
	});

})();