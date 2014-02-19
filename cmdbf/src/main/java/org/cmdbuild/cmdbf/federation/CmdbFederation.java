/* TODO
 * 1) identity reconciliation
 * 2) FOREIGNKEY as relationships
 * 3) history as records
 */

package org.cmdbuild.cmdbf.federation;

import java.util.Collection;

import org.cmdbuild.cmdbf.ManagementDataRepository;
import org.cmdbuild.config.CmdbfConfiguration;
import org.dmtf.schemas.cmdbf._1.tns.query.ExpensiveQueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.InvalidPropertyTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.query.QueryErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnknownTemplateIDFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedConstraintFault;
import org.dmtf.schemas.cmdbf._1.tns.query.UnsupportedSelectorFault;
import org.dmtf.schemas.cmdbf._1.tns.query.XPathErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.DeregistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidMDRFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.InvalidRecordFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.RegistrationErrorFault;
import org.dmtf.schemas.cmdbf._1.tns.registration.UnsupportedRecordTypeFault;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.DeregisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryResultType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.QueryType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterRequestType;
import org.dmtf.schemas.cmdbf._1.tns.servicedata.RegisterResponseType;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.ObjectFactory;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypeList;
import org.dmtf.schemas.cmdbf._1.tns.servicemetadata.RecordTypes;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class CmdbFederation implements ManagementDataRepository {
	private Collection<ManagementDataRepository> mdrCollection;
	private CmdbfConfiguration cmdbfConfiguration;
		
	public CmdbFederation(Collection<ManagementDataRepository> mdrCollection, CmdbfConfiguration cmdbfConfiguration) {
		this.mdrCollection = mdrCollection;		
		this.cmdbfConfiguration = cmdbfConfiguration;
	}
	
	@Override
	public String getMdrId() {
		return cmdbfConfiguration.getMdrId();
	}

	@Override
	public QueryResultType graphQuery(QueryType body) throws InvalidPropertyTypeFault, UnknownTemplateIDFault,
			ExpensiveQueryErrorFault, QueryErrorFault, XPathErrorFault,
			UnsupportedSelectorFault, UnsupportedConstraintFault {
		return new FederationQueryResult(body, mdrCollection);
	}
	
	@Override
	public RegisterResponseType register(final RegisterRequestType body) throws UnsupportedRecordTypeFault, InvalidRecordFault, InvalidMDRFault, RegistrationErrorFault {
		ManagementDataRepository mdr = Iterables.find(mdrCollection, new Predicate<ManagementDataRepository>(){
			public boolean apply(ManagementDataRepository input){
				return input.getMdrId().equals(body.getMdrId());
			}
		});
		if(mdr != null)
			return mdr.register(body);
		else
			throw new InvalidMDRFault(body.getMdrId());
	}

	@Override
	public DeregisterResponseType deregister(final DeregisterRequestType body) throws DeregistrationErrorFault, InvalidMDRFault {
		ManagementDataRepository mdr = Iterables.find(mdrCollection, new Predicate<ManagementDataRepository>(){
			public boolean apply(ManagementDataRepository input){
				return input.getMdrId().equals(body.getMdrId());
			}
		});
		if(mdr != null)
			return mdr.deregister(body);
		else
			throw new InvalidMDRFault(body.getMdrId());

	}
		
	@Override
	public RecordTypeList getRecordTypesList() {
		ObjectFactory factory = new ObjectFactory();
		RecordTypeList recordTypeList = factory.createRecordTypeList();
		for(ManagementDataRepository mdr : mdrCollection) {
			for(RecordTypes recordTypes : mdr.getRecordTypesList().getRecordTypes())
				recordTypeList.getRecordTypes().add(recordTypes);
		}
		return recordTypeList;
	}
}