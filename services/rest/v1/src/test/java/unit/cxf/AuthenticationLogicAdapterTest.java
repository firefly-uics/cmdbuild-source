package unit.cxf;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.service.rest.v1.cxf.CxfSessions.AuthenticationLogicAdapter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AuthenticationLogicAdapterTest {

	private AuthenticationLogic authenticationLogic;
	private AuthenticationLogicAdapter authenticationLogicAdapter;

	@Before
	public void setUp() throws Exception {
		authenticationLogic = mock(AuthenticationLogic.class);
		authenticationLogicAdapter = new AuthenticationLogicAdapter(authenticationLogic);
	}

	@Test
	public void loginWithoutPreset() throws Exception {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString("username") //
				.withPassword("password") //
				.build();

		// when
		authenticationLogicAdapter.login(loginDTO);

		// then
		verify(authenticationLogic).login(eq(loginDTO), any(UserStore.class));
	}

	@Test
	public void loginWithPreset() throws Exception {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString("username") //
				.withPassword("password") //
				.build();
		final OperationUser operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(),
				new NullGroup());

		// when
		authenticationLogicAdapter.login(loginDTO, operationUser);

		// then
		final ArgumentCaptor<UserStore> userStoreCaptor = ArgumentCaptor.forClass(UserStore.class);
		verify(authenticationLogic).login(eq(loginDTO), userStoreCaptor.capture());
		final OperationUser capturedOperationUser = userStoreCaptor.getValue().getUser();
		assertThat(capturedOperationUser, equalTo(operationUser));
	}

}
