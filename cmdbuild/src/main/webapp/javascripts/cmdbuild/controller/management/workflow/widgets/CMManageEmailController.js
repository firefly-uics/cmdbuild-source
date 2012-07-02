(function () {

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMManageEmailController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",

		statics: {
			WIDGET_NAME: ".ManageEmail"
		},

		TEMPLATE_FIELDS: ['toAddresses','ccAddresses','subject','content','condition'],
		TEMPLATE_CONDITION: 'condition',

		constructor: function(ui, supercontroller, widget, clientForm, card) {

			this.reader = CMDBuild.management.model.widget.ManageEmailConfigurationReader;

			this.callParent(arguments);

			this.emailsWereGenerated = false;
			this.gridStoreWasLoaded = false;

			this.widgetConf = widget;
			this.templatesData = _extractVariablesForTemplateResolver(this);
			this.readWrite = !this.reader.readOnly(widget);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: this.templatesData,
				serverVars: card
			});

			this.mon(this.view, this.view.CMEVENTS.updateTemplatesButtonClick, function() {
				this.emailsWereGenerated = false;
				this.addEmailFromTemplateIfNeeded();
			}, this);
		},

		// override
		beforeActiveView: function() {
			if (!this.gridStoreWasLoaded) {
				this.view.getEl().mask(CMDBuild.Translation.common.wait_title);
				this.view.emailGrid.store.load({
					scope: this,
					callback: function(records, operation, success) {
						this.gridStoreWasLoaded = true;
						this.view.getEl().unmask();
						this.addEmailFromTemplateIfNeeded();
					}
				});
			} else {
				this.addEmailFromTemplateIfNeeded();
			}
		},

		addEmailFromTemplateIfNeeded: function() {
			if (this.emailsWereGenerated) {
				return;
			}

			var me = this;
			if (me.readWrite
					&& me.thereAreTemplates()) {

				_createEmailFromTemplate(me);
			}
		},

		thereAreTemplates: function() {
			return this.countTemplates() > 0;
		},

		countTemplates: function() {
			var t = this.reader.templates(this.widgetConf) || [];
			return t.length;
		},

		// override
		getData: function(isAdvance) {
			var outgoingEmails = this.view.getOutgoing(true);
			var	outgoingEmailsEnc = Ext.JSON.encode(outgoingEmails);
			var	deletedEnc = Ext.JSON.encode(this.view.getDeletedEmails());

			return {
				Updated: outgoingEmailsEnc,
				Deleted: deletedEnc
			};
		},

		// override
		isValid: function() {
			return !(this.reader.required(this.widgetConf)
				&& this.getOutgoing().length == 0);
		},

		// override
		isBusy: function() {
			this.addEmailFromTemplateIfNeeded();
			return this.busy;
		}

	});

	function _createEmailFromTemplate(me) {
		if (me.busy) {
			return;
		}

		me.busy = true;
		me.view.removeTemplatesFromStore();
		me.emailsWereGenerated = true;

		me.templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(me.templatesData),
			callback: function onTemlatesWereSolved(values) {
				for (var i=1; i<=me.countTemplates(); ++i) {
					var v = {};
					var conditionExpr = values[me.TEMPLATE_CONDITION+i];
					if (!conditionExpr || eval(conditionExpr)) {
						Ext.each(me.TEMPLATE_FIELDS, function(field) {
							v[field] = values[field+i];
						});
						me.view.addTemplateToStore(v);
					}
				}

				me.templateResolver.bindLocalDepsChange(function() {
					if (me.emailsWereGenerated) {
						me.emailsWereGenerated = false;
						new CMDBuild.Msg.warn(null, CMDBuild.Translation.management.modworkflow.extattrs.manageemail.mailsAreChanged);
					}
				});

				me.busy = false;
			}
		});
	}

	/**
	 * Extract the variables of each template and
	 * add a suffix to them with the index.
	 * This is needed to be passed as a unique array to the
	 * template resolver
	 */
	function _extractVariablesForTemplateResolver(me) {
		var templates = me.reader.templates(me.widgetConf) || [];
		var variables = {};

		for (var i=0, l=templates.length, t=null; i<l; ++i) {
			t = templates[i];
			for (var key in t) {
				variables[key + (i+1)] = t[key];
			}
		}

		_debug("ManageEmail, templateResolver variables", variables);
		return variables;
	}
})();