package il.rotstein.server.config;

import java.util.HashSet;
import java.util.Set;

public class Configurations {
	
	public static final String CONFIGURATION_RESOURSE = "config.json";
	
	//from json
	private String[] ignore_users;
	
	//cached
	private Set<String> ignoredSet;
	
	
	public Set<String> getIgnoreSet() {
		
		if( ignoredSet == null) {
			
			ignoredSet = new HashSet<String>();
			
			for ( String email : ignore_users ){
				ignoredSet.add( email );
			}
			
		}
		
		return ignoredSet;
	}
	
}
