

;(function ($) {
    //Object Instance
    $.accountRegister = function(el, options) {
        var state = el,
            methods = {};

        state.opts = $.extend({}, $.accountRegister.defaults, options);
        var svc = lusidity.environment("host-primary")+"/files/docs/";
        svc = $.jCommon.string.replaceAll(svc, "/svc/", "/");
        var _mailTo = 'disa.meade.re.mbx.rmk@mail.mil';
        var _instruct = svc + "RMK Registration Specific Instructions.pdf";
        var _dd2875 = svc + "DD Form 2875_RMK.pdf";
        var _df787 =  svc + "DISA Form 787.pdf";

        // Store a reference to the environment object
        el.data("accountRegister", state);
        state.KEY_ID = '/vertex/uri';
        state.KEY_TITLE = 'title';
        state.KEY_ORG = '/organization/organization';
        state.KEY_ORG_E = '/object/edge/organization_edge';
        state.KEY_ORG_P = '/organization/organization/organizations';
        state.orgQuery = {
            url: '/query/org',
            root: function () {
                return {
                    asFilterable: true,
                    domain: state.KEY_ORG,
                    "native": {query: {filtered: {filter: {bool: {should: [{term: {"title.folded": "root organizations"}},{term: {"title.folded": "defense information systems agency"}}]}}}}}
                };
            },
            children: function(item){
                return {
                    asFilterable: true,
                    domain: state.KEY_ORG_E,
                    type: state.KEY_ORG,
                    lid: item.lid,
                    "native": {
                        query: {
                            filtered: {
                                filter: {
                                    bool: {
                                        must: [
                                            {term: {'/object/endpoint/endpointFrom.relatedId.raw': item.lid}},
                                            {term: {'label.raw': state.KEY_ORG_P}}
                                        ]
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };
        // Private environment methods
        methods = {
            init: function() {
                $('.page-content').css({padding: '0 0 0 0', overflow: 'hidden'});

                state.opts.pnlMiddleNode.addClass('no-border').css({overflow: 'hidden'});
                lusidity.resizePage(70);
                methods.form.init();
                window.onbeforeunload = function (e) {
                    if($.jCommon.string.contains(e.target.URL, '/register')){
                        e.preventDefault();
                    }
                };
                lusidity.environment('onResize',  function (e) {
                    lusidity.resizePage(70);
                    state.opts.pnlMiddleNode.panel('resize');
                });
            },
            resize: function () {
                //$(window).unbind();
                //state.opts.pnlMiddleNode.children().remove();
                //methods.init();
            },
            ajax: {
                post: function(d, onSuccess, onFail){
                    var action = {
                        connector: null,
                        async: true,
                        data: JSON.stringify(d),
                        methodType: 'post',
                        showProgress: false,
                        onbeforesend: {
                            message: { msg: null, debug: false },
                            execute: function (xhr) {
                                xhr.overrideMimeType("application/json,UTF-8");
                            }
                        },
                        oncompleted: {
                            execute: function (jqXHR, textStatus) {
                            }
                        },
                        onsuccess: {
                            message: { msg: null, debug: false },
                            execute: function (data) {
                                if(onSuccess && $.isFunction(onSuccess)){
                                    onSuccess(data);
                                }
                            }
                        },
                        onfailure: {
                            message: { msg: null, debug: false },
                            execute: function (jqXHR, textStatus, errorThrown) {
                                if (!state.opts.debug) {
                                    if (onFail && $.isFunction(onFail)) {
                                        onFail(jqXHR, textStatus, errorThrown);
                                    }
                                    else {
                                        lusidity.info.red('Sorry, we cannot find the resource requested.&nbsp;&nbsp;'
                                        + 'You can try refreshing the&nbsp;<a href="' + state.url.original + '">page</a>.');
                                        lusidity.info.show(10);
                                        $('div.container-content').hide();
                                    }
                                }
                            }
                        },
                        url: state.opts.url
                    };
                    lusidity.environment('request',action);
                }
            },
            form: {
                init: function () {
                    state.opts.pnlMiddleNode.children().remove();
                    state.opts.pnlMiddleNode.formBuilder({
                        debug: false,
                        fill: true,
                        title: 'RMK Registration',
                        borders: false,
                        glyph: 'glyphicons glyphicons-user-add',
                        url: null,
                        actions: [],
                        show: false,
                        defaultData: {
                            vertexType: '/people/person'
                        },
                        data: state.opts.data,
                        mode: 'add',
                        isDeletable: function () {
                            return false;
                        },
                        deleteMessage: function (body, data) {
                        },
                        onDelete: function (item) {
                        },
                        close: function (node) {
                           
                        },
                        display: function (node) {
                            state.opts.pnlMiddleNode.css({overflow: 'none'});
                            if (state.opts.editNode) {
                                state.opts.editNode.hide();
                            }
                            if (state.opts.addNode) {
                                state.opts.addNode.hide();
                            }
                            if (state.opts.viewerNode) {
                                state.opts.viewerNode.hide();
                            }
                            node.show();
                        },
                        before: function () {
                            state.loaders('show');
                        },
                        formError: function (msg) {
                            lusidity.info.red(msg);
                            lusidity.info.show(5);
                        },
                        onSuccess: function (data) {
                            state.loaders('hide');
                            var item = data.item ? data.item : data;
                            if(!item.failed){
                                if(item.hasAccount){
                                    methods.error.navigate({status: 1});
                                }
                                else{
                                    lusidity.info.red(item.error + 'Click <a href="/register">here</a> to try again.');
                                }
                                lusidity.info.show(10);
                            }
                            else{
                                methods.error.navigate({status: 1});
                            }
                        },
                        onFailed: function () {
                            state.loaders('hide');
                            lusidity.info.red('Sorry, we failed to create the account.');
                        },
                        steps: [
                            {
                                css: {padding: '10px 10px'},
                                buttons: {
                                    disabled: false,
                                    cls: 'wizard'
                                },
                                nodes:[
                                    {
                                        node: 'raw',
                                        getNode: function(){
                                            var r = dCrt('div').css('clear', 'both');
                                            r.append(dCrt('h4').html("Welcome to the Risk Management Knowledgebase!"));
                                            r.append(dCrt('p').html('You are not currently registered for a Risk Management Knowledgebase (RMK) account.'));
                                            r.append(dCrt('p').html('The RMK is a Defense Information Systems Agency (DISA) data integration solution designed to ingest, disambiguate, correlate, index, process, and present large quantities of IT network and cybersecurity data at operational speeds to provide near real-time risk posture assessments and risk mitigation prioritization.  ' +
                                                'If you have an operational requirement to have access to such data and can receive your supervisor\'s concurrence by way of an RMK-provided DD Form 2875, then please proceed with the registration process. The next portion of the RMK account registration process will require the following information:'));
                                            r.append(dCrt('ul')
                                                .append(dCrt('li').html("Your Name and POC information"))
                                                .append(dCrt('li').html("Your Office/Organizational Code (Please be as specific as possible.)"))
                                                .append(dCrt('li').html("Your Supervisor's Name and POC information")));
                                            r.append(dCrt('p').html("Once you have provided this information on the subsequent screen, please follow the provided links at the bottom of the page during Step 3 to download the required DD Form 2875 and the DISA Form 787 for electronic signatures. Also, please download the detailed registration process instruction document on the provided link. If you have any questions, please do not hesitate to email the RMK Team at the RMK Group Email here: <a href=\"mailto:" + _mailTo + "\">" + _mailTo + "</a>.  " +
                                                "In the Subject line of the email above, please use: <i>\"RMK - New Account - Your Name\"</i>. Using this tag line in the Email Subject line will help us identify your request on this matter and respond more quickly."));
                                            r.append(dCrt('p').html('Thank you.'));
                                            r.append(dCrt('p').html('The RMK Team'));

                                            return r;
                                        }
                                    }
                                ]                                
                            },
                            {
                                css: {padding: '10px 10px 10px 0'},
                                onValidate: function (node) {   
                                },
                                onReady: function (node) {
                                    node.inputValidator({text: [$('#organizationPosition'),$('#firstName'),$('#lastName')], phone: [$('#phone'),$('#svPhone')], extension: [$("#extension"),$("#svExtension")], email: [$("#email"), $("#supervisorEmail")]});
                                    var inputs = node.find('input');
                                    $.each(inputs, function(){
                                        var txt = $(this).val();
                                        if($.jCommon.string.empty(txt)){
                                            $(this).focus();
                                            return false;
                                        }
                                    });
                                },
                                nodes:[
                                    {
                                        node: 'raw',
                                        getNode: function(){
                                            return dCrt('p').html(
                                                'Please complete this form to register as an RMK user.  '
                                                + 'All information is subject to verification by your supervisor or an application administrator.'
                                            );
                                        }
                                    },
                                    {
                                        groupCss: {display: 'inline-block', marginRight: '5px'},
                                        focussed: true,
                                        node: 'input',
                                        type: 'text',
                                        required: true,
                                        id: 'firstName',
                                        label: "First Name",
                                        placeholder: 'Enter your first name.'
                                    },
                                    {
                                        groupCss: {display: 'inline-block', marginRight: '5px'},
                                        node: 'input',
                                        type: 'text',
                                        required: false,
                                        id: 'middleName',
                                        label: "Middle Name",
                                        placeholder: 'Enter your middle name.'
                                    },
                                    {
                                        groupCss: {display: 'inline-block'},
                                        node: 'input',
                                        type: 'text',
                                        required: true,
                                        id: 'lastName',
                                        label: "Last Name",
                                        placeholder: 'Enter your last name.'
                                    },
                                    {
                                        node: 'br'
                                    },
                                    {
                                        node: 'modal',
                                        type: 'text',
                                        placeholder: 'Click to select your organization...',
                                        modal: {
                                            name: "treeModal",
                                            plugin: {
                                                title: 'Select your Organization',
                                                header: "Select your Organization",
                                                name: 'treeView',
                                                exclusions:["Root Organizations"],
                                                rootSelectable: true,
                                                rootSelected: false,
                                                defaultSelectable: false,
                                                totals: false,
                                                disableLoadingEffect:true,
                                                expandable: true,
                                                expandRoot: true,
                                                counted: false,
                                                get: {
                                                    url: state.orgQuery.url,
                                                    rootQuery: function (data) {
                                                        return state.orgQuery.root();
                                                    },
                                                    childQuery: function (data) {
                                                        return state.orgQuery.children(data);
                                                    },
                                                    countQuery: function (data) {
                                                        return state.orgQuery.children(data);
                                                    }
                                                },
                                                mapper: {
                                                    id: state.KEY_ID,
                                                    uri: state.KEY_ID,
                                                    label: 'title'
                                                },
                                                limit: 10000,
                                                fade: false
                                            }
                                        },
                                        linked: true,
                                        id: 'organization',
                                        label: 'Organization',
                                        readOnly: true,
                                        required: true,
                                        onChanged: function (node, item) {
                                            node.removeClass("hasError").removeClass("success");
                                            var result = node.val();
                                            if(result && result.length>0) {
                                                node.addClass("success");
                                            }
                                            else{
                                                node.addClass("hasError")
                                            }
                                        }
                                    },
                                    {
                                        node: 'input',
                                        type: 'text',
                                        required: true,
                                        id: 'organizationPosition',
                                        label: "Job Title",
                                        placeholder: 'Enter your job title.'
                                    },
                                    {
                                        groupCss: {display: 'inline-block', marginRight: '5px'},
                                        node: 'input',
                                        type: 'tel',
                                        required: true,
                                        id: 'phone',
                                        label: "Phone Number",
                                        placeholder: 'Enter your phone number.'
                                    },
                                    {
                                        groupCss: {display: 'inline-block', marginRight: '5px'},
                                        node: 'input',
                                        type: 'text',
                                        id: 'extension',
                                        label: "Extension",
                                        placeholder: ''
                                    },
                                    {
                                        node: 'input',
                                        type: 'email',
                                        required: true,
                                        id: 'email',
                                        label: "Email Address",
                                        placeholder: 'Enter your email address.'
                                    },
                                    {
                                        node: 'br'
                                    },
                                    {
                                        node: 'input',
                                        type: 'email',
                                        required: true,
                                        id: 'supervisorEmail',
                                        label: "Supervisor's Email Address",
                                        placeholder: 'Enter your supervisor\'s email address.',
                                        afterNode: function(node, config){
                                            node.on('keydown', function (e) {
                                                var cr = e.which || e.keyCode;
                                                if(cr===13){
                                                    e.preventDefault();
                                                    e.stopPropagation();
                                                    var next = node.next('input');
                                                    next.focus();
                                                }
                                            });
                                            var last = null;
                                            node.on('blur', function () {
                                                var txt = node.val();
                                                if(last!==txt) {
                                                    $('#svLastName').val('').removeAttr('disabled');
                                                    $('#svFirstName').val('').removeAttr('disabled');
                                                    $('#svMiddleName').val('').removeAttr('disabled');
                                                    $('#svPhone').val('').removeAttr('disabled');
                                                    if (!txt) {
                                                        return false;
                                                    }
                                                    last = txt;
                                                    var s = function (data) {
                                                        if (data) {
                                                            if(data.lastName){
                                                                $('#svLastName').val(data.lastName).attr('disabled', 'disabled');
                                                            }
                                                            if(data.firstName){
                                                                $('#svFirstName').val(data.firstName).attr('disabled', 'disabled');
                                                            }
                                                            if(data.middleName){
                                                                $('#svMiddleName').val(data.middleName).attr('disabled', 'disabled');
                                                            }
                                                            if(data.phoneNumber){
                                                                $('#svPhone').focus();
                                                                $('#svPhone').val($.jCommon.string.toPhoneNumber(data.phoneNumber));
                                                                $('#svPhone').blur();
                                                            }
                                                        }
                                                    };
                                                    $.htmlEngine.request("/email/no_auth?email=" + txt, s, s, null, 'get', false);
                                                }
                                            });
                                        }
                                    },
                                    {
                                        groupCss: {display: 'inline-block', marginRight: '5px'},
                                        focussed: true,
                                        node: 'input',
                                        type: 'text',
                                        required: true,
                                        id: 'svFirstName',
                                        label: "Supervisor's First Name",
                                        placeholder: "Enter supervisor's first name."
                                    },
                                    {
                                        groupCss: {display: 'inline-block', marginRight: '5px'},
                                        node: 'input',
                                        type: 'text',
                                        required: false,
                                        id: 'svMiddleName',
                                        label: "Supervisor's Middle Name",
                                        placeholder: "Enter supervisor's middle name."
                                    },
                                    {
                                        groupCss: {display: 'inline-block'},
                                        node: 'input',
                                        type: 'text',
                                        required: true,
                                        id: 'svLastName',
                                        label: "Supervisor's Last Name",
                                        placeholder: "Enter supervisor's last name."
                                    },
                                    {
                                        node: 'br'
                                    },
                                    {
                                        groupCss: {display: 'inline-block', marginRight: '5px'},
                                        node: 'input',
                                        type: 'tel',
                                        required: true,
                                        id: 'svPhone',
                                        label: "Supervisor's Phone Number",
                                        placeholder: 'Enter supervisor\'s phone number.'
                                    },
                                    {
                                        groupCss: {display: 'inline-block', marginRight: '5px'},
                                        node: 'input',
                                        type: 'text',
                                        id: 'svExtension',
                                        label: "Supervisor's Extension",
                                        placeholder: ''
                                    },
                                    {
                                        node: 'br'
                                    }
                                ]
                            }
                        ],
                        getUrl: function () {
                            return state.opts.url;
                        }
                    });
                    
                    
                    if (state.opts.data) {
                        $.each(state.opts.data, function (key, value) {
                            var input = $('#' + key);
                            if (input.length > 0) {
                                input.val(value);
                            }
                        })
                    }
                },
                consent: function () {
                    var h ='<div class="info-block">' +
                        '                        <div style="font-size: 12px">' +
                        '                        <ul style="position: relative; top:-3px;">' +
                        '                            <li>The U.S. Government routinely intercepts and monitors communications on this information system for purposes including, but not limited to, penetration testing, communications security (COMSEC) monitoring, network operations and defense, personnel misconduct (PM), law enforcement (LE), and counterintelligence (CI) investigations. </li>' +
                        '                            <li>At any time, the U.S. Government may inspect and seize data stored on this information system. </li>' +
                        '                            <li>Communications using, or data stored on, this information system are not private, are subject to routine monitoring, interception, and search, and may be disclosed or used for any U.S. Government-authorized purpose.</li>' +
                        '                            <li>This information system includes security measures (e.g., authentication and access controls) to protect U.S. Government interests--not for your personal benefit or privacy.</li>' +
                        '                            <li>' +
                        '                                <div>Notwithstanding the above, using an information system does not constitute consent to personnel misconduct, law enforcement, or counterintelligence investigative searching or monitoring of the content of privileged communications or data (including work product) that are related to personal representation or services by attorneys, psychotherapists, or clergy, and their assistants. Under these circumstances, such communications and work product are private and confidential, as further explained below:</div>' +
                        '                                <ul style="position: relative; top:-3px;">' +
                        '                                    <li>Nothing in this User Agreement shall be interpreted to limit the user\'s consent to, or in any other way restrict or affect, any U.S. Government actions for purposes of network administration, operation, protection, or defense, or for communications security. This includes all communications and data on an information system, regardless of any applicable privilege or confidentiality.</li>' +
                        '                                    <li>The user consents to interception/capture and seizure of ALL communications and data for any authorized purpose (including personnel misconduct, law enforcement, or counterintelligence investigation). However, consent to interception/capture or seizure of communications and data is not consent to the use of privileged communications or data for personnel misconduct, law enforcement, or counterintelligence investigation against any party and does not negate any applicable privilege or confidentiality that otherwise applies.</li>' +
                        '                                    <li>Whether any particular communication or data qualifies for the protection of a privilege, or is covered by a duty of confidentiality, is determined in accordance with established legal standards and DoD policy. Users are strongly encouraged to seek personal legal counsel on such matters prior to using an information system if the user intends to rely on the protections of a privilege or confidentiality.</li>' +
                        '                                    <li>Users should take reasonable steps to identify such communications or data that the user asserts are protected by any such privilege or confidentiality. However, the user\'s identification or assertion of a privilege or confidentiality is not sufficient to create such protection where none exists under established legal standards and DoD policy.</li>' +
                        '                                    <li>A user\'s failure to take reasonable steps to identify such communications or data as privileged or confidential does not waive the privilege or confidentiality if such protections otherwise exist under established legal standards and DoD policy. However, in such cases the U.S. Government is authorized to take reasonable actions to identify such communication or data as being subject to a privilege or confidentiality, and such actions do not negate any applicable privilege or confidentiality.</li>' +
                        '                                    <li>These conditions preserve the confidentiality of the communication or data, and the legal protections regarding the use and disclosure of privileged information, and thus such communications and data are private and confidential. Further, the U.S. Government shall take all reasonable measures to protect the content of captured/seized privileged communications and data to ensure they are appropriately protected.</li>' +
                        '                                </ul>' +
                        '                            </li>' +
                        '                            <li>In cases when the user has consented to content searching or monitoring of communications or data for personnel misconduct, law enforcement, or counterintelligence investigative searching, (i.e., for all communications and data other than privileged communications or data that are related to personal representation or services by attorneys, psychotherapists, or clergy, and their assistants), the U.S. Government may, solely at its discretion and in accordance with DoD policy, elect to apply a privilege or other restriction on the U.S. Government\'s otherwise-authorized use or disclosure of such information.</li>' +
                        '                            <li>All of the above conditions apply regardless of whether the access or use of an information system includes the display of a Notice and Consent Banner ("banner"). When a banner is used, the banner functions to remind the user of the conditions that are set forth in this User Agreement, regardless of whether the banner describes these conditions in full detail or provides a summary of such conditions, and regardless of whether the banner expressly references this User Agreement."</li>' +
                        '                        </ul>' +
                        '                        </div>' +
                        '                    </div>';

                    var sc = dCrt('div').css({clear: 'both', maxHeight: '340px', height: '340px', display: 'block', marginTop: '0', overflowY: 'auto', overflowX: 'none'});
                   
                    var p = dCrt('p').append(h);
                    sc.append(p);
                    return sc;
                },
                s2: function () {

                },
                s3: function () {

                }
            },
            error: {
                navigate: function (data) {
                    window.location = "/notification?status=" + data.status;
                }
            },
            submit: function(){
                var inputs = state.find('input');
                var msg;
                var error=false;
                var data = {};
                $.each(inputs, function(){
                    var input = $(this);
                    var key = input.attr('id');
                    var val = input.val();
                    var required = input.attr(required);
                    error = (input.hasClass('hasError') || (required && $.jCommon.string.empty(val)));
                    if(error){
                        msg = key + ' is required.';
                        return false;
                    }
                    data[key] = val;
                });
                if(error){
                    lusidity.info.red(msg);
                }
                else{
                    state.children().hide();
                    state.loaders('show');
                    var onSuccess = function(data){
                        state.loaders('hide');
                        var item = data.item ? data.item : data;
                        if(!item.failed){
                            if(item.hasAccount){
                                methods.error.navigate({status: 401});
                            }
                            else{
                                lusidity.info.red(item.error + 'Click <a href="/register">here</a> to try again.');
                            }
                            lusidity.info.show(10);
                        }
                        else{
                            methods.error.navigate({status: 1});
                        }
                    };
                    var onFail = function(jqXHR, textStatus, errorThrown){
                        state.loaders('hide');
                        lusidity.info.red('Sorry, we failed to create the account.');
                    };
                    methods.ajax.post(data, onSuccess, onFail);
                }
            }
        };
        //public methods


        //environment: Initialize
        methods.init();
    };

    //Default Settings
    $.accountRegister.defaults = {};

    //Plugin Function
    $.fn.accountRegister = function(method, options) {
        if (method === undefined) method = {};

        if (typeof method === "object") {
            return this.each(function() {
                new $.accountRegister($(this),method);
            });
        } else {
            // Helper strings to quickly perform functions
            var $accountRegister = $(this).data('accountRegister');
            switch (method) {
                case 'some method': return 'some process';
                case 'state':
                default: return $accountRegister;
            }
        }
    };

})(jQuery);
