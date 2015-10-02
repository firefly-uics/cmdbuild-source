(function() {

	Ext.define('CMDBuild.controller.administration.lookup.List', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.lookup.Lookup',
			'CMDBuild.model.lookup.Lookup'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.Lookup}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLookupListAbortButtonClick',
			'onLookupListAddButtonClick',
			'onLookupListDrop',
			'onLookupListModifyButtonClick = onLookupListItemDoubleClick',
			'onLookupListRowSelected',
			'onLookupListSaveButtonClick',
			'onLookupListTabShow',
			'onLookupListToggleActiveStateButtonClick',
			'selectedLookupSet'
		],

		/**
		 * @property {CMDBuild.view.administration.lookup.list.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.lookup.list.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.model.lookup.Lookup.gridStore} or null
		 */
		selectedLookup: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.lookup.list.ListView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.lookup.Lookup} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.lookup.list.ListView', { delegate: this });

			// Shorthands
			this.grid = this.view.grid;
			this.form = this.view.form;
		},


		onLookupListAbortButtonClick: function() {
			if (this.selectedLookupIsEmpty()) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onLookupListRowSelected();
			}
		},

		onLookupListAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();

			this.selectedLookupSet(); // Reset selectedLookup

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.lookup.Lookup.gridStore'));
		},

		onLookupListDrop: function() {
			var gridRowsObjects = [];

			Ext.Array.forEach(this.grid.getStore().getRange(), function(row, i, allRows) {
				var rowObject = {};
				rowObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = row.get('Description');
				rowObject[CMDBuild.core.constants.Proxy.ID] = row.get('Id');
				rowObject[CMDBuild.core.constants.Proxy.INDEX] = i + 1;

				gridRowsObjects.push(rowObject);
			}, this);

			var params = {};
			params[CMDBuild.core.constants.Proxy.TYPE] = this.cmfg('selectedLookupTypeGet', CMDBuild.core.constants.Proxy.ID);
			params['lookuplist'] = Ext.encode(gridRowsObjects); // TODO: should be renamed (camelcase)

			CMDBuild.core.proxy.lookup.Lookup.setOrder({
				params: params,
				scope: this,
				success: function(result, options, decodedResult) {
					this.onLookupListTabShow();
				}
			});
		},

		onLookupListModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onLookupListRowSelected: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.TYPE] = this.cmfg('selectedLookupTypeGet', CMDBuild.core.constants.Proxy.ID);

			this.selectedLookupSet(this.grid.getSelectionModel().getSelection()[0]); // TODO: need refactor to get all lookup details (server service)

			// Update toggleActiveStateButton button
			if (this.selectedLookupGet('Active')) {
				this.form.toggleActiveStateButton.setText(CMDBuild.Translation.disableLookup);
				this.form.toggleActiveStateButton.setIconCls('delete');
			} else {
				this.form.toggleActiveStateButton.setText(CMDBuild.Translation.enableLookup);
				this.form.toggleActiveStateButton.setIconCls('ok');
			}

			this.form.loadRecord(this.selectedLookupGet());
			this.form.parentCombobox.getStore().load({ // Refresh store
				params: params
			});

			this.form.setDisabledModify(true, true);
		},

		onLookupListSaveButtonClick: function() {
			// Validate before save
			if (this.validate(this.form)) {
				var formData = this.form.getData(true);
				formData['Type'] = this.cmfg('selectedLookupTypeGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.core.proxy.lookup.Lookup.save({ // TODO: server side refactor needed to follow new CMDBuild standards (create/update)
					params: formData,
					scope: this,
					success: this.success
				});
			}
		},

		onLookupListTabShow: function() {
			if (!this.cmfg('selectedLookupTypeIsEmpty')) {
				var me = this;

				var params = {};
				params[CMDBuild.core.constants.Proxy.TYPE] = this.cmfg('selectedLookupTypeGet', CMDBuild.core.constants.Proxy.ID);

				this.grid.getStore().load({
					params: params,
					callback: function(records, operation, success) {
						var rowIndex = 0;

						if (!me.selectedLookupIsEmpty())
							rowIndex = this.find('Id', me.selectedLookupGet('Id'));

						me.grid.getSelectionModel().select(rowIndex, true);
						me.form.setDisabledModify(true);
					}
				});
			}
		},

		onLookupListToggleActiveStateButtonClick: function() {
			var params = {};
			params[CMDBuild.core.constants.Proxy.ID] = this.selectedLookupGet('Id');

			if (this.form.activeCheckbox.getValue()) {
				CMDBuild.core.proxy.lookup.Lookup.disable({
					params: params,
					scope: this,
					success: function(result, options, decodedResult) {
						this.onLookupListTabShow();
					}
				});
			} else {
				CMDBuild.core.proxy.lookup.Lookup.enable({
					params: params,
					scope: this,
					success: function(result, options, decodedResult) {
						this.onLookupListTabShow();
					}
				});
			}
		},

		// SelectedLookup methods
			/**
			 * @returns {Boolean}
			 */
			selectedLookupIsEmpty: function() {
				return Ext.isEmpty(this.selectedLookup);
			},

			/**
			 * Returns full model object or just one property if required
			 *
			 * @param {String} parameterName
			 *
			 * @returns {CMDBuild.model.lookup.Lookup.gridStore} or Mixed
			 */
			selectedLookupGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName))
					return this.selectedLookup.get(parameterName);

				return this.selectedLookup;
			},

			/**
			 * @property {Object} lookupObject
			 */
			selectedLookupSet: function(lookupObject) {
				this.selectedLookup = null;

				if (!Ext.isEmpty(lookupObject) && Ext.isObject(lookupObject)) {
					if (Ext.getClassName(lookupObject) == 'CMDBuild.model.lookup.Lookup.gridStore') {
						this.selectedLookup = lookupObject;
					} else {
						this.selectedLookup = Ext.create('CMDBuild.model.lookup.Lookup.gridStore', lookupObject);
					}
				}
			},

		/**
		 * @param {Object} result
		 * @param {Object} options
		 * @param {Object} decodedResult
		 */
		success: function(result, options, decodedResult) {
			if (!Ext.isEmpty(decodedResult.lookup))
				this.selectedLookupSet(decodedResult.lookup);

			// HACK to apply TranslationUuid to form to be able to save translations ... because lookups doesn't have an identifier like a name
			this.form.loadRecord(this.selectedLookupGet());

			CMDBuild.view.common.field.translatable.Utils.commit(this.form);

			this.onLookupListTabShow();
		}
	});

})();