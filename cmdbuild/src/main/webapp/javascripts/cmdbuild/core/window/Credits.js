(function () {

	Ext.define('CMDBuild.core.window.Credits', {
		extend: 'Ext.window.Window',

		modal: true,
		resizable: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				contentEl: Ext.create('Ext.Element', {
					html: '<div id="cm-credits-content" class="cm-credits-container">'
							+ '<div class="cm-credits-logo-container">'
								+ '<img src="images/logo.jpg">'
								+ '<p class="cm-credits-release-version">' + CMDBuild.locale.core.version + ' ' + CMDBuild.Translation.release + '</p>'
							+ '</div>'
							+ '<div class="cm-credits-links-container">'
								+ '<div class="cm-credits-links-left">'
									+ '<h1>' + CMDBuild.locale.core.needYouHelp + '</h1>'
									+ '<ul>'
										+ '<li>' + CMDBuild.locale.core.lookAtTheManuals + '</li>'
										+ '<li>' + CMDBuild.locale.core.goToTheForum + '</li>'
										+ '<li class="cm-credit-last-link">' + CMDBuild.locale.core.requestTecnicalSupport + '</li>'
									+ '</ul>'
								+ '</div>'
								+ '<div class="cm-credits-links-right">'
									+ '<h1>' + CMDBuild.locale.core.wouldYouFollowCMDBuild + '</h1>'
									+ '<ul>'
										+ '<li>' + CMDBuild.locale.core.subscribeToNewsLetter + '</li>'
										+ '<li>' + CMDBuild.locale.core.folowUsOnTweeter + '</li>'
										+ '<li class="cm-credit-last-link">' + CMDBuild.locale.core.participatesInTheLinkedInGroup + '</li>'
									+ '</ul>'
								+ '</div>'
								+ '<div class="cm-credits-producer">'
									+ '<h1>Credits</h1>'
									+ '<p>' + CMDBuild.locale.core.cmdbuildIsASofwareDevelopedByTecnoteca + '</p>'
									+ '<p>' + CMDBuild.locale.core.cmdbuildIsAtradeMarkRegisterd + '</p>'
								+ '</div>'
							+ '</div>'
						+ '</div>'
				})
			});

			this.callParent(arguments);
		}
	});

})();
