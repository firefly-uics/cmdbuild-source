(function() {
	var TARGET_CLASS_ID = "dst_cid";
	var tr = CMDBuild.Translation.management.modcard;
	var col_tr = CMDBuild.Translation.management.modcard.relation_columns;

	Ext.define("CMRelationPanelModel", {
		extend: "Ext.data.Model",
		fields: [
			'dom_id', 'dom_desc', 'label',
			'dst_code', 'dst_id', 'dst_desc', 'dst_cid',
			'rel_attr', 'rel_date', 'rel_id',
			'relations_size', 'src'
		]
	});

	Ext.define("CMDBuild.view.management.classes.CMCardRelationsPanel", {
		extend: "Ext.tree.Panel",
		initComponent: function() {

			this.addRelationButton = new CMDBuild.AddRelationMenuButton({
				text : tr.add_relations
			});

			var me = this;

			Ext.apply(this, {
				loadMask: false,
				hideMode: "offsets",
				store: new Ext.data.TreeStore({
					model: "CMRelationPanelModel",
					root : {
						expanded : true,
						children : []
					},
					autoLoad: false
				}),
				rootVisible: false,
				columns: [
					{header: col_tr.domain, sortable: false, dataIndex: 'dom_id', hidden: true},
					{header: col_tr.destclass, flex: 2, sortable: false, dataIndex: 'label', xtype: 'treecolumn'},
					{header: col_tr.begin_date, flex: 1, sortable: false, dataIndex: 'rel_date'},
					{header: col_tr.code, flex: 1, sortable: false, dataIndex: 'dst_code'},
					{header: col_tr.description, flex: 2, sortable: false, dataIndex: 'dst_desc'},
					{header: CMDBuild.Translation.administration.modClass.tabs.attributes, flex: 4, sortable: false, dataIndex: 'rel_attr'},
					{
						header: '&nbsp', 
						width: 90,
						fixed: true, 
						sortable: false, 
						renderer: renderRelationActions, 
						align: 'center', 
						cellCls: 'grid-button', 
						dataIndex: 'Fake',
						menuDisabled: true,
						hideable: false
					}
				],
				tbar: [this.addRelationButton]
			});

			this.callParent(arguments);
		},

		clearStore: function() {
			this.store.getRootNode().removeAll();
		},

		fillWithData: function(domains) {
			domains = domains || [];
			var nodes = [],
				r = this.store.getRootNode();

			for (var i=0, l=domains.length; i<l; ++i) {
				var domainRensonseObj = domains[i],
					domainCachedData = _CMCache.getDomainById(domainRensonseObj.id);

				nodes.push( buildNodeFor(domainRensonseObj, domainCachedData));
			}

			r.removeAll();
			r.appendChild(nodes);
		},

		onAddCardButtonClick: function() {
			this.disable();
		},

		convertRelationInNodes: convertRelationInNodes
	});
	
	function buildNodeFor(domainRensonseObj, domainCachedData) {
		var children = [],
			attributes = domainCachedData.data.attributes,
			attributesToString = "",
			oversize = domainRensonseObj.relations_size > CMDBuild.Config.cmdbuild.relationlimit,
			src = domainRensonseObj.src,
			domId = domainCachedData.get("id");

		node = {
			dom_id: domId,
			label: buildDescriptionForDomainNode(domainRensonseObj, domainCachedData),

			src: src,
			relations_size: domainRensonseObj.relations_size,

			expanded: !oversize,
			leaf: false,
			children: []
		};

		if (oversize) {
			// it is asynchronous, add an empty obj to get the possibility to expand the tree widget
			node.children.push({});
		} else {
			node.children = convertRelationInNodes(domainRensonseObj.relations, domId, src);
		}

		if (attributes.length > 0) {
			for (var i=0, l=attributes.length; i<l; i++) {
				attributesToString += i==0 ? "" : " - ";
				attributesToString += attributes[i].description;
			}

			node.rel_attr = attributesToString;
		}
		return node;
	}

	function convertRelationInNodes(relations, dom_id, src) {
		relations = relations || [];
		var r,c,i=0,
			l=relations.length,
			nodes = [];

		for (; i<l; ++i) {
			r = relations[i];
			c = _CMCache.getEntryTypeById(r.dst_cid);
			r.leaf = true;
			r.label = c.get("text");
			r.dom_id = dom_id;
			r.src = src;

			nodes.push(r);
		}

		return nodes;
	}

	function renderRelationActions(value, metadata, record) {
		if (record.get("depth") == 1) { // the domains node has no icons to render
			return "";
		}

		var tr = CMDBuild.Translation.management.modcard,
			actionsHtml = '<img style="cursor:pointer" title="'+tr.open_relation+'" class="action-relation-go" src="images/icons/bullet_go.png"/>&nbsp;',
			tableId = record.get(TARGET_CLASS_ID),
			domainObj = _CMCache.getDomainById(record.get("dom_id")),
			table = _CMCache.getClassById(tableId);

		if (domainObj.get("writePrivileges")) {
			actionsHtml += '<img style="cursor:pointer" title="'+tr.edit_relation+'" class="action-relation-edit" src="images/icons/link_edit.png"/>&nbsp;'
			+ '<img style="cursor:pointer" title="'+tr.delete_relation+'" class="action-relation-delete" src="images/icons/link_delete.png"/>&nbsp;';
		}

		if (table && table.get("priv_write")) {
			actionsHtml += '<img style="cursor:pointer" class="action-relation-editcard" src="images/icons/modify.png"/>';
		} else {
			actionsHtml += '<img style="cursor:pointer" title="'+tr.view_relation+'" class="action-relation-viewcard" src="images/icons/zoom.png"/>';
		}

		return actionsHtml;
	}

	function buildDescriptionForDomainNode(domainRensonseObj, domainCachedData) {
		var prefix = domainCachedData.get("descr"+domainRensonseObj.src),
			s = domainRensonseObj.relations_size,
			postfix = s  > 1 ? CMDBuild.Translation.management.modcard.relation_columns.items : CMDBuild.Translation.management.modcard.relation_columns.item;
		
		return prefix + " ("+ s + " " + postfix + ")" ;
	}
})();