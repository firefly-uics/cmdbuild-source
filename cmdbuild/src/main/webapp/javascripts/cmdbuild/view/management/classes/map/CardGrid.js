(function() {

	Ext.define("CMDBuild.view.management.classes.map.CMCardGridPagingBar", {
		extend : "Ext.toolbar.Paging",

		// configuration
		grid : undefined,
		// configuration

		// override
		doRefresh : function(value) {
			if (this.grid) {
				var sm = this.grid.getSelectionModel();
				if (sm) {
					sm.deselectAll();
				}
			}
			return this.callOverridden(arguments);
		}
	});

	Ext.define('CMDBuild.view.management.classes.map.CardGrid', {
		extend : 'Ext.grid.Panel',

		requires : [ 'CMDBuild.proxy.gis.Card', 'CMDBuild.core.Utils' ],

		/**
		 * @cfg {CMDBuild.controller.management.classes.map.CardGrid}
		 */
		delegate : undefined,

		border : false,
		cls : 'cmdb-border-bottom',
		frame : false,
		map : undefined,

		/**
		 * @property {String}
		 */
		oldClassName : undefined,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent : function() {
			var store = this.getStoreForFields([]);
			var thisGrid = this;
			var me = this;
			buildPagingBar(this);
			Ext.apply(this, {
				columns : [],
				store : store
			});
			this.interactionDocument.observe(this);

			this.callParent(arguments);
		},

		listeners : {
			select : function(row, record, index) {
				this.interactionDocument.setNoZoom(true);
				this.navigateOnCard(record);
			}
		},

		/**
		 * @param {Object}
		 *            record
		 * @param {String}
		 *            record.Id
		 * @param {String}
		 *            record.IdClass
		 *
		 * @returns {Void}
		 */
		navigateOnCard : function(record) {
			this.delegate.cmfg('onCardNavigation', {
				Id : record.get('Id'),
				IdClass : record.get('IdClass')
			});
		},
		zoomOnCard : function(record) {
			this.delegate.cmfg('onCardZoom', {
				Id : record.get('Id'),
				IdClass : record.get('IdClass')
			});
		},
		// protected
		getStoreExtraParams : function() {
//			return Ext.isEmpty(this.delegate.cmfg('fieldStoreGet')) ? null : this.delegate.cmfg('fieldStoreGet')
//					.getProxy().extraParams;
			return null;
		},
		// protected
		buildClassColumn: function() {
			return {
				header: CMDBuild.Translation.subClass,
				width: 100,
				sortable: false,
				dataIndex: this.CLASS_COLUMN_DATA_INDEX
			};
		},
		refresh : function() {
			var currentCard = this.interactionDocument.getCurrentCard();
			if (!currentCard) {
				return;
			}
			var currentClassName = currentCard.className;
			if (!currentClassName) {
				return;
			}
			this.store.proxy.setExtraParam("className", currentClassName);
			if (this.oldClassName !== currentClassName) {
				var cl = _CMCache.getEntryTypeByName(currentClassName);
				var me = this;
				if (cl) {
					this.updateStoreForClassId(cl.get("id"), {
						cb : function() {
							me.store.proxy.setExtraParam("className", currentClassName);
							me.store.loadPage(1);
						}
					});
				}
				this.oldClassName = currentClassName;
			} else {
				this.store.load({
					scope : this,
					callback : function(records, operation, success) {
					}
				});
			}
		},
		// protected
		loadAttributes : function(classId, cb) {
			_CMCache.getAttributeList(classId, cb);
		},
		// protected
		addRendererToHeader : function(h) {
			h.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
				value = value || record.get(h.dataIndex);

				if (typeof value == 'undefined' || value == null) {
					return '';
				} else if (typeof value == 'object') {
					/**
					 * Some values (like reference or lookup) are serialized as
					 * object {id: "", description:""}. Here we display the
					 * description
					 */
					value = value.description;
				} else if (typeof value == 'boolean') { // Localize the boolean
					// values
					value = value ? Ext.MessageBox.buttonText.yes : Ext.MessageBox.buttonText.no;
				} else if (typeof value == 'string') { // Strip HTML tags from
					// strings in grid
					value = Ext.util.Format.stripTags(value);
				}

				return value;
			};
		},

		updateStoreForClassId : function(classId, o) {
			var me = this;

			this.loadAttributes(classId, function(attributes) {
				function callCbOrLoadFirstPage(me) {
					if (o && o.cb) {
						o.cb.call(o.scope || me);
					} else {
						me.store.loadPage(1);
					}
				}

				if (me.currentClassId == classId) {
					callCbOrLoadFirstPage(me);
				} else {
					me.currentClassId = classId;

					if (me.gridSearchField) {
						me.gridSearchField.setValue(""); // clear only the
						// field without
						// reload the grid
					}

					if (me.cmAdvancedFilter)
						me.controllerAdvancedFilterButtons.cmfg('entryTypeSet', {
							value : _CMCache.getEntryTypeById(classId).getData()
						});

					if (me.printGridMenu) {
						me.printGridMenu.setDisabled(!classId);
					}

					me.setColumnsForClass(attributes);
					// me.setGridSorting(attributes);
					callCbOrLoadFirstPage(me);
				}
			});
		},

		// protected
		getStoreForFields : function(fields) {
			var pageSize = CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT);
			var s = this.buildStore(fields, pageSize);

			return s;
		},

		/**
		 * @param {Array}
		 *            fields
		 * @param {Number}
		 *            pageSize
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @private
		 *
		 * TODO: waiting for refactor (build grid proxy)
		 */
		buildStore : function(fields, pageSize) {
			fields.push({
				name : 'Id',
				type : 'int'
			});
			fields.push({
				name : 'IdClass',
				type : 'int'
			});
			fields.push('IdClass_value');

			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad : false,
				fields : fields,
				pageSize : pageSize,
				remoteSort : true,
				proxy : {
					type : 'ajax',
					url : CMDBuild.proxy.index.Json.card.readAll,
					reader : {
						type : 'json',
						root : 'rows',
						totalProperty : 'results',
						idProperty : 'Id'
					},
					extraParams : this.getStoreExtraParams()
				}
			});
		},
		// protected
		setColumnsForClass : function(classAttributes) {
			var columns = this.buildColumnsForAttributes(classAttributes);
			var s = this.getStoreForFields(columns.fields);

			this.suspendLayouts();
			this.reconfigure(s, columns.headers);
			this.resumeLayouts(true);

			if (this.pagingBar) {
				this.pagingBar.bindStore(s);
			}

		},

		// protected
		buildColumnsForAttributes : function(classAttributes) {
			this.classAttributes = classAttributes;
			var headers = [];
			var fields = [];

			if (CMDBuild.core.Utils.isSuperclass(this.currentClassId)) {
				headers.push(this.buildClassColumn());
			}

			for (var i = 0; i < classAttributes.length; i++) {
				var attribute = classAttributes[i];
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);

				if (header && header.dataIndex != 'IdClass_value') {

					this.addRendererToHeader(header);
					// There was a day in which I receved the order to skip the
					// Notes attribute.
					// Today, the boss told me to enable the notes. So, I leave
					// the condition
					// commented to document the that a day the notes were
					// hidden.

					// if (attribute.name != "Notes") {
					headers.push(header);
					// }

					fields.push(header.dataIndex);
				} else if (attribute.name == "Description") {
					// FIXME Always add Description, even if hidden, for the
					// reference popup
					fields.push("Description");
				}
			}
			return {
				headers : headers,
				fields : fields
			};
		},
	});
	function buildPagingBar(me) {
		var items = [];
		var cmBasicFilter = [];
		me.cmBasicFilter = true;
		if (me.cmBasicFilter) {
			me.gridSearchField = new CMDBuild.field.GridSearchField({
				grid : me
			});
			items.push(me.gridSearchField);
		}

		me.pagingBar = new CMDBuild.view.management.common.CMCardGridPagingBar({
			grid : me,
			store : me.store,
			displayInfo : true,
			displayMsg : '{0} - {1} ' + CMDBuild.Translation.of + ' {2}',
			emptyMsg : CMDBuild.Translation.noTopicsToDisplay,
			items : items
		});

		me.bbar = me.pagingBar;
	}

})();
