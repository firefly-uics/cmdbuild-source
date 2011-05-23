var tableTypes = CMDBuild.Constants.cachedTableType;
CMDBuild.Structure = {
	navigation: {
		title: undefined, //to not show it in the administration module 
		createTree: function() {
			if (CMDBuild.Runtime.DisabledModules.navigation || !CMDBuild.Cache.menuTree) {
				return undefined;
			} else {
				return new CMDBuild.Management.MenuTree({
					title: CMDBuild.Translation.management.modmenu.menu
				});
			}
		}
	},
	"class": {	
		title:  CMDBuild.Translation.management.modcard.treetitle,
		createTree: function() {
			var tr = CMDBuild.Translation.common.tree_names;
			var classes = CMDBuild.Cache.getTree(tableTypes["class"],
					rootId=undefined, rootText=tr["class"], sorted=true);
	        var simpleTables = CMDBuild.Cache.getTree(tableTypes.simpletable,
	        		rootId=tableTypes.simpletable, rootText=tr.simpletable, sorted=true);
	        
	        if (!classes && !simpleTables) {
				return undefined;
			}
	        
	        if (CMDBuild.Runtime.DisabledModules["class"] || (classes.childNodes.length == 0 && !simpleTables)) {
		        return undefined;
	        } else {
	        	return new CMDBuild.TreePanel( {
		            title: CMDBuild.Structure["class"].title,
		            border: false,
		            rootVisible: false,
		            root: [ classes, simpleTables ],
		            cmType: "card"
		        });
	        }
        }
	},
	process: {
		title: CMDBuild.Translation.management.modworkflow.treetitle,
		createTree: function() {
			var process = CMDBuild.Cache.getTree(tableTypes.processclass,
					undefined, undefined, sorted=true);
			if (!process) {
				return undefined;
			}
			if (CMDBuild.Runtime.DisabledModules.process || process.childNodes.length == 0) {
				return undefined;
			} else {
				return new CMDBuild.TreePanel( {
		            title: CMDBuild.Structure.process.title,
		            border: false,
		            rootVisible: true,
		            root: [ process ]
		        });
			}
		}
	},
	report: {
		title: CMDBuild.Translation.management.modreport.treetitle,
		createTree: function() {
			var reports = CMDBuild.Cache.getTree(tableTypes.report, tableTypes.report, undefined, sorted=true);
			if (!reports) {
				return undefined;
			}
			if (CMDBuild.Runtime.DisabledModules.report || !reports) {
				return undefined;
			} else {
				return new CMDBuild.TreePanel({
					title: CMDBuild.Structure.report.title,			
					border: false,
					rootVisible: false,
					root: [reports]
				});
			}
		}
	},
	utilities: {
		title: CMDBuild.Translation.management.modutilities.title,
		submodules: {
			changePassword: {
				title: CMDBuild.Translation.management.modutilities.changepassword.title, 
				type: 'changepassword'
			},
			bulkupdate: {
				title:CMDBuild.Translation.management.modutilities.bulkupdate.title, 
				type: 'bulkcardupdate'
			},
			importcsv: {
				title: CMDBuild.Translation.management.modutilities.csv.title,
				type: 'importcsv'
			},
			exportcsv: {
				title: CMDBuild.Translation.management.modutilities.csv.title_export,
				type: 'exportcsv'
			}
		},
		createTree: function() {
			var tree = new CMDBuild.Management.UtilitiesTree({
				title: CMDBuild.Structure.utilities.title,
				rootVisible: false,
				border: false,
				submodules: CMDBuild.Structure.utilities.submodules
			});
			
			if (tree.getRootNode().childNodes.length == 0) {
				delete tree;
				return undefined;
			} else {
				return tree;
			}
		}
	}
};