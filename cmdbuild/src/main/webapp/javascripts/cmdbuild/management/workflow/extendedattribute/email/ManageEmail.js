CMDBuild.Management.ManageEmail = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {
	loaded: false,
	TEMPLATE_FIELDS: ['ToAddresses','CcAddresses','Subject','Content','Condition'],
	TEMPLATE_CONDITION: 'Condition',

	initialize : function(extAttrDef) {
		this.readWrite = this.getVariable("xa:ReadOnly") ? false : true;

		function normalizeExtAttrDef(extAttrDef, TEMPLATE_FIELDS) {
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
		this.extAttrDef = normalizeExtAttrDef(extAttrDef, this.TEMPLATE_FIELDS);

		this.emailGrid = new CMDBuild.Management.EmailGrid({
			 autoScroll: true,
			 extAttrDef: this.extAttrDef,
			 extAttr: this,
			 processInstanceId: this.getProcessId(),
			 readWrite: this.readWrite,
			 border: false,
			 style: {'border-bottom': '1px ' + CMDBuild.Constants.colors.blue.border + ' solid'}
		});

		return {
			items: [this.emailGrid]
		};
	},

	onExtAttrShow: function() {
		if (this.readWrite) {
			this.emailGrid.addTemplatesIfNeededOnLoad();
		}
	},

	onSave: function(form, reactedFn, isAdvance) {
		if (this.readWrite) {
			if (isAdvance) {
				var realReactFn = this.realReact.createDelegate(this, [reactedFn, isAdvance]);
				this.emailGrid.addTemplatesIfNeededOnLoad(realReactFn);
			} else {
				this.realReact(reactedFn, isAdvance);
			}
		} else {
	        reactedFn(this.identifier, true);
		}
    },

    realReact: function(reactedFn, isAdvance) {
        var outgoingEmails = this.getOutgoing(true);
        var outgoingEmailsEnc = Ext.util.JSON.encode(outgoingEmails);
        var deletedEnc = Ext.util.JSON.encode(this.emailGrid.deletedEmails);
        this.react({
        		Outgoing: outgoingEmailsEnc,
	    		Deleted: deletedEnc,
	    		ImmediateSend: isAdvance
	    }, reactedFn);
    },

    getOutgoing: function(modifiedOnly) {
    	var allOutgoing = modifiedOnly ? false : true;
    	var outgoingEmails = [];
		var emails = this.emailGrid.getStore().getRange();
		for (var i=0, n=emails.length; i<n; ++i) {
			var currentEmail = emails[i];
			if (allOutgoing || !currentEmail.get("Id") || currentEmail.dirty) {
				outgoingEmails.push(currentEmail.data);
			}
		}
		return outgoingEmails;
    },

    isValid: function() {
		if (this.required && this.getOutgoing().length == 0) {
			return false;
		} else {
			return true;
		}
	}
});
Ext.reg("manageEmail", CMDBuild.Management.ManageEmail);
