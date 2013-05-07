<%@page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8"%>

<%
response.setHeader("Cache-Control","no-cache");
response.setHeader("Pragma","no-cache");
response.setHeader("Expires","0");
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<c:set var="ctx" value="${pageContext['request'].contextPath}"/>

<!DOCTYPE HTML>
<html>
    <head>

        <title>Welcome to Spring Web MVC - Atmosphere Sample</title>

        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

		<script src="http://code.jquery.com/jquery-1.9.1.min.js"></script>
		<script src="http://ajax.microsoft.com/ajax/jquery.templates/beta1/jquery.tmpl.js"></script>
		<script src="https://raw.github.com/Atmosphere/atmosphere/master/modules/jquery/src/main/webapp/jquery/jquery.atmosphere.js"></script>
    </head>
    <body>
       	<div id="placeHolder">
       		Searching...
       	</div>
		
        <script id="template" type="text/x-jquery-tmpl">			
			\${mtGoxBidEUR} - \${mtGoxAskEUR} - \${btceBidEUR} - \${btceAskEUR}
			<br/>
        </script>

        <script type="text/javascript">
        	var socket = $.atmosphere;

            function handleAtmosphere( transport ) {
                var asyncHttpStatistics = {
                        transportType: 'N/A',
                        responseState: 'N/A',
                        numberOfCallbackInvocations: 0,
                        numberOfErrors: 0
                    };
                
                function refresh() {
                    console.log("Refreshing data tables...");
                    $('#responseState').html(asyncHttpStatistics.responseState);
                    $('#numberOfCallbackInvocations').html(asyncHttpStatistics.numberOfCallbackInvocations);
                    $('#numberOfErrors').html(asyncHttpStatistics.numberOfErrors);
                }
                var request = new $.atmosphere.AtmosphereRequest();
                request.transport = transport;
                request.url = "<c:url value='/feeds/howya'/>";
                request.contentType = "application/json";
                request.fallbackTransport = null;
                
                request.onMessage = function(response){
                    buildTemplate(response);
                };

                request.onMessagePublished = function(response){

                };

                request.onOpen = function() { $.atmosphere.log('info', ['socket open']); };
                request.onError =  function() { $.atmosphere.log('info', ['socket error']); };
                request.onReconnect =  function() { $.atmosphere.log('info', ['socket reconnect']); };

                var subSocket = socket.subscribe(request);
                
                function buildTemplate(response){
                	asyncHttpStatistics.numberOfCallbackInvocations++;
                    asyncHttpStatistics.transportType = response.transport;
                    asyncHttpStatistics.responseState = response.responseState;

                    $.atmosphere.log('info', ["response.state: " + response.state]);
                    $.atmosphere.log('info', ["response.transport: " + response.transport]);
                    $.atmosphere.log('info', ["response.responseBody: " + response.responseBody]);
                    
                    if(response.state = "messageReceived"){
                    
	                	var data = response.responseBody;
	
	                    if (data) {
	                        try {
	                            var result =  $.parseJSON(data);
	
	                            var visible = $('#placeHolder').is(':visible');
	
	                            

	                            $( "#template" ).tmpl( result ).prependTo( "#placeHolder");
	
	                        } catch (error) {
	                            asyncHttpStatistics.numberOfErrors++;
	                            console.log("An error ocurred: " + error);
	                        }
	                    } else {
	                        console.log("response.responseBody is null - ignoring.");
	                    }
	
	                	refresh();
                	}
                }
            }

            handleAtmosphere("websocket");
            
        </script>
    </body>
</html>
