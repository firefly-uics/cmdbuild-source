(function () {
	Ext.define("CMDBuild.controller.management.workflow.widgets.CMManageEmailController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",
		cmName: "Create Report",

		TEMPLATE_FIELDS: ['ToAddresses','CcAddresses','Subject','Content','Condition'],
		TEMPLATE_CONDITION: 'Condition',

		constructor: function() {
			this.callParent(arguments);

			this.emailsWereGenerated = false;
			this.gridStoreWasLoaded = false;

			this.widgetConf = _normalizewidgetData(this.view.widgetConf, this.TEMPLATE_FIELDS);
			this.readWrite = !this.widgetConf.ReadOnly;

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.view.clientForm,
				xaVars: this.widgetConf,
				serverVars: this.view.activity
			});

			this.usedTemplates = _getTemplatesToResolve(this);

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
			return this.usedTemplates.length > 0;
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
			if (this.widgetConf.Required 
				&& this.getOutgoing().length == 0) {

				return false;
			} else {
				return true;
			}
		},

		// override
		isBusy: function() {
			this.addEmailFromTemplateIfNeeded();
			return this.busy;
		}

	});

	function _getTemplatesToResolve(me) {
		var templatesVars = [];
		var templatesLength;

		// the template is valid if at least one of his
		// fields is defined
		function isValidTemplate(me, i) {
			var extAttrDef = me.widgetConf,
				valid = false;

			Ext.each(me.TEMPLATE_FIELDS, function(field) {
				if (extAttrDef[field + i]) {
					valid = true;
					return false;
				}
			});

			return valid;
		}

		for (var i=1; true; ++i) {
			if (!isValidTemplate(me, i)) {
				templatesLength = i-1;
				break;
			}
			Ext.each(me.TEMPLATE_FIELDS, function(field) {
				templatesVars.push(field+i);
			});
		}

		return {
			vars: templatesVars,
			length: templatesLength
		};
	}

	function _createEmailFromTemplate(me) {
		if (me.busy) {
			return;
		}

		me.busy = true;
		me.view.removeTemplatesFromStore();
		me.emailsWereGenerated = true;

		me.templateResolver.resolveTemplates({
			attributes: me.usedTemplates.vars,
			callback: function onTemlatesWereSolved(values) {
				for (var i=1; i<=me.usedTemplates.length; ++i) {
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

	function _normalizewidgetData(extAttrDef, TEMPLATE_FIELDS) {
		var normalizedExtAttrDef = Ext.apply({}, extAttrDef);

		Ext.each(TEMPLATE_FIELDS, function(attr) {
			if (normalizedExtAttrDef[attr+1]) {
				return false;
			}
			normalizedExtAttrDef[attr+1] = extAttrDef[attr];
			delete normalizedExtAttrDef[attr];
		});
		return normalizedExtAttrDef;
	}
})();