(function() {

	Ext.ns("CMDBuild.administration.domain");
	Ext.define("CMDBuild.administration.domain.CMDomainForm", {
		extend: "CMDBuild.administration.form.CMFormTemplate",
		alias: "domainform",
		translation : CMDBuild.Translation.administration.modClass.domainProperties,
		model: null,
		MODEL_TYPE: CMDBuild.core.model.CMDomainModel,
		
		initComponent : function() {
			this.modifyButtonLabel = this.translation.modify_domain;
			this.deleteButtonLabel = this.translation.delete_domain;

			this.class_store = CMDBuild.Cache.getClassesAndProcessAsStore(); 

			this.masterdetail = new Ext.ux.form.XCheckbox({
				xtype: 'xcheckbox',
				fieldLabel: this.translation.master_detail,
				width: 220,
				name: this.MODEL_TYPE.STRUCTURE.isMasterDetail.name,
				disabled: true
			});

			this.active = new Ext.ux.form.XCheckbox({
				fieldLabel: this.translation.is_active,
				width: 220,
				name: this.MODEL_TYPE.STRUCTURE.active.name,
				checked: true,
				disabled: true
			});

			this.cardinality_combo = new Ext.form.ComboBox({
				xdomainformtype: 'combo',
				fieldLabel: this.translation.cardinality,
				width: 220,
				name: this.MODEL_TYPE.STRUCTURE.cardinality.name,
				valueField: 'name',
				displayField: 'value',
				triggerAction: 'all',
				store: new Ext.data.SimpleStore({
					fields: ['name', 'value'],
					data: [
						['1:1', '1:1'],
						['1:N', '1:N'],
						['N:1', 'N:1'],
						['N:N', 'N:N']
					]
				}),
				allowBlank: false,
				mode: 'local',
				disabled: true
			});
	
			this.cardinality_combo.on('select', enableMDCheckBox, this);
			
			this.domainName = new Ext.form.TextField({
				fieldLabel : this.translation.name,
				name : this.MODEL_TYPE.STRUCTURE.name.name,
				width : 220,
				allowBlank : false,
				disabled : true,
				vtype : 'alphanum',
				enableKeyEvents: true
			});
			
			this.domainDescription = new Ext.form.TextField({
				fieldLabel : this.translation.description,
				width : 220,
				name : this.MODEL_TYPE.STRUCTURE.description.name,
				disabled : true,
				allowBlank : false,
				vtype : 'cmdbcomment'
			});
			
			this.formFields = [{
				name: this.MODEL_TYPE.STRUCTURE.id.name,
				xtype: 'hidden',
				value: -1
			},
				this.domainName,
				this.domainDescription,
			{
				xtype: 'combo',
				fieldLabel: this.translation.class_target,
				name: this.MODEL_TYPE.STRUCTURE.idClass1.name,
				hiddenName: this.MODEL_TYPE.STRUCTURE.idClass1.name,
				width: 220,
				triggerAction: 'all',
				valueField: 'id',
				displayField: 'description',
				minChars: 0,
				allowBlank: false,
				store: this.class_store,
				disabled: true,
				mode: "local"
			}, {
				xtype: 'combo',
				fieldLabel: this.translation.class_destination,
				name: this.MODEL_TYPE.STRUCTURE.idClass2.name,
				hiddenName: this.MODEL_TYPE.STRUCTURE.idClass2.name,
				width: 220,
				triggerAction: 'all',
				valueField: 'id',
				displayField: 'description',
				minChars: 0,
				allowBlank: false,
				store: this.class_store,
				disabled: true,
				mode: "local"
			}, {
				xtype: 'textfield',
				fieldLabel: this.translation.description_direct,
				width: 220,
				allowBlank: false,
				name: this.MODEL_TYPE.STRUCTURE.directDescription.name,
				disabled: true,
				vtype: 'cmdbcomment'
			}, {
				xtype: 'textfield',
				fieldLabel: this.translation.description_inverse,
				width: 220,
				allowBlank: false,
				name: this.MODEL_TYPE.STRUCTURE.reverseDescription.name,
				disabled: true,
				vtype: 'cmdbcomment'
			},
				this.cardinality_combo,
				this.masterdetail,
				this.active
			];

			CMDBuild.administration.domain.CMDomainForm.superclass.initComponent.apply(this, arguments);

			this.domainName.on('change', function(domainNameField, newValue, oldValue) {
				this.autoComplete(this.domainDescription, newValue, oldValue);
			}, this);
		},
	
		onDomainSelected: function(cmDomain) {
			this.disableModify();
			this.fillWithModel(cmDomain);
		},
		
		setDefaultValues: function() {
			this.active.setValue(true);
		}
	});

	// a domain must set MD only if the cardinality is "1:N" or "N:1" 
	function enableMDCheckBox() {
		if ( this.cardinality_combo.getValue() 
			&& !(this.cardinality_combo.getValue() == '1:N' 
			|| this.cardinality_combo.getValue() == 'N:1')) {
				
			this.masterdetail.setValue(false);
			this.masterdetail.disable();
		} else {
			this.masterdetail.enable();
		}
	}
})();