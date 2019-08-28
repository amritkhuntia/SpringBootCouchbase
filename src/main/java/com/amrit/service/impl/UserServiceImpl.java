package com.amrit.service.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.amrit.beans.User;
import com.amrit.service.UserService;

public class UserServiceImpl implements UserService ,UserDetailsService{

	private static final Logger logger = Logger.getLogger(UserServiceImpl.class);

	static Cluster cluster = CouchbaseCluster.create();
	static Bucket defaultBucket = cluster.openBucket();
	static BucketManager bucketManager = defaultBucket.bucketManager();

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public void save(User user) {

		ObjectMapper mapper = new ObjectMapper();
		String userObjectString = null;
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		try {
			userObjectString = mapper.writeValueAsString(user);
		} catch (JsonProcessingException e) {
			logger.info(e.getMessage());
		}

		JsonObject json = JsonObject.fromJson(userObjectString);

		JsonDocument testJsonToBeStored = JsonDocument.create(json.getString("username"), json);
		defaultBucket.upsert(testJsonToBeStored);

	}

	public User findUserByUsername(String username) {
		ViewResult result = queryView();
		User user = new User();
		user.setUsername(username);
		for (ViewRow row : result) {
			JsonDocument doc = row.document();
			if (doc.content().getString("name").equals(username)) {
				user.setPassword(doc.content().getString("password"));
			}
		}

		return user;
	}

	private ViewResult queryView() {

		if (!viewExists("userDetail", "userByName")) {
			createView();
		}
		ViewResult result = defaultBucket.query(ViewQuery.from("userDetail", "userByName"));
		return result;
	}

	private void createView() {

		DesignDocument designDoc = DesignDocument.create("userDetail", Arrays.asList(DefaultView.create("userByName",
				"function map( doc, meta ) {" + "if(doc.name != null) {" + "emit( doc.name );}}")));
		bucketManager.insertDesignDocument(designDoc);
	}

	private boolean viewExists(String designDocName, String viewName) {
		DesignDocument designDoc = bucketManager.getDesignDocument(designDocName);
		if (designDoc != null) {
			List<View> views = designDoc.views();
			for (int i = 0; i < views.size(); i++) {
				if (views.get(i).name() != null && views.get(i).name().equals(viewName))
					return true;
			}

		}
		return false;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findUserByUsername(username);
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), null);
	}
}
