package unit.api.fluent.ws;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.api.fluent.NewRelation;
import org.cmdbuild.services.soap.Relation;
import org.junit.Before;
import org.junit.Test;

public class NewRelationTest extends AbstractWsFluentApiTest {

	private NewRelation newRelation;

	@Before
	public void createNewRelation() throws Exception {
		newRelation = api().newRelation() //
				.withDomainName(DOMAIN_NAME) //
				.withCard1(CLASSNAME, CARD_ID) //
				.withCard2(ANOTHER_CLASSNAME, ANOTHER_CARD_ID);
	}

	@Test
	public void parametersPassedToProxyWhenCreatingNewRelation() {
		newRelation.create();

		verify(proxy()).createRelation(relationCapturer());
		verifyNoMoreInteractions(proxy());

		final Relation wsRelation = capturedRelation();
		assertThat(wsRelation.getDomainName(), equalTo(newRelation.getDomainName()));
		assertThat(wsRelation.getClass1Name(), equalTo(newRelation.getClassName1()));
		assertThat(wsRelation.getCard1Id(), equalTo(newRelation.getClassId1()));
		assertThat(wsRelation.getClass2Name(), equalTo(newRelation.getClassName2()));
		assertThat(wsRelation.getCard2Id(), equalTo(newRelation.getClassId2()));
	}

}
