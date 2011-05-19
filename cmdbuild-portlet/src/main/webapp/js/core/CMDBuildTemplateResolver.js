//These funcions comes from CMDBuild Template Resolver

function splitTemplate(template) {
    if (template) {
        var templateParts = [];
        var halfSplit = template.split("{");
        templateParts.push({
            text: halfSplit[0]
        });
        for (var i=1, len=halfSplit.length; i<len; ++i) {
            var subSplit = halfSplit[i].split("}");
            if (subSplit.length != 2) {
                return undefined;
            }
            templateParts.push({
                qvar: getQName(subSplit[0]),
                text: subSplit[1]
            });
        }
        return templateParts;
    } else {
        return {
            text: template
        };
    }
}

function getQName(variable) {
    if (variable instanceof Object) { // already a qname
        return variable;
    }
    var nsSplitIndex = variable.indexOf(":");
    if (nsSplitIndex == -1) {
        nsSplitIndex = variable.indexOf("#"); // try cql var namespace character
    }
    if (nsSplitIndex == -1) {
        return {
            raw: variable,
            namespace: "server", // default namespace
            localname: variable
        };
    } else {
        return {
            raw: variable,
            namespace: variable.slice(0,nsSplitIndex),
            localname: variable.slice(nsSplitIndex+1)
        };
    }
}

function buildCQLQueryParameters(cqlQuery) {
    if (!cqlQuery) {
        return undefined;
    }
    var params = {
        CQL: ""
    };
    var templateParts = splitTemplate(cqlQuery);
    for (var i=0, len=templateParts.length; i<len; ++i) {
        var item = templateParts[i];
        if (item.qvar) {
            var escapedVarName = "p" + i;
            var value = getVariable(item.qvar);
            if (value !== 0 && !value) { // NOTE: CQL is undefined if any of its variables is undefined OR EMPTY
                return undefined;
            }
            params.CQL += "{"+escapedVarName+"}";
            params[escapedVarName] = value;
        }
        if (item.text) {
            params.CQL += item.text;
        }
    }
    return params;
}

/*
 * Changed not to user Function.createDelegate
 * parameter ctx is always undfined
 */
function getVariable(variable, ctx) {
    var varQName = getQName(variable);
    var nsFunctionArray = {
        client: getActivityFormVariable,
        server: getActivityServerVariable,
        user: getCurrentUserInfo
    /*
        xa: this.getExtendedAttributeVariable,
        js: this.getJSVariable,
        cql: getCQLVariable
        */
    };
    if (varQName.namespace in {
        cql:"",
        js:""
    } && !ctx) {
        return "";
    }
    var nsFunction = nsFunctionArray[varQName.namespace];
    if (nsFunction) {
        return nsFunction.call(this, varQName, ctx);
    } else {
        return "";
    }
}

function getCurrentUserInfo(varName) {
    var infoMap = {
        id: CMDBuild.Runtime['CMDBuildUserId'],
        name: CMDBuild.Runtime['CMDBuildUserName'],    
        group: CMDBuild.Runtime['CMDBuildUserGroup']
    };
    return infoMap[varName];
}

function getServerVars() {
    return CMDBuildServerVars;
}

function getActivityServerVariable(varName) {
    var splitLocalnameVariable = splitLocalName(varName.localname);
    var suffix = {
        "": "",
        "Id": "",
        "Description": "_value"
    }
    [splitLocalnameVariable.detail];
    var value = this.getServerVars()[splitLocalnameVariable.name + suffix];
    return value;
}

/* Changed */
function findFormField(varName) {
    return jQuery("."+varName);
}

function getActivityFormVariable(varName) {
    var value;
    var splittedLocalName = splitLocalName(varName.localname);
    var field = findFormField(splittedLocalName.name);
    if (field) {
        if (jQuery(field).is('select')) {
            // reference and lookup
            var selector = jQuery(field).selector + " :selected";
            value = {
                "Id": function() {
                    return jQuery(selector).attr('value');
                },
                "Description": function() {
                    return jQuery(selector).text();
                },
                "": function() {
                    return jQuery(selector).attr('value');
                }
            }
            [splittedLocalName.detail]();
        }
    }
    return value;
}

function splitLocalName(localname) {
    var splitIndex;
    if ((splitIndex = localname.indexOf(".")) > 0) {
        return {
            name: localname.slice(0,splitIndex),
            detail: localname.slice(splitIndex+1)
        };
    } else if ((splitIndex = localname.search("_value")) > 0) {
        return {
            name: localname.slice(0,splitIndex),
            detail: "Description"
        };
    } else {
        return {
            name: localname,
            detail: ""
        };
    }
}

function buildCQLQueryParameters(cqlQuery) {
    if (!cqlQuery) {
        return undefined;
    }
    var params = {
        CQL: ""
    };
    var templateParts = splitTemplate(cqlQuery);
    for (var i=0, len=templateParts.length; i<len; ++i) {
        var item = templateParts[i];
        if (item.qvar) {
            var escapedVarName = "p" + i;
            var value = getVariable(item.qvar);
            if (value !== 0 && !value) { // NOTE: CQL is undefined if any of its variables is undefined OR EMPTY
                return undefined;
            }
            params.CQL += "{"+escapedVarName+"}";
            params[escapedVarName] = value;
        }
        if (item.text) {
            params.CQL += item.text;
        }
    }
    return params;
}