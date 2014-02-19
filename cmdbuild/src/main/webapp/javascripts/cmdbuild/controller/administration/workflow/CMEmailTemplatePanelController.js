(function() {

	Ext.define("CMDBuild.controller.administration.workflow.CMEmailTemplatePanelController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		mixins: {
			gridFormPanelDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate"
		},

		constructor: function(view) {
			this.callParent(arguments);
			this.mixins.gridFormPanelDelegate.constructor.call(this, view);
	
			this.fieldManager = null;
			this.gridConfigurator = null;
			this.entryType = null;
			this.record = null;
			this.danglingEntryTypeId = null;

			var me = this;
			this.view.mon(this.view, "activate", function() {
				if (me.danglingEntryTypeId) {
					me.onClassSelected(me.danglingEntryTypeId);
				}
			});
		},

		// override CMBasePanelController
		onClassSelected: function(entryTypeId) {
			this.view.enable();

			// check the visibility of
			// the view to defer the
			// construction after the activation
			this.entryType = null;
			if (this.view.isVisible()) {
				this.entryType = _CMCache.getEntryTypeById(entryTypeId);
				this.danglingEntryTypeId = null;
			} else {
				this.danglingEntryTypeId = entryTypeId;
				return;
			}

			// build subcomponents, only
			// the first time
			if (this.gridConfigurator == null) {
				this.gridConfigurator = new CMDBuild.delegate.administration.emailTemplate.CMEmailTemplateGridConfigurator();
				this.view.configureGrid(this.gridConfigurator);
			}

			if (this.fieldManager == null) {
				this.fieldManager = new CMDBuild.delegate.administration.emailTemplate.CMEmailTemplateFormFieldsManager();
				this.view.buildFields(this.fieldManager);
				this.view.disableModify();
			}

			// if there is an entryType
			// load the related templates
			if (this.entryType) {
				// set the className as extraParams to
				// allow the reload of the grid without errors
				var store = this.gridConfigurator.getStore();
				if (store.proxy.extraParams) {
					store.proxy.extraParams.className = this.entryType.getName();
				} else {
					store.proxy.extraParams = {
						className: this.entryType.getName()
					};
				}

				var me = this;
				this.gridConfigurator.getStore().load({
					callback: function() {
						me.selectFirstRow();
					}
				});
			}
		},

		// override CMBasePanelController
		onAddClassButtonClick: function() {
			this.view.disable();
		},

		// as gridFormPanelDelegate

		/**
		 * called after the save button click
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 */
		// override
		onGridAndFormPanelSaveButtonClick: function(form) {
			var me = this;
			var values = this.fieldManager.getValues();

			if (values[CMDBuild.ServiceProxy.parameter.CLASS_ID]) {
				values[CMDBuild.ServiceProxy.parameter.CLASS_ID] = this.entryType.getId();
			}

			var request = {
				params: values,
				success: function() {
					me.gridConfigurator.getStore().load();
				}
			};

			if (this.record == null) {
				_CMProxy.emailTemplate.create(request);
			} else {
				_CMProxy.emailTemplate.update(request);
			}
		},

		/**
		 * called after the confirmation of a remove
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 */
		// override
		onGridAndFormPanelRemoveConfirmed: function(form) {
			var me = this;

			_CMProxy.emailTemplate.remove({
				params: {
					templateName: me.record.getTemplateName()
				},
				success: function() {
					me.gridConfigurator.getStore().load();
				}
			});

		}
	});
})();