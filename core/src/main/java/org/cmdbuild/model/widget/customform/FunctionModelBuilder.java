package org.cmdbuild.model.widget.customform;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Function;

class FunctionModelBuilder extends AttributesBasedModelBuilder {

	private final CMDataView dataView;
	private final String functionName;

	public FunctionModelBuilder(final CMDataView dataView, final String functionName) {
		this.dataView = dataView;
		this.functionName = functionName;
	}

	@Override
	public Iterable<Attribute> attributes() {
		return from(dataView.findFunctionByName(functionName).getInputParameters()) //
				// TODO filter?
				.transform(new Function<CMFunctionParameter, Attribute>() {

					@Override
					public Attribute apply(final CMFunctionParameter input) {
						final Attribute output = new Attribute();
						input.getType().accept(new CMAttributeTypeVisitor() {

							@Override
							public void visit(final BooleanAttributeType attributeType) {
								output.setType(TYPE_BOOLEAN);
							}

							@Override
							public void visit(final CharAttributeType attributeType) {
								output.setType(TYPE_CHAR);
							}

							@Override
							public void visit(final DateAttributeType attributeType) {
								output.setType(TYPE_DATE);
							}

							@Override
							public void visit(final DateTimeAttributeType attributeType) {
								output.setType(TYPE_DATE_TIME);
							}

							@Override
							public void visit(final DoubleAttributeType attributeType) {
								output.setType(TYPE_DOUBLE);
							}

							@Override
							public void visit(final DecimalAttributeType attributeType) {
								output.setType(TYPE_DECIMAL);
							}

							@Override
							public void visit(final EntryTypeAttributeType attributeType) {
								output.setType(TYPE_ENTRY_TYPE);
							}

							@Override
							public void visit(final ForeignKeyAttributeType attributeType) {
								output.setType(TYPE_REFERENCE);
							}

							@Override
							public void visit(final IntegerAttributeType attributeType) {
								output.setType(TYPE_INTEGER);
							}

							@Override
							public void visit(final IpAddressAttributeType attributeType) {
								output.setType(TYPE_IP_ADDRESS);
							}

							@Override
							public void visit(final LookupAttributeType attributeType) {
								output.setType(TYPE_LOOKUP);
							}

							@Override
							public void visit(final ReferenceAttributeType attributeType) {
								output.setType(TYPE_REFERENCE);
							}

							@Override
							public void visit(final StringAttributeType attributeType) {
								output.setType(TYPE_STRING);
							}

							@Override
							public void visit(final StringArrayAttributeType attributeType) {
								output.setType(TYPE_STRING_ARRAY);
							}

							@Override
							public void visit(final TextAttributeType attributeType) {
								output.setType(TYPE_TEXT);
							}

							@Override
							public void visit(final TimeAttributeType attributeType) {
								output.setType(TYPE_TIME);
							}

						});
						output.setName(input.getName());
						output.setDescription(input.getName());
						output.setUnique(false);
						output.setMandatory(false);
						output.setWritable(true);
						return output;
					}

				});
	}

}