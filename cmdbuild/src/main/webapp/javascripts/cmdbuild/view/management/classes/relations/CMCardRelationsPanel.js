(function() {
	var TARGET_CLASS_ID = "dst_cid",
		tr = CMDBuild.Translation.management.modcard,
		col_tr = CMDBuild.Translation.management.modcard.relation_columns;

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
		cmWithAddButton: true,

		initComponent: function() {
			this.buildTBar();

			this.attrsColumn = new Ext.grid.column.Column({
				header: CMDBuild.Translation.administration.modClass.tabs.attributes,
				hideMode: "visibility", // Otherwise it fails calling twice hide() on it
				flex: 3,
				sortable: false,
				dataIndex: "rel_attr"
			});

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
					this.attrsColumn,
					{
						header: '&nbsp',
						fixed: true, 
						sortable: false, 
						renderer: this.renderRelationActions, 
						align: 'center', 
						tdCls: 'grid-button', 
						dataIndex: 'Fake',
						menuDisabled: true,
						hideable: false
					}
				]
			});

			this.callParent(arguments);
		},

		buildTBar: function() {
			if (this.cmWithAddButton) {
				this.addRelationButton = new CMDBuild.AddRelationMenuButton({
					text : tr.add_relations
				});

				this.tbar = [this.addRelationButton]
			} else {
				// build a null-object to skip some check
				this.addRelationButton = {
					enable: function(){},
					disable: function(){},
					setDomainsForEntryType: function(){},
					on: function(){}
				}
			}
		},

		clearStore: function() {
			this.store.getRootNode().removeAll();
		},

		fillWithData: function(domains) {
			this.showAttributesColumn = false;

			domains = domains || [];
			var nodes = [],
				r = this.store.getRootNode();

			for (var i=0, l=domains.length; i<l; ++i) {
				var domainResponseObj = domains[i],
					domainCachedData = _CMCache.getDomainById(domainResponseObj.id);
				
				if (domainCachedData) {
					nodes.push(buildNodeForDomain.call(this, domainResponseObj, domainCachedData));
				} else {
					CMDBuild.log.error("I have not cached data for domain", domainResponseObj.id);
				}
			}

			r.removeAll();
			r.appendChild(nodes);

			if (this.showAttributesColumn) {
				this.attrsColumn.show();
			} else {
				this.attrsColumn.hide();
			}
		},

		onAddCardButtonClick: function() {
			this.disable();
		},

		convertRelationInNodes: convertRelationInNodes,
		renderRelationActions: renderRelationActions
	});
	
	function buildNodeForDomain(domainResponseObj, domainCachedData) {
		var children = [],
			attributes = domainCachedData.data.attributes,
			attributesToString = "",
			oversize = domainResponseObj.relations_size > CMDBuild.Config.cmdbuild.relationlimit,
			src = domainResponseObj.src,
			domId = domainCachedData.get("id"),
			node;

		node = {
			dom_id: domId,
			label: buildDescriptionForDomainNode(domainResponseObj, domainCachedData),

			src: src,
			relations_size: domainResponseObj.relations_size,

			expanded: !oversize,
			leaf: false,
			children: [],
			rel_attr_keys: []
		};

		if (attributes.length > 0) {
			this.showAttributesColumn = true;
			var key = "";
			for (var i=0, l=attributes.length; i<l; i++) {
				if (attributes[i].fieldmode == "hidden")
					continue;

				key = attributes[i].description
				attributesToString += i==0 ? "" : " | ";
				attributesToString += key;
				node.rel_attr_keys.push(key);
			}

			node.rel_attr = attributesToString;
		}

		if (oversize) {
			// it is asynchronous, add an empty obj to get the possibility to expand the tree widget
			node.children.push({});
		} else {
			node.children = convertRelationInNodes(domainResponseObj.relations, domId, src, node);
		}

		return node;
	}

	function convertRelationInNodes(relations, dom_id, src, node) {
		relations = relations || [];
		var r,c,i=0,
			l=relations.length,
			nodes = [],
			attributesToString = "",
			key,
			val;

		for (; i<l; ++i) {
			r = relations[i];
			c = _CMCache.getEntryTypeById(r.dst_cid);

			if (!c) {
				continue;
			}

			r.leaf = true;
			r.label = c.get("text");
			r.dom_id = dom_id;
			r.src = src;

			attributesToString = "";
			for (var j=0; j<node.rel_attr_keys.length; ++j) {
				key = node.rel_attr_keys[j];
				val = r.rel_attr[key] || " - "; // val never undefined

				attributesToString += j==0 ? "" : " | ";
				attributesToString += val.dsc || val;
			}
			r.attr_as_obj = r.rel_attr; // used in modify window
			r.rel_attr = attributesToString;
			nodes.push(r);
		}

		return nodes;
	}

	function renderRelationActions(value, metadata, record) {
		if (record.get("depth") == 1) { // the domains node has no icons to render
			return "";
		}

		var tr = CMDBuild.Translation.management.modcard,
			actionsHtml = '<img style="cursor:pointer" title="'+tr.open_relation+'" class="action-relation-go" src="images/icons/bullet_go.png"/>',
			tableId = record.get(TARGET_CLASS_ID),
			domainObj = _CMCache.getDomainById(record.get("dom_id")),
			table = _CMCache.getClassById(tableId);

		if (domainObj.get("writePrivileges")) {
			actionsHtml += '<img style="cursor:pointer" title="'+tr.edit_relation+'" class="action-relation-edit" src="images/icons/link_edit.png"/>'
			+ '<img style="cursor:pointer" title="'+tr.delete_relation+'" class="action-relation-delete" src="images/icons/link_delete.png"/>';
		}

		if (table && table.get("priv_write")) {
			actionsHtml += '<img style="cursor:pointer" class="action-relation-editcard" src="images/icons/modify.png"/>';
		} else {
			actionsHtml += '<img style="cursor:pointer" title="'+tr.view_relation+'" class="action-relation-viewcard" src="images/icons/zoom.png"/>';
		}

		return actionsHtml;
	}

	function buildDescriptionForDomainNode(domainResponseObj, domainCachedData) {
		var prefix = domainCachedData.get("descr"+domainResponseObj.src),
			s = domainResponseObj.relations_size,
			postfix = s  > 1 ? CMDBuild.Translation.management.modcard.relation_columns.items : CMDBuild.Translation.management.modcard.relation_columns.item;
		
		return prefix + " ("+ s + " " + postfix + ")" ;
	}
})();