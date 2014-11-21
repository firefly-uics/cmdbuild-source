(function() {

	var tr = CMDBuild.Translation.management.modcard.history_columns;

	Ext.define('CMDBuild.view.management.classes.CMCardHistoryTab', {
		extend: 'Ext.grid.Panel',

		/**
		 * @cfg {Boolean}
		 */
		autoScroll: true,

		/**
		 * @cfg {String}
		 */
		cls: 'history_panel',

		/**
		 * @property {Array}
		 */
		columns: undefined,

		/**
		 * @property {Object}
		 */
		currentTemplate: null,

		/**
		 * @cfg {String}
		 */
		eventtype: 'card',

		/**
		 * @cfg {String}
		 */
		eventmastertype: 'class',

		/**
		 * @property {Ext.data.JsonStore}
		 */
		store: undefined,

		constructor: function() {
			Ext.apply(this, {
				plugins: [{
					ptype: 'rowexpander',
					rowBodyTpl: 'ROW EXPANDER REQUIRES THIS TO BE DEFINED',
					getRowBodyFeatureData: function(record, idx, rowValues) {
						Ext.grid.plugin.RowExpander.prototype.getRowBodyFeatureData.apply(this, arguments);

						rowValues.rowBody  = genHistoryBody(record);
					},
					expanderWidth: 18
				}],
				columns: this.getGridColumns(),
				store: Ext.create('Ext.data.JsonStore', {
					proxy: {
						type: 'ajax',
						url: 'services/json/management/modcard/getcardhistory',
						reader: {
							type: 'json',
							root: 'rows'
						}
					},
					sorters: [
						{
							property: 'BeginDate',
							direction: 'DESC'
						},
						{
							property: '_EndDate',
							direction: 'DESC'
						}
					],
					fields: this.getStoreFields(),
					baseParams: {
						IsProcess: (this.eventmastertype == 'processclass')
					}
				})
			});

			this.callParent(arguments);

			this.view.on('expandbody', function() {
				this.doLayout(); // To refresh the scrollbar status
			}, this);
		},

		/**
		 * @return {Array} columns
		 */
		getGridColumns: function() {
			var columns = [
				{
					header: tr.begin_date,
					width: 180,
					fixed: true,
					sortable: false,
					dataIndex: 'BeginDate',
					renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'),
					flex: 1
				},
				{
					header: tr.end_date,
					width: 180,
					fixed: true,
					sortable: false,
					dataIndex: 'EndDate',
					renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'),
					flex: 1
				},
				{
					header: tr.user,
					width: 20,
					sortable: false,
					dataIndex: 'User',
					flex: 1
				}
			];

			if (this.isFullVersion()) {
				columns = columns.concat([
					{
						header: tr.attributes,
						width: 60,
						fixed: true,
						sortable: false,
						renderer: tickRenderer,
						dataIndex: '_AttrHist',
						align: 'center',
						tdCls: 'grid-button',
						flex: 1
					},
					{
						header: tr.relation,
						width: 60,
						fixed: true,
						sortable: false,
						renderer: tickRenderer,
						dataIndex: '_RelHist',
						align: 'center',
						tdCls: 'grid-button',
						flex: 1
					},
					{
						header: tr.domain,
						width: 20,
						sortable: false,
						dataIndex: 'DomainDesc',
						flex: 1
					},
					{
						header: tr.description,
						width: 40,
						sortable: false,
						dataIndex: 'CardDescription',
						flex: 1
					}
				]);
			};

			return columns;
		},

		/**
		 * @return {Boolean}
		 */
		isFullVersion: function() {
			return !_CMUIConfiguration.isSimpleHistoryModeForCard();
		},

		getStoreFields: function() {
			return [
				{
					name: 'BeginDate',
					type: 'date',
					dateFormat: 'd/m/Y H:i:s'
				},
				{
					name: 'EndDate',
					type: 'date',
					dateFormat: 'd/m/Y H:i:s'
				},
				{ // For sorting only
					name: '_EndDate',
					type: 'int'
				},
				'User',
				'_AttrHist',
				'_RelHist',
				'DomainDesc',
				'Class',
				'CardCode',
				'CardDescription'
			];
		},

		reset: function() {
			this.getStore().removeAll();
		},

		tabIsActive: tabIsActive,

		/*
		 * DEPRECATED FUNCTIONS
		 */
			reloadCard: function() { _deprecated();
				this.enable();
				this.loaded = false;
				this.loadCardHistory();
			},

			loadCardHistory: function() { _deprecated();
				if (this.loaded
						|| !this.currentClassId
						|| !this.currentCardId) {
					return;
				}

				var params = {};
				params[_CMProxy.parameter.CARD_ID] = this.currentCardId;
				params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.currentClassId);

				this.getStore().load({
					params: params
				});

				this.loaded = true;
			},

			onAddCardButtonClick: function() { _deprecated();
				this.disable();
			},

			onClassSelected: function(classId) { _deprecated();
				if (this.currentClassId != classId) {
					this.currentClassId = classId;
					this.disable();
				}
			},

			onCardSelected: function(card) { _deprecated();
				var et = _CMCache.getEntryTypeById(card.get("IdClass"));
				if (et && et.get("tableType") == CMDBuild.Constants.cachedTableType.simpletable) {
					this.disable();
				} else {
					this.currentCardId = card.raw.Id;
					this.currentClassId = card.raw.IdClass;

					this.currentCardPrivileges = {
						create: card.raw.priv_create,
						write: card.raw.priv_write
					};

					// FIXME The workflow does not call onAddCardButtonClick()
					var existingCard = (this.currentCardId > 0);
					this.setDisabled(!existingCard);

					if (tabIsActive(this)) {
						this.reloadCard();
					} else {
						this.on("activate", this.reloadCard, this);
					}
				}
			}
	});

	/**
	 * @param {Object} record
	 *
	 * @return {String} body - HTML format string
	 */
	function genHistoryBody(record) {
		var body = '';
		var classAttributes = _CMCache.mapOfAttributes[_CMCardModuleState.card.get('IdClass')];
		var attributesDescriptionArray = [];

		// Build attributesDescriptionArray to test if display attribute
		for (var i in classAttributes)
			attributesDescriptionArray.push(classAttributes[i][CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION]);

		if (record.raw['_RelHist']) {
			body += historyAttribute(tr.domain, record.raw['DomainDesc'])
				+ historyAttribute(tr.destclass, record.raw['Class'])
				+ historyAttribute(tr.code, record.raw['CardCode'])
				+ historyAttribute(tr.description, record.raw['CardDescription']);
		}

		for (var i = 0; i < record.raw['Attr'].length; i++) {
			var attribute = record.raw['Attr'][i];

			if (Ext.Array.contains(attributesDescriptionArray, attribute.d)) {
				var label = attribute.d;
				var changed = attribute.c;
				var value = Ext.isEmpty(attribute.v) ? '' : attribute.v;

				body += historyAttribute(label, value, changed);
			}
		}

		return body;
	}

	/**
	 * @param {String} label
	 * @param {Mixed} value
	 * @param {Boolean} changed
	 *
	 * @return {String} HTML format string
	 */
	function historyAttribute(label, value, changed) {
		return '<p' + (changed ? ' class="changed"' : '') + '>'
				+ '<b>' + label + '</b>: ' + ((value || {}).dsc || value)
			+ '</p>';
	};

	function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}

	/**
	 * @param {Boolean} value
	 */
	function tickRenderer(value) {
		if (value) {
			return '<img style="cursor:pointer" src="images/icons/tick.png"/>&nbsp;';
		} else {
			return '&nbsp;';
		}
	}

})();