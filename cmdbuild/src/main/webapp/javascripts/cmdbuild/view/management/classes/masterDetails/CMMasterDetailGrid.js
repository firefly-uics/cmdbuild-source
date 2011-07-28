(function() {
	var detailURL = "services/json/management/modcard/getdetaillist",
		fkURL =  "services/json/management/modcard/getcardlist";

	Ext.define("CMDBuild.Management.MasterDetailCardGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		loadDetails: function(p) {
			var detailIdClass = getDetailClass(p.detail);

			function setExtraParamsAndLoad() {
				this.store.proxy.url = detailURL;
				this.store.proxy.extraParams["DirectedDomain"] = p.detail.directedDomain;
				this.store.proxy.extraParams["Id"] = p.masterCard.get("Id");
				this.store.proxy.extraParams["IdClass"] = p.masterCard.get("IdClass");

				this.store.loadPage(1);
			}

			load.call(this, detailIdClass, setExtraParamsAndLoad);
		},

		loadFk: function(p) {
			var idClass = p.detail.idClass,
				fkClass = _CMCache.getEntryTypeById(idClass);

			function setExtraParamsAndLoad() {
				this.store.proxy.url = fkURL;
				this.store.proxy.extraParams['IdClass'] = idClass;
				this.store.proxy.extraParams['CQL'] = "from " 
					+ fkClass.get("name") 
					+ " where " + p.detail.name + "=" 
					+ p.masterCard.get("Id");

				this.store.loadPage(1);
			}

			load.call(this, idClass, setExtraParamsAndLoad);
		},

		updateStoreForClassId: function(classId, cb) {
			this.currentClassId = classId;
			_CMCache.getAttributeList(classId, 
				Ext.bind(function(attributes) {
					this.setColumnsForClass(attributes);
					if (cb) {
						cb.call(this);
					}
				}, this)
			);
		},

		reset: function() {
			this.store.removeAll();
			this.currentClassId = null;
			this.reconfigure(null, []);
		},

		getIconsToRender: function() {
			var icons = [];
			if (this.editable) {
				icons = ["editDetail", "deleteDetail", "showGraph", "note"];
			} else {
				icons = ["showDetail", "showGraph", "note"];
			}

			if (CMDBuild.Config.dms.enabled == "true") {
				icons.push("attach");
			}

			return icons;
		},

		// override
		buildExtraColumns: function() {
			var iconToRender = this.getIconsToRender();
			return [{
				header : '&nbsp',
				width : iconToRender.length * 20,
				fixed : true,
				sortable : false,
				renderer : function() {
					return imageTagBuilderForIcon(iconToRender);
				},
				align : 'center',
				cellCls : 'grid-button',
				dataIndex : 'Fake',
				menuDisabled : true,
				hideable : false
			}];
		}
	});
	
	function load(idClassToLoad, setExtraParamsAndLoad) {
		if (this.currentClassId != idClassToLoad) {
			this.updateStoreForClassId(idClassToLoad, setExtraParamsAndLoad);
		} else {
			setExtraParamsAndLoad.call(this);
		}
	}
	
	function getDetailClass(detail) {
		var cardinality = detail.get("cardinality");
		if (cardinality == "1:N") {
			return detail.get("idClass2");
		} else if (cardinality == "N:1") {
			return detail.get("idClass1");
		}
	}

	function imageTagBuilderForIcon(iconName) {
		var ICONS_FOLDER = "images/icons/",
			ICONS_EXTENSION = "png",
			EVENT_CLASS_PREFIX = "action-masterdetail-",
			TAG_TEMPLATE = '<img style="cursor:pointer" title="{0}" class="{1}{2}" src="{3}{4}.{5}"/>&nbsp;',
			tag = "",
			icons = {
				showDetail: {
					title: CMDBuild.Translation.management.moddetail.showdetail,
					event: "show",
					icon: "zoom"
				},
				editDetail: {
					title: CMDBuild.Translation.management.moddetail.editdetail,
					event: "edit",
					icon: "modify"
				},
				deleteDetail: {
					title: CMDBuild.Translation.management.moddetail.deletedetail,
					event: "delete",
					icon: "cross"
				},
				showGraph: {
					title: CMDBuild.Translation.management.moddetail.showgraph,
					event: "graph",
					icon: "chart_organisation"
				},		
				note: {
					title: CMDBuild.Translation.management.moddetail.shownotes,
					event: "note",
					icon: "note"
				},
				attach: {
					title: CMDBuild.Translation.management.moddetail.showattach,
					event: "attach",
					icon: "attach"
				}
			};
		
		function buildTag(iconName) {
			var icon = icons[iconName];
			if (icon) {
				return Ext.String.format(TAG_TEMPLATE, icon.title, EVENT_CLASS_PREFIX, icon.event, ICONS_FOLDER, icon.icon, ICONS_EXTENSION);
			} else {
				return Ext.String.format("<span>{0}</span>", iconName);
			}
		}
	
		if (Ext.isArray(iconName)) {
			for (var i=0, len=iconName.length; i<len; ++i) {
				tag += buildTag(iconName[i]);
			}
		} else {
			tag = buildTag(iconName);
		}
		
		return tag;
	}

})();