(function() {
	
	var MD = "detail";
	var FK = "foreignkey";

	Ext.define("CMDBuild.Management.CardMasterDetailTab", {
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
			this.tabs.on("click", onTabClick, this);

//            this.subscribe('cmdb-load-' + this.eventType, this.updateDetailForLoadedCard, this);
//            this.subscribe('cmdb-reload-' + this.eventType, this.onReloadCard, this);

            // given the tab is not active but enabled
            // and we change card
            // when the tab is activated
            //then the grid should be updated
			this.on('activate', function() {
				if (!this.isLoaded) {
					this.updateDetailGrid(this.actualMasterData);
				}
			}, this);
		},

		onClassSelected: function(id) {
			if (!id) {
				return
			}

			this.currentForeignKey = null;
			this.currentDetail = null;
			this.actualMasterData = null;
			this.currentTab = null;

			this.lastClassSelected = id;
			loadDetailsAndFKThenBuildSideTabs.call(this);

			this.disable();
		},

		onCardSelected: function(card) {
			this.actualMasterData = card;
			this.updateDetailGridIfDisplayed();

			this.enable();
		},

		selectDetail: function(detail) {
			this.currentForeignKey = undefined;
			this.currentDetail = detail;

			var et = _CMCache.getEntryTypeById(getDetailClass(this.currentDetail));

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

		updateDetailGridIfDisplayed: function() {
			this.isLoaded = false;
			this.updateDetailGrid(this.actualMasterData); //TODO 3 to 4 only if visible
		},

		updateDetailGrid: function() {

			if (this.currentDetail && this.actualMasterData) {

				this.detailGrid.loadDetails({
					domain: this.currentDetail,
					masterCard: this.actualMasterData
				});

			} else {

				this.detailGrid.reset();
				_debug("no actualMasterData");

			}

			this.isLoaded = true;

//				var isSuperClass = (this.currentDetail.detailSubclasses.length > 1);
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
	
	function onTabClick(tab) {
		if (this.currentTab === tab) {
			return;
		}

		this.currentTab = tab;
		var targetPanel = tab.targetPanel,
			type = targetPanel.detailType,
			detail = this.details[type][targetPanel.detailId];

		if (type == MD) {
			this.selectDetail(detail);
		} else {
			this.selectForeignKey(detail);
		}

		this.addDetailButton.enable();
		this.updateDetailGrid();
	}
	
	function loadDetailsAndFKThenBuildSideTabs(params) {
		var domainList = _CMCache.getMasterDetailsForClassId(this.lastClassSelected);

		this.details = {};
		this.details[MD] = {};

		for (var i = 0, len = domainList.length; i < len; i++) {
			var domain = domainList[i];
			domain['directedDomain'] = setDirectedDomain(domain);
			this.details[MD][domain.get("id")] = domain;
		}

		buildTabs.call(this);

		// TODO 3 to 4 add the FK

//			CMDBuild.ServiceProxy.getFKTargetingClass( {
//				params: {
//					idClass: idClass
//				},
//				scope: _this,
//				success: takeFkAttributesAndBuildTabs
//			});

//		function takeFkAttributesAndBuildTabs(response, options, attributes) {
//			this.details[FK] = {};
//			for (var i=0, l = attributes.length; i < l; ++i) {
//				var attr = attributes[i];
//				this.details[FK][attr.name] = attr;
//			}
//
//			if (CMDBuild.Utils.isEmpty(this.details[FK]) 
//					&& CMDBuild.Utils.isEmpty(this.details[MD])) {
//				this.disable();
//				this.tabs.removeAll();
//				this.fireEvent("empty");
//			} else {
//				this.enable();
//				
//			}
//		}

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
		function build() {
			this.tabs.removeAll();
			
			function _buildTabs(type) {
				for (var key in this.details[type]) {
					var detailLabel = this.details[type][key].get("description");

					this.tabs.addTabFor({
						title: detailLabel,
						tabLabel: detailLabel,
						detailType: type,
						detailId: this.details[type][key].get("id"),
						on: function() {}
					}, type);
				}
			}
			
			_buildTabs.call(this, MD);
			_buildTabs.call(this, FK);

			this.doLayout();
			this.tabs.activateFirst();
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