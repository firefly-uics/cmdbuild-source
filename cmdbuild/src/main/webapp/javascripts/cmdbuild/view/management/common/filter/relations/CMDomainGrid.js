(function() {
	var tr = CMDBuild.Translation.administration.modClass.domainProperties;

	Ext.define("CMDomainGrid.Store", {
		extend: "Ext.data.Store",
		fields: ['DomainId','DomainDescription', 'DireDescription','DestDescription'
			,'DestClassId', 'notInRelation', 'all', 'directedId'],
		load: function(idClass) {
			var me = this,
				domains = _CMCache.getDirectedDomainsByEntryType(idClass);
			
			me.removeAll();
			Ext.Array.forEach(domains, function(d, i) {
				var domain = _CMCache.getDomainById(d.dom_id),
					destination = _CMCache.getEntryTypeById(d.dst_cid),
					id = domain.get("id"),
					directedId = id + (d.src == "_1" ? "_D" : "_I");
				
				me.add({
					DomainDescription: domain.get("description"),
					DomainId: id,
					DireDescription: d.src == "_1" ? domain.get("descr_1") : domain.get("descr_2"),
					DestClassId: destination.get("id"),
					DestDescription: destination.get("text"),
					directedId: directedId,
					notInRelation: false,
					all: false
				})
			}) 
		}
	});

	Ext.define("CMDBuild.view.management.common.filter.CMDomainGrid", {
		extend: "Ext.grid.Panel",

		idClass: undefined, //set on instantiation (I hope)

		initComponent: function() {
			this.columns = this.getColumnsConfiguration();
			this.store = new CMDomainGrid.Store();

			this.plugins = [
				Ext.create('Ext.grid.plugin.CellEditing', {
					clicksToEdit: 2
				})
			],

			this.callParent(arguments);

			this.store.load(this.idClass);

			this.mon(this.getSelectionModel(), "selectionchange", function(sm, selection) {
				if (selection.length > 0) {
					var s = selection[0],
						classId = s.get("DestClassId");

					this.destinationComboStore.fillForClassId(classId);
				}
			}, this);

			this.mon(this, 'edit', function(editor, e) {
				var c = e.column.getEditor();
				e.record.set(e.column.dataIndex, c.getRawValue());
				e.record.set("DestClassId", c.getValue());
				e.record.commit();

				this.fireEvent("cm-select-destination-subclass", e.record);
			}, this);

			this.mon(this, "render", function() {
				this.destinationCombo.ownerCt = this.ownerCt;
			}, this);

			this.notInRelationCheckColumn.on("checkchange", onCheckChange, this);
			this.inRelationWithAll.on("checkchange", onCheckChange, this);
		},

		getColumnsConfiguration: function() {
			this.destinationComboStore = Ext.create('Ext.data.Store', {
				fields:["id", "descr"],
				fillForClassId: function(id) {
					this.removeAll();
					
					var ett = _CMCache.getEntryTypes(),
						out = [];
		
					for (var et in ett) {
						et = ett[et];
						if (et.get("parent") == id || et.get("id") == id) {
							out.push({
								id: et.get("id"),
								descr: et.get("text")
							});
						}
					}

					this.add(out);
				}
			});

			this.notInRelationCheckColumn = new Ext.ux.CheckColumn({
				header: CMDBuild.Translation.management.findfilter.notinrel,
				dataIndex: 'notInRelation',
				width: 30,
				fixed: true,
				menuDisabled: true,
				hideable: false
			});

			this.inRelationWithAll = new Ext.ux.CheckColumn({
				header: CMDBuild.Translation.management.findfilter.all,
				dataIndex: 'all',
				width: 30,
				fixed: true,
				menuDisabled: true,
				hideable: false
			});

			this.destinationCombo = new Ext.form.field.ComboBox({
				store: this.destinationComboStore,
				displayField: 'descr',
				valueField: "id",
				queryMode: "local",
				triggerAction: 'all'
			});

			return [{
				header : tr.domain,
				dataIndex : 'DomainDescription',
				flex : 1
			}, {
				header: CMDBuild.Translation.management.findfilter.direction,
				dataIndex: 'DireDescription',
				flex: 1
			},{
				header: tr.class_destination,
				dataIndex: "DestDescription",
				flex: 1,
				field: this.destinationCombo
			}

			,this.notInRelationCheckColumn
			,this.inRelationWithAll

			,{
				header : '&nbsp;',
				width: 30, 
				fixed: true, 
				sortable: false, 
				renderer: function(value) {
					return (value == true)?'<img class="action-attachment-download" src="images/icons/tick.png"/>':'';
				},
				align: 'center', 
				tdCls: 'grid-button', 
				dataIndex: 'setted',
				menuDisabled: true,
				id: 'imagecolumn',
				hideable: false
			}]
		}
	});

	function onCheckChange(column, recordIndex, checked) {
		var eventName = "cm-check-" + column.dataIndex;
		this.fireEvent(eventName, recordIndex, checked);
	}
})();