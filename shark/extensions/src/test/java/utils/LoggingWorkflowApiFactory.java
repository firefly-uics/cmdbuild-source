package utils;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.collect.Factory.entry;
import static org.cmdbuild.common.collect.Factory.linkedHashMapOf;
import static org.cmdbuild.common.collect.Factory.treeMapOf;
import static utils.TestLoggerConstants.LOGGER_CATEGORY;
import static utils.TestLoggerConstants.UNUSED_SHANDLE;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.DownloadedReport;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.Function;
import org.cmdbuild.api.fluent.ProcessInstanceDescriptor;
import org.cmdbuild.api.fluent.Relation;
import org.cmdbuild.api.fluent.RelationsQuery;
import org.cmdbuild.api.fluent.Report;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.NewMail;
import org.cmdbuild.workflow.api.SchemaApi;
import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class LoggingWorkflowApiFactory implements SharkWorkflowApiFactory {

	private static final String NAME_VALUE_SEPARATOR = ": ";
	private static final String ATTRIBUTES_SEPARATOR = ", ";

	public static final int CREATED_CARD_ID = 101;
	public static final String CREATED_PROCESS_INSTANCE_ID = "NEWPI";

	public static final String FOUND_REFERENCE_CLASSNAME = "referenceClassName";

	public static final ReferenceType FOUND_REFERENCE = new ReferenceType(12345,
			computedIdForName(FOUND_REFERENCE_CLASSNAME), "d");

	public static final LookupType FOUND_LOOKUP = new LookupType(69, "ty", "de", "co");

	public static final Map<String, String> FAKE_FUNCTION_RESPONSE;

	public static final Card FOUND_CARD;
	private static final Card FOUND_CARD_FOR_REFERENCE;
	public static final String CLASS_NAME = "FoundClass";
	public static final int CARD_ID = 42;
	public static final String INTEGER_ATTRIBUTE = "anInteger";
	public static final int INTEGER_ATTRIBUTE_VALUE = 12345;
	public static final String STRING_ATTRIBUTE = "aString";
	public static final String STRING_ATTRIBUTE_VALUE = "this is a string";

	private static final File DOWNLOADED_REPORT_FILE = new File("/downloaded/report/is/here");

	public static final DownloadedReport DOWNLOADED_REPORT = new DownloadedReport(DOWNLOADED_REPORT_FILE);

	static {
		FAKE_FUNCTION_RESPONSE = new HashMap<String, String>();
		FAKE_FUNCTION_RESPONSE.put("IntegerOutput", "1");
		FAKE_FUNCTION_RESPONSE.put("StringOutput", "two");
		FAKE_FUNCTION_RESPONSE.put("ReferenceOutput", String.valueOf(FOUND_REFERENCE.getId()));
		FAKE_FUNCTION_RESPONSE.put("LookupOutput", String.valueOf(FOUND_LOOKUP.getId()));

		final FluentApiExecutor UNUSED_EXECUTOR = null;
		FOUND_CARD = new FluentApi(UNUSED_EXECUTOR) //
				.existingCard(CLASS_NAME, CARD_ID) //
				.with(INTEGER_ATTRIBUTE, Integer.toString(INTEGER_ATTRIBUTE_VALUE)) //
				.with(STRING_ATTRIBUTE, STRING_ATTRIBUTE_VALUE);
		FOUND_CARD_FOR_REFERENCE = new FluentApi(UNUSED_EXECUTOR) //
				.existingCard(FOUND_REFERENCE_CLASSNAME, FOUND_REFERENCE.getId()) //
				.with(DESCRIPTION_ATTRIBUTE, FOUND_REFERENCE.getDescription());
	}

	private CallbackUtilities cus;

	@Override
	public void setup(final CallbackUtilities cus) {
		this.cus = cus;
	}

	@Override
	public void setup(final CallbackUtilities cus, final WMSessionHandle shandle, final String procInstId) {
		this.cus = cus;
	}

	@Override
	public WorkflowApi createWorkflowApi() {
		return new WorkflowApi(new FluentApiExecutor() {

			@Override
			public CardDescriptor create(final Card card) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						createNewCardLogLine( //
								card.getClassName(), //
								card.getAttributes()));
				return new CardDescriptor(card.getClassName(), CREATED_CARD_ID);
			}

			@Override
			public void update(final Card card) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						updateExistingCardLogLine( //
								card.getClassName(), //
								card.getId(), //
								card.getAttributes()));
			}

			@Override
			public void delete(final Card card) {
				// TODO Auto-generated method stub
			}

			@Override
			public Card fetch(final Card card) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						fetchExistingCardLogLine( //
								card.getClassName(), //
								card.getId(), //
								card.getAttributes()));
				if (card.getId() == FOUND_REFERENCE.getId()) {
					return FOUND_CARD_FOR_REFERENCE;
				} else {
					return FOUND_CARD;
				}
			}

			@Override
			public List<Card> fetchCards(final Card card) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						fetchQueryClassLogLine(card.getClassName(), card.getAttributes()));
				final Card foundCard = createWorkflowApi() //
						.existingCard(card.getClassName(), FOUND_REFERENCE.getId()) //
						.withDescription(FOUND_REFERENCE.getDescription());
				return asList(foundCard);
			}

			@Override
			public void create(final Relation relation) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						createRelationLogLine( //
								relation.getDomainName(), //
								relation.getClassName1(), //
								relation.getCardId1(), //
								relation.getClassName2(), //
								relation.getCardId2()));
			}

			@Override
			public void delete(final Relation relation) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						deleteRelationLogLine( //
								relation.getDomainName(), //
								relation.getClassName1(), //
								relation.getCardId1(), //
								relation.getClassName2(), //
								relation.getCardId2()));
			}

			@Override
			public List<Relation> fetch(final RelationsQuery query) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						selectRelationsLogLine( //
								query.getDomainName(), //
								query.getClassName(), //
								query.getCardId()));
				final Relation relation = new Relation(query.getDomainName());
				relation.setCard1(query.getClassName(), query.getCardId());
				relation.setCard2(FOUND_REFERENCE_CLASSNAME, FOUND_REFERENCE.getId());
				return asList(relation);
			}

			@Override
			public Map<String, String> execute(final Function function) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						callFunctionLogLine(function.getFunctionName(), function.getInputs()));
				return FAKE_FUNCTION_RESPONSE;
			}

			@Override
			public DownloadedReport download(final Report report) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						downloadReportLogLine(report.getTitle(), report.getFormat(), report.getParameters()));
				return DOWNLOADED_REPORT;
			}

			@Override
			public ProcessInstanceDescriptor createProcessInstance(final Card processCard, final AdvanceProcess advance) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						createNewProcessInstanceLogLine( //
								processCard.getClassName(), //
								processCard.getAttributes()));
				return new ProcessInstanceDescriptor(processCard.getClassName(), CREATED_CARD_ID,
						CREATED_PROCESS_INSTANCE_ID);
			}

		}, new SchemaApi() {

			@Override
			public ClassInfo findClass(final String className) {
				// Log nothing
				return new ClassInfo(className, computedIdForName(className));
			}

			@Override
			public ClassInfo findClass(final int classId) {
				// Log nothing
				return new ClassInfo(computedNameForId(classId), classId);
			}

			@Override
			public LookupType selectLookupById(final int id) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						selectLookupByIdLogLine(id));
				return FOUND_LOOKUP;
			}

			@Override
			public LookupType selectLookupByCode(final String type, final String code) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						selectLookupByCodeLogLine(type, code));
				return FOUND_LOOKUP;
			}

			@Override
			public LookupType selectLookupByDescription(final String type, final String description) {
				cus.info(UNUSED_SHANDLE, //
						LOGGER_CATEGORY, //
						selectLookupByCodeLogLine(type, description));
				return FOUND_LOOKUP;
			}

		}, new MailApi() {

			@Override
			public NewMail newMail() {

				return new NewMail() {

					private final List<String> froms = new ArrayList<String>();
					private final List<String> tos = new ArrayList<String>();
					private final List<String> ccs = new ArrayList<String>();
					private final List<String> bccs = new ArrayList<String>();
					private String subject;
					private String content;
					private String contentType;
					private final List<URL> attachments = new ArrayList<URL>();

					@Override
					public NewMail withFrom(final String from) {
						this.froms.add(from);
						return this;
					}

					@Override
					public NewMail withTo(final String to) {
						this.tos.add(to);
						return null;
					}

					@Override
					public NewMail withCc(final String cc) {
						this.ccs.add(cc);
						return this;
					}

					@Override
					public NewMail withBcc(final String bcc) {
						this.bccs.add(bcc);
						return this;
					}

					@Override
					public NewMail withSubject(final String subject) {
						this.subject = subject;
						return this;
					}

					@Override
					public NewMail withContent(final String content) {
						this.content = content;
						return this;
					}

					@Override
					public NewMail withContentType(final String contentType) {
						this.contentType = contentType;
						return this;
					}

					@Override
					public NewMail withAttachment(final URL url) {
						this.attachments.add(url);
						return this;
					}

					@Override
					public void send() {
						cus.info(UNUSED_SHANDLE, //
								LOGGER_CATEGORY, //
								sendMail(froms, tos, ccs, bccs, subject, content, contentType, attachments));
					}

				};

			}

		});
	}

	@SuppressWarnings("unchecked")
	public static String createNewCardLogLine(final String className, final Map<String, Object> attributes) {
		return logLine("createNewCard", //
				linkedHashMapOf(entry("className", className)), //
				treeMapOf(attributes));
	}

	@SuppressWarnings("unchecked")
	public static String fetchExistingCardLogLine(final String className, final int cardId,
			final Map<String, Object> attributes) {
		return logLine("fetchExistingCard", //
				linkedHashMapOf(entry("className", className), entry("cardId", cardId)), //
				treeMapOf(attributes));
	}

	@SuppressWarnings("unchecked")
	public static String updateExistingCardLogLine(final String className, final int cardId,
			final Map<String, Object> attributes) {
		return logLine("updateExistingCard", //
				linkedHashMapOf(entry("className", className), entry("cardId", cardId)), //
				treeMapOf(attributes));
	}

	@SuppressWarnings("unchecked")
	public static String fetchQueryClassLogLine(final String className, final Map<String, Object> attributes) {
		return logLine("fetchQueryClass", //
				linkedHashMapOf(entry("className", className)), //
				treeMapOf(attributes));
	}

	@SuppressWarnings("unchecked")
	public static String selectLookupByIdLogLine(final int id) {
		return logLine("selectLookupByIdLogLine", //
				linkedHashMapOf(entry("id", id)));
	}

	@SuppressWarnings("unchecked")
	public static String selectLookupByCodeLogLine(final String type, final String code) {
		return logLine("selectLookupByCode", //
				linkedHashMapOf(entry("type", type), entry("code", code)));
	}

	@SuppressWarnings("unchecked")
	public static String selectLookupByDescriptionLogLine(final String type, final String description) {
		return logLine("selectLookupByDescription", //
				linkedHashMapOf(entry("type", type), entry("description", description)));
	}

	@SuppressWarnings("unchecked")
	public static String callFunctionLogLine(final String functionName, final Map<String, Object> params) {
		return logLine(
				"callFunctionLogLine", //
				linkedHashMapOf(entry("functionName", functionName),
						entry("params", "{" + serializeAttributes(treeMapOf(params)) + "}")));
	}

	@SuppressWarnings("unchecked")
	public static String downloadReportLogLine(final String title, final String format, final Map<String, Object> params) {
		return logLine("createReport", //
				linkedHashMapOf(entry("title", title), entry("format", format)), treeMapOf(params));
	}

	@SuppressWarnings("unchecked")
	public static String createRelationLogLine(final String domainName, final String className1, final long cardId1,
			final String className2, final long cardId2) {
		return logLine("createRelation", //
				linkedHashMapOf(entry("domainName", domainName), //
						entry("className1", className1), entry("cardId1", cardId1), //
						entry("className2", className2), entry("cardId2", cardId2)));
	}

	@SuppressWarnings("unchecked")
	public static String deleteRelationLogLine(final String domainName, final String className1, final long cardId1,
			final String className2, final long cardId2) {
		return logLine("deleteRelation", //
				linkedHashMapOf(entry("domainName", domainName), //
						entry("className1", className1), entry("cardId1", cardId1), //
						entry("className2", className2), entry("cardId2", cardId2)));
	}

	@SuppressWarnings("unchecked")
	public static String selectRelationsLogLine(final String domainName, final String className, final long cardId) {
		return logLine("selectRelation", //
				linkedHashMapOf(entry("domainName", domainName), //
						entry("className1", className), entry("cardId1", cardId)));
	}

	@SuppressWarnings("unchecked")
	public static String manageRelationLogLine(final String functionName, final String domainName,
			final String className1, final long cardId1, final String className2, final long cardId2) {
		return logLine(functionName, //
				linkedHashMapOf(entry("domainName", domainName), //
						entry("className1", className1), entry("cardId1", cardId1), //
						entry("className2", className2), entry("cardId2", cardId2)));
	}

	@SuppressWarnings("unchecked")
	public static String createNewProcessInstanceLogLine(final String className, final Map<String, Object> attributes) {
		return logLine("createNewProcessInstance", //
				linkedHashMapOf(entry("className", className)), //
				treeMapOf(attributes));
	}

	public static String sendMail(final List<String> froms, final List<String> tos, final List<String> ccs,
			final List<String> bccs, final String subject, final String content, final String contentType,
			final List<URL> attachments) {
		return logLine("sendMail", new StringBuilder() //
				.append(tos) //
				.append(ccs) //
				.append(bccs) //
				.append(subject) //
				.append(content) //
				.append(contentType) //
				.append(attachments) //
				.toString());
	}

	/*
	 * Utils
	 */

	private static String logLine(final String functionName, final Map<String, Object> fixed,
			final Map<String, Object>... optionals) {

		final LinkedHashMap<String, Object> all = new LinkedHashMap<String, Object>();
		all.putAll(fixed);

		for (final Map<String, Object> optional : optionals) {
			all.putAll(optional);
		}

		return logLine(functionName, all.toString());
	}

	private static String logLine(final String functionName, final String content) {
		return format("%s(%s)", functionName, content);
	}

	public static int computedIdForName(final String className) {
		return className.hashCode();
	}

	public static String computedNameForId(final int classId) {
		return "Class" + classId;
	}

	private static String serializeAttributes(final Map<String, ?> attributes) {
		final Map<String, Object> sortedAttributes = new TreeMap<String, Object>();
		sortedAttributes.putAll(attributes);

		final StringBuffer stringBuffer = new StringBuffer();
		boolean firstAttribute = true;
		for (final Entry<String, Object> entry : sortedAttributes.entrySet()) {
			if (!firstAttribute) {
				stringBuffer.append(ATTRIBUTES_SEPARATOR);
			}
			stringBuffer //
					.append(entry.getKey()) //
					.append(NAME_VALUE_SEPARATOR) //
					.append(entry.getValue());
			firstAttribute = false;
		}
		return stringBuffer.toString();
	}

}
