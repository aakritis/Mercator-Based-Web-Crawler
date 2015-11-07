package edu.upenn.cis455.storage;

import java.io.File;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.StoreConfig; 

public class DBWrapper {

	private static String envDirectory = null;
	private static File envHome = null;
	private static Environment env;
	private static EntityStore store;

	/* TODO: write object store wrapper for BerkeleyDB */

	public void setup(String directory) throws DatabaseException {
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		envConfig.setAllowCreate(true);
		envConfig.setTransactional(true);
		storeConfig.setAllowCreate(true);
		storeConfig.setTransactional(true);
		try {
			envDirectory = directory;
			System.out.println("[DEBUG} envDirectory :" + envDirectory);
			envHome = new File(envDirectory);
			env = new Environment(envHome, envConfig);
			store = new EntityStore(env, "EntityStore", storeConfig);
		}
		catch (Exception e) {
			System.out.println("Error creating database. Please check directory path + " );
			e.printStackTrace();
		}
	}

	public Environment getEnvironment() {
		return env;
	}

	public EntityStore getStore() {
		return store;
	}

	public void shutdown() throws DatabaseException {
		store.close();
		env.close();
	} 

}