/*
 * Sonar Message Flow Plugin
 * Copyright (C) 2015 Hendrik Scholz and EXXETA AG
 * http://www.exxeta.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exxeta.iss.sonar.msgflow.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The class parses the message flow files (.msgflow). The XML data is 
 * transformed into an internal message flow model. 
 * 
 * @author Hendrik Scholz (EXXETA AG)
 */
public class MessageFlowParser {
	
	/**
	 * The logger for the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MessageFlowParser.class);
	
	/**
	 * Parses the message flow file (.msgflow) and creates the message flow node model.
	 * 
	 * @param fileName the file name of the message flow file to be parsed
	 * @param collectorNodes the list of Collector Nodes to which the new message flow node should be added
	 * @param computeNodes the list of Compute Nodes to which the new message flow node should be added
	 * @param fileInputNodes the list of File Input Nodes to which the new message flow node should be added
	 * @param fileOutputNodes the list of File Output Nodes to which the new message flow node should be added
	 * @param httpInputNodes the list of Http Input Nodes to which the new message flow node should be added
	 * @param httpRequestNodes the list of Http Request Nodes to which the new message flow node should be added
	 * @param httpReplyNodes the list of Http Reply Nodes to which the new message flow node should be added
	 * @param mqInputNodes the list of MQ Input Nodes to which the new message flow node should be added
	 * @param mqOutputNodes the list of MQ Output Nodes to which the new message flow node should be added
	 * @param mqGetNodes the list of MQ Get Nodes to which the new message flow node should be added
	 * @param mqHeaderNodes the list of MQ Header Nodes to which the new message flow node should be added
	 * @param mqReplyNodes the list of MQ Reply Nodes to which the new message flow node should be added
	 * @param resetContentDescriptorNodes the list of Reset Content Descriptor Nodes to which the new message flow node should be added
	 * @param soapInputNodes the list of Soap Input Nodes to which the new message flow node should be added
	 * @param soapRequestNodes the list of Soap Request Nodes to which the new message flow node should be added
	 * @param timeoutControlNodes the list of Timeout Control Nodes to which the new message flow node should be added
	 * @param timeoutNotificationNodes the list of Timeout Notification Nodes to which the new message flow node should be added
	 * @param tryCatchNodes the list of Try Catch Nodes to which the new message flow node should be added
	 * @param imsRequestNodes the list of IMS Request Nodes to which the new message flow node should be added
	 * @param filterNodes the list of Filter Nodes to which the new message flow node should be added
	 * @param traceNodes the list of Trace Nodes to which the new message flow node should be added
	 * @param labelNodes the list of Label Nodes to which the new message flow node should be added
	 * @param routeToLabelNodes the list of Route To Label Nodes to which the new message flow node should be added
	 * @param aggregateControlNodes the list of aggregate Control Nodes to which the new message flow node should be added
	 * @param databaseNodes the list of database Nodes to which the new message flow node should be added
	 * @param miscellaneousNodes the list of miscellaneous/Uncategorized Nodes to which the new message flow node should be added
	 * @param connections the list of all the connections for the message flow
	 * @param comments the list of all the comment notes for the message flow
	 * @param shortDescription the short description of the message flow (Using StringBuilder as String is immutable)
	 * @param longDescription the long description of the message flow (Using StringBuilder as String is immutable)
	 */
	public void parse(String fileName,
					  ArrayList<MessageFlowNode> collectorNodes,
					  ArrayList<MessageFlowNode> computeNodes,
					  ArrayList<MessageFlowNode> fileInputNodes,
					  ArrayList<MessageFlowNode> fileOutputNodes,
					  ArrayList<MessageFlowNode> httpInputNodes,
					  ArrayList<MessageFlowNode> httpRequestNodes,
					  ArrayList<MessageFlowNode> httpReplyNodes,
					  ArrayList<MessageFlowNode> mqInputNodes,
					  ArrayList<MessageFlowNode> mqOutputNodes,
					  ArrayList<MessageFlowNode> mqGetNodes,
					  ArrayList<MessageFlowNode> mqHeaderNodes,
					  ArrayList<MessageFlowNode> mqReplyNodes,
					  ArrayList<MessageFlowNode> resetContentDescriptorNodes,
					  ArrayList<MessageFlowNode> soapInputNodes,
					  ArrayList<MessageFlowNode> soapRequestNodes,
					  ArrayList<MessageFlowNode> timeoutControlNodes,
					  ArrayList<MessageFlowNode> timeoutNotificationNodes,
					  ArrayList<MessageFlowNode> tryCatchNodes,
					  ArrayList<MessageFlowNode> imsRequestNodes,
					  ArrayList<MessageFlowNode> filterNodes,
					  ArrayList<MessageFlowNode> traceNodes,
					  ArrayList<MessageFlowNode> labelNodes,
					  ArrayList<MessageFlowNode> routeToLabelNodes,
					  ArrayList<MessageFlowNode> aggregateControlNodes,
					  ArrayList<MessageFlowNode> databaseNodes,
					  ArrayList<MessageFlowNode> routeNodes,
					  ArrayList<MessageFlowNode> miscellaneousNodes,
					  ArrayList<MessageFlowConnection> connections,
					  ArrayList<MessageFlowCommentNote> comments,
					  StringBuilder shortDescription,
					  StringBuilder longDescription) {
		LOG.debug("START");

		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileName);
		
			XPathExpression numberOfNodes = XPathFactory.newInstance().newXPath().compile("count(//nodes)");
			int non = Integer.parseInt((String)numberOfNodes.evaluate(document, XPathConstants.STRING));
			
			for (; non > 0; non--) {
				LOG.debug("Prepare expressions - START");
				
				XPathExpression idExpr								= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/@id");
				XPathExpression nameExpr							= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/translation/@string");
				XPathExpression typeExpr							= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/@type");
				XPathExpression buildTreeUsingSchemaExpr			= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/@parserXmlnscBuildTreeUsingXMLSchema");
				XPathExpression mixedContentRetainModeExpr			= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/@parserXmlnscMixedContentRetainMode");
				XPathExpression commentsRetainModeExpr				= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/@parserXmlnscCommentsRetainMode");
				XPathExpression validateMasterExpr					= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/@validateMaster");
				XPathExpression messageDomainPropertyExpr			= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/@messageDomainProperty");
				XPathExpression messageSetPropertyExpr				= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/@messageSetProperty");
				XPathExpression requestMsgLocationInTreeExpr		= XPathFactory.newInstance().newXPath().compile("//nodes[" + non + "]/@requestMsgLocationInTree");

				XPathExpression messageDomainExpr					= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/@messageDomain");
				XPathExpression messageSetExpr						= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/@messageSet");
				XPathExpression recordDefinitionExpr				= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/@recordDefinition");
				XPathExpression resetMessageDomainExpr				= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/@resetMessageDomain");
				XPathExpression resetMessageSetExpr					= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/@resetMessageSet");
				XPathExpression resetMessageTypeExpr				= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/@resetMessageType");
				XPathExpression resetMessageFormatExpr				= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/@resetMessageFormat");
				XPathExpression monitoringEventsExpr				= XPathFactory.newInstance().newXPath().compile("count(//nodes[" + non +  "]/monitorEvents)");
				XPathExpression monitoringEventsEventEnabledExpr	= XPathFactory.newInstance().newXPath().compile("//nodes[" + non +  "]/monitorEvents/@eventEnabled");

				LOG.debug("Prepare expressions - END");
				LOG.debug("Evaluate expressions - START");

				String id						= (String)idExpr.evaluate(document, XPathConstants.STRING);
				String name						= (String)nameExpr.evaluate(document, XPathConstants.STRING);
				String type						= (String)typeExpr.evaluate(document, XPathConstants.STRING);
				
				LOG.debug("id: " + id);
				LOG.debug("name: " + name);
				LOG.debug("type: " + type);
//	commenting the below code to add subflow nodes as a part of Miscellaneous nodelist
//				if (type.contains("ComIbm") == false) {
//					/* if the node is not a ComIbm node */
//					LOG.debug("omitted node of type " + type);
//					continue;
//				}

				String messageDomainProperty	= (String)messageDomainPropertyExpr.evaluate(document, XPathConstants.STRING);
				String messageSetProperty		= (String)messageSetPropertyExpr.evaluate(document, XPathConstants.STRING);
				String requestMsgLocationInTree	= (String)requestMsgLocationInTreeExpr.evaluate(document, XPathConstants.STRING);
				String messageDomain			= (String)messageDomainExpr.evaluate(document, XPathConstants.STRING);
				String messageSet				= (String)messageSetExpr.evaluate(document, XPathConstants.STRING);
				String recordDefinition			= (String)recordDefinitionExpr.evaluate(document, XPathConstants.STRING);
				//added condition to store subflow types with the extention
				if (type.contains("ComIbm")) {
					type 						= type.substring(0, type.indexOf(".")).replace("ComIbm", "");
				}else{
					type 						= type.substring(0, type.indexOf(":"));
				}
				boolean buildTreeUsingSchema 	= Boolean.parseBoolean((String)buildTreeUsingSchemaExpr.evaluate(document, XPathConstants.STRING));
				boolean mixedContentRetainMode	= ((String)mixedContentRetainModeExpr.evaluate(document, XPathConstants.STRING)).equals("all");
				boolean commentsRetainMode		= ((String)commentsRetainModeExpr.evaluate(document, XPathConstants.STRING)).equals("all");
				boolean validateMaster			= ((String)validateMasterExpr.evaluate(document, XPathConstants.STRING)).equals("contentAndValue");
				boolean resetMessageDomain	 	= Boolean.parseBoolean((String)resetMessageDomainExpr.evaluate(document, XPathConstants.STRING));
				boolean resetMessageSet 		= Boolean.parseBoolean((String)resetMessageSetExpr.evaluate(document, XPathConstants.STRING));
				boolean resetMessageType 		= Boolean.parseBoolean((String)resetMessageTypeExpr.evaluate(document, XPathConstants.STRING));
				boolean resetMessageFormat 		= Boolean.parseBoolean((String)resetMessageFormatExpr.evaluate(document, XPathConstants.STRING));
				
				int monitoringEvents				= Integer.parseInt((String)monitoringEventsExpr.evaluate(document, XPathConstants.STRING));
				String monitoringEventsEventEnabled	= (String)monitoringEventsEventEnabledExpr.evaluate(document, XPathConstants.STRING);
				boolean areMonitoringEventsEnabled	= true;
				
				/* 
				 * monitoring events are enabled unless defined otherwise
				 * 
				 * - monitoring events are missing
				 * - existing monitoring events are disabled 
				 */
				if (monitoringEvents == 0 ||
					monitoringEventsEventEnabled.equals("false")) {
					areMonitoringEventsEnabled = false;
				}
				
				XPathExpression numberOfInputTerminals = XPathFactory.newInstance().newXPath().compile("count(//connections[@targetNode='" + id + "'])");
				int noit = Integer.parseInt((String)numberOfInputTerminals.evaluate(document, XPathConstants.STRING));

				XPathExpression numberOfOutputTerminals = XPathFactory.newInstance().newXPath().compile("count(//connections[@sourceNode='" + id + "'])");
				int noot = Integer.parseInt((String)numberOfOutputTerminals.evaluate(document, XPathConstants.STRING));
				
				ArrayList<String> inputTerminals = new ArrayList<String>();
				ArrayList<String> outputTerminals = new ArrayList<String>();
				
				for (; noit > 0; noit--) {
					XPathExpression inputTerminalExpr = XPathFactory.newInstance().newXPath().compile("//connections[@targetNode='" + id + "'][" + noit + "]/@targetTerminalName");
					inputTerminals.add(((String)inputTerminalExpr.evaluate(document, XPathConstants.STRING)));
				}
				
				for (; noot > 0; noot--) {
					XPathExpression outputTerminalExpr = XPathFactory.newInstance().newXPath().compile("//connections[@sourceNode='" + id + "'][" + noot + "]/@sourceTerminalName");
					outputTerminals.add(((String)outputTerminalExpr.evaluate(document, XPathConstants.STRING)));
				}
				/**
				 * Added to extract the values of the node specific properties and the values  
				 */
				Map<String, Object> properties = new HashMap<String, Object>();
				if(type.equals("MQInput")||type.equals("MQOutput")||type.equals("MQGet")||type.equals("MQReply")){
					if(type.equals("MQInput")||type.equals("MQOutput")||type.equals("MQGet")){
						XPathExpression queueNameExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@queueName");
						String queueName = (String) queueNameExp.evaluate(document,XPathConstants.STRING);
					
						properties.put("queueName",queueName);
					}
					
					XPathExpression txnModeExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@transactionMode");
					String txnMode = (String) txnModeExp.evaluate(document,XPathConstants.STRING);
				
					properties.put("transactionMode",txnMode);
				}
				else if (type.equals("IMSRequest")) {
					XPathExpression shortDescriptionIMSExp		= XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/shortDescription/@string");
					String shortDescriptionIMS = (String) shortDescriptionIMSExp.evaluate(document,XPathConstants.STRING);
					properties.put("shortDescription", shortDescriptionIMS);
					
					XPathExpression longDescriptionIMSExp		= XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/longDescription/@string");
					String longDescriptionIMS = (String) longDescriptionIMSExp.evaluate(document,XPathConstants.STRING);
					properties.put("longDescription", longDescriptionIMS);
					
					XPathExpression useNodePropertiesExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@useNodeProperties");
					String useNodeProperties = (String) useNodePropertiesExp.evaluate(document,XPathConstants.STRING);
					properties.put("useNodeProperties", useNodeProperties);
					
					XPathExpression configurableServiceExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@configurableService");
					String configurableService = (String) configurableServiceExp.evaluate(document,XPathConstants.STRING);
					properties.put("configurableService", configurableService);
					
					XPathExpression commitModeExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@commitMode");
					String commitMode = (String) commitModeExp.evaluate(document,XPathConstants.STRING);
					properties.put("commitMode", commitMode);
					
				}
				else if(type.equals("WSReply")) {
					XPathExpression ignoreTransportFailuresExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@ignoreTransportFailures");
					String ignoreTransportFailures = (String) ignoreTransportFailuresExp.evaluate(document,XPathConstants.STRING);
					properties.put("ignoreTransportFailures", ignoreTransportFailures);
					XPathExpression generateDefaultHttpHeadersExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@generateDefaultHttpHeaders");
					String generateDefaultHttpHeaders = (String) generateDefaultHttpHeadersExp.evaluate(document,XPathConstants.STRING);
					properties.put("generateDefaultHttpHeaders", generateDefaultHttpHeaders);
				}
				else if(type.equals("SOAPRequest")){					
					XPathExpression requestTimeoutExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@requestTimeout");
					String requestTimeout = (String) requestTimeoutExp.evaluate(document,XPathConstants.STRING);
					properties.put("requestTimeout", requestTimeout);
				}
				else if(type.equals("AggregateControl")){
					XPathExpression timeoutIntervalExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@timeoutInterval");
					String timeoutInterval = (String) timeoutIntervalExp.evaluate(document,XPathConstants.STRING);
					properties.put("timeoutInterval", timeoutInterval);
				}
				else if(type.equals("Compute")){
					XPathExpression computeExpressionExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@computeExpression");
					String computeExpression = (String) computeExpressionExp.evaluate(document,XPathConstants.STRING);
					computeExpression = computeExpression.substring(computeExpression.indexOf("#")+1, computeExpression.indexOf(".Main"));
					properties.put("computeExpression", computeExpression);
					
					String computeExpressionFull = (String) computeExpressionExp.evaluate(document,XPathConstants.STRING);
					properties.put("computeExpressionFull", computeExpressionFull);
					
					XPathExpression dataSourceExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@dataSource");
					String dataSource = (String) dataSourceExp.evaluate(document,XPathConstants.STRING);
					properties.put("dataSource", dataSource);
					
				}
				else if(type.equals("Filter")){
					XPathExpression filterExpressionExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@filterExpression");
					String filterExpression = (String) filterExpressionExp.evaluate(document,XPathConstants.STRING);
					filterExpression = filterExpression.substring(filterExpression.indexOf("#")+1, filterExpression.indexOf(".Main"));
					properties.put("filterExpression", filterExpression);
				}
				else if(type.equals("Database")){
					XPathExpression statementExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/@statement");
					String statement = (String) statementExp.evaluate(document,XPathConstants.STRING);
					statement = statement.substring(statement.indexOf("#")+1, statement.indexOf(".Main"));
					properties.put("statement", statement);
				}
				else if(type.equals("Route")){
					XPathExpression filterTableCountExp = XPathFactory.newInstance().newXPath().compile("count(//nodes[@id='"+id+"']/filterTable)");
					int nof = Integer.parseInt((String) filterTableCountExp.evaluate(document,XPathConstants.STRING));
					ArrayList<String> routeTable = new ArrayList<String>();
//					System.out.println(noFil);
					for(; nof > 0 ; nof-- ){
						XPathExpression outTerminalExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+id+"']/filterTable["+ nof +"]/@routingOutputTerminal");
						String outTerminal = (String)outTerminalExp.evaluate(document,XPathConstants.STRING);
						routeTable.add(outTerminal);
					}
					properties.put("routeTerminals", routeTable);
				}
				
				if (type.equals("MQInput") || type.equals("FileInput") || type.equals("WSInput")
						|| type.equals("SOAPInput")) {
					XPathExpression componentLevelExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='" + id + "']/@componentLevel");
					String componentLevel = (String) componentLevelExp.evaluate(document, XPathConstants.STRING);
					properties.put("componentLevel", componentLevel);

					XPathExpression additionalInstancesExp = XPathFactory.newInstance().newXPath().compile("//nodes[@id='" + id + "']/@additionalInstances");
					String additionalInstances = (String) additionalInstancesExp.evaluate(document, XPathConstants.STRING);
					properties.put("additionalInstances", additionalInstances);
				}
				LOG.debug("Evaluate expressions - END");
				LOG.debug("Fill nodes - START");

				/* create new MessageFlowNode using values extracted from msgflow file */
				MessageFlowNode mfn = new MessageFlowNode(id, name, type, buildTreeUsingSchema, mixedContentRetainMode, commentsRetainMode, validateMaster, messageDomainProperty, messageSetProperty, requestMsgLocationInTree, messageDomain, messageSet, recordDefinition, resetMessageDomain, resetMessageSet, resetMessageType, resetMessageFormat, areMonitoringEventsEnabled, inputTerminals, outputTerminals,properties);
				
				if (type.equals("Collector")) {
					/* Collector */
					LOG.debug("Collector");

					collectorNodes.add(mfn);
				} else if (type.equals("Compute")) {
					/* Compute */
					LOG.debug("Compute");
					
					computeNodes.add(mfn);
				} else if (type.equals("FileInput")) {
					LOG.debug("FileInput");
					
					/* FileInput */
					fileInputNodes.add(mfn);
				} else if (type.equals("FileOutput")) {
					LOG.debug("FileOutput");
					
					/* FileOutput */
					fileOutputNodes.add(mfn);
				} else if (type.equals("WSInput")) {
					LOG.debug("WSInput");
					
					/* HTTPInput */
					httpInputNodes.add(mfn);
				} else if (type.equals("WSRequest")) {
					LOG.debug("WSRequest");
					
					/* HTTPRequest */
					httpRequestNodes.add(mfn);
				}else if (type.equals("WSReply")) {
					LOG.debug("WSReply");
					
					/* HTTPReply */
					httpReplyNodes.add(mfn);
				} else if (type.equals("MQInput")) {
					LOG.debug("MQInput");
					
					/* MQInput */
					mqInputNodes.add(mfn);
				} else if (type.equals("MQOutput")) {
					LOG.debug("MQOutput");
					
					/* MQOutput */
					mqOutputNodes.add(mfn);
				} else if (type.equals("MQGet")) {
					LOG.debug("MQGet");
					
					/* MQGet */
					mqGetNodes.add(mfn);
				} else if (type.equals("MQHeader")) {
					LOG.debug("MQHeader");
					
					/* MQHeader */
					mqHeaderNodes.add(mfn);
				} else if (type.equals("MQReply")) {
					LOG.debug("MQReply");
					
					/* MQReply */
					mqReplyNodes.add(mfn);
				} else if (type.equals("ResetContentDescriptor")) {
					LOG.debug("ResetContentDescriptor");
					
					/* ResetContentDescriptor */
					resetContentDescriptorNodes.add(mfn);
				} else if (type.equals("SOAPInput")) {
					LOG.debug("SOAPInput");
					
					/* SOAPInput */
					soapInputNodes.add(mfn);
				} else if (type.equals("SOAPRequest")) {
					LOG.debug("SOAPRequest");
					
					/* SOAPRequest */
					soapRequestNodes.add(mfn);
				} else if (type.equals("TimeoutControl")) {
					LOG.debug("TimeoutControl");
					
					/* TimeoutControl */
					timeoutControlNodes.add(mfn);
				} else if (type.equals("TimeoutNotification")) {
					LOG.debug("TimeoutNotification");
					
					/* TimeoutNotification */
					timeoutNotificationNodes.add(mfn);
				} else if (type.equals("TryCatch")) {
					LOG.debug("TryCatch");
					
					/* TryCatch */
					tryCatchNodes.add(mfn);
				} else if (type.equals("IMSRequest")) {
					LOG.debug("IMSRequest");
					
					/* IMS Request */
					imsRequestNodes.add(mfn);
				} else if (type.equals("Filter")) {
					LOG.debug("Filter");
					
					/* Filter */
					filterNodes.add(mfn);
				} else if (type.equals("Trace")) {
					LOG.debug("Trace");
					
					/* Trace */
					traceNodes.add(mfn);
				} else if (type.equals("Label")) {
					LOG.debug("Label");
					
					/* Label */
					labelNodes.add(mfn);
				} else if (type.equals("RouteToLabel")) {
					LOG.debug("RouteToLabel");
					
					/* RouteToLabel */
					routeToLabelNodes.add(mfn);
				} else if (type.equals("AggregateControl")) {
					LOG.debug("AggregateControl");

					/* AggregateControl */
					aggregateControlNodes.add(mfn);
				} else if (type.equals("Database")) {
					LOG.debug("Database");
					
					/* Database */
					databaseNodes.add(mfn);
				} else if(type.equals("Route")){
					LOG.debug("Route");
					
					/*routeNodes*/
					routeNodes.add(mfn);
				} else {
					LOG.debug("Miscellaneous");
					
					/* Miscellaneous */
					miscellaneousNodes.add(mfn);
				}
				
				LOG.debug("Fill nodes - END");
			}
			/**
			 * Added the below snippet to get short and long description of the message flow
			 */
			XPathExpression shortDescriptionExp = XPathFactory.newInstance().newXPath().compile("//eClassifiers/shortDescription/@string");
			XPathExpression longDescriptionExp = XPathFactory.newInstance().newXPath().compile("//eClassifiers/longDescription/@string");
			shortDescription.delete(0, shortDescription.length());
			shortDescription.append((String) shortDescriptionExp.evaluate(document,XPathConstants.STRING));
			longDescription.delete(0, longDescription.length());
			longDescription.append((String) longDescriptionExp.evaluate(document,XPathConstants.STRING));
			
			/**
			 * Added to identify all the connections for the message flow change starts
			 */
			XPathExpression numberOfConnections = XPathFactory.newInstance().newXPath().compile("count(//connections)");
			int noc = Integer.parseInt((String)numberOfConnections.evaluate(document, XPathConstants.STRING));
			
			for (; noc > 0; noc--) {
				
				XPathExpression srcNodeExp			= XPathFactory.newInstance().newXPath().compile("//connections[" + noc +  "]/@sourceNode");
				XPathExpression targetNodeExp		= XPathFactory.newInstance().newXPath().compile("//connections[" + noc +  "]/@targetNode");
				XPathExpression srcTeminalExp		= XPathFactory.newInstance().newXPath().compile("//connections[" + noc +  "]/@sourceTerminalName");
				XPathExpression targetTerminalExp	= XPathFactory.newInstance().newXPath().compile("//connections[" + noc +  "]/@targetTerminalName");
				
				String srcNode 			= (String)srcNodeExp.evaluate(document, XPathConstants.STRING);
				String targetNode 		= (String)targetNodeExp.evaluate(document, XPathConstants.STRING);
				String srcTerminal 		= (String)srcTeminalExp.evaluate(document, XPathConstants.STRING);
				String targetTerminal 	= (String)targetTerminalExp.evaluate(document, XPathConstants.STRING);

				XPathExpression srcNodeNameExp		= XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+srcNode+"']/translation/@string");
				XPathExpression targetNodeNameExp	= XPathFactory.newInstance().newXPath().compile("//nodes[@id='"+targetNode+"']/translation/@string");
				
				String srcNodeName 		= (String)srcNodeNameExp.evaluate(document, XPathConstants.STRING);
				String targetNodeName 	= (String)targetNodeNameExp.evaluate(document, XPathConstants.STRING);
				
				MessageFlowConnection conection = new MessageFlowConnection(srcNode,srcNodeName,targetNode,targetNodeName,srcTerminal,targetTerminal);
				connections.add(conection);
			}
			
			/**
			 * Added to identify the comment notes and the contents of it for the message flow
			 */
			XPathExpression numberOfStickyNotes = XPathFactory.newInstance().newXPath().compile("count(//stickyNote)");
			int nos = Integer.parseInt((String)numberOfStickyNotes.evaluate(document, XPathConstants.STRING));
			
			for (; nos > 0; nos--) {
				XPathExpression associationExp =  XPathFactory.newInstance().newXPath().compile("//stickyNote[" + nos +  "]/@association");
				XPathExpression commentExp =  XPathFactory.newInstance().newXPath().compile("//stickyNote[" + nos +  "]/body/@string");
				XPathExpression locationExp =  XPathFactory.newInstance().newXPath().compile("//stickyNote[" + nos +  "]/@location");
				String associationList = (String) associationExp.evaluate(document,XPathConstants.STRING);
				ArrayList<String> association = new ArrayList<String>();
				for(String nodeId : associationList.split(" ")) {
					association.add(nodeId);
				}
				String comment = (String)commentExp.evaluate(document,XPathConstants.STRING);
				int locationX = Integer.parseInt(((String)locationExp.evaluate(document, XPathConstants.STRING)).split(",")[0]);
				int locationY = Integer.parseInt(((String)locationExp.evaluate(document, XPathConstants.STRING)).split(",")[1]);
				MessageFlowCommentNote msgFlowComment = new MessageFlowCommentNote(association, comment, locationX, locationY);
				comments.add(msgFlowComment);
			}
			
			/**
			 * Changes ends 
			 * */
			
		} catch (XPathExpressionException e) {
			LOG.error(e.getMessage());
		} catch (SAXException e) {
			LOG.error(e.getMessage());
		} catch (ParserConfigurationException e) {
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		
		LOG.debug("END");
	}
}
