package i5.las2peer.services.las2peerakg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.LinkedHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.nio.file.Paths;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import i5.las2peer.api.Context;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import i5.las2peer.api.logging.MonitoringEvent;
import io.swagger.annotations.SwaggerDefinition;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

// TODO Describe your own service
/**
 * las2peer-Template-Service
 * 
 * This is a template for a very basic las2peer service that uses the las2peer
 * WebConnector for RESTful access to it.
 * 
 * Note: If you plan on using Swagger you should adapt the information below in
 * the SwaggerDefinition annotation to suit your project. If you do not intend
 * to provide a Swagger documentation of your service API, the entire Api and
 * SwaggerDefinition annotation should be removed.
 * 
 */
// TODO Adjust the following configuration
@Api
@SwaggerDefinition(info = @Info(title = "las2peer akg Service", version = "1.0.0", description = "A las2peer Template Service for demonstration purposes.", termsOfService = "http://your-terms-of-service-url.com", contact = @Contact(name = "John Doe", url = "provider.com", email = "john.doe@provider.com"), license = @License(name = "your software license name", url = "http://your-software-license-url.com")))
@ServicePath("/akg")
// TODO Your own service class
public class akgService extends RESTService {

	@POST
	@Path("/compareWords")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME", notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response compareWords(String body) throws ParseException, IOException {
		System.out.println(body);
		JSONObject jsonBody = new JSONObject();
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		jsonBody = (JSONObject) p.parse(body);
		String userMail = jsonBody.getAsString("email");
		String entityName = jsonBody.getAsString("entityName");
		JSONObject entities = (JSONObject) jsonBody.get("entities");
		if (entities.get(entityName) == null) {
			// error, given entityname is not part of the recognized entities
			// return something
			System.out.println(entities);
			System.out.println(entityName);
			System.out.println(entities.get(entityName));
			jsonBody = new JSONObject();
			jsonBody.put("text",
					"Es gab ein Problem bei der Erkennung der Literatur, schreibe !exit um wieder von vorne zu beginnen :/");
			return Response.ok().entity(jsonBody).build();
		}
		System.out.println(entities);
		System.out.println(entityName);
		System.out.println(((JSONObject)entities.get(entityName)).getAsString("value"));
		String[] correctWords = jsonBody.getAsString(((JSONObject)entities.get(entityName)).getAsString("value")).split(",");
		String[] userWord = jsonBody.getAsString("msg").split(",");
		if (userWord.length != 5) {
			// return error that the number of examples is not right
			jsonBody = new JSONObject();
			jsonBody.put("text", "Bitte gib 5 Beispiele an ;)");
			jsonBody.put("closeContext", false);
			return Response.ok().entity(jsonBody).build();

		}
		int matches = 0;
		String answers = "";
		for (String word : userWord) {
			for (String correctWord : correctWords) {
				if (word.toLowerCase().replaceAll("\\s+", "").equals(correctWord.toLowerCase().replaceAll("\\s+", ""))) {
					matches++;
					if(answers.equals("")) {
						answers += correctWord.toLowerCase().replaceAll("\\s+", ""); 
					} else {
						answers += ", " + correctWord.toLowerCase().replaceAll("\\s+", "");
					}
				}
			}
		}

		// xapi nutzer vergleicht/compared terme + seminar thema 
		//monitor event : ntzer hat verglichen
		
		JSONObject actor = new JSONObject();
		actor.put("objectType", "Agent");
		JSONObject account = new JSONObject();

		account.put("name", encryptThisString(userMail));
		account.put("homePage", "https://chat.tech4comp.dbis.rwth-aachen.de");
		actor.put("account", account);
		// compared_words
		JSONObject verb = (JSONObject) p
				.parse(new String("{'display':{'en-US':'compared_words'},'id':'https://tech4comp.de/xapi/verb/compared_words'}"));
		JSONObject object = (JSONObject) p
				.parse(new String("{'definition':{'interactionType':'other', 'name':{'en-US':'" + "compareWords"
						+ "'}, 'description':{'en-US':'" + entities.get(entityName)
						+ "'}, 'type':'https://tech4comp.de/xapi/activitytype/compareWords'},'id':'https://tech4comp.de/biwi5/returnContent"
						+ encryptThisString(userMail) + "', 'objectType':'Activity'}"));
		try {
		JSONObject context = (JSONObject) p
				.parse(new String("{'extensions':{'https://tech4comp.de/xapi/context/extensions/filecontent':{'userAnswers':'"
						+ jsonBody.getAsString("msg") + "', 'word1':'"+ jsonBody.getAsString("msg").split(",")[0].toLowerCase().replaceAll("\\s","") +"', 'word2':'"+ jsonBody.getAsString("msg").split(",")[1].toLowerCase().replaceAll("\\s","") +"', 'word3':'"+ jsonBody.getAsString("msg").split(",")[2].toLowerCase().replaceAll("\\s","") +"', 'word4':'"+ jsonBody.getAsString("msg").split(",")[3].toLowerCase().replaceAll("\\s","") +"', 'word5':'"+ jsonBody.getAsString("msg").split(",")[4].toLowerCase().replaceAll("\\s","") +"','score':'"+ matches+"'}}}"));
		JSONObject xAPI = new JSONObject();

		xAPI.put("authority", p.parse(
				new String("{'objectType': 'Agent','name': 'New Client', 'mbox': 'mailto:hello@learninglocker.net'}")));
		xAPI.put("context", context); //
		// xAPI.put("timestamp", java.time.LocalDateTime.now());
		xAPI.put("actor", actor);
		xAPI.put("object", object);
		xAPI.put("verb", verb);
		
		
		Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_69, xAPI.toString() + "*" + jsonBody.getAsString("email"));
		Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_96, xAPI.toString());
		} catch(Exception e) {
			System.out.println("error?");
			e.printStackTrace();
		}

		jsonBody = new JSONObject();
		jsonBody.put("text", matches
				+ " von deinen assoziierten Begriffen sind auch Schl\u00FCsselkonzepte des Textes (" + answers + ").  \r\n"
				+ " Versuche sp\u00E4ter beim Lesen noch mehr Schl\u00FCsselbegriffe zu finden und reflektiere, wie du das Gelesene in dein bisheriges Wissen integrieren kannst und was f\u00FCr dich neu ist.  \r\n"
				+ " \r\n" + " M\u00F6chtest du noch weitere Assoziationen abgleichen?");
		return Response.ok().entity(jsonBody).build();
		// $X von deinen assoziierten Begriffen sind auch Schl??????????????????sselkonzepte des Textes.
		// \n Versuche sp??????????????????ter beim Lesen noch mehr Schl??????????????????sselbegriffe zu finden und
		// reflektiere, wie du das Gelesene in dein bisheriges Wissen integrieren kannst
		// und was f??????????????????r dich neu ist. \n\n M??????????????????chtest du noch weitere Assoziationen
		// abgleichen?
	}
	
	
	@POST
	@Path("/returnContent")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME", notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response returnContent(String body) throws ParseException, IOException {
		System.out.println(body);
		JSONObject jsonBody = new JSONObject();
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		jsonBody = (JSONObject) p.parse(body);
		String userMail = jsonBody.getAsString("email");
		String entityName = jsonBody.getAsString("entityName");
		JSONObject entities = (JSONObject) jsonBody.get("entities");
		if (entities.get(entityName) == null) {
			// error, given entityname is not part of the recognized entities
			// return something
			jsonBody = new JSONObject();
			jsonBody.put("text",
					"Es gab ein Problem bei der Erkennung der Literatur, schreibe !exit um wieder von vorne zu beginnen :/");
			return Response.ok().entity(jsonBody).build();
		}
		String content = jsonBody.getAsString(((JSONObject)entities.get(entityName)).getAsString("value"));
		// xapi nutzer vergleicht/compared terme + seminar thema 
		//monitor event : ntzer hat verglichen
		JSONObject actor = new JSONObject();
		actor.put("objectType", "Agent");
		JSONObject account = new JSONObject();

		account.put("name", encryptThisString(userMail));
		account.put("homePage", "https://chat.tech4comp.dbis.rwth-aachen.de");
		actor.put("account", account);
		// compared_words
		JSONObject verb = (JSONObject) p
				.parse(new String("{'display':{'en-US':'returnedContent'},'id':'https://tech4comp.de/xapi/verb/return_content'}"));
		JSONObject object = (JSONObject) p
				.parse(new String("{'definition':{'interactionType':'other', 'name':{'en-US':'" + "returnContent"
						+ "'}, 'description':{'en-US':'" + jsonBody.getAsString(entities.getAsString(entityName))
						+ "'}, 'type':'https://tech4comp.de/xapi/activitytype/compareWords'},'id':'https://tech4comp.de/biwi5/returnContent"
						+ encryptThisString(userMail) + "', 'objectType':'Activity'}"));
		JSONObject context = (JSONObject) p
				.parse(new String("{'extensions':{'https://tech4comp.de/xapi/context/extensions/filecontent':{'returnedContent':'"
						+ content + "'}}}"));
		JSONObject xAPI = new JSONObject();

		xAPI.put("authority", p.parse(
				new String("{'objectType': 'Agent','name': 'New Client', 'mbox': 'mailto:hello@learninglocker.net'}")));
		xAPI.put("context", context); //
		// xAPI.put("timestamp", java.time.LocalDateTime.now());
		xAPI.put("actor", actor);
		xAPI.put("object", object);
		xAPI.put("verb", verb);
		
		System.out.println(xAPI.toString());
		Context.get().monitorEvent(MonitoringEvent.SERVICE_CUSTOM_MESSAGE_69, xAPI.toString() + "*" + jsonBody.getAsString("email"));
		jsonBody = new JSONObject();
		jsonBody.put("text", content);
		return Response.ok().entity(jsonBody).build();

	}
	
	@POST
	@Path("/suggestMaterial")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME", notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "REPLACE THIS WITH YOUR OK MESSAGE") })
	public Response suggestMaterial(String body) throws ParseException, IOException {
		System.out.println(body);
		JSONObject jsonBody = new JSONObject();
		JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
		jsonBody = (JSONObject) p.parse(body);
		String entityName = jsonBody.getAsString("entityName");
		JSONObject entities = (JSONObject) jsonBody.get("entities");
		if (entities.get(entityName) == null) {
			// error, given entityname is not part of the recognized entities
			// return something
			jsonBody = new JSONObject();
			jsonBody.put("text",
					"Es gab ein Problem bei der Erkennung der Literatur, schreibe !exit um wieder von vorne zu beginnen :/");
			return Response.ok().entity(jsonBody).build();
		}
		entityName = entities.getAsString(entityName);
		String text = jsonBody.getAsString("msg");
		String[] words = text.split(",");
		MiniClient client = new MiniClient();
		// add var for address here
		client.setConnectorEndpoint("http://137.226.232.175:32303");
		HashMap<String, String> headers = new HashMap<String, String>();
		int counter = 0;
		String s = "";
		String graph = "";
		if(entityName.equals("Bildungssysteme")) {
			graph = "http://tech4comp.de/knowledgeMap/beffea78cb3db849bb00c36faded0e8e";
		} else if(entityName.equals("Inklusion in Kanada")) {
			graph = "http://tech4comp.de/knowledgeMap/9553a8dac552d16e9a4aa51de68541ae";
		} else if(entityName.equals("Deutschland und Kanada im Vergleich")) {
			graph = "http://tech4comp.de/knowledgeMap/28d0aafb7f9cda9420d2d1c55b32bc41";
		} else if(entityName.equals("Steuerung im Bildungswesen")) {
			graph = "http://tech4comp.de/knowledgeMap/dfe11f23419d21ee12f98044a26dc055";
		} else if(entityName.equals("Bildungswesen der BRD")) {
			graph = "http://tech4comp.de/knowledgeMap/ebe36f244610da368fcfd6cef4a5ec63";
		}else if(entityName.equals("Bildung")) {
			graph = "http://tech4comp.de/knowledgeMap/ul/biwi/laura";
		}else if(entityName.equals("Digitalisierung und Bildungswelt")) {
			graph = "http://tech4comp.de/knowledgeMap/e9758f807da573481a76927aff4cfbfa";
		}else if(entityName.equals("Erziehung")) {
			graph = "http://tech4comp.de/knowledgeMap/6a2f4e0eb47907c6b026600e566c6677";
		}else if(entityName.equals("F??hrungsstile")) {
			graph = "http://tech4comp.de/knowledgeMap/204d681f56e4c87232193794a457f603";
		}else if(entityName.equals("Sozialisation")) {
			graph= "http://tech4comp.de/knowledgeMap/72909bd4c7ae025d65351314455c638f";
		}else if(entityName.equals("Sozialisation und Medien")) {
			graph= "http://tech4comp.de/knowledgeMap/26b00db86194a83b0e18b7c2652cfeb6";
		}else if(entityName.equals("Bildungsgerechtigkeit")) {
			graph = "http://tech4comp.de/knowledgeMap/98dc514f500ab6de937f0c9c7736eb87";
		}else if(entityName.equals("Meritokratische Illusion")) {
			graph = "http://tech4comp.de/knowledgeMap/69214204b3bad1907f85b2e5debd5c0d";
		}else if(entityName.equals("PISA")) {
			graph = "http://tech4comp.de/knowledgeMap/004d7bd0b4cd76748598815569035e02";
		}else if(entityName.equals("PISA-Ergebnisse")) {
			graph = "http://tech4comp.de/knowledgeMap/38c740da987008ec6af185b70f75776f";
		}else if(entityName.equals("Inklusion und Bildungsgerechtigkeit")) {
			graph = "http://tech4comp.de/knowledgeMap/325ea363af0e79171ed4152c2aaee308";
		}else if(entityName.equals("Massnahmen zur Inklusion in Sachsen")) {
			graph = "http://tech4comp.de/knowledgeMap/fbb4a0a6099673ae0f5f666a3ea97c03";
		}else if(entityName.equals("Inklusiver Unterricht in Kanada")) {
			graph = "http://tech4comp.de/knowledgeMap/ca19296fab26fbf27c1689ff81b1c58e";
		}else if(entityName.equals("Kritik am kanadischen Bildungssystem")) {
			graph = "http://tech4comp.de/knowledgeMap/efa2782c5556dc4f345e35d41b2880a4";
		}else {
			graph= "noname";
		}
		for (int i = 0; i < words.length; i++) {
			JSONObject reqbody = new JSONObject();
			JSONArray terms = new JSONArray();
			terms.add(words[i].trim());
			reqbody.put("terms", terms);
			reqbody.put("graph", graph);
			ClientResponse r = client.sendRequest("POST", "materials", reqbody.toJSONString(),
					"application/json", "application/json", headers);
			JSONObject result = (JSONObject) p.parse(r.getResponse());
			System.out.println(result);
			if (result.keySet().size() > 1) {
				counter++;
				JSONArray materials = (JSONArray) result.get("@graph");
				for (Object j : materials) {
					JSONObject jo = (JSONObject) j;
					if(!jo.getAsString("title").equals("")) {
					s += "\\n" + words[i] + ": [" + jo.getAsString("title") + "]("
							+ jo.getAsString("link") + ")";
					System.out.println("Adding Material");
					}
				}
			}
			System.out.println("done with word" + i);
		}
		System.out.println("text is "+s);
		JSONObject jsonResponse = new JSONObject();
		if(s.equals("")) {
			jsonResponse.put("text", jsonBody.getAsString("missingMaterial"));
			System.out.println("Result empty");
		} else {
			jsonResponse.put("text", s);
			System.out.println("Result not empty");
		}
		return Response.ok().entity(jsonResponse).build();
	}

	

	
	public static String encryptThisString(String input) {
		try {
			// getInstance() method is called with algorithm SHA-384
			MessageDigest md = MessageDigest.getInstance("SHA-384");

			// digest() method is called
			// to calculate message digest of the input string
			// returned as array of byte
			byte[] messageDigest = md.digest(input.getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);

			// Add preceding 0s to make it 32 bit
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}

			// return the HashText
			return hashtext;
		}

		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
