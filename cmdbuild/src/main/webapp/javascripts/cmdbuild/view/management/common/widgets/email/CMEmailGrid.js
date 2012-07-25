(function() {

	var reader = CMDBuild.management.model.widget.ManageEmailConfigurationReader;
	var fields = reader.FIELDS;

	Ext.define("CMDBuild.management.mail.Model", {
		extend: 'Ext.data.Model',
		fields: [
			fields.ID,
			fields.STATUS,
			fields.BEGIN_DATE,
			fields.FROM_ADDRESS,
			fields.TO_ADDRESS,
			fields.CC_ADDRESS, 
			fields.SUBJECT,
			fields.CONTENT,
			'Fake' // for the icons
		]
	});

Ext.define("CMDBuild.view.management.common.widgets.CMEmailGrid", {

	extend: "Ext.grid.GridPanel",
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
						root: 'response',
						type: "json",
						totalProperty: 'results'
					}
				},
				sorters: {property: fields.STATUS, direction: 'ASC'},
				groupField: fields.STATUS,
				autoLoad: false
			});

		if (this.readWrite) {
			var me = this,
				tbar = [{
					iconCls : 'add',
					text : CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose,
					handler : function(values) {
						new CMDBuild.view.management.common.widgets.CMEmailWindow({
							emailGrid: me,
							record: me.createRecord({})
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
				{header: '&nbsp', sortable: true, dataIndex: fields.STATUS, hidden: true},
				{header: tr.datehdr, sortable: true, dataIndex: fields.BEGIN_DATE, flex: 1},
				{header: tr.addresshdr, sortable: false, renderer: renderAddress, dataIndex: 'Fake', flex: 1},
				{header: tr.subjecthdr, sortable: false, dataIndex: fields.SUBJECT, flex: 1},
				{header: '&nbsp', sortable: false, renderer: renderEmailContent, dataIndex: fields.CONTENT, menuDisabled: true, hideable: false, flex: 2},
				{header: '&nbsp', width: 90, fixed: true, sortable: false, renderer: renderEmailActions, align: 'center', tdCls: 'grid-button', dataIndex: 'Fake', menuDisabled: true, hideable: false}
			],
			features: [{
				ftype: 'groupingsummary',
				groupHeaderTpl: [
					'{name:this.formatName}',
					{
						formatName: function(name) {
							return tr.lookup[name] || name
						}
					}
				],
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

	addTemplateToStore: function(values) {
		var record = this.createRecord(values);
		// mark the record added by template to be able to
		// delete it in removeTemplatesToStore
		record._cmTemplate = true;
		this.addToStoreIfNotInIt(record);
	},

	removeTemplatesFromStore: function() {
		var me = this;
		var data = me.store.data.clone();

		for (var i=0, l=data.length, r=null; i<l; ++i) {
			r = data.getAt(i);

			if (r && r._cmTemplate) {
				me.store.remove(r);	
			}
		}
	},

	createRecord: function(recordValues) {
		recordValues[fields.STATUS] = recordValues[fields.STATUS] || "Draft";
		return new CMDBuild.management.mail.Model(recordValues);
	},

	onViewEmail: function(record) {
		var viewEmailWin = new CMDBuild.view.management.common.widgets.CMEmailWindow({
			emailGrid: this,
			readOnly: true,
			record: record
		});
		viewEmailWin.show();
	},

	onEditEmail: function(record) {
		new CMDBuild.view.management.common.widgets.CMEmailWindow({
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
	recordIsEditable: recordIsEditable
});

	function recordIsEditable(record) {
		var status = record.get(fields.STATUS);
		return (status == 'Draft');
	}

	function recordIsReceived(record) {
		var status = record.get(fields.STATUS);
		return (status == 'Received') || (status == 'New');
	}

	function renderAddress(value, metadata, record) {
		if (recordIsReceived(record)) {
			return record.data[fields.FROM_ADDRESS];
		} else {
			return record.data[fields.TO_ADDRESS];
		}
	}

	function renderEmailContent(value, metadata, record) {
		var htmlContent = record.data[fields.CONTENT];
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