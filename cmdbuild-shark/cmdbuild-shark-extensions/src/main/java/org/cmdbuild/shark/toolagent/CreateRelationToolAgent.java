package org.cmdbuild.shark.toolagent;

/**
 * Creates a relation between 2 cards in the given domain.<br/>
 * Possible parameters combintations:
 * <ol>
 * <li>DomainName:string</li>
 * <li>ClassName1:string</li>
 * <li>ClassName2:string</li>
 * <li>ObjId1:int</li>
 * <li>ObjId2:int</li>
 * </ol>
 * 
 * Or:
 * <ol>
 * <li>DomainName:string</li>
 * <li>ObjRef1:Reference</li>
 * <li>ObjRef2:Reference</li>
 * </ol>
 * 
 * Or:
 * <ol>
 * <li>DomainName:string</li>
 * <li>ClassName1:string</li>
 * <li>ObjId1:string</li>
 * <li>ObjRef2:Reference</li>
 * </ol>
 * 
 * Or:
 * <ol>
 * <li>DomainName:string</li>
 * <li>ObjRef1:Reference</li>
 * <li>ClassName2:string</li>
 * <li>ObjId2:string</li>
 * </ol>
 * 
 * Output: done:boolean
 * 
 * @deprecated replaced by {@link ManageRelationToolAgent}
 */
@Deprecated
public final class CreateRelationToolAgent extends ManageRelationToolAgent {

}
