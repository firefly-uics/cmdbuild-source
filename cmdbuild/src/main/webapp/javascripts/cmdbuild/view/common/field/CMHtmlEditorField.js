(function() {

	Ext.define('CMDBuild.view.common.field.CMHtmlEditorField', {
		extend: 'Ext.ux.form.field.TinyMCE',

		/**
		 * @cfg {Boolean}
		 */
		dirty: false,

		/**
		 * @cfg {Mixed} object or string
		 */
		tinyMCEConfig: undefined,

		/**
		 * Custom CMDBuild buttons configurations to use
		 *
		 * @cfg {Object} CMDBuilds custom configurations
		 *
		 * @private
		 */
		customConfigurations: {
			common: {
				skin: 'extjs',
				skin_variant: 'blue',
				schema: 'html5',
				language: 'en',

				// Original value is 23, hard coded. With 23 the editor calculates the height wrong.
				// With these settings, you can do the fine tuning of the height by the initialization.
				theme_advanced_row_height: 27,
				delta_height: 1,
				width: '100%',
				theme_advanced_resizing: false,
				theme_advanced_resize_horizontal: false
			},

			full: {
				theme: 'advanced',
				plugins: 'autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist',

				// Theme options
				theme_advanced_buttons1: 'save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect',
				theme_advanced_buttons2: 'cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor',
				theme_advanced_buttons3: 'tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen',
				theme_advanced_buttons4: 'insertlayer,moveforward,movebackward,absolute,|,styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,pagebreak,restoredraft',
				theme_advanced_toolbar_location: 'top',
				theme_advanced_toolbar_align: 'left',
				theme_advanced_statusbar_location: 'none'

			},
			standard: {
				theme: 'advanced',
				plugins: 'autolink,paste,fullscreen',

				// Theme options
				theme_advanced_buttons1: 'bold,italic,underline,|,fontsizeselect,|,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,link,unlink,|,bullist,numlist,|,pastetext,pasteword,|,cleanup,removeformat,|,fullscreen,|,code',
				theme_advanced_toolbar_location: 'top',
				theme_advanced_toolbar_align: 'left',
				theme_advanced_statusbar_location: 'none'
			}
		},

		initComponent: function() {
			// Setup TinyMCE configuration from string identifier
			if (
				Ext.isEmpty(this.tinyMCEConfig)
				|| typeof this.tinyMCEConfig == 'string'
			) {
				var validIdentifiers = ['full', 'standard'];

				if(!Ext.Array.contains(validIdentifiers, this.tinyMCEConfig))
					this.tinyMCEConfig = 'standard';

				this.tinyMCEConfig = Ext.Object.merge(this.customConfigurations['common'], this.customConfigurations[this.tinyMCEConfig]);
			}

			// Language setup
			this.tinyMCEConfig.language = CMDBuild.Config[CMDBuild.core.proxy.CMProxyConstants.LANGUAGE];

			// Editor color setup for Administration
			if (Ext.isEmpty(CMDBuild.app.Management)) {
				var extVersion = CMDBuild.core.Utils.getExtJsVersion();

				this.tinyMCEConfig.skin_variant = 'silver';
				this.tinyMCEConfig.popup_css = 'javascripts/ext-' + extVersion + '-ux/form/field/tinymce/themes/advanced/skins/extjs/dialog_silver.css';
			}

			this.callParent(arguments);

			this.on('change', function() {
				this.setDirty(); // Set as dirty
			}, this);
		},

		initValue: function() {
			this.dirty = false;
		},

		/**
		 * Dirty functionality implementation
		 */
		isDirty: function() {
			if (!Ext.isEmpty(this.getEditor()))
				return this.getEditor().isDirty() || this.dirty;

			return false;
		},

		setDirty: function() {
			this.dirty = true;
		}
	});

})();