package unit.dao;

import static org.cmdbuild.dao.attribute.AbstractDateAttribute.REST_DATETIME_FORMAT;
import static org.cmdbuild.dao.attribute.AbstractDateAttribute.SOAP_DATETIME_FORMAT;
import static org.cmdbuild.dao.attribute.DateAttribute.JSON_DATE_FORMAT;
import static org.cmdbuild.dao.attribute.DateAttribute.POSTGRES_DATE_FORMAT;
import static org.cmdbuild.dao.attribute.DateTimeAttribute.JSON_DATETIME_FORMAT;
import static org.cmdbuild.dao.attribute.DateTimeAttribute.POSTGRES_DATETIME_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.attribute.BooleanAttribute;
import org.cmdbuild.dao.attribute.DateAttribute;
import org.cmdbuild.dao.attribute.DateTimeAttribute;
import org.cmdbuild.dao.attribute.DecimalAttribute;
import org.cmdbuild.dao.attribute.DoubleAttribute;
import org.cmdbuild.dao.attribute.StringAttribute;
import org.cmdbuild.elements.TableFactoryImpl;
import org.cmdbuild.elements.AttributeImpl.AttributeDataDefinitionMeta;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.junit.BeforeClass;
import org.junit.Test;


public class AttributeValueTest {

	private static IAttribute booleanAttribute;
	private static IAttribute dateAttribute;
	private static IAttribute dateTimeAttribute;
	//private static IAttribute charAttribute;
	private static IAttribute decimalAttribute;
	private static IAttribute doubleAttribute;
	//private static IAttribute foreignKeyAttribute;
	//private static IAttribute geometryAttribute;
	//private static IAttribute ipAddressAttribute;
	//private static IAttribute lookupAttribute;
	//private static IAttribute referenceAttribute;
	private static IAttribute stringAttribute;
	//private static IAttribute textAttribute;
	//private static IAttribute timeAttribute;

	private static Map<String, String> NO_META = new HashMap<String, String>();

	@BeforeClass
	public static void createAttributes() {
		ITableFactory tf = new TableFactoryImpl(UserContext.systemContext());
		BaseSchema cmClass = tf.create();
		booleanAttribute = new BooleanAttribute(cmClass, "Boolean", NO_META);
		//charAttribute = new CharAttribute(cmClass, "Char");
		dateAttribute = new DateAttribute(cmClass, "Date", NO_META);
		dateTimeAttribute = new DateTimeAttribute(cmClass, "DateTime", NO_META);
		decimalAttribute = new DecimalAttribute(cmClass, "Decimal", doubleMeta(10, 5));
		doubleAttribute = new DoubleAttribute(cmClass, "Double", NO_META);
		//foreignKeyAttribute = new ForeignKeyAttribute(cmClass, "ForeignKey");
		//geometryAttribute = new GeometryAttribute(cmClass, "Geometry");
		//ipAddressAttribute = new IPAddressAttribute(cmClass, "IPAddress");
		//lookupAttribute = new LookupAttribute(cmClass, "Lookup");
		//referenceAttribute = new ReferenceAttribute(cmClass, "Reference");
		stringAttribute = new StringAttribute(cmClass, "String", stringMeta(200));
		//textAttribute = new TextAttribute(cmClass, "Text");
		//timeAttribute = new TimeAttribute(cmClass, "Time");
	}

	@SuppressWarnings("serial")
	private static Map<String, String> doubleMeta(final int precision, final int scale) {
		return new HashMap<String, String>() {{
			put(AttributeDataDefinitionMeta.PRECISION.name(), Integer.toString(precision));
			put(AttributeDataDefinitionMeta.SCALE.name(), Integer.toString(scale));
		}};
	}

	@SuppressWarnings("serial")
	private static Map<String, String> stringMeta(final int length) {
		return new HashMap<String, String>() {{
			put(AttributeDataDefinitionMeta.LENGTH.name(), Integer.toString(length));
		}};
	}

	private void assertTypeErrorOnRead(Object value, IAttribute attribute) {
		try {
			attribute.readValue(value);
			fail("Wrong value did not throw any exception");
		} catch (ORMException e) {
			assertEquals(ORMExceptionType.ORM_TYPE_ERROR, e.getExceptionType());
		} catch (Exception e) {
			fail("Wrong value did not throw a type error exception");
		}
	}

	/*
	 * Binary
	 */

	/*
	 * Boolean
	 */
	@Test
	public void readBooleanValueFromObject() {
		assertEquals(Boolean.TRUE, booleanAttribute.readValue(Boolean.TRUE));
		assertEquals(Boolean.FALSE, booleanAttribute.readValue(Boolean.FALSE));
		assertEquals(null, booleanAttribute.readValue(null));
		assertTypeErrorOnRead(Integer.valueOf(0), booleanAttribute);
	}

	@Test
	public void readBooleanValueFromString() {
		assertEquals(Boolean.TRUE, booleanAttribute.readValue("true"));
		assertEquals(Boolean.FALSE, booleanAttribute.readValue("false"));
		assertEquals(Boolean.FALSE, booleanAttribute.readValue("something else"));
		assertEquals(null, booleanAttribute.readValue(""));
	}

	@Test
	public void serializeBooleanValue() {
		assertEquals("true", booleanAttribute.valueToString(Boolean.TRUE));
		assertEquals("false", booleanAttribute.valueToString(Boolean.FALSE));
		assertEquals("", booleanAttribute.valueToString(null));
	}

	@Test
	public void databaseFormatBooleanValue() {
		assertEquals("true", booleanAttribute.valueToDBFormat(Boolean.TRUE));
		assertEquals("false", booleanAttribute.valueToDBFormat(Boolean.FALSE));
		assertEquals("NULL", booleanAttribute.valueToDBFormat(null));
	}

	/*
	 * Date
	 */
	@Test
	public void readDateValueFromObject() {
		final Date today = today();
		assertEquals(today, dateAttribute.readValue(today));
		assertEquals(today, dateAttribute.readValue(todayNotMidnight()));
		assertTypeErrorOnRead(Integer.valueOf(0), dateAttribute);
	}

	@Test
	public void readDateValueFromString() {
		Date today = today();
		assertEquals(today, dateAttribute.readValue(dateToString(today, JSON_DATE_FORMAT)));
		assertEquals(today, dateAttribute.readValue(dateToString(today, SOAP_DATETIME_FORMAT)));
		assertEquals(today, dateAttribute.readValue(dateToString(today, REST_DATETIME_FORMAT)));
		assertEquals(null, dateAttribute.readValue(""));
		assertTypeErrorOnRead("not a date", dateAttribute);
	}

	@Test
	public void serializeDateValue() {
		Date today = today();
		assertEquals(dateToString(today, JSON_DATE_FORMAT), dateAttribute.valueToString(today));
		assertEquals("", dateAttribute.valueToString(null));
	}

	@Test
	public void databaseFormatDateValue() {
		Date today = today();
		assertEquals("'"+dateToString(today, POSTGRES_DATE_FORMAT)+"'", dateAttribute.valueToDBFormat(today));
		assertEquals("NULL", dateAttribute.valueToDBFormat(null));
	}

	private String dateToString(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

	private Date todayNotMidnight() {
		Calendar defaultCalendar = Calendar.getInstance();
		defaultCalendar.set(Calendar.SECOND, 42);
		return defaultCalendar.getTime();
	}

	private Date today() {
		Calendar defaultCalendar = Calendar.getInstance();
		defaultCalendar.set(Calendar.HOUR_OF_DAY, 0);
		defaultCalendar.set(Calendar.MINUTE, 0);
		defaultCalendar.set(Calendar.SECOND, 0);
		defaultCalendar.set(Calendar.MILLISECOND, 0);
		return defaultCalendar.getTime();
	}

	/*
	 * DateTime
	 */

	@Test
	public void readDateTimeValueFromObject() {
		Date now = nowNotMillis();
		assertEquals(now, dateTimeAttribute.readValue(now));
		assertEquals(null, dateTimeAttribute.readValue(null));
		assertTypeErrorOnRead(Integer.valueOf(0), dateTimeAttribute);
	}

	@Test
	public void readDateTimeValueFromString() {
		Date now = nowNotMillis();
		assertEquals(now, dateTimeAttribute.readValue(dateToString(now, JSON_DATETIME_FORMAT)));
		assertEquals(now, dateTimeAttribute.readValue(dateToString(now, SOAP_DATETIME_FORMAT)));
		assertEquals(now, dateTimeAttribute.readValue(dateToString(now, REST_DATETIME_FORMAT)));
		assertEquals(null, dateTimeAttribute.readValue(""));
		assertTypeErrorOnRead("not a date", dateTimeAttribute);
	}

	@Test
	public void serializeDateTimeValue() {
		Date now = nowNotMillis();
		assertEquals(dateToString(now, JSON_DATETIME_FORMAT), dateTimeAttribute.valueToString(now));
		assertEquals("", dateTimeAttribute.valueToString(null));
	}

	@Test
	public void databaseFormatDateTimeValue() {
		Date now = nowNotMillis();
		assertEquals("'"+dateToString(now, POSTGRES_DATETIME_FORMAT)+"'", dateTimeAttribute.valueToDBFormat(now));
		assertEquals("NULL", dateTimeAttribute.valueToDBFormat(null));
	}

	private Date nowNotMillis() {
		Calendar defaultCalendar = Calendar.getInstance();
		defaultCalendar.set(Calendar.MILLISECOND, 0);
		return defaultCalendar.getTime();
	}

	/*
	 * Decimal
	 */

	@Test
	public void readDecimalValueFromObject() {
		BigDecimal decimalValue = new BigDecimal("0.42");
		assertEquals(decimalValue, decimalAttribute.readValue(decimalValue));
		Double doubleValue = new Double("0.42");
		assertEquals(new BigDecimal(doubleValue), decimalAttribute.readValue(doubleValue));
		assertEquals(null, decimalAttribute.readValue(null));
		assertTypeErrorOnRead(Integer.valueOf(0), decimalAttribute);
	}

	@Test
	public void readDecimalValueFromString() {
		assertEquals(new BigDecimal("42"), decimalAttribute.readValue("42"));
		assertEquals(new BigDecimal("0.42"), decimalAttribute.readValue("0.42"));
		assertEquals(null, decimalAttribute.readValue(""));
		assertTypeErrorOnRead("not a decimal", decimalAttribute);
	}

	@Test
	public void serializeDecimalValue() {
		assertEquals("42", decimalAttribute.valueToString(new BigDecimal("42")));
		assertEquals("0.42", decimalAttribute.valueToString(new BigDecimal("0.42")));
		assertEquals("", decimalAttribute.valueToString(null));
	}

	@Test
	public void databaseDecimalValue() {
		assertEquals("42", decimalAttribute.valueToDBFormat(new BigDecimal("42")));
		assertEquals("0.42", decimalAttribute.valueToDBFormat(new BigDecimal("0.42")));
		assertEquals("NULL", decimalAttribute.valueToDBFormat(null));
	}

	/*
	 * Double
	 */

	@Test
	public void readDoubleValueFromObject() {
		assertEquals(0.42, doubleAttribute.readValue(0.42));
		assertEquals(null, doubleAttribute.readValue(null));
		assertTypeErrorOnRead(Integer.valueOf(0), doubleAttribute);
	}

	@Test
	public void readDoubleValueFromString() {
		assertEquals(42.0, doubleAttribute.readValue("42"));
		assertEquals(0.42, doubleAttribute.readValue("0.42"));
		assertEquals(null, doubleAttribute.readValue(""));
		assertTypeErrorOnRead("not a decimal", doubleAttribute);
	}

	@Test
	public void serializeDoubleValue() {
		assertEquals("42", doubleAttribute.valueToString(42));
		assertEquals("0.42", doubleAttribute.valueToString(0.42));
		assertEquals("", doubleAttribute.valueToString(null));
	}

	@Test
	public void databaseDoubleValue() {
		assertEquals("42", doubleAttribute.valueToDBFormat(42));
		assertEquals("0.42", doubleAttribute.valueToDBFormat(0.42));
		assertEquals("NULL", doubleAttribute.valueToDBFormat(null));
	}

	/*
	 * IntArray
	 */

	/*
	 * Integer
	 */

	/*
	 * Lookup
	 */

	/*
	 * Reference
	 */

	/*
	 * StringArray
	 */

	/*
	 * String
	 */

	// TODO handle CHAR, STRING, TEXT
	@Test
	public void readStringValue() {
		assertEquals("a string", stringAttribute.readValue("a string"));
		assertEquals(null, stringAttribute.readValue(""));
		assertEquals(null, stringAttribute.readValue(null));
		assertTypeErrorOnRead(Integer.valueOf(0), stringAttribute);
	}

}
