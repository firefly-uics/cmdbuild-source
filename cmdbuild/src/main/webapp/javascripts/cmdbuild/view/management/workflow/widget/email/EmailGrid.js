CMDBuild.Management.EmailGrid = Ext.extend(Ext.grid.GridPanel, {

	extAttrDef: undefined,
	extAttr: undefined,
	processInstanceId: undefined,

	initComponent: function() {
		var readWrite = this.readWrite;

		this.usedTemplates = this.getTemplatesToResolve();

		this.deletedEmails = [];
    	var reader = new Ext.data.JsonReader({
			root: "rows",
			fields: ['Id', 'EmailStatus_value', 'BeginDate', 'FromAddress', 'ToAddresses', 'CcAddresses', 'Subject', 'Content', 'Fake']
		});

		var proxy = new Ext.data.HttpProxy({
			url: 'services/json/management/email/getemaillist'
		});

		function recordIsOutgoing(record) {
			var status = record.get('EmailStatus_value');
			return (status == 'Outgoing');
		}
		this.recordIsOutgoing = recordIsOutgoing;

		function recordIsEditable(record) {
			var status = record.get('EmailStatus_value');
			return (status == 'Outgoing') || (status == 'Draft');
		}
		this.recordIsEditable = recordIsEditable;

		function recordIsReceived(record) {
			var status = record.get('EmailStatus_value');
			return (status == 'Received') || (status == 'New');
		}

		function renderEmailActions(value, metadata, record) {
			if (recordIsEditable(record) && readWrite) {
				return '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modworkflow.extattrs.manageemail.deleteicon+'" class="action-email-delete" src="images/icons/delete.png"/>&nbsp;'
					+ '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modworkflow.extattrs.manageemail.editicon+'" class="action-email-edit" src="images/icons/modify.png"/>&nbsp;';
			} else {
				return '<span style="cursor:pointer; width: 16px; height: 16px" />&nbsp;'
			     + '<img style="cursor:pointer" title="'+CMDBuild.Translation.management.modworkflow.extattrs.manageemail.viewicon+'" class="action-email-view" src="images/icons/zoom.png"/>';
			}
	    }

		function renderAddress(value, metadata, record) {
			if (recordIsReceived(record)) {
				return record.data['FromAddress'];
			} else {
				return record.data['ToAddresses'];
			}
		}

		function renderEmailContent(value, metadata, record) {
			var htmlContent = record.data['Content'];
			if (htmlContent) {
				return htmlContent.replace(/\<[Bb][Rr][\/]?\>/g," ")
					.replace(/\<[^\>]*\>/g,"");
			} else {
				return undefined;
			}
		}

		function cellclickHandler(grid, rowIndex, colIndex, event) {
			var className = event.target.className;
			var record = grid.getStore().getAt(rowIndex);
			var functionArray = {
				'action-email-delete': this.onDeleteEmail,
				'action-email-edit': this.onEditEmail,
				'action-email-view': this.onViewEmail
			};
			if (functionArray[className])
				functionArray[className].createDelegate(grid)(record);
		}
		this.on('cellclick', cellclickHandler);

		function doubleclickHandler(grid, rowIndex, event) {
			var record = grid.getStore().getAt(rowIndex);
			var fn = recordIsEditable(record) ? this.onEditEmail : this.onViewEmail;
			fn.createDelegate(grid)(record);
		}
		this.on('rowdblclick', doubleclickHandler);

		if (readWrite) {
			var tbar = [{
	      		iconCls : 'add',
	      		text : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose,
				handler : this.onComposeEmail,
	      		scope: this
	    	}];
		}

		Ext.apply(this, {
			loadMask: true,
			isLoaded: false,
	        collapsible: false,	        
	        tbar: tbar,
			store: new Ext.data.GroupingStore({
				reader: reader,
				proxy: proxy,
				remoteSort: false,
				groupField: 'EmailStatus_value',
				sortInfo:  {field: "EmailStatus_value", direction: "ASC"},
				baseParams: {
					ProcessInstanceId: this.processInstanceId
				},
				autoLoad: true
	        }),
	        cm: new Ext.grid.ColumnModel([
	        	{header: '&nbsp', sortable: true, dataIndex: 'EmailStatus_value', hidden: true},
	        	{header: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.datehdr, width: 20, sortable: true, dataIndex: 'BeginDate'},
	        	{header: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.addresshdr, width: 20, sortable: false, renderer: renderAddress, dataIndex: 'Fake'},
	        	{header: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjecthdr, width: 40, sortable: false, dataIndex: 'Subject'},
	        	{header: '&nbsp', width: 60, sortable: false, renderer: renderEmailContent, dataIndex: 'Content', menuDisabled: true, id: 'imagecolumn', hideable: false},
	            {header: '&nbsp', width: 90, fixed: true, sortable: false, renderer: renderEmailActions, align: 'center', cellCls: 'grid-button', dataIndex: 'Fake', menuDisabled: true, id: 'imagecolumn', hideable: false}
	        ]),
	        view: new Ext.grid.GroupingView({
				forceFit: true,
				groupTextTpl: '{[CMDBuild.Translation.management.modworkflow.extattrs.manageemail.lookup[values.rs[0].data.EmailStatus_value]]}'
			})
		});
		CMDBuild.Management.EmailGrid.superclass.initComponent.apply(this, arguments);
		this.store.on('load', this.onStoreLoad, this);
	},
	
	onStoreLoad: function(){
		this.isLoaded = true;
	},
	
	// scope: this
	getTemplatesToResolve: function() {
		var templatesVars = [];
		var templatesLength;
		for (var i=1; true; ++i) {
			if (!this.isValidTemplate(i)) {
				templatesLength = i-1;
				break;
			}
			Ext.each(this.extAttr.TEMPLATE_FIELDS, function(field) {
				templatesVars.push(field+i);
			});
		}
		return {
			vars: templatesVars,
			length: templatesLength
		};
	},

	// scope: this
	// callback optional
	addTemplatesIfNeededOnLoad: function(callbackFn) {
		if (this.isLoaded) {
			this.addTemplatesIfNeeded(callbackFn);
		} else {
			this.store.on('load', function() {
				this.addTemplatesIfNeeded(callbackFn);
			}, this);
		}
	},

	addTemplatesIfNeeded: function(callbackFn) {
		if (this.readWrite
				&& (this.usedTemplates.length > 0)
				&& this.storeHasNoOutgoing()) {
			this.addTemplates(callbackFn);
		} else if (callbackFn) {
			callbackFn();
		}
	},
	
	// scope: this
	storeHasNoOutgoing: function(store) {
		var records = this.store.getRange();
		for (var i=0, len=records.length; i<len; ++i) {
			var record = records[i];
			if (this.recordIsOutgoing(record)) {
				return false;
			}
		}
		return true;
	},

	// scope: this
	// callback optional
	addTemplates: function(callbackFn) {
		this.extAttr.resolveTemplates({
			attributes: this.usedTemplates.vars,
			callback: function(values) {
				for (var i=1; i<=this.usedTemplates.length; ++i) {
					var v = {};
					var conditionExpr = values[this.extAttr.TEMPLATE_CONDITION+i];
					if (!conditionExpr || eval(conditionExpr)) {
						Ext.each(this.extAttr.TEMPLATE_FIELDS, function(field) {
							v[field] = values[field+i];
						});
						this.addTemplateToStore(v);
					}
				}
				if (callbackFn) {
					callbackFn();
				}
			},
			scope: this
		});
	},

	// scope: this
	isValidTemplate: function(i) {
		var extAttrDef = this.extAttrDef;
		var valid = false;
		Ext.each(this.extAttr.TEMPLATE_FIELDS, function(field) {
			if (extAttrDef[field+i]) {
				valid = true;
				return false;
			}
		});
		return valid;
	},

	// scope: this
	addTemplateToStore: function(values) {
		var record = this.createRecord(values);
		this.addOrUpdateRecord(record);
	},

	// scope: this
	onComposeEmail: function() {
		this.showComposeEmailWindow({});
	},

	// scope: this
	showComposeEmailWindow: function(values) {
		var composeEmailWin = new CMDBuild.Management.EmailWindow({
			emailGrid: this,
			record: this.createRecord(values)
		});
		composeEmailWin.show();
	},

	createRecord: function(recordValues) {
		recordValues["EmailStatus_value"] = "Outgoing";
		return new Ext.data.Record(recordValues);
	},

	onViewEmail: function(record) {
		var viewEmailWin = new CMDBuild.Management.EmailWindow({
			emailGrid: this,
			readOnly: true,
			record: record
		});
		viewEmailWin.show();
	},

	onEditEmail: function(record) {
		var editEmailWin = new CMDBuild.Management.EmailWindow({
			emailGrid: this,
			readOnly: false,
			record: record
		});
		editEmailWin.show();
	},

	onDeleteEmail: function(record) {
		Ext.Msg.confirm(
			CMDBuild.Translation.common.confirmpopup.title,
			CMDBuild.Translation.common.confirmpopup.areyousure,
			function(btn) {
				if (btn != 'yes')
					return;
				this.removeRecord(record);
	 		}, this);
	},

	removeRecord: function(record) {
		var oldId = record.data.Id;
		if (oldId) {
			this.deletedEmails.push(oldId);
		}
		this.getStore().remove(record);
	},

	addOrUpdateRecord: function(record) {
		var store = this.getStore();
		if (store.getById(record.id)) {
			store.add([record]);
		} else {
			store.addSorted(record);
		}
	}
});
Ext.reg('emailgrid', CMDBuild.Management.EmailGrid);
