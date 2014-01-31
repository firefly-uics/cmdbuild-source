(function() {
	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>";

	Ext.define("CMDBuild.controller.management.common.widgets.CMWorkflowControllerWidgetReader",{
		getType: function(w) {return "Activity";},
		getCode: function(w) {return w.workflowId;},
		getPreset: function(w) {return w.preset;}
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMWorkflowController", {
		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMWorkflow.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.widgetReader = new CMDBuild.controller.management.common.widgets.CMWorkflowControllerWidgetReader();
			this.presets = this.widgetReader.getPreset(this.widgetConf);
			var widgetManager = new CMDBuild.view.management.common.widgets.CMWidgetManagerPopup(this.view);
			this.widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerControllerPopup(widgetManager);
			view.setDelegate(this);
			this.widgetControllerManager.setDelegate(this);

			//this.mon(this.view, "widget-click", alert("qui"), this);
			this.mon(this.view, this.view.CMEVENTS.saveButtonClick, onSaveCardClick, this);
			this.mon(this.view, this.view.CMEVENTS.advanceButtonClick, onAdvanceCardClick, this);
//			this.className = _CMCache.getEntryTypeNameById(this.widgetReader.getCode(this.widgetConf));
			var me = this;
			_CMCache.getAttributeList(this.widgetReader.getCode(this.widgetConf), function(attributes) {
				me.cardAttributes = attributes;
			});		
		},

		ensureEditPanel: function() {
		},
		onWidgetButtonClick: function(widget) {
			this.widgetControllerManager.onWidgetButtonClick(widget);
		},
		beforeActiveView: function() {
			var me = this,
				wr = this.widgetReader;

			if (!me.widgetReader) {
				return;
			}

			if (me.configured && me.templateResolver) {
				resolveTemplate(me);
			} else {
				me.view.setLoading(true);

				Ext.Ajax.request({
					url : 'services/json/workflow/getstartactivity',
					params : {
						classId: wr.getCode(me.widgetConf)
					},
					success : function(response) {
						var ret = Ext.JSON.decode(response.responseText);
						me.attributes = CMDBuild.controller.common.WorkflowStaticsController.filterAttributesInStep(me.cardAttributes, ret.response.variables);
						me.view.configureForm(me.attributes);
						me.templateResolver = new CMDBuild.Management.TemplateResolver({
							clientForm: me.clientForm,
							xaVars: me.presets,
							serverVars: this.getTemplateResolverServerVars()
						});
	
						resolveTemplate(me);
						this.widgetControllerManager.buildControllers(ret.response.widgets);
						me.view.getWidgetButtonsPanel().editMode();
						me.view.setLoading(false);
						me.configured = true;
					},
					scope: me
				});
			}
		},

		destroy: function() {
			this.mon(this.view, this.view.CMEVENTS.saveButtonClick, onSaveCardClick, this);
		}
	});
	function resolveTemplate(me) {
		var wr = me.widgetReader;

		me.templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(me.presets),
			callback: function(o) {
				me.view.fillFormValues(o);
			}
		});
	}

	function onAdvanceCardClick() {
		saveWorkflow(this, true);
	}
	function onSaveCardClick() {
		saveWorkflow(this, false);
	}
	function saveWorkflow(me, advance) {
		var form = me.view.formPanel.getForm();
		var valid = advance ? validate(me) : true;
		if (valid) {
			CMDBuild.LoadMask.get().show();
			var requestParams = {};
			requestParams.classId = me.widgetReader.getCode(me.widgetConf);
			requestParams.attributes = Ext.JSON.encode(form.getValues());
			requestParams.advance = advance;
			requestParams.activityInstanceId = undefined;
			requestParams.ww = Ext.JSON.encode(me.widgetControllerManager.getData(advance));
			CMDBuild.ServiceProxy.workflow.saveActivity({
				params: requestParams,
				scope : me,
				clientValidation: true, //to force the save request
				callback: function(operation, success, response) {
					CMDBuild.LoadMask.get().hide();
				},
				success: function(operation, requestConfiguration, decodedResponse) {
				}
			});
		} else {
			_debug("There are no processInstance to save");
		}
	}
	function validateForm(me) {
		var form = me.view.formPanel.getForm();
		var invalidAttributes = CMDBuild.controller.common.CardStaticsController.getInvalidAttributeAsHTML(form);

		if (invalidAttributes != null) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidAttributes, false);

			return false;
		} else {
			return true;
		}
	}

	function validate(me) {
		var valid = validateForm(me);//,
		wrongWidgets = me.widgetControllerManager.getWrongWFAsHTML();

		if (wrongWidgets != null) {
			valid = false;
			var msg = Ext.String.format(ERROR_TEMPLATE
					, CMDBuild.Constants.css.error_msg
					, CMDBuild.Translation.errors.invalid_extended_attributes);
			CMDBuild.Msg.error(null, msg + wrongWidgets, popup = false);
		}

		return valid;
	}

})();
