(function() {
	
	var MD = "detail";
	var FK = "foreignkey";

	CMDBuild.Management.CardMasterDetailTab = Ext.extend(Ext.Panel, {
        editable: true,
        eventType: 'card',
        eventmastertype: 'class',
        
        constructor: function() {
			this.addDetailButton = new CMDBuild.AddCardMenuButton({
	            classId: undefined,
	            eventName: "cmdb-addDetail",
	            baseText: CMDBuild.Translation.management.moddetail.adddetail,
	            textPrefix: CMDBuild.Translation.management.moddetail.adddetail
	        });
			this.detailGrid = new CMDBuild.Management.MasterDetailCardGrid({
                editable: this.editable,
                region: "center"
            });
			this.tabs = new CMDBuild.Tabs();
			CMDBuild.Management.CardMasterDetailTab.superclass.constructor.apply(this, arguments);
		},
		
        initComponent: function() {
			this.layout = "border";
            this.tbar = [ this.addDetailButton ];
            this.items = [ this.detailGrid, this.tabs];
            CMDBuild.Management.CardMasterDetailTab.superclass.initComponent.apply(this, arguments);
			
			this.addDetailButton.on('cmdb-addDetail', this.takeDetailAttributes, this);
            this.tabs.on("click", onTabClick, this);
            
            this.subscribe('cmdb-load-' + this.eventType, this.updateDetailForLoadedCard, this);
            this.subscribe('cmdb-reload-' + this.eventType, this.onReloadCard, this);

            // given the tab is not active but enabled
            // and we change card
            // when the tab is activated
            // then the grid should be updated
            this.on('activate', function() {
            	if (!this.isLoaded) {
    				this.updateDetailGrid(this.actualMasterData);
    			}
            }, this);
        },

        onSelectClass: function(params) {
        	if (!params) {
            	return
        	}
        	
        	this.currentForeignKey = null;
        	this.currentDetail = null;
        	this.currentTab = null;
        	
            this.lastClassSelected = params;
            this.detailGrid.reset();
            
            initAddDetailButton.call(this);
            loadDetailsAndFKThenBuildSideTabs.call(this);
        },

        onReloadCard: function(params) {
            if (this.currentDetail) {
                Ext.each(this.currentDetail.detailSubclasses,
                    function(item, index, allItem) {
                        if (item.classId == params.classId) {
                        	this.updateDetailGridIfDisplayed();
                            return false;
                        }
                    }, this);
            }
        },

        selectDetail: function(params) {
            this.currentForeignKey = undefined;
            this.currentDetail = params;
           
            if (this.currentDetail.detailSubclasses) {
                var detailTable = CMDBuild.Cache.getTableById(this.currentDetail.detailclass);
                if (detailTable) {
                    this.addDetailButton.setClassId(detailTable);
                }
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

        updateDetailForLoadedCard: function(eventParams) {
            this.actualMasterData = eventParams.record.data;
            this.updateDetailGridIfDisplayed();
        },

        updateDetailGridIfDisplayed: function() {
            this.isLoaded = false;
            if (this.isVisible() && !this.disabled) {
                this.updateDetailGrid(this.actualMasterData);
            }
        },

        updateDetailGrid: function() {
        	var detailClassId, callback=Ext.emptyFn;
        	
            if (this.currentDetail && this.actualMasterData) {
                var isSuperClass = (this.currentDetail.detailSubclasses.length > 1);
                detailClassId = getDetailClass(this.currentDetail);
                callback = this.loadDetailCardList.createDelegate( this, [
                    this.actualMasterData.Id,
                    this.actualMasterData.IdClass,
                    this.currentDetail.directedDomain,
                    isSuperClass,
                    this.currentDetail.classType
                ],true);
            } else if (this.currentForeignKey && this.actualMasterData) {
            	detailClassId = this.currentForeignKey.id;
                callback = this.loadFKCardList.createDelegate(this, [
	                this.currentForeignKey,
	                this.currentforeignKeyAttribute,
	                this.actualMasterData.Id 
	            ], true);
            }
            
            CMDBuild.Management.FieldManager.loadAttributes(detailClassId, callback);
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
            this.isLoaded = true;
        },

        loadFKCardList: function(attributes, fkClass, fkAttribute, idCard) {
            this.detailGrid.loadFKCardList(attributes, fkClass, fkAttribute, idCard);
            this.isLoaded = true;
        },

        takeDetailAttributes: function(detail) {
            var callback = showAddDetailWindow
                    .createDelegate(this, [ detail ], true);
            CMDBuild.Management.FieldManager.loadAttributes(
                    detail.classId, callback);
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
    	var type = tab.targetPanel.detailType;
    	var detail = this.details[type][tab.text];
        if (type == MD) {
        	this.selectDetail(detail);
        } else {
        	this.selectForeignKey(detail);
        }
        this.addDetailButton.enable();
        this.updateDetailGrid();
    }
	
	function loadDetailsAndFKThenBuildSideTabs(params) {
		var _this = this;
		var idClass = this.lastClassSelected.id;
		CMDBuild.ServiceProxy.getDomainList( {
		    params: {
			    idClass: idClass
		    },
		    scope: _this,
		    success: filterMasterDetailAndLoadFK
		});

		function filterMasterDetailAndLoadFK(response, options, decoded) {
			var domainList = decoded.rows;
			this.details = {};
			this.details[MD] = {};

			for (var i = 0, len = domainList.length; i < len; i++) {
				var domain = domainList[i];
				if (domain.detailSubclasses) {
					domain['directedDomain'] = setDirectedDomain(domain);
					this.details[MD][domain.description] = domain;
				}
			}
			
			CMDBuild.ServiceProxy.getFKTargetingClass( {
			    params: {
				    idClass: idClass
			    },
			    scope: _this,
			    success: takeFkAttributesAndBuildTabs
			});

		}

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
		
		function buildTabs() {
			function build() {
				this.tabs.removeAll();
				
				function _buildTabs(type) {
					for (var detailLabel in this.details[type]) {
						this.tabs.addTabFor({
							title: detailLabel,
							tabLabel: detailLabel,
							detailType: type,
							on: function() {}
						}, type);
					}
				}
				
				_buildTabs.call(this, MD);
				_buildTabs.call(this, FK);

				this.tabs.doLayout(); // force the recalculation of tabs width
				this.doLayout();
				this.tabs.activateFirst();
			}
			
			if (this.isVisible()) {
				build.call(this);
			} else {
				this.on("show", build, this, {single: true});
			}			
		}
		
		function setDirectedDomain(domain) {
			if (domain.cardinality == "1:N") {
				return domain.idDomain + "_D";
			} else if (domain.cardinality == "N:1") {
				return domain.idDomain + "_I";
			} else {
				CMDBuild.log.error('Wrong cardinality');
			}
		}
	}
	
	function getDetailClass(detail) {
        if (detail.cardinality == "1:N") {
            return detail.class2id;
        } else if (detail.cardinality == "N:1") {
            return detail.class1id;
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
	
	Ext.reg('cardmasterdetailtab', CMDBuild.Management.CardMasterDetailTab);
})();