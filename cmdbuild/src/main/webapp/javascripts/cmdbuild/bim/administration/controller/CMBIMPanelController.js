(function() {

	Ext.require([
		'CMDBuild.bim.proxy.Bim',
		'CMDBuild.bim.proxy.Ifc',
		'CMDBuild.bim.proxy.Layer'
	]);

	Ext.define('CMDBuild.controller.administration.filter.CMBIMPanelController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		mixins: {
			gridFormPanelDelegate: 'CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate'
		},

		constructor: function(view) {
			this.callParent(arguments);
			this.mixins.gridFormPanelDelegate.constructor.call(this, view);
			this.fieldManager = null;
			this.gridConfigurator = null;
			this.record = null;

			this.view.delegate = this;
		},

		onViewOnFront: function(group) {
			var me = this;

			if (this.fieldManager == null) {
				this.fieldManager = new CMDBuild.delegate.administration.bim.CMBIMFormFieldsManager();
				this.view.buildFields(this.fieldManager);
				this.bimCardBinding();

				this.fieldManager.getImportIfcButton().handler = function() {
					me.onImportIfc();
				};
			}
			this.view.disableModify();

			if (this.gridConfigurator == null) {
				this.gridConfigurator = new CMDBuild.delegate.administration.bim.CMBIMGridConfigurator();
				this.view.configureGrid(this.gridConfigurator);
			}

			this.gridConfigurator.getStore().load();
			this.selectFirstRow();
		},

		// as gridFormPanelDelegate

		/**
		 * Called after the save button click
		 *
		 * @param (CMDBuild.view.administration.common.basepanel.CMForm) form
		 */
		// override
		onGridAndFormPanelSaveButtonClick: function(form) {
			var me = this;
			var params = me.fieldManager.getValues() || {};
			var proxyFunction = CMDBuild.bim.proxy.Bim.create;

			if (this.record != null) {
				proxyFunction = CMDBuild.bim.proxy.Bim.update;
				params['id'] = this.record.getId();
			}

			if (form != null) {
				CMDBuild.core.LoadMask.show();

				this.view.enableModify();

				proxyFunction(form, params,
					function onSuccess() {
						me.fieldManager.enableFileField();
						CMDBuild.core.LoadMask.hide();
						me.gridConfigurator.getStore().load();
						me.view.disableModify(me.enableCMTBar = false);
						form.reset();
						me.view.grid.getSelectionModel().deselectAll();
					},
					function onFailure() {
						me.view.disableModify(me.enableCMTBar = false);
						form.reset();
						me.view.grid.getSelectionModel().deselectAll();

						CMDBuild.core.LoadMask.hide();
					}
				);
			}
		},

		onImportIfc: function() {
			if (!Ext.isEmpty(this.record))
				CMDBuild.bim.proxy.Ifc.import({
					scope: this,
					params: {
						projectId: this.record.getId()
					}
				});
		},

		/**
		 * @param (CMDBuild.view.administration.common.basepanel.CMForm) form the form that call the function
		 * @param (String) action - a string that say if the button is clicked when configured to activate or deactivate something ['disable' | 'enable']
		 */
		// override
		onEnableDisableButtonClick: function(form, action) {
			var me = this;

			if (!me.record)
				return;

			var proxyFunction = CMDBuild.bim.proxy.Bim.disable;
			if (action == 'enable') {
				proxyFunction = CMDBuild.bim.proxy.Bim.enable;
				this.view.updateEnableDisableButton(false);
			}
			else {
				this.view.updateEnableDisableButton(true);
			}

			CMDBuild.core.LoadMask.show();
			proxyFunction({
				params: {
					id: me.record.getId()
				},
				callback: function() {
					CMDBuild.core.LoadMask.hide();
					me.gridConfigurator.getStore().load();
				}
			});
		},

		// as form delegate
		bimCardBinding: function() {
			var bindingReference = this.view.query('#bimCardBinding')[0];

			CMDBuild.bim.proxy.Layer.readRootName({
				success: function(operation, config, response) {
					bindingReference.initializeItems(response.root);
				},
				callback: Ext.emptyFn
			});
		},

		// as grid delegate
		// override
		onCMGridSelect: function(grid, record) {
			this.mixins.gridFormPanelDelegate.onCMGridSelect.apply(this, arguments);

			if (record)
				this.view.updateEnableDisableButton(!record.get('active'));
		}
	});


	// Legacy code

	Ext.require(['CMDBuild.core.Message']);

	/**
	 * @class CMDBuild.delegate.administration.common.basepanel.CMGridDelegate
	 *
	 * Respond to the events fired from the Grid
	 */
	Ext.define("CMDBuild.delegate.administration.common.basepanel.CMGridDelegate", {
		/**
		 *
		 * @param {CMDBuild.view.administration.common.basepanel.CMGrid} grid
		 * the grid that calls this method
		 * @param {Ext.data.Model} record
		 * the selected record
		 */
		onCMGridSelect: function(grid, record) {}
	});

	/**
	 * @class CMDBuild.delegate.administration.common.basepanel.CMFormDelegate
	 *
	 * Responds to the events fired from the Form
	 */
	Ext.define("CMDBuild.delegate.administration.common.basepanel.CMFormDelegate", {
		/**
		 *
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 * the form that call the function
		 */
		onFormModifyButtonClick: function(form) {},

		/**
		 *
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 * the form that call the function
		 */
		onFormRemoveButtonClick: function(form) {},

		/**
		 *
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 * the form that call the function
		 */
		onFormSaveButtonClick: function(form) {},

		/**
		 *
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 * the form that call the function
		 */
		onFormAbortButtonClick: function(form) {},

		/**
		 *
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 * the form that call the function
		 *
		 * @param {String} action
		 * a string that say if the button is clicked when configured
		 * to activate or deactivate something ["disable" | "enable"]
		 */
		onEnableDisableButtonClick: function(form, action) {}

	});

	/**
	 * Give a base implementation of the delegates
	 * 	CMDBuild.delegate.administration.common.basepanel.CMFormDelegate
	 * 	CMDBuild.delegate.administration.common.basepanel.CMGridDelegate
	 *
	 * and add his own method that are called form a CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel
	 */

	Ext.define("CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate", {

		mixins: {
			formDelegate: "CMDBuild.delegate.administration.common.basepanel.CMFormDelegate",
			gridDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridDelegate"
		},

		constructor: function(view) {
			this.view = view;
			view.addDelegate(this);
			view.form.addDelegate(this);
			view.grid.addDelegate(this);
		},

		selectFirstRow: function() {
			if (this.view.grid) {
				var store = this.view.grid.getStore();
				var sm = this.view.grid.getSelectionModel();

				if (store && sm) {
					var count = store.getTotalCount();
					if (count>0) {
						sm.select(store.getAt(0));
					}
				}
			}
		},

		/**
		 *
		 * @param {CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel} panel
		 * called from the panel after a click on the add button
		 */
		onGridAndFormPanelAddButtonClick: function(panel) {
			var all = true;
			this.record = null;
			this.fieldManager.reset();
			panel.enableModify(all);
			panel.clearSelection();
		},

		/**
		 * called after the save button click
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 */
		onGridAndFormPanelSaveButtonClick: function(form) {},

		/**
		 * called after the confirmation of a remove
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 */
		onGridAndFormPanelRemoveConfirmed: function(form) {},

		/**
		 *
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 * the form that call the function
		 *
		 * @param {String} action
		 * a string that say if the button is clicked when configured
		 * to activate or deactivate something ["enable" | "disable"]
		 */
		onEnableDisableButtonClick: function(form, action) {},

		// as form delegate

		onFormModifyButtonClick: function(form) {
			this.view.enableModify();
		},

		onFormRemoveButtonClick: function(form) {
			var me = this;
			Ext.Msg.show({
				title: CMDBuild.Translation.attention,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == "yes") {
						me.onGridAndFormPanelRemoveConfirmed(form);
						me.view.disableModify();
					}
				}
			});
		},

		onFormSaveButtonClick: function(form) {
			var form = this.view.form.getForm();
			if (form && form.isValid()) {
				this.view.disableModify();
				this.onGridAndFormPanelSaveButtonClick(form);
			} else {
				CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			}
		},

		onFormAbortButtonClick: function(form) {
			var enableCMTBar = false;
			if (this.record) {
				this.fieldManager.loadRecord(this.record);
				enableCMTBar = true;
			} else {
				this.fieldManager.reset();
			}

			this.view.disableModify(enableCMTBar);
		},

		// as grid delegate

		onCMGridSelect: function(grid, record) {
			this.record = record;
			var enableToolbar = !!record;
			this.view.disableModify(enableToolbar);
			if (this.fieldManager) {
				this.fieldManager.loadRecord(record);
			}
		}
	});

})();