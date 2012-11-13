package org.cmdbuild.services.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.elements.DomainFactoryImpl;
import org.cmdbuild.elements.ProcessTypeFactoryImpl;
import org.cmdbuild.elements.RelationFactoryImpl;
import org.cmdbuild.elements.TableFactoryImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;

public class SystemContext extends UserContext {

	private class SystemPrivilegeManager implements PrivilegeManager {

		@Override public void assureAdminPrivilege() {}
		@Override public void assureCreatePrivilege(ITable table) {}
		@Override public void assureCreatePrivilege(IDomain domain) {}
		@Override public void assureReadPrivilege(ITable table) {}
		@Override public void assureReadPrivilege(IDomain domain) {}
		@Override public void assureWritePrivilege(ITable table) {}
		@Override public void assureWritePrivilege(IDomain domain) {}
		@Override public PrivilegeType getPrivilege(BaseSchema schema) { return PrivilegeType.WRITE; }
		@Override public boolean hasCreatePrivilege(ITable table) { return true; }
		@Override public boolean hasCreatePrivilege(IDomain domain) { return true; }
		@Override public boolean hasReadPrivilege(ITable table) { return true; }
		@Override public boolean hasReadPrivilege(IDomain domain) { return true; }
		@Override public boolean hasReadPrivilege(BaseSchema schema) { return true; }
		@Override public boolean hasWritePrivilege(ITable table) { return true; }
		@Override public boolean hasWritePrivilege(IDomain domain) { return true; }
		@Override public boolean isAdmin() { return true; }
	}

	private static SystemContext INSTANCE = new SystemContext();

	private SystemContext() {
	}

	public static UserContext getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean belongsTo(String groupName) {
		return false;
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}

	@Override
	public boolean allowsPasswordLogin() {
		return false;
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Group getDefaultGroup() {
		return GroupImpl.getSystemGroup();
	}

	@Override
	public Collection<Group> getGroups() {
		List<Group> groups = new ArrayList<Group>(1);
		groups.add(GroupImpl.getSystemGroup());
		return groups;
	}

	@Override
	public String getRequestedUsername() {
		return getUsername();
	}

	@Override
	public User getUser() {
		return UserImpl.getSystemUser();
	}

	@Override
	public UserType getUserType() {
		return UserType.APPLICATION;
	}

	@Override
	public String getUsername() {
		return getUser().getName();
	}

	@Override
	public Group getWFStartGroup() {
		return getDefaultGroup();
	}

	@Override
	public boolean hasDefaultGroup() {
		return true;
	}

	@Override
	public boolean isGuest() {
		return false;
	}

	@Override
	public PrivilegeManager privileges() {
		return new SystemPrivilegeManager();
	}

	@Override
	public ProcessTypeFactory processTypes() {
		return new ProcessTypeFactoryImpl(this);
	}

	@Override
	public RelationFactory relations() {
		return new RelationFactoryImpl(this);
	}

	@Override
	public ITableFactory tables() {
		return new TableFactoryImpl(this);
	}

	@Override
	public DomainFactory domains() {
		return new DomainFactoryImpl(this);
	}
}
