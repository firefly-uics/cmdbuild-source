(function() {

	Ext.define('CMDBuild.controller.administration.filter.Groups', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.filter.Groups',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.filter.Filter}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFilterGroupsAbortButtonClick',
			'onFilterGroupsAddButtonClick',
			'onFilterGroupsModifyButtonClick = onFilterGroupsItemDoubleClick',
			'onFilterGroupsRemoveButtonClick',
			'onFilterGroupsRowSelected',
			'onFilterGroupsSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.filter.groups.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.filter.Groups}
		 */
		selectedFilter: undefined,

		/**
		 * @property {CMDBuild.view.administration.filter.groups.GroupsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.filter.Filter} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.filter.groups.GroupsView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
			this.grid = this.view.grid;
		},

		onFilterGroupsAbortButtonClick: function() {
			if (!Ext.isEmpty(this.selectedFilter)) {
				this.onFilterGroupsRowSelected();
			} else {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			}
		},

		onFilterGroupsAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedFilter = null;

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.filter.Groups'));
		},

		onFilterGroupsModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onFilterGroupsRemoveButtonClick: function() {
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
		 * TODO: waiting for refactor (server endpoint to get single filter data)
		 */
		onFilterGroupsRowSelected: function() {
			this.selectedFilter = this.grid.getSelectionModel().getSelection()[0];

			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = this.selectedFilter.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.core.proxy.filter.Groups.getDefaults({
				params: params,
				scope: this,
				success: function(result, options, decodedResult) {
					decodedResult = decodedResult.response.elements;

					this.selectedFilter.set(CMDBuild.core.constants.Proxy.DEFAULT_FOR_GROUPS, decodedResult);

					this.form.loadRecord(this.selectedFilter);
					this.form.setDisabledModify(true, true);
				}
			});
		},

		onFilterGroupsSaveButtonClick: function() {
			if (this.validate(this.form)) {
				var formDataModel = Ext.create('CMDBuild.model.filter.Groups', this.form.getData(true));

				var params = formDataModel.getData();
				params[CMDBuild.core.constants.Proxy.CONFIGURATION] = Ext.encode(params[CMDBuild.core.constants.Proxy.CONFIGURATION]);
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = params[CMDBuild.core.constants.Proxy.ENTRY_TYPE]; // TODO: waiting for refactor (reads a entryType parameter but i write as className)

				if (Ext.isEmpty(formDataModel.get(CMDBuild.core.constants.Proxy.ID))) {
					CMDBuild.core.proxy.filter.Groups.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.core.proxy.filter.Groups.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		removeItem: function() {
			if (!Ext.isEmpty(this.selectedFilter)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ID] = this.selectedFilter.get(CMDBuild.core.constants.Proxy.ID);

				CMDBuild.core.proxy.filter.Groups.remove({
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
		 *
		 * TODO: waiting for refactor (save all group attributes in one call)
		 */
		success: function(result, options, decodedResult) {
			var me = this;
			var savedFilterObject = decodedResult[CMDBuild.core.constants.Proxy.FILTER] || options.params;

			CMDBuild.view.common.field.translatable.Utils.commit(this.view.form);

			var params = {};
			params[CMDBuild.core.constants.Proxy.FILTERS] = Ext.encode([savedFilterObject[CMDBuild.core.constants.Proxy.ID]]);
			params[CMDBuild.core.constants.Proxy.GROUPS] = Ext.encode(this.form.defaultForGroupsField.getValue());

			CMDBuild.core.proxy.filter.Groups.setDefaults({ params: params });

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