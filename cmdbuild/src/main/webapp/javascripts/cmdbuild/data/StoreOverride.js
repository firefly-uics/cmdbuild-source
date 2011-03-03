/**
 * see http://extjs.com/forum/showthread.php?t=38186
 */

Ext.apply(Ext.StoreMgr, {
  types : Ext.apply({},{
    jsonstore: Ext.data.JsonStore,
    simplestore:Ext.data.SimpleStore
  }),
  
  lookup: function(id) {
    if (typeof id == "object" && !id.load && id.xtype) {
      return new this.types[id.xtype](id);
    }
    return typeof id == "object" ? id : this.get(id);
  }
});
// Support for xtype based store
Ext.grid.GridPanel.prototype.initComponent = Ext.grid.GridPanel.prototype.initComponent.createInterceptor(function() {
    if (typeof this.store == "object" && !this.store.load && this.store.xtype) {
      this.destroyStore = true;
    }
    this.store = Ext.StoreMgr.lookup(this.store);
});
Ext.grid.GridPanel.prototype.onDestroy = Ext.grid.GridPanel.prototype.onDestroy.createInterceptor(function() {
if (this.destroyStore === true && this.store) {
    this.store.destroy();
  }
});

// Support for xtype based store
Ext.form.ComboBox.prototype.initComponent = Ext.form.ComboBox.prototype.initComponent.createInterceptor(function() {
  if (typeof this.store == "object" && !this.store.load && this.store.xtype) {
    this.destroyStore = true;
  }
  this.store = Ext.StoreMgr.lookup(this.store);
}
);
Ext.form.ComboBox.prototype.onDestroy = Ext.form.ComboBox.prototype.onDestroy.createInterceptor(function() {
if (this.destroyStore === true && this.store) {
    this.store.destroy();
  }
});