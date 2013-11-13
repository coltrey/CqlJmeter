package com.github.cqljmeter.config;

import java.util.concurrent.ExecutionException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ClusterHolder {
	private static final Logger log = LoggingManager.getLoggerForClass();
	private final Cluster cluster;
	
	private final LoadingCache<String, Session> sessions = 
			CacheBuilder.newBuilder().build(new CacheLoader<String, Session>() {
				@Override
				public Session load(String key) throws Exception {
					log.debug("New session for " + key);
					return cluster.connect(key);
				}}); 

	public ClusterHolder(Cluster cluster) {
		this.cluster = cluster;
	}
	
	public void shutdown() {
		try {
			this.cluster.shutdown().force().get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Throwables.propagate(e);
		} catch (ExecutionException e) {
			Throwables.propagate(e);
		}
	}

	public Session getSesssion(String keyspace) {
		return sessions.getUnchecked(keyspace);
	}
}
