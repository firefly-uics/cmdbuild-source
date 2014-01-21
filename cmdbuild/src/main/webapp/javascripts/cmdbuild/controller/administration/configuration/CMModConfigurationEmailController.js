(function() {

	var tr = CMDBuild.Translation.administration.setup; // Path to translation

	Ext.define("CMDBuild.controller.administration.configuration.CMModConfigurationEmailController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.grid = this.view.emailGrid;
			this.form = this.view.emailForm;
			this.view.delegate = this;
			this.grid.delegate = this;
			this.form.delegate = this;

			this.selectedEmailAccount = null;
		},

		/**
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 * Gatherer function to catch events
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();
				case 'onAddButtonClick':
					return this.onAddButtonClick();
				case 'onModifyButtonClick':
					return this.onModifyButtonClick();
				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick();
				case 'onRowSelected':
					return this.onRowSelected(param.record);
				case 'onSaveButtonClick':
					return this.onSaveButtonClick();
				default: {
					if (
						this.parentDelegate
						&& typeof this.parentDelegate === 'object'
					) {
						return this.parentDelegate.cmOn(name, param, callBack);
					}
				}
			}
		},

		onAbortButtonClick: function() {
			if (this.selectedEmailAccount != null) {
				this.onRowSelected(this.selectedEmailAccount);
			} else {
				this.form.reset();
				this.form.disableModify();
			}
		},

		onAddButtonClick: function() {
			this.grid.getSelectionModel().deselectAll();
			this.selectedEmailAccount = null;
			this.form.reset();
			this.form.enableModify(true);
		},

		onModifyButtonClick: function() {
			this.form.disableCMTbar();
			this.form.enableCMButtons();
			this.form.enableModify(true);
		},

		onRemoveButtonClick: function() {
			Ext.Msg.show({
				title: tr.remove,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes') {
						this.removeEmailAccount();
					}
				}
			});
		},

		onRowSelected: function(record) {
			if (this.grid.getSelectionModel().hasSelection()) {
				this.selectedEmailAccount = this.grid.getSelectionModel().getSelection()[0];
				this.form.loadRecord(record);
				this.form.disableModify(true);
			}
		},

		onSaveButtonClick: function() {
			var nonvalid = this.form.getNonValidFields();
			if (nonvalid.length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

			var formData = this.form.getData();
			if (formData.id != null) {
//				CMDBuild.LoadMask.get().show();
//				CMDBuild.Ajax.request({
//					method: 'POST',
//					url: 'services/json/schema/modsecurity/saveuser',
//					params: formData,
//					scope: this,
//					success: this.success(),
//					callback: this.callback()
//				});

//				CMDBuild.ServiceProxy.configuration.email.updateEmailAccount({
//					method: 'POST',
//					params: formData,
//					scope: this,
//					success: this.success(),
//					callback: this.callback()
//				});
			} else {
//				CMDBuild.ServiceProxy.configuration.email.createEmailAccount({
//					method: 'POST',
//					params: formData,
//					scope: this,
//					success: this.success(),
//					callback: this.callback()
//				});
			}
		},

		removeEmailAccount: function() {
			if (this.selectedEmailAccount == null) {
				// Nothing to remove
				return;
			}

//			var me = this;
//			var params = {};
//			accountData[_CMProxy.parameter.DOMAIN_NAME] = this.selectedEmailAccount.get("name");
//
//			CMDBuild.LoadMask.get().show();
//			CMDBuild.ServiceProxy.administration.domain.remove({
//				params: params,
//				success : function(form, action) {
//					me.view.reset();
//					_CMCache.onDomainDeleted(me.selectedEmailAccount.get("id"));
//				},
//				callback : function() {
//					CMDBuild.LoadMask.get().hide();
//				}
//			});

//			CMDBuild.ServiceProxy.configuration.email.removeEmailAccount({
//				method: 'POST',
//				params: accountData,
//				scope: this,
//				success : function(form, action) {
//					me.view.reset();
//					_CMCache.onDomainDeleted(me.selectedEmailAccount.get("id"));
//				},
//				callback : function() {
//					CMDBuild.LoadMask.get().hide();
//				}
//			});
		},
// ######################### DA SISTEMARE E VEDERE SE FUNZIA QUANDO AVRO LO STORE FUNZIONANTE
		success: function(result, options, decodedResult) {
			var emailAccountId = decodedResult.rows.id;
			var store = this.grid.store;
			store.load({
				scope: this,
				callback: function(records, operation, success) {
					var rowIndex = this.find('id', record.getId());
					this.view.select(rowIndex);
				}
			});
			this.form.disableModify();
		},

		callback: function() {
			CMDBuild.LoadMask.get().hide();
		}
	});

})();
