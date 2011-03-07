CMDBuild.Administration.SetupTree = Ext.extend(CMDBuild.TreePanel, {
	id: 'administration_setup_tree',
	title: CMDBuild.Translation.administration.setup.setupTitle,	
	//custom
	translation: CMDBuild.Translation.administration.setup,
	
	initComponent: function(){
		CMDBuild.Administration.SetupTree.superclass.initComponent.apply(this, arguments);
		this.root.appendChild(this.cmdbuildTreeNode = new Ext.tree.TreeNode({
			text: this.translation.cmdbuild.menuTitle, 
			allowDrag:false,
			selectable: true,
			type: 'modsetupcmdbuild'
		}));
		
		this.root.appendChild(this.serverTreeNode = new Ext.tree.TreeNode({
			text: this.translation.server.menuTitle, 
			allowDrag:false,
			selectable: true,
			type: 'modsetupserver'
		}));
		
		this.root.appendChild(this.workflowTreeNode = new Ext.tree.TreeNode({
			text: this.translation.workflow.menuTitle, 
			allowDrag:false,
			selectable: true,
			type: 'modsetupworkflow'
		}));
		
		this.root.appendChild(this.legacydmsTreeNode = new Ext.tree.TreeNode({
			text: this.translation.workflow.email.title, 
			allowDrag:false,
			selectable: true,
			type: 'modsetupemail'
		}));
		
		this.root.appendChild(this.graphTreeNode = new Ext.tree.TreeNode({
			text: this.translation.graph.menuTitle, 
			allowDrag:false,
			selectable: true,
			type: 'modsetupgraph'
		}));
		
		this.root.appendChild(this.legacydmsTreeNode = new Ext.tree.TreeNode({
			text: this.translation.legacydms.menuTitle, 
			allowDrag:false,
			selectable: true,
			type: 'modsetupalfresco'
		}));
		
		this.root.appendChild(this.gisTreeNode = new Ext.tree.TreeNode({
			text: this.translation.gis.title, 
			allowDrag:false,
			selectable: true,
			type: 'modsetupgis'
		}));
	}
});