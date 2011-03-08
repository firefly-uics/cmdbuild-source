SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET default_tablespace = '';
SET default_with_oids = false;



CREATE TABLE objectid (
    next numeric(19,0) NOT NULL
);



CREATE TABLE qrtz_blob_triggers (
    trigger_name character varying(80) NOT NULL,
    trigger_group character varying(80) NOT NULL,
    blob_data bytea
);



CREATE TABLE qrtz_calendars (
    calendar_name character varying(80) NOT NULL,
    calendar bytea NOT NULL
);



CREATE TABLE qrtz_cron_triggers (
    trigger_name character varying(80) NOT NULL,
    trigger_group character varying(80) NOT NULL,
    cron_expression character varying(80) NOT NULL,
    time_zone_id character varying(80)
);



CREATE TABLE qrtz_fired_triggers (
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(80) NOT NULL,
    trigger_group character varying(80) NOT NULL,
    is_volatile boolean NOT NULL,
    instance_name character varying(80) NOT NULL,
    fired_time bigint NOT NULL,
    state character varying(16) NOT NULL,
    job_name character varying(80),
    job_group character varying(80),
    is_stateful boolean,
    requests_recovery boolean
);



CREATE TABLE qrtz_job_details (
    job_name character varying(80) NOT NULL,
    job_group character varying(80) NOT NULL,
    description character varying(120),
    job_class_name character varying(128) NOT NULL,
    is_durable boolean NOT NULL,
    is_volatile boolean NOT NULL,
    is_stateful boolean NOT NULL,
    requests_recovery boolean NOT NULL,
    job_data bytea
);



CREATE TABLE qrtz_job_listeners (
    job_name character varying(80) NOT NULL,
    job_group character varying(80) NOT NULL,
    job_listener character varying(80) NOT NULL
);



CREATE TABLE qrtz_locks (
    lock_name character varying(40) NOT NULL
);



CREATE TABLE qrtz_paused_trigger_grps (
    trigger_group character varying(80) NOT NULL
);



CREATE TABLE qrtz_scheduler_state (
    instance_name character varying(80) NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL,
    recoverer character varying(80)
);



CREATE TABLE qrtz_simple_triggers (
    trigger_name character varying(80) NOT NULL,
    trigger_group character varying(80) NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);



CREATE TABLE qrtz_trigger_listeners (
    trigger_name character varying(80) NOT NULL,
    trigger_group character varying(80) NOT NULL,
    trigger_listener character varying(80) NOT NULL
);



CREATE TABLE qrtz_triggers (
    trigger_name character varying(80) NOT NULL,
    trigger_group character varying(80) NOT NULL,
    job_name character varying(80) NOT NULL,
    job_group character varying(80) NOT NULL,
    is_volatile boolean NOT NULL,
    description character varying(120),
    next_fire_time bigint,
    prev_fire_time bigint,
    trigger_state character varying(16) NOT NULL,
    trigger_type character varying(8) NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(80),
    misfire_instr smallint,
    job_data bytea
);



CREATE TABLE shkactivities (
    id character varying(100) NOT NULL,
    activitysetdefinitionid character varying(90),
    activitydefinitionid character varying(90) NOT NULL,
    process numeric(19,0) NOT NULL,
    theresource numeric(19,0),
    pdefname character varying(200) NOT NULL,
    processid character varying(200) NOT NULL,
    resourceid character varying(100),
    state numeric(19,0) NOT NULL,
    blockactivityid character varying(100),
    performer character varying(100),
    isperformerasynchronous boolean,
    priority integer,
    name character varying(254),
    activated bigint NOT NULL,
    activatedtzo bigint NOT NULL,
    accepted bigint,
    acceptedtzo bigint,
    laststatetime bigint NOT NULL,
    laststatetimetzo bigint NOT NULL,
    limittime bigint NOT NULL,
    limittimetzo bigint NOT NULL,
    description character varying(254),
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkactivitydata (
    activity numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    isresult boolean NOT NULL,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkactivitydatablobs (
    activitydatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkactivitydatawob (
    activity numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    isresult boolean NOT NULL,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkactivityhistorydetails (
    activityid character varying(100) NOT NULL,
    activityhistoryinfo numeric(19,0) NOT NULL,
    thetype integer NOT NULL,
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    reassignfrom character varying(100),
    reassignto character varying(100),
    variabledefinitionid character varying(100),
    variabletype integer,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkactivityhistoryinfo (
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    activityid character varying(100) NOT NULL,
    activityname character varying(254),
    activitydefinitionid character varying(90) NOT NULL,
    activitydefinitionname character varying(90),
    activitydefinitiontype integer,
    createdtime bigint NOT NULL,
    createdtimetzo bigint,
    startedtime bigint,
    startedtimetzo bigint,
    suspendedtime bigint,
    suspendedtimetzo bigint,
    resumedtime bigint,
    resumedtimetzo bigint,
    acceptedtime bigint,
    acceptedtimetzo bigint,
    rejectedtime bigint,
    rejectedtimetzo bigint,
    closedtime bigint,
    closedtimetzo bigint,
    createdbyusername character varying(100),
    startedbyusername character varying(100),
    suspendedbyusername character varying(100),
    resumedbyusername character varying(100),
    acceptedbyusername character varying(100),
    rejectedbyusername character varying(100),
    closedbyusername character varying(100),
    currentusername character varying(100),
    laststate character varying(100),
    laststatetime bigint,
    laststatetimetzo bigint,
    activityduration bigint,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkactivitystateeventaudits (
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkactivitystates (
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkandjointable (
    process numeric(19,0) NOT NULL,
    blockactivity numeric(19,0),
    activitydefinitionid character varying(90) NOT NULL,
    activity numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkassignmenteventaudits (
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(100) NOT NULL,
    activityname character varying(254),
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    activitydefinitionid character varying(90) NOT NULL,
    activitydefinitionname character varying(90),
    activitydefinitiontype integer NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    oldresourceusername character varying(100),
    oldresourcename character varying(100),
    newresourceusername character varying(100) NOT NULL,
    newresourcename character varying(100),
    isaccepted boolean NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkassignmentstable (
    activity numeric(19,0) NOT NULL,
    theresource numeric(19,0) NOT NULL,
    activityid character varying(100) NOT NULL,
    activityprocessid character varying(100) NOT NULL,
    activityprocessdefname character varying(200) NOT NULL,
    resourceid character varying(100) NOT NULL,
    isaccepted boolean NOT NULL,
    isvalid boolean NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkcounters (
    name character varying(100) NOT NULL,
    the_number numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkcreateprocesseventaudits (
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    pactivityid character varying(100),
    pprocessid character varying(100),
    pprocessname character varying(254),
    pprocessfactoryname character varying(200),
    pprocessfactoryversion character varying(20),
    pactivitydefinitionid character varying(90),
    pactivitydefinitionname character varying(90),
    pprocessdefinitionid character varying(90),
    pprocessdefinitionname character varying(90),
    ppackageid character varying(90),
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkdataeventaudits (
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(100),
    activityname character varying(254),
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    activitydefinitionid character varying(90),
    activitydefinitionname character varying(90),
    activitydefinitiontype integer,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkdeadlines (
    process numeric(19,0) NOT NULL,
    activity numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    timelimit bigint NOT NULL,
    timelimittzo bigint NOT NULL,
    exceptionname character varying(100) NOT NULL,
    issynchronous boolean NOT NULL,
    isexecuted boolean NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkeventtypes (
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkgroupgrouptable (
    sub_gid numeric(19,0) NOT NULL,
    groupid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkgrouptable (
    groupid character varying(100) NOT NULL,
    description character varying(254),
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkgroupuser (
    username character varying(100) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkgroupuserpacklevelpart (
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkgroupuserproclevelpart (
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkneweventauditdata (
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkneweventauditdatablobs (
    neweventauditdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkneweventauditdatawob (
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shknextxpdlversions (
    xpdlid character varying(90) NOT NULL,
    nextversion character varying(20) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shknormaluser (
    username character varying(100) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkoldeventauditdata (
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkoldeventauditdatablobs (
    oldeventauditdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkoldeventauditdatawob (
    dataeventaudit numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkpacklevelparticipant (
    participant_id character varying(90) NOT NULL,
    packageoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkpacklevelxpdlapp (
    application_id character varying(90) NOT NULL,
    packageoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkpacklevelxpdlapptaappdetail (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkpacklevelxpdlapptaappdetusr (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkpacklevelxpdlapptaappuser (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkpacklevelxpdlapptoolagntapp (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocessdata (
    process numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocessdatablobs (
    processdatawob numeric(19,0) NOT NULL,
    variablevalue bytea,
    ordno integer NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocessdatawob (
    process numeric(19,0) NOT NULL,
    variabledefinitionid character varying(100) NOT NULL,
    variabletype integer NOT NULL,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocessdefinitions (
    name character varying(200) NOT NULL,
    packageid character varying(90) NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    processdefinitioncreated bigint NOT NULL,
    processdefinitionversion character varying(20) NOT NULL,
    state integer NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocesses (
    syncversion bigint NOT NULL,
    id character varying(100) NOT NULL,
    processdefinition numeric(19,0) NOT NULL,
    pdefname character varying(200) NOT NULL,
    activityrequesterid character varying(100),
    activityrequesterprocessid character varying(100),
    resourcerequesterid character varying(100) NOT NULL,
    externalrequesterclassname character varying(254),
    state numeric(19,0) NOT NULL,
    priority integer,
    name character varying(254),
    created bigint NOT NULL,
    createdtzo bigint NOT NULL,
    started bigint,
    startedtzo bigint,
    laststatetime bigint NOT NULL,
    laststatetimetzo bigint NOT NULL,
    limittime bigint NOT NULL,
    limittimetzo bigint NOT NULL,
    description character varying(254),
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocesshistorydetails (
    processid character varying(100) NOT NULL,
    processhistoryinfo numeric(19,0) NOT NULL,
    thetype integer NOT NULL,
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    variabledefinitionid character varying(100),
    variabletype integer,
    variablevalue bytea,
    variablevaluexml text,
    variablevaluevchar character varying(4000),
    variablevaluedbl double precision,
    variablevaluelong bigint,
    variablevaluedate timestamp without time zone,
    variablevaluebool boolean,
    ordno integer NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocesshistoryinfo (
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    pactivityid character varying(100),
    pprocessid character varying(100),
    pprocessname character varying(254),
    pprocessfactoryname character varying(200),
    pprocessfactoryversion character varying(20),
    pactivitydefinitionid character varying(90),
    pactivitydefinitionname character varying(90),
    pprocessdefinitionid character varying(90),
    pprocessdefinitionname character varying(90),
    ppackageid character varying(90),
    createdtime bigint,
    createdtimetzo bigint,
    startedtime bigint,
    startedtimetzo bigint,
    suspendedtime bigint,
    suspendedtimetzo bigint,
    resumedtime bigint,
    resumedtimetzo bigint,
    closedtime bigint,
    closedtimetzo bigint,
    createdbyusername character varying(100),
    startedbyusername character varying(100),
    suspendedbyusername character varying(100),
    resumedbyusername character varying(100),
    closedbyusername character varying(100),
    laststate character varying(100),
    laststatetime bigint,
    laststatetimetzo bigint,
    processduration bigint,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocessrequesters (
    id character varying(100) NOT NULL,
    activityrequester numeric(19,0),
    resourcerequester numeric(19,0),
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocessstateeventaudits (
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkprocessstates (
    keyvalue character varying(30) NOT NULL,
    name character varying(50) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkproclevelparticipant (
    participant_id character varying(90) NOT NULL,
    processoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkproclevelxpdlapp (
    application_id character varying(90) NOT NULL,
    processoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkproclevelxpdlapptaappdetail (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkproclevelxpdlapptaappdetusr (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkproclevelxpdlapptaappuser (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkproclevelxpdlapptoolagntapp (
    xpdl_appoid numeric(19,0) NOT NULL,
    toolagentoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkresourcestable (
    username character varying(100) NOT NULL,
    name character varying(100),
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkstateeventaudits (
    recordedtime bigint NOT NULL,
    recordedtimetzo bigint NOT NULL,
    theusername character varying(100) NOT NULL,
    thetype numeric(19,0) NOT NULL,
    activityid character varying(100),
    activityname character varying(254),
    processid character varying(100) NOT NULL,
    processname character varying(254),
    processfactoryname character varying(200) NOT NULL,
    processfactoryversion character varying(20) NOT NULL,
    activitydefinitionid character varying(90),
    activitydefinitionname character varying(90),
    activitydefinitiontype integer,
    processdefinitionid character varying(90) NOT NULL,
    processdefinitionname character varying(90),
    packageid character varying(90) NOT NULL,
    oldprocessstate numeric(19,0),
    newprocessstate numeric(19,0),
    oldactivitystate numeric(19,0),
    newactivitystate numeric(19,0),
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shktoolagentapp (
    tool_agent_name character varying(250) NOT NULL,
    app_name character varying(90) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shktoolagentappdetail (
    app_mode numeric(10,0) NOT NULL,
    toolagent_appoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shktoolagentappdetailuser (
    toolagent_appoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shktoolagentappuser (
    toolagent_appoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shktoolagentuser (
    username character varying(100) NOT NULL,
    pwd character varying(100),
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkusergrouptable (
    userid numeric(19,0) NOT NULL,
    groupid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkuserpacklevelpart (
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkuserproclevelparticipant (
    participantoid numeric(19,0) NOT NULL,
    useroid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkusertable (
    userid character varying(100) NOT NULL,
    firstname character varying(50),
    lastname character varying(50),
    passwd character varying(50) NOT NULL,
    email character varying(254),
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdlapplicationpackage (
    package_id character varying(90) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdlapplicationprocess (
    process_id character varying(90) NOT NULL,
    packageoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdldata (
    xpdlcontent bytea NOT NULL,
    xpdlclasscontent bytea NOT NULL,
    xpdl numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdlhistory (
    xpdlid character varying(90) NOT NULL,
    xpdlversion character varying(20) NOT NULL,
    xpdlclassversion bigint NOT NULL,
    xpdluploadtime timestamp without time zone NOT NULL,
    xpdlhistoryuploadtime timestamp without time zone NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdlhistorydata (
    xpdlcontent bytea NOT NULL,
    xpdlclasscontent bytea NOT NULL,
    xpdlhistory numeric(19,0) NOT NULL,
    cnt numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdlparticipantpackage (
    package_id character varying(90) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdlparticipantprocess (
    process_id character varying(90) NOT NULL,
    packageoid numeric(19,0) NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdlreferences (
    referredxpdlid character varying(90) NOT NULL,
    referringxpdl numeric(19,0) NOT NULL,
    referredxpdlnumber integer NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);



CREATE TABLE shkxpdls (
    xpdlid character varying(90) NOT NULL,
    xpdlversion character varying(20) NOT NULL,
    xpdlclassversion bigint NOT NULL,
    xpdluploadtime timestamp without time zone NOT NULL,
    objectid numeric(19,0) NOT NULL,
    objectversion integer NOT NULL
);
























INSERT INTO qrtz_locks (lock_name) VALUES ('TRIGGER_ACCESS');
INSERT INTO qrtz_locks (lock_name) VALUES ('JOB_ACCESS');
INSERT INTO qrtz_locks (lock_name) VALUES ('CALENDAR_ACCESS');
INSERT INTO qrtz_locks (lock_name) VALUES ('STATE_ACCESS');
INSERT INTO qrtz_locks (lock_name) VALUES ('MISFIRE_ACCESS');







































































































































































































































ALTER TABLE ONLY objectid
    ADD CONSTRAINT objectid_pkey PRIMARY KEY (next);



ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (calendar_name);



ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (entry_id);



ALTER TABLE ONLY qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (job_name, job_group);



ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT qrtz_job_listeners_pkey PRIMARY KEY (job_name, job_group, job_listener);



ALTER TABLE ONLY qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (lock_name);



ALTER TABLE ONLY qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (trigger_group);



ALTER TABLE ONLY qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (instance_name);



ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT qrtz_trigger_listeners_pkey PRIMARY KEY (trigger_name, trigger_group, trigger_listener);



ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);



ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitydata
    ADD CONSTRAINT shkactivitydata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitydatablobs
    ADD CONSTRAINT shkactivitydatablobs_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitydatawob
    ADD CONSTRAINT shkactivitydatawob_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivityhistorydetails
    ADD CONSTRAINT shkactivityhistorydetails_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivityhistoryinfo
    ADD CONSTRAINT shkactivityhistoryinfo_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitystateeventaudits
    ADD CONSTRAINT shkactivitystateeventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkactivitystates
    ADD CONSTRAINT shkactivitystates_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkassignmenteventaudits
    ADD CONSTRAINT shkassignmenteventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkcounters
    ADD CONSTRAINT shkcounters_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkcreateprocesseventaudits
    ADD CONSTRAINT shkcreateprocesseventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkdataeventaudits
    ADD CONSTRAINT shkdataeventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkeventtypes
    ADD CONSTRAINT shkeventtypes_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgrouptable
    ADD CONSTRAINT shkgrouptable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgroupuser
    ADD CONSTRAINT shkgroupuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkneweventauditdata
    ADD CONSTRAINT shkneweventauditdata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkneweventauditdatablobs
    ADD CONSTRAINT shkneweventauditdatablobs_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkneweventauditdatawob
    ADD CONSTRAINT shkneweventauditdatawob_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shknextxpdlversions
    ADD CONSTRAINT shknextxpdlversions_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shknormaluser
    ADD CONSTRAINT shknormaluser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkoldeventauditdata
    ADD CONSTRAINT shkoldeventauditdata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkoldeventauditdatablobs
    ADD CONSTRAINT shkoldeventauditdatablobs_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkoldeventauditdatawob
    ADD CONSTRAINT shkoldeventauditdatawob_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelparticipant
    ADD CONSTRAINT shkpacklevelparticipant_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapp
    ADD CONSTRAINT shkpacklevelxpdlapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessdata
    ADD CONSTRAINT shkprocessdata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessdatablobs
    ADD CONSTRAINT shkprocessdatablobs_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessdatawob
    ADD CONSTRAINT shkprocessdatawob_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessdefinitions
    ADD CONSTRAINT shkprocessdefinitions_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocesshistorydetails
    ADD CONSTRAINT shkprocesshistorydetails_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocesshistoryinfo
    ADD CONSTRAINT shkprocesshistoryinfo_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessstateeventaudits
    ADD CONSTRAINT shkprocessstateeventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkprocessstates
    ADD CONSTRAINT shkprocessstates_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelparticipant
    ADD CONSTRAINT shkproclevelparticipant_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapp
    ADD CONSTRAINT shkproclevelxpdlapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkresourcestable
    ADD CONSTRAINT shkresourcestable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentapp
    ADD CONSTRAINT shktoolagentapp_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentappdetail
    ADD CONSTRAINT shktoolagentappdetail_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shktoolagentuser
    ADD CONSTRAINT shktoolagentuser_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkusertable
    ADD CONSTRAINT shkusertable_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlapplicationpackage
    ADD CONSTRAINT shkxpdlapplicationpackage_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlapplicationprocess
    ADD CONSTRAINT shkxpdlapplicationprocess_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdldata
    ADD CONSTRAINT shkxpdldata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlhistory
    ADD CONSTRAINT shkxpdlhistory_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlhistorydata
    ADD CONSTRAINT shkxpdlhistorydata_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlparticipantpackage
    ADD CONSTRAINT shkxpdlparticipantpackage_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlparticipantprocess
    ADD CONSTRAINT shkxpdlparticipantprocess_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdlreferences
    ADD CONSTRAINT shkxpdlreferences_objectid PRIMARY KEY (objectid);



ALTER TABLE ONLY shkxpdls
    ADD CONSTRAINT shkxpdls_objectid PRIMARY KEY (objectid);



CREATE UNIQUE INDEX i1_shkactivities ON shkactivities USING btree (id);



CREATE UNIQUE INDEX i1_shkactivitydata ON shkactivitydata USING btree (cnt);



CREATE UNIQUE INDEX i1_shkactivitydatablobs ON shkactivitydatablobs USING btree (activitydatawob, ordno);



CREATE UNIQUE INDEX i1_shkactivitydatawob ON shkactivitydatawob USING btree (cnt);



CREATE UNIQUE INDEX i1_shkactivityhistorydetails ON shkactivityhistorydetails USING btree (cnt);



CREATE UNIQUE INDEX i1_shkactivityhistoryinfo ON shkactivityhistoryinfo USING btree (activityid);



CREATE UNIQUE INDEX i1_shkactivitystateeventaudits ON shkactivitystateeventaudits USING btree (keyvalue);



CREATE UNIQUE INDEX i1_shkactivitystates ON shkactivitystates USING btree (keyvalue);



CREATE UNIQUE INDEX i1_shkandjointable ON shkandjointable USING btree (cnt);



CREATE UNIQUE INDEX i1_shkassignmenteventaudits ON shkassignmenteventaudits USING btree (cnt);



CREATE UNIQUE INDEX i1_shkassignmentstable ON shkassignmentstable USING btree (cnt);



CREATE UNIQUE INDEX i1_shkcounters ON shkcounters USING btree (name);



CREATE UNIQUE INDEX i1_shkcreateprocesseventaudits ON shkcreateprocesseventaudits USING btree (cnt);



CREATE UNIQUE INDEX i1_shkdataeventaudits ON shkdataeventaudits USING btree (cnt);



CREATE UNIQUE INDEX i1_shkdeadlines ON shkdeadlines USING btree (cnt);



CREATE UNIQUE INDEX i1_shkeventtypes ON shkeventtypes USING btree (keyvalue);



CREATE UNIQUE INDEX i1_shkgroupgrouptable ON shkgroupgrouptable USING btree (sub_gid, groupid);



CREATE UNIQUE INDEX i1_shkgrouptable ON shkgrouptable USING btree (groupid);



CREATE UNIQUE INDEX i1_shkgroupuser ON shkgroupuser USING btree (username);



CREATE UNIQUE INDEX i1_shkgroupuserpacklevelpart ON shkgroupuserpacklevelpart USING btree (participantoid, useroid);



CREATE UNIQUE INDEX i1_shkgroupuserproclevelpart ON shkgroupuserproclevelpart USING btree (participantoid, useroid);



CREATE UNIQUE INDEX i1_shkneweventauditdata ON shkneweventauditdata USING btree (cnt);



CREATE UNIQUE INDEX i1_shkneweventauditdatablobs ON shkneweventauditdatablobs USING btree (neweventauditdatawob, ordno);



CREATE UNIQUE INDEX i1_shkneweventauditdatawob ON shkneweventauditdatawob USING btree (cnt);



CREATE UNIQUE INDEX i1_shknextxpdlversions ON shknextxpdlversions USING btree (xpdlid, nextversion);



CREATE UNIQUE INDEX i1_shknormaluser ON shknormaluser USING btree (username);



CREATE UNIQUE INDEX i1_shkoldeventauditdata ON shkoldeventauditdata USING btree (cnt);



CREATE UNIQUE INDEX i1_shkoldeventauditdatablobs ON shkoldeventauditdatablobs USING btree (oldeventauditdatawob, ordno);



CREATE UNIQUE INDEX i1_shkoldeventauditdatawob ON shkoldeventauditdatawob USING btree (cnt);



CREATE UNIQUE INDEX i1_shkpacklevelparticipant ON shkpacklevelparticipant USING btree (participant_id, packageoid);



CREATE UNIQUE INDEX i1_shkpacklevelxpdlapp ON shkpacklevelxpdlapp USING btree (application_id, packageoid);



CREATE UNIQUE INDEX i1_shkpacklevelxpdlapptaappdetail ON shkpacklevelxpdlapptaappdetail USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX i1_shkpacklevelxpdlapptaappdetusr ON shkpacklevelxpdlapptaappdetusr USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX i1_shkpacklevelxpdlapptaappuser ON shkpacklevelxpdlapptaappuser USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX i1_shkpacklevelxpdlapptoolagntapp ON shkpacklevelxpdlapptoolagntapp USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX i1_shkprocessdata ON shkprocessdata USING btree (cnt);



CREATE UNIQUE INDEX i1_shkprocessdatablobs ON shkprocessdatablobs USING btree (processdatawob, ordno);



CREATE UNIQUE INDEX i1_shkprocessdatawob ON shkprocessdatawob USING btree (cnt);



CREATE UNIQUE INDEX i1_shkprocessdefinitions ON shkprocessdefinitions USING btree (name);



CREATE UNIQUE INDEX i1_shkprocesses ON shkprocesses USING btree (id);



CREATE UNIQUE INDEX i1_shkprocesshistorydetails ON shkprocesshistorydetails USING btree (cnt);



CREATE UNIQUE INDEX i1_shkprocesshistoryinfo ON shkprocesshistoryinfo USING btree (processid);



CREATE UNIQUE INDEX i1_shkprocessrequesters ON shkprocessrequesters USING btree (id);



CREATE UNIQUE INDEX i1_shkprocessstateeventaudits ON shkprocessstateeventaudits USING btree (keyvalue);



CREATE UNIQUE INDEX i1_shkprocessstates ON shkprocessstates USING btree (keyvalue);



CREATE UNIQUE INDEX i1_shkproclevelparticipant ON shkproclevelparticipant USING btree (participant_id, processoid);



CREATE UNIQUE INDEX i1_shkproclevelxpdlapp ON shkproclevelxpdlapp USING btree (application_id, processoid);



CREATE UNIQUE INDEX i1_shkproclevelxpdlapptaappdetail ON shkproclevelxpdlapptaappdetail USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX i1_shkproclevelxpdlapptaappdetusr ON shkproclevelxpdlapptaappdetusr USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX i1_shkproclevelxpdlapptaappuser ON shkproclevelxpdlapptaappuser USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX i1_shkproclevelxpdlapptoolagntapp ON shkproclevelxpdlapptoolagntapp USING btree (xpdl_appoid, toolagentoid);



CREATE UNIQUE INDEX i1_shkresourcestable ON shkresourcestable USING btree (username);



CREATE UNIQUE INDEX i1_shkstateeventaudits ON shkstateeventaudits USING btree (cnt);



CREATE UNIQUE INDEX i1_shktoolagentapp ON shktoolagentapp USING btree (tool_agent_name, app_name);



CREATE UNIQUE INDEX i1_shktoolagentappdetail ON shktoolagentappdetail USING btree (app_mode, toolagent_appoid);



CREATE UNIQUE INDEX i1_shktoolagentappdetailuser ON shktoolagentappdetailuser USING btree (toolagent_appoid, useroid);



CREATE UNIQUE INDEX i1_shktoolagentappuser ON shktoolagentappuser USING btree (toolagent_appoid, useroid);



CREATE UNIQUE INDEX i1_shktoolagentuser ON shktoolagentuser USING btree (username);



CREATE UNIQUE INDEX i1_shkusergrouptable ON shkusergrouptable USING btree (userid, groupid);



CREATE UNIQUE INDEX i1_shkuserpacklevelpart ON shkuserpacklevelpart USING btree (participantoid, useroid);



CREATE UNIQUE INDEX i1_shkuserproclevelparticipant ON shkuserproclevelparticipant USING btree (participantoid, useroid);



CREATE UNIQUE INDEX i1_shkusertable ON shkusertable USING btree (userid);



CREATE UNIQUE INDEX i1_shkxpdlapplicationpackage ON shkxpdlapplicationpackage USING btree (package_id);



CREATE UNIQUE INDEX i1_shkxpdlapplicationprocess ON shkxpdlapplicationprocess USING btree (process_id, packageoid);



CREATE UNIQUE INDEX i1_shkxpdldata ON shkxpdldata USING btree (cnt);



CREATE UNIQUE INDEX i1_shkxpdlhistory ON shkxpdlhistory USING btree (xpdlid, xpdlversion);



CREATE UNIQUE INDEX i1_shkxpdlhistorydata ON shkxpdlhistorydata USING btree (cnt);



CREATE UNIQUE INDEX i1_shkxpdlparticipantpackage ON shkxpdlparticipantpackage USING btree (package_id);



CREATE UNIQUE INDEX i1_shkxpdlparticipantprocess ON shkxpdlparticipantprocess USING btree (process_id, packageoid);



CREATE UNIQUE INDEX i1_shkxpdlreferences ON shkxpdlreferences USING btree (referredxpdlid, referringxpdl);



CREATE UNIQUE INDEX i1_shkxpdls ON shkxpdls USING btree (xpdlid, xpdlversion);



CREATE INDEX i2_shkactivities ON shkactivities USING btree (process, activitysetdefinitionid, activitydefinitionid);



CREATE UNIQUE INDEX i2_shkactivitydata ON shkactivitydata USING btree (activity, variabledefinitionid, ordno);



CREATE UNIQUE INDEX i2_shkactivitydatawob ON shkactivitydatawob USING btree (activity, variabledefinitionid, ordno);



CREATE INDEX i2_shkactivityhistorydetails ON shkactivityhistorydetails USING btree (activityid);



CREATE INDEX i2_shkactivityhistoryinfo ON shkactivityhistoryinfo USING btree (processid);



CREATE UNIQUE INDEX i2_shkactivitystateeventaudits ON shkactivitystateeventaudits USING btree (name);



CREATE UNIQUE INDEX i2_shkactivitystates ON shkactivitystates USING btree (name);



CREATE INDEX i2_shkandjointable ON shkandjointable USING btree (process, blockactivity, activitydefinitionid);



CREATE UNIQUE INDEX i2_shkassignmentstable ON shkassignmentstable USING btree (activity, theresource);



CREATE INDEX i2_shkdeadlines ON shkdeadlines USING btree (process, timelimit);



CREATE UNIQUE INDEX i2_shkeventtypes ON shkeventtypes USING btree (name);



CREATE INDEX i2_shkgroupgrouptable ON shkgroupgrouptable USING btree (groupid);



CREATE UNIQUE INDEX i2_shkneweventauditdata ON shkneweventauditdata USING btree (dataeventaudit, variabledefinitionid, ordno);



CREATE UNIQUE INDEX i2_shkneweventauditdatawob ON shkneweventauditdatawob USING btree (dataeventaudit, variabledefinitionid, ordno);



CREATE UNIQUE INDEX i2_shkoldeventauditdata ON shkoldeventauditdata USING btree (dataeventaudit, variabledefinitionid, ordno);



CREATE UNIQUE INDEX i2_shkoldeventauditdatawob ON shkoldeventauditdatawob USING btree (dataeventaudit, variabledefinitionid, ordno);



CREATE UNIQUE INDEX i2_shkprocessdata ON shkprocessdata USING btree (process, variabledefinitionid, ordno);



CREATE UNIQUE INDEX i2_shkprocessdatawob ON shkprocessdatawob USING btree (process, variabledefinitionid, ordno);



CREATE INDEX i2_shkprocesses ON shkprocesses USING btree (processdefinition);



CREATE INDEX i2_shkprocesshistorydetails ON shkprocesshistorydetails USING btree (processid);



CREATE INDEX i2_shkprocessrequesters ON shkprocessrequesters USING btree (activityrequester);



CREATE UNIQUE INDEX i2_shkprocessstateeventaudits ON shkprocessstateeventaudits USING btree (name);



CREATE UNIQUE INDEX i2_shkprocessstates ON shkprocessstates USING btree (name);



CREATE UNIQUE INDEX i2_shkxpdldata ON shkxpdldata USING btree (xpdl);



CREATE INDEX i3_shkactivities ON shkactivities USING btree (process, state);



CREATE INDEX i3_shkandjointable ON shkandjointable USING btree (activity);



CREATE INDEX i3_shkassignmentstable ON shkassignmentstable USING btree (theresource, isvalid);



CREATE INDEX i3_shkdeadlines ON shkdeadlines USING btree (activity, timelimit);



CREATE INDEX i3_shkprocesses ON shkprocesses USING btree (state);



CREATE INDEX i3_shkprocessrequesters ON shkprocessrequesters USING btree (resourcerequester);



CREATE INDEX i4_shkassignmentstable ON shkassignmentstable USING btree (activityid);



CREATE INDEX i4_shkprocesses ON shkprocesses USING btree (activityrequesterid);



CREATE INDEX i5_shkassignmentstable ON shkassignmentstable USING btree (resourceid);



CREATE INDEX i5_shkprocesses ON shkprocesses USING btree (resourcerequesterid);



ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT qrtz_job_listeners_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);



ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT qrtz_trigger_listeners_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);



ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);



ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_state FOREIGN KEY (state) REFERENCES shkactivitystates(objectid);



ALTER TABLE ONLY shkactivities
    ADD CONSTRAINT shkactivities_theresource FOREIGN KEY (theresource) REFERENCES shkresourcestable(objectid);



ALTER TABLE ONLY shkactivitydata
    ADD CONSTRAINT shkactivitydata_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkactivitydatablobs
    ADD CONSTRAINT shkactivitydatablobs_activitydatawob FOREIGN KEY (activitydatawob) REFERENCES shkactivitydatawob(objectid);



ALTER TABLE ONLY shkactivitydatawob
    ADD CONSTRAINT shkactivitydatawob_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkactivityhistorydetails
    ADD CONSTRAINT shkactivityhistorydetails_activityhistoryinfo FOREIGN KEY (activityhistoryinfo) REFERENCES shkactivityhistoryinfo(objectid);



ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_blockactivity FOREIGN KEY (blockactivity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkandjointable
    ADD CONSTRAINT shkandjointable_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkassignmenteventaudits
    ADD CONSTRAINT shkassignmenteventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(objectid);



ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkassignmentstable
    ADD CONSTRAINT shkassignmentstable_theresource FOREIGN KEY (theresource) REFERENCES shkresourcestable(objectid);



ALTER TABLE ONLY shkcreateprocesseventaudits
    ADD CONSTRAINT shkcreateprocesseventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(objectid);



ALTER TABLE ONLY shkdataeventaudits
    ADD CONSTRAINT shkdataeventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(objectid);



ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_activity FOREIGN KEY (activity) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkdeadlines
    ADD CONSTRAINT shkdeadlines_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_groupid FOREIGN KEY (groupid) REFERENCES shkgrouptable(objectid);



ALTER TABLE ONLY shkgroupgrouptable
    ADD CONSTRAINT shkgroupgrouptable_sub_gid FOREIGN KEY (sub_gid) REFERENCES shkgrouptable(objectid);



ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkpacklevelparticipant(objectid);



ALTER TABLE ONLY shkgroupuserpacklevelpart
    ADD CONSTRAINT shkgroupuserpacklevelpart_useroid FOREIGN KEY (useroid) REFERENCES shkgroupuser(objectid);



ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkproclevelparticipant(objectid);



ALTER TABLE ONLY shkgroupuserproclevelpart
    ADD CONSTRAINT shkgroupuserproclevelpart_useroid FOREIGN KEY (useroid) REFERENCES shkgroupuser(objectid);



ALTER TABLE ONLY shkneweventauditdata
    ADD CONSTRAINT shkneweventauditdata_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(objectid);



ALTER TABLE ONLY shkneweventauditdatablobs
    ADD CONSTRAINT shkneweventauditdatablobs_neweventauditdatawob FOREIGN KEY (neweventauditdatawob) REFERENCES shkneweventauditdatawob(objectid);



ALTER TABLE ONLY shkneweventauditdatawob
    ADD CONSTRAINT shkneweventauditdatawob_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(objectid);



ALTER TABLE ONLY shkoldeventauditdata
    ADD CONSTRAINT shkoldeventauditdata_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(objectid);



ALTER TABLE ONLY shkoldeventauditdatablobs
    ADD CONSTRAINT shkoldeventauditdatablobs_oldeventauditdatawob FOREIGN KEY (oldeventauditdatawob) REFERENCES shkoldeventauditdatawob(objectid);



ALTER TABLE ONLY shkoldeventauditdatawob
    ADD CONSTRAINT shkoldeventauditdatawob_dataeventaudit FOREIGN KEY (dataeventaudit) REFERENCES shkdataeventaudits(objectid);



ALTER TABLE ONLY shkpacklevelparticipant
    ADD CONSTRAINT shkpacklevelparticipant_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlparticipantpackage(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapp
    ADD CONSTRAINT shkpacklevelxpdlapp_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlapplicationpackage(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetail(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetail
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetail_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetailuser(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappdetusr
    ADD CONSTRAINT shkpacklevelxpdlapptaappdetusr_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappuser(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptaappuser
    ADD CONSTRAINT shkpacklevelxpdlapptaappuser_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentapp(objectid);



ALTER TABLE ONLY shkpacklevelxpdlapptoolagntapp
    ADD CONSTRAINT shkpacklevelxpdlapptoolagntapp_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkpacklevelxpdlapp(objectid);



ALTER TABLE ONLY shkprocessdata
    ADD CONSTRAINT shkprocessdata_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkprocessdatablobs
    ADD CONSTRAINT shkprocessdatablobs_processdatawob FOREIGN KEY (processdatawob) REFERENCES shkprocessdatawob(objectid);



ALTER TABLE ONLY shkprocessdatawob
    ADD CONSTRAINT shkprocessdatawob_process FOREIGN KEY (process) REFERENCES shkprocesses(objectid);



ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_processdefinition FOREIGN KEY (processdefinition) REFERENCES shkprocessdefinitions(objectid);



ALTER TABLE ONLY shkprocesses
    ADD CONSTRAINT shkprocesses_state FOREIGN KEY (state) REFERENCES shkprocessstates(objectid);



ALTER TABLE ONLY shkprocesshistorydetails
    ADD CONSTRAINT shkprocesshistorydetails_processhistoryinfo FOREIGN KEY (processhistoryinfo) REFERENCES shkprocesshistoryinfo(objectid);



ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_activityrequester FOREIGN KEY (activityrequester) REFERENCES shkactivities(objectid);



ALTER TABLE ONLY shkprocessrequesters
    ADD CONSTRAINT shkprocessrequesters_resourcerequester FOREIGN KEY (resourcerequester) REFERENCES shkresourcestable(objectid);



ALTER TABLE ONLY shkproclevelparticipant
    ADD CONSTRAINT shkproclevelparticipant_processoid FOREIGN KEY (processoid) REFERENCES shkxpdlparticipantprocess(objectid);



ALTER TABLE ONLY shkproclevelxpdlapp
    ADD CONSTRAINT shkproclevelxpdlapp_processoid FOREIGN KEY (processoid) REFERENCES shkxpdlapplicationprocess(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetail(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetail
    ADD CONSTRAINT shkproclevelxpdlapptaappdetail_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappdetailuser(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappdetusr
    ADD CONSTRAINT shkproclevelxpdlapptaappdetusr_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentappuser(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptaappuser
    ADD CONSTRAINT shkproclevelxpdlapptaappuser_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_toolagentoid FOREIGN KEY (toolagentoid) REFERENCES shktoolagentapp(objectid);



ALTER TABLE ONLY shkproclevelxpdlapptoolagntapp
    ADD CONSTRAINT shkproclevelxpdlapptoolagntapp_xpdl_appoid FOREIGN KEY (xpdl_appoid) REFERENCES shkproclevelxpdlapp(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_newactivitystate FOREIGN KEY (newactivitystate) REFERENCES shkactivitystateeventaudits(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_newprocessstate FOREIGN KEY (newprocessstate) REFERENCES shkprocessstateeventaudits(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_oldactivitystate FOREIGN KEY (oldactivitystate) REFERENCES shkactivitystateeventaudits(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_oldprocessstate FOREIGN KEY (oldprocessstate) REFERENCES shkprocessstateeventaudits(objectid);



ALTER TABLE ONLY shkstateeventaudits
    ADD CONSTRAINT shkstateeventaudits_thetype FOREIGN KEY (thetype) REFERENCES shkeventtypes(objectid);



ALTER TABLE ONLY shktoolagentappdetail
    ADD CONSTRAINT shktoolagentappdetail_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentapp(objectid);



ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentappdetail(objectid);



ALTER TABLE ONLY shktoolagentappdetailuser
    ADD CONSTRAINT shktoolagentappdetailuser_useroid FOREIGN KEY (useroid) REFERENCES shktoolagentuser(objectid);



ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_toolagent_appoid FOREIGN KEY (toolagent_appoid) REFERENCES shktoolagentapp(objectid);



ALTER TABLE ONLY shktoolagentappuser
    ADD CONSTRAINT shktoolagentappuser_useroid FOREIGN KEY (useroid) REFERENCES shktoolagentuser(objectid);



ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_groupid FOREIGN KEY (groupid) REFERENCES shkgrouptable(objectid);



ALTER TABLE ONLY shkusergrouptable
    ADD CONSTRAINT shkusergrouptable_userid FOREIGN KEY (userid) REFERENCES shkusertable(objectid);



ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_participantoid FOREIGN KEY (participantoid) REFERENCES shkpacklevelparticipant(objectid);



ALTER TABLE ONLY shkuserpacklevelpart
    ADD CONSTRAINT shkuserpacklevelpart_useroid FOREIGN KEY (useroid) REFERENCES shknormaluser(objectid);



ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_participantoid FOREIGN KEY (participantoid) REFERENCES shkproclevelparticipant(objectid);



ALTER TABLE ONLY shkuserproclevelparticipant
    ADD CONSTRAINT shkuserproclevelparticipant_useroid FOREIGN KEY (useroid) REFERENCES shknormaluser(objectid);



ALTER TABLE ONLY shkxpdlapplicationprocess
    ADD CONSTRAINT shkxpdlapplicationprocess_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlapplicationpackage(objectid);



ALTER TABLE ONLY shkxpdldata
    ADD CONSTRAINT shkxpdldata_xpdl FOREIGN KEY (xpdl) REFERENCES shkxpdls(objectid);



ALTER TABLE ONLY shkxpdlhistorydata
    ADD CONSTRAINT shkxpdlhistorydata_xpdlhistory FOREIGN KEY (xpdlhistory) REFERENCES shkxpdlhistory(objectid);



ALTER TABLE ONLY shkxpdlparticipantprocess
    ADD CONSTRAINT shkxpdlparticipantprocess_packageoid FOREIGN KEY (packageoid) REFERENCES shkxpdlparticipantpackage(objectid);



ALTER TABLE ONLY shkxpdlreferences
    ADD CONSTRAINT shkxpdlreferences_referringxpdl FOREIGN KEY (referringxpdl) REFERENCES shkxpdls(objectid);
