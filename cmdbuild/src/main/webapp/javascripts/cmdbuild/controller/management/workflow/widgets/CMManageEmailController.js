(function() {
	Ext.define("CMDBuild.controller.management.workflow.widgets.CMManageEmailController", {
		extend: "CMDBuild.controller.management.workflow.widget.CMBaseWFWidgetController",
		cmName: "Create Report",

		TEMPLATE_FIELDS: ['ToAddresses','CcAddresses','Subject','Content','Condition'],
		TEMPLATE_CONDITION: 'Condition',

		constructor: function() {
			this.callParent(arguments);
			this.loaded = false;

			this.widgetConf = normalizewidgetData(this.view.widgetConf, this.TEMPLATE_FIELDS);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.view.clientForm,
				xaVars: this.widgetConf,
				serverVars: this.view.activity
			});

			this.usedTemplates = this.getTemplatesToResolve();
			this.readWrite = !this.widgetConf.ReadOnly;
		},

		getTemplatesToResolve: function() {
			var templatesVars = [];
			var templatesLength;
			for (var i=1; true; ++i) {
				if (!this.isValidTemplate(i)) {
					templatesLength = i-1;
					break;
				}
				Ext.each(this.TEMPLATE_FIELDS, function(field) {
					templatesVars.push(field+i);
				});
			}
			return {
				vars: templatesVars,
				length: templatesLength
			};
		},

		// the template is valid if at least one of his
		// fields is defined
		isValidTemplate: function(i) {
			var extAttrDef = this.widgetConf,
				valid = false;

			Ext.each(this.TEMPLATE_FIELDS, function(field) {
				if (extAttrDef[field + i]) {
					valid = true;
					return false;
				}
			});

			return valid;
		},

		addTemplatesIfNeededOnLoad: function(callbackFn) {
			if (this.loadeded) {
				this.addTemplatesIfNeeded(callbackFn);
			} else {
				this.view.emailGrid.store.on('load', function() {
					this.addTemplatesIfNeeded(callbackFn);
				}, this, {single: true});
			}
		},

		addTemplatesIfNeeded: function(callbackFn) {
			if (this.readWrite
					&& (this.usedTemplates.length > 0)
					&& this.templatesShouldBeAdded()) {
				this.addTemplates(callbackFn);
			} else if (callbackFn) {
				callbackFn();
			}
		},

		templatesShouldBeAdded: function() {
			return this.view.emailGrid.storeHasNoOutgoing();
		},

		addTemplates: function(callbackFn) {
			this.templateResolver.resolveTemplates({
				attributes: this.usedTemplates.vars,
				callback: function(values) {
					for (var i=1; i<=this.usedTemplates.length; ++i) {
						var v = {};
						var conditionExpr = values[this.TEMPLATE_CONDITION+i];
						if (!conditionExpr || eval(conditionExpr)) {
							Ext.each(this.TEMPLATE_FIELDS, function(field) {
								v[field] = values[field+i];
							});
							this.view.emailGrid.addTemplateToStore(v);
						}
					}
					if (callbackFn) {
						callbackFn();
					}
				},
				scope: this
			});
		},

		// override
		beforeActiveView: function() {
			if (this.readWrite) {
				this.addTemplatesIfNeededOnLoad();
			}

			if (!this.loaded) {
				// FIXME I don't know why the loadMask of the grid does not work!
				this.view.getEl().mask("Loading...");
				this.view.emailGrid.store.load({
					scope: this,
					callback: function(records, operation, success) {
						this.loaded = true;
						this.view.getEl().unmask();
					}
				});
			}
		},

		getData: function(isAdvance) {
			var outgoingEmails = this.view.getOutgoing(true),
				outgoingEmailsEnc = Ext.JSON.encode(outgoingEmails),
				deletedEnc = Ext.JSON.encode(this.view.getDeletedEmails());

			return {
				Outgoing: outgoingEmailsEnc,
				Deleted: deletedEnc,
				ImmediateSend: isAdvance
			};
		},

		isValid: function() {
			if (this.widgetConf.Required && this.getOutgoing().length == 0) {
				return false;
			} else {
				return true;
			}
		}

	});
	
	function normalizewidgetData(extAttrDef, TEMPLATE_FIELDS) {
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