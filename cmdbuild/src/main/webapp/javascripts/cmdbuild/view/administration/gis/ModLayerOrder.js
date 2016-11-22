(function() {

	Ext.define("CMDBuild.view.administration.gis.ModLayerOrder", {
		extend: "Ext.grid.Panel",

		title: CMDBuild.Translation.administration.modcartography.layermanager.title,
		region: 'center',
		frame: false,
		border: true,
		withCheckToHideLayer: false,

		initComponent: function() {
			Ext.apply(this, {
				sm: new Ext.selection.RowModel(),
				store: _CMCache.getLayersStore()
			});

			this.columns = [{
				header: CMDBuild.Translation.administration.modClass.attributeProperties.description,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: 'description',
				flex: 1
			},{
				header: CMDBuild.Translation.administration.modClass.geo_attributes.master,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: 'masterTableName',
				flex: 1
			},{
				header: CMDBuild.Translation.administration.modClass.attributeProperties.type,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: 'type',
				flex: 1
			},{
				header: CMDBuild.Translation.administration.modClass.geo_attributes.min_zoom,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: 'minZoom',
				flex: 1
			},{
				header: CMDBuild.Translation.administration.modClass.geo_attributes.max_zoom,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: 'maxZoom',
				flex: 1
			}];

			if (this.withCheckToHideLayer) {
				this.columns.push(buildCheckColumn.call(this));
			}

			this.viewConfig = {
				loadMask: false,
				plugins : {
					ptype : 'gridviewdragdrop',
					dragGroup : 'layersGridDDGroup',
					dropGroup : 'layersGridDDGroup'
				},
				listeners : {
					scope: this,
					beforedrop: Ext.emptyFn,
					drop : this.beforeRowMove
				}
			};
			this.callParent(arguments);
		},

		enableDragDrop: true,

		beforeRowMove: function(node, data, dropRec, dropPosition) {
			this.fireEvent("cm-rowmove", {
				node: node,
				data: data,
				dropRec: dropRec,
				dropPosition: dropPosition
			});

			return true;
		},

		/**
		 * template method for the subclasses
		 */
		onVisibilityChecked: function(cell, recordIndex, checked) {}
	});

	function buildCheckColumn() {
		var column = Ext.create('Ext.grid.column.CheckColumn', {
			dataIndex: 'isvisible',
			text: CMDBuild.Translation.administration.modClass.geo_attributes.visibility,
			sortable: false,
			processEvent: Ext.emptyFn // Makes column readOnly
		});

		this.mon(column, 'checkchange', this.onVisibilityChecked, this);

		this.getVisibilityColDataIndex = function() {
			return column.dataIndex;
		};

		return column;
	}

})();
