package unit.bim.mapper.xml;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.cmdbuild.bim.mapper.xml.XmlParser;
import org.junit.Before;
import org.junit.Test;

public class XmlParserTest {

	private static final String xmlString = "<bim-conf><entity name='IfcBuilding' label='Edificio'></entity></bim-conf>";
	private static XmlParser parser;

	@Before
	public void setUp() throws Exception {
		
		
		parser = new XmlParser(xmlString);

	}

	@Test
	public void countEntities() throws Exception {
		// given

		// when
		int numberOfTypesToRead = parser.getNumberOfNestedEntities(XmlParser.ROOT);

		// then
		assertTrue(numberOfTypesToRead == 1);
	}

	@Test
	public void getEntityName() throws Exception {
		// given
		System.out.println(xmlString);
		// when
		String firstIfcType = parser.getEntityName(XmlParser.ROOT + "/entity[1]");

		// then
		assertThat(firstIfcType, equalTo("IfcBuilding"));
	}

}
