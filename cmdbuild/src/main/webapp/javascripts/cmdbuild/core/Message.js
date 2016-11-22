(function () {

	Ext.define('CMDBuild.core.Message', {

		/**
		 * @cfg {Array}
		 */
		detailBuffer: [],

		singleton: true,

		/**
		 * @param {Number} id
		 * @param {String} stacktrace
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		buildDetailLink: function (id, stacktrace) {
			CMDBuild.core.Message.detailBuffer[id] = stacktrace;

			return '<p class="cmdb-show-detail-link" id="errorDetails_" onClick="javascript:buildDetaiWindow(' + id + ')">' + CMDBuild.Translation.showDetails + '</p>';
		},

		/**
		 * @param {String} title
		 * @param {Mixed} body
		 * @param {Boolean} popup
		 * @param {String} iconCls
		 *
		 * @returns {Ext.ux.window.notification.Notification or Ext.MessageBox}
		 *
		 * @private
		 */
		buildWindow: function (title, text, popup, iconCls) {
			iconCls= Ext.isString(iconCls) ? iconCls : 'x-icon-information';
			popup = Ext.isBoolean(popup) ? popup : false;
			text = Ext.isString(text) ? text : '';
			title = Ext.isString(title) ? title : '';

			var win = undefined;

			if (popup) {
				win = Ext.MessageBox.show({
					buttons: Ext.MessageBox.OK,
					icon: iconCls,
					msg: text,
					title: title,
					width: 300
				});
			} else {
				win = Ext.create('Ext.ux.window.notification.Notification', {
					autoCloseDelay: 5000,
					html: text,
					iconCls: iconCls,
					position: 'br',
					slideInDuration: 500,
					title: title,
					width: 200
				}).show();
			}

			return win;
		},

		/**
		 * @param {String} title
		 * @param {Object or String} body
		 * @param {Boolean} popup
		 *
		 * @returns {Void}
		 */
		error: function (title, body, popup) {
			title = Ext.isString(title) ? title : CMDBuild.Translation.error;
			popup = Ext.isBoolean(popup) ? popup : false;

			var text = body;

			if (Ext.isObject(body) && !Ext.isEmpty(body.text)) {
				text = body.text;

				if (!Ext.isEmpty(body.detail))
					text += CMDBuild.core.Message.buildDetailLink(CMDBuild.core.Message.detailBuffer.length, body.detail);
			}

			CMDBuild.core.Message.buildWindow(title, text, popup, Ext.MessageBox.ERROR);
		},

		/**
		 * @param {String} title
		 * @param {Mixed} body
		 * @param {Boolean} popup
		 *
		 * @returns {Void}
		 */
		info: function (title, text, popup) {
			CMDBuild.core.Message.buildWindow(title, text, popup, Ext.MessageBox.INFO);
		},

		/**
		 * @returns {Void}
		 */
		success: function () {
			CMDBuild.core.Message.buildWindow('', CMDBuild.Translation.success, false, Ext.MessageBox.INFO);
		},

		/**
		 * @param {String} title
		 * @param {Mixed} body
		 * @param {Boolean} popup
		 *
		 * @returns {Void}
		 */
		warning: function (title, text, popup) {
			title = Ext.isString(title) ? title : CMDBuild.Translation.warning;
			popup = Ext.isBoolean(popup) ? popup : false;

			CMDBuild.core.Message.buildWindow(title, text, popup, Ext.MessageBox.WARNING);
		}
	});

})();

/**
 * @param {Number} detailBufferIndex
 *
 * @returns {Void}
 */
function buildDetaiWindow(detailBufferIndex) {
	var detailsWindow = Ext.create('CMDBuild.core.window.AbstractModal', {
		dimensionsMode: 'percentage',
		title: CMDBuild.Translation.details,

		dockedItems: [
			Ext.create('Ext.toolbar.Toolbar', {
				dock: 'bottom',
				itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
				ui: 'footer',

				layout: {
					type: 'hbox',
					align: 'middle',
					pack: 'center'
				},

				items: [
					Ext.create('CMDBuild.core.buttons.text.Close', {
						scope: this,

						handler: function (button, e) {
							detailsWindow.destroy();
						}
					})
				]
			})
		],

		items: [
			Ext.create('Ext.panel.Panel', {
				border: false,
				autoScroll: true,

				html: '<pre style="padding:5px; font-size: 1.2em">'	+ CMDBuild.core.Message.detailBuffer[detailBufferIndex] + '</pre>'
			})
		]
	}).show();
}
