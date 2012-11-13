package unit.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.controller.FailureResponseMatcher.failureResponse;
import static utils.controller.SuccessResponseMatcher.successResponse;

import java.util.HashSet;

import org.cmdbuild.auth.AuthenticatedUser;
import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.services.json.controller.AuthenticationController;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AuthenticationControllerTest {

	private static final String USERNAME = "Davide";
	private static final String PASSWORD = "4 $trong 0ne";
	private static final String WRONG_PASSWORD = "d4v1d3";
	private static final String GROUP_NAME = "Engineers";
	private static final String NO_GROUP = null;

	private final AuthenticatedUser authUser;
	private final AuthenticationService authService;
	private final AuthenticationController controller;

	public AuthenticationControllerTest() {
		authUser = mock(AuthenticatedUser.class);
		authService = mock(AuthenticationService.class);
		controller = new AuthenticationController(authService);
	}

	@Test
	public void credentialsArePassedToTheAuthenticationService() {
		ArgumentCaptor<Login> loginCaptor = ArgumentCaptor.forClass(Login.class);
		when(authService.authenticate(any(Login.class), anyString())).thenReturn(authUser);

		controller.login(USERNAME, PASSWORD, NO_GROUP);

		verify(authService, only()).authenticate(loginCaptor.capture(), eq(PASSWORD));
		assertThat(loginCaptor.getValue().getValue(), is(USERNAME));
	}

	@Test
	public void noAuthenticationIsDoneIfNoUsernameOrPassword() {
		when(authService.getAuthenticatedUser()).thenReturn(authUser);

		controller.login(null, null, NO_GROUP);
		controller.login(null, PASSWORD, NO_GROUP);
		controller.login(USERNAME, null, NO_GROUP);

		verify(authService, never()).authenticate(any(Login.class), anyString());
		verify(authService, times(3)).getAuthenticatedUser();
	}

	@Test
	public void failsOnAuthenticationFailure() {
		when(authService.authenticate(any(Login.class), anyString())).thenReturn(authUser);
		when(authUser.isAnonymous()).thenReturn(true);

		JsonResponse response = controller.login(USERNAME, WRONG_PASSWORD, NO_GROUP);

		assertThat(response, is(failureResponse(AuthExceptionType.AUTH_LOGIN_WRONG.toString())));
	}

	@Test
	public void succeedsIfNoGroupNeedsToBeSelected() {
		when(authService.authenticate(any(Login.class), anyString())).thenReturn(authUser);
		when(authUser.isAnonymous()).thenReturn(false);
		when(authUser.isValid()).thenReturn(true);

		JsonResponse response = controller.login(USERNAME, PASSWORD, NO_GROUP);

		assertThat(response, successResponse());
	}

	@Test
	public void noGroupIsChosenIfGroupParameterNotPassed() {
		when(authService.getAuthenticatedUser()).thenReturn(authUser);

		controller.login(null, null, null);

		verify(authUser, never()).selectGroup(anyString());
		verify(authUser, never()).filterPrivileges(anyString());
	}

	@Test
	public void aSingleGroupIsChosenIfGroupParameterPassed() {
		when(authService.getAuthenticatedUser()).thenReturn(authUser);

		controller.login(null, null, GROUP_NAME);

		verify(authUser, times(1)).selectGroup(GROUP_NAME);
		verify(authUser, times(1)).filterPrivileges(GROUP_NAME);
	}

	@Test
	public void failsReturningGroupListIfMultipleGroupsAndNoDefault() {
		when(authService.getAuthenticatedUser()).thenReturn(authUser);
		when(authUser.isAnonymous()).thenReturn(false);
		when(authUser.isValid()).thenReturn(false);
		when(authUser.getGroups()).thenReturn(new HashSet<CMGroup>());

		JsonResponse response = controller.login(null, null, null);

		assertThat(response, is(failureResponse(AuthExceptionType.AUTH_MULTIPLE_GROUPS.toString())));
	}
}
