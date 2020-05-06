package com.swd.nycfood.outlets;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.mysql.cj.xdevapi.Client;
import com.mysql.cj.xdevapi.ClientFactory;
import com.mysql.cj.xdevapi.Collection;
import com.mysql.cj.xdevapi.DbDoc;
import com.mysql.cj.xdevapi.DocResult;
import com.mysql.cj.xdevapi.JsonParser;
import com.mysql.cj.xdevapi.Result;
import com.mysql.cj.xdevapi.Session;

@RestController
public class OutletsController {
	
	private static final String SCHEMA = "nycfood";
	private static final String COLLECTION = "outlets";
	private Gson mapper = new Gson();
	private Client cli = null;
		
	OutletsController() {
		String cnxUrl = "mysqlx://localhost:33060/nycfood?user=root&password=Simp50n5!";
		String pool = "{\"pooling\": {\"enabled\":true, \"maxSize\":25, \"maxIdleTime\":30000, \"queueTimeout\":10000}}";
		cli = new ClientFactory().getClient(cnxUrl,pool);
	}
		
	/**
	 * Returns an array of JSON documents : [{ borough: value }, ...]. Each Document is unique and sorted alphabetically on borough value
	 */
	@GetMapping("/nycfood/boroughs")
	ResponseEntity<String> getBoroughs() {
		Session sess = cli.getSession();
		Collection col = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		DocResult dr = col.find().fields("borough AS borough").groupBy("borough").sort("borough").execute();
		String boroughs = dr.fetchAll().toString();
		sess.close();
		return new ResponseEntity<>(boroughs,HttpStatus.OK);
	}
	
	/**
	 * Returns an alphabetically sorted array of cuisines: [cuisine1, cuisine2, ...]
	 */
	@GetMapping("/nycfood/cuisines")
	ResponseEntity<List<String>> getCuisines() {
		List<String> cuisineList = new ArrayList<>();
		Session sess = cli.getSession();
		Collection col = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		DocResult dr = col.find().fields("cuisine AS cuisine").groupBy("cuisine").sort("cuisine").sort("cuisine").execute();
		dr.forEach(dbDoc -> cuisineList.add(mapper.fromJson(dbDoc.get("cuisine").toString(),String.class)));
		sess.close();
		return new ResponseEntity<>(cuisineList,HttpStatus.OK);
	}
	
	/**
	 * Returns an array of JSON documents [ { _id: value, name: value, borough: value cuisine: value }, ... ].
	 * Sorted alphabetically on borough, cuisine and name (in that order)
	 */
	@GetMapping("/nycfood/outlets")
	ResponseEntity<String> getOutlets() {
		Session sess = cli.getSession();
		Collection col = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		DocResult dr = col.find().fields("_id AS _id", "name AS name", "cuisine AS cuisine", "borough AS borough").sort("borough","cuisine","name").execute();
		String outlets = dr.fetchAll().toString();
		sess.close();
		return new ResponseEntity<>(outlets,HttpStatus.OK);
	}
	
	/**
	 * Returns the full listing for the outlet identified by id
	 * If outlet cannot be found then a Document (JSON formatted String) with an error message detailing this will be returned
	 */
	@GetMapping("/nycfood/outlet/{id}")
	ResponseEntity<String> getOutlet(@PathVariable String id) {
		Session sess = cli.getSession();
		Collection col = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		DocResult dr = col.find("_id = :param").bind("param",id).execute();
		if (dr.count() == 0) {  
			sess.close();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No matching document for id on path.");
		}
		String po = dr.fetchOne().toString();
		sess.close();
		return new ResponseEntity<>(po,HttpStatus.OK);
	}
	
	/**
	 * Deletes the outlet identified by id and returns a Result Document detailing success
	 * If outlet cannot be found then a Document (JSON formatted String) with an error message detailing this will be returned
	 */	
	@DeleteMapping("/nycfood/outlet/{id}")
	ResponseEntity<Result> deleteOutlet(@PathVariable String id) {
		Session sess = cli.getSession();
		Collection col = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		Result result = col.remove("_id = :param").bind("param",id).execute();
		sess.close();
		if (result.getAffectedItemsCount() == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No matching document for id on path.");
		}
		return new ResponseEntity<>(result,HttpStatus.OK);
	}

	/**
	 * Grades the outlet identified by id with the values provided in newGrade and returns a Result Document detailing success
	 * If outlet cannot be found then a Document (JSON formatted String) with an error message detailing this will be returned
	 */	
	@PatchMapping("/nycfood/outlet/{id}")
	ResponseEntity<Result> gradeOutlet(@RequestBody Grade newGrade, @PathVariable String id) {
		DbDoc grade = JsonParser.parseDoc(mapper.toJson(newGrade));
		Session sess = cli.getSession();
		Collection col = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		Result result = col.modify("_id = :param").arrayInsert(".grades[0]",grade).bind("param",id).execute();
		sess.close();
		if (result.getAffectedItemsCount() == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No matching document for id on path.");
		}
		return new ResponseEntity<>(result,HttpStatus.OK);
	}
	
	/**
	 * Replaces the outlet identified by id with the values in replacement and returns a Result Document detailing success
	 * If outlet cannot be found then a Document (JSON formatted String) with an error message detailing this will be returned
	 */	
	@PutMapping("/nycfood/outlet/{id}")
	ResponseEntity<Result> replaceOutlet(@RequestBody Outlet replacement, @PathVariable String id) {
		Session sess = cli.getSession();
		Collection col = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		Result result = col.replaceOne(id,mapper.toJson(replacement));
		sess.close();
		if (result.getAffectedItemsCount() == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No matching document for id on path.");
		}
		return new ResponseEntity<>(result,HttpStatus.OK);
	}
	
	/**
	 * Creates and persists an Outlet Document with the values provided by newOutlet and returns a Result Document detailing success
	 * If outlet cannot be stored then a Document (JSON formatted String) with an error message detailing this will be returned
	 */	
	@PostMapping("/nycfood/outlet")
	ResponseEntity<Result> createOutlet(@RequestBody Outlet newOutlet) {
		Session sess = cli.getSession();
		Collection outlets = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		Result result = outlets.add(mapper.toJson(newOutlet)).execute();
		sess.close();
		if (result.getAffectedItemsCount() != 1) {
			throw new ResourceAccessException("Cannot store outlet.");
		}
		return new ResponseEntity<>(result,HttpStatus.OK);
	}
	
	/**
	 * For testing... To be deleted
	 */
	@GetMapping("nycfood/test")
	ResponseEntity<String> getStuff() {
		Session sess = cli.getSession();
		Collection col = sess.getSchema(SCHEMA).getCollection(COLLECTION);
		DocResult dr = col.find("borough LIKE 'Manhat%' AND grades[0].score > 50").fields("name AS name", "address as address", "grades[0].score as last_score").execute();
		String stuff = dr.fetchAll().toString();
		sess.close();
		return new ResponseEntity<>(stuff,HttpStatus.OK);
	}
}