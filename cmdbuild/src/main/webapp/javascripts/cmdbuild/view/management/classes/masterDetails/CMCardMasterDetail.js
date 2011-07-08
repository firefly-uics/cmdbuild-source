(function() {
	
	var MD = "detail";
	var FK = "foreignkey";

	Ext.define("CMDBuild.view.management.classes.masterDetails.CMCardMasterDetail", {
		extend: "Ext.panel.Panel",

		editable: true,
		eventType: 'card',
		eventmastertype: 'class',

		constructor: function() {
			this.addDetailButton = new CMDBuild.AddCardMenuButton({
				classId: undefined,
				baseText: CMDBuild.Translation.management.moddetail.adddetail,
				textPrefix: CMDBuild.Translation.management.moddetail.adddetail
			});

			this.detailGrid = new CMDBuild.Management.MasterDetailCardGrid({
				editable: this.editable,
				border: "0 1 0 0",
				region: "center",
				columns: [],
				loadMask: false,
				cmAdvancedFilter: false,
				cmAddGraphColumn: false
			});

			this.tabs = new CMDBuild.Tabs({
				region: "east"
			});

			this.callParent(arguments);
		},

		initComponent: function() {

			Ext.apply(this, {
				layout: "border",
				tbar: [ this.addDetailButton ],
				items: [ this.detailGrid, this.tabs]
			});

			this.callParent(arguments);

//            this.subscribe('cmdb-load-' + this.eventType, this.updateDetailForLoadedCard, this);
//            this.subscribe('cmdb-reload-' + this.eventType, this.onReloadCard, this);
		},

		loadDetailsAndFKThenBuildSideTabs: function(classId) {
			var domainList = _CMCache.getMasterDetailsForClassId(classId),
				me = this;
			
			this.details = {};
			this.details[MD] = {};
			this.details[FK] = {};

			for (var i = 0, len = domainList.length; i < len; i++) {
				var domain = domainList[i];
				domain['directedDomain'] = setDirectedDomain(domain);
				this.details[MD][domain.get("id")] = domain;
			}

			CMDBuild.ServiceProxy.getFKTargetingClass( {
				params: {
					idClass: classId
				},
				scope: me,
				success: takeFkAttributesAndBuildTabs
			});

			function takeFkAttributesAndBuildTabs(response, options, attributes) {
				this.details[FK] = {};
				for (var i=0, l = attributes.length; i < l; ++i) {
					var attr = attributes[i];
					this.details[FK][attr.name] = attr;
				}

				if (CMDBuild.Utils.isEmpty(this.details[FK]) 
						&& CMDBuild.Utils.isEmpty(this.details[MD])) {
					this.disable();
					this.tabs.removeAll();
					this.fireEvent("empty");
				} else {
					this.enable();
					buildTabs.call(this);
				}
			}
		},

		selectDetail: function(detail) {
			var et = _CMCache.getEntryTypeById(getDetailClass(detail));

			if (et) {
				this.addDetailButton.updateForEntry(et);
			}

		},

		selectForeignKey: function(params) {
			this.currentDetail = undefined;
			var foreignKeyAttribute = params;
			this.currentForeignKey = CMDBuild.Cache.getTableById(foreignKeyAttribute.idClass);
			this.currentforeignKeyAttribute = foreignKeyAttribute;
			this.detailGrid.fkAttribute = this.currentforeignKeyAttribute;
			this.addDetailButton.setClassId(this.currentForeignKey);
		},

		resetDetailGrid: function() {
			this.detailGrid.reset();
		},

		activateFirstTab: function() {
			this.tabs.activateFirst();
		},

		updateDetailGrid: function(p) {
			this.detailGrid.loadDetails(p);

//			var isSuperClass = (this.currentDetail.detailSubclasses.length > 1);
//				detailClassId = getDetailClass(this.currentDetail);
//                callback = this.loadDetailCardList.createDelegate( this, [
//                    this.actualMasterData.Id,
//                    this.actualMasterData.IdClass,
//                    this.currentDetail.directedDomain,
//                    isSuperClass,
//                    this.currentDetail.classType
//                ],true);
//            } else if (this.currentForeignKey && this.actualMasterData) {
//            	detailClassId = this.currentForeignKey.id;
//                callback = this.loadFKCardList.createDelegate(this, [
//	                this.currentForeignKey,
//	                this.currentforeignKeyAttribute,
//	                this.actualMasterData.Id 
//	            ], true);
//            }
        },

        loadDetailCardList: function(attributeList, cardId, classId, idDomain, superclass, classType) {
            this.actualAttributeList = attributeList;
            this.idDomain = idDomain;
            this.detailGrid.loadDetailCardList( {
                directedDomain: idDomain,
                cardId: cardId,
                classId: classId,
                classAttributes: attributeList,
                className: this.currentDetail.name,
                superclass: superclass,
                classType: classType
            });
            
        },

        loadFKCardList: function(attributes, fkClass, fkAttribute, idCard) {
            this.detailGrid.loadFKCardList(attributes, fkClass, fkAttribute, idCard);
            this.isLoaded = true;
        }

	});

	function initAddDetailButton() {
		this.addDetailButton.disable();
		if (this.editable) {
			this.addDetailButton.show();
		} else {
			this.addDetailButton.hide();
		}
	}
	
	

	function setDirectedDomain(domain) {
		var cardinality = domain.get("cardinality"),
			idDomain = domain.get("id");
		
		if (cardinality == "1:N") {
			return idDomain + "_D";
		} else if (cardinality == "N:1") {
			return idDomain + "_I";
		} else {
			CMDBuild.log.error('Wrong cardinality');
		}
	}
	
	function buildTabs() {
		var details = this.details;
		function build() {
			this.tabs.removeAll(true);

			function _buildTabs(tabs, type) {
				tabs = tabs || [];
				for (var key in tabs) {
					var d = tabs[key],
						detailLabel = d.get("description"),
						detailId = d.get("id")

					this.tabs.addTabFor({
						title: detailLabel,
						tabLabel: detailLabel,
						detailType: type,
						detailId: detailId,
						on: function() {}
					}, type);
				}
			}

			_buildTabs.call(this, details[MD], MD);
			_buildTabs.call(this, details[FK], FK);

			this.doLayout();

			Ext.Function.createDelayed(function() {
				this.tabs.activateFirst();
			}, 100, this)();
		}

		if (this.isVisible()) {
			build.call(this);
		} else {
			this.on("show", build, this, {single: true});
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

	function showAddDetailWindow(attributes, detail) {
		var idDomain;
		if (this.currentDetail) {
			idDomain = this.currentDetail.directedDomain;
		} else {
			idDomain = this.currentForeignKey.id;
		}

		var win = new CMDBuild.Management.AddDetailWindow( {
			titlePortion: "",
			detail: detail,
			classAttributes: attributes,
			fkAttribute: this.currentforeignKeyAttribute,
			masterData: this.actualMasterData,
			idDomain: idDomain,
			classId: detail.classId,
			className: detail.className
		});
		win.show();
	}
})();