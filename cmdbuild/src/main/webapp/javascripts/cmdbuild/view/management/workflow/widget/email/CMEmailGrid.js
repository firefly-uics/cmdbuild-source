(function() {
	Ext.define("CMDBuild.management.mail.Model", {
		extend: 'Ext.data.Model',
		fields: ['Id', 'EmailStatus_value', 'BeginDate', 'FromAddress', 'ToAddresses', 'CcAddresses', 'Subject', 'Content', 'Fake']
	});

Ext.define("CMDBuild.Management.EmailGrid", {

	extend: "Ext.grid.GridPanel",
	extAttrDef: undefined,
	extAttr: undefined,
	processId: undefined,

	CMEVENTS: {
		updateTemplatesButtonClick: "cm-update-templates"
	},

	initComponent: function() {

		this.deletedEmails = [];

		var readWrite = this.readWrite,
			tr = CMDBuild.Translation.management.modworkflow.extattrs.manageemail,
			store = new Ext.data.Store({
				model: "CMDBuild.management.mail.Model",
				remoteSort: false,
				proxy: {
					type: "ajax",
					url: 'services/json/management/email/getemaillist',
					reader: {
						root: 'rows',
						type: "json",
						totalProperty: 'results'
					},
					extraParams: {
						ProcessId: this.processId
					}
				},
				sorters: {property: 'EmailStatus_value', direction: 'ASC'},
				groupField: "EmailStatus_value",
				autoLoad: false
			});

		if (this.readWrite) {
			var me = this,
				tbar = [{
					iconCls : 'add',
					text : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose,
					handler : function(values) {
						new CMDBuild.Management.EmailWindow({
							emailGrid: me,
							record: me.createRecord(values)
						}).show();
					}
				}, {
					iconCls : 'x-tbar-loading',
					text : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.regenerates,
					handler : function() {
						me.fireEvent(me.CMEVENTS.updateTemplatesButtonClick);
					}
				}];

			me.addEvents([me.CMEVENTS.updateTemplatesButtonClick]);
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

		Ext.apply(this, {
			loadMask: false,
			isLoaded: false,
			collapsible: false,
			tbar: tbar,
			store: store,
			columns: [
				{header: '&nbsp', sortable: true, dataIndex: 'EmailStatus_value', hidden: true},
				{header: tr.datehdr, sortable: true, dataIndex: 'BeginDate', flex: 1},
				{header: tr.addresshdr, sortable: false, renderer: renderAddress, dataIndex: 'Fake', flex: 1},
				{header: tr.subjecthdr, sortable: false, dataIndex: 'Subject', flex: 1},
				{header: '&nbsp', sortable: false, renderer: renderEmailContent, dataIndex: 'Content', menuDisabled: true, hideable: false, flex: 2},
				{header: '&nbsp', width: 90, fixed: true, sortable: false, renderer: renderEmailActions, align: 'center', tdCls: 'grid-button', dataIndex: 'Fake', menuDisabled: true, hideable: false}
			],
			features: [{
				ftype: 'groupingsummary',
				groupHeaderTpl: '{name}',
				hideGroupedHeader: true,
				enableGroupingMenu: false
			}]
		});

		this.callParent(arguments);

		this.store.on('load', this.onStoreLoad, this);
		this.on('beforeitemclick', cellclickHandler, this);
		this.on("itemdblclick", doubleclickHandler, this);
	},

	onStoreLoad: function() {
		this.isLoaded = true;
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

	addTemplateToStore: function(values) {
		var record = this.createRecord(values);
		// mark the record added by template to be able to
		// delete it in removeTemplatesToStore
		record._cmTemplate = true;
		this.addToStoreIfNotInIt(record);
	},

	removeTemplatesFromStore: function() {
		var me = this;

		me.store.each(function(r) {
			if (r._cmTemplate) {
				me.store.remove(r);
			}
		});
	},

	createRecord: function(recordValues) {
		recordValues["EmailStatus_value"] = "Outgoing";
		return new CMDBuild.management.mail.Model(recordValues);
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
		new CMDBuild.Management.EmailWindow({
			emailGrid: this,
			readOnly: false,
			record: record
		}).show();
	},

	onDeleteEmail: function(record) {
		Ext.Msg.confirm(
			CMDBuild.Translation.common.confirmpopup.title,
			CMDBuild.Translation.common.confirmpopup.areyousure,
			function(btn) {
				if (btn != 'yes') {
					return;
				}
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

	addToStoreIfNotInIt: function(record) {
		var store = this.getStore();

		if (store.findBy( function(item) {
			return item.id == record.id;
		}) == -1) {
			// use loadRecords because store.add does not update the grouping
			// so the grid goes broken
			store.loadRecords([record], {addRecords: true});
		}
	},
	recordIsOutgoing: recordIsOutgoing,
	recordIsEditable: recordIsEditable
});

	function recordIsOutgoing(record) {
		var status = record.get('EmailStatus_value');
		return (status == 'Outgoing');
	}

	function recordIsEditable(record) {
		var status = record.get('EmailStatus_value');
		return (status == 'Outgoing') || (status == 'Draft');
	}

	function recordIsReceived(record) {
		var status = record.get('EmailStatus_value');
		return (status == 'Received') || (status == 'New');
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
			return htmlContent.replace(/\<[Bb][Rr][\/]?\>/g," ").replace(/\<[^\>]*\>/g,"");
		} else {
			return undefined;
		}
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className,
			functionArray = {
				'action-email-delete': this.onDeleteEmail,
				'action-email-edit': this.onEditEmail,
				'action-email-view': this.onViewEmail
			},
			me=this;

		if (functionArray[className]) {
			functionArray[className].call(me, model);
		}
	}

	function doubleclickHandler(grid, model, html, index, e, options) {
		var fn = recordIsEditable(model) ? this.onEditEmail : this.onViewEmail;
		fn.call(this, model);
	}
})();