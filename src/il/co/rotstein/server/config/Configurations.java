package il.co.rotstein.server.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

public class Configurations {

	private final static Logger log = Logger.getLogger( Configurations.class.getName() );

	public static final String CONFIGURATION_RESOURSE = "/WEB-INF/config.json";

	private static JsonConfig config;

	//cached
	private static Set<String> ignoredSet;
	private static List<AddOnConfiguration> addOnConfigurations;

	public static void load( ServletContext context ) {

		try {

			InputStream is = context.getResourceAsStream( CONFIGURATION_RESOURSE );
			
			Gson gson = new GsonBuilder().registerTypeAdapter( JsonConfig.class , new JsonConfigInstanceCreator() ).create();

			config = gson.fromJson( new InputStreamReader( is ) , JsonConfig.class );

			log.info( "configurations loaded successfuly" );



		} catch ( Exception e ) {

			log.log( Level.WARNING , "Fail to load coonfigurations, using defaults instead" , e );

		}

	}


	public static Set<String> getIgnoreSet() {

		if( ignoredSet == null ) {

			ignoredSet = new HashSet<String>();

			if( config != null ) {

				for ( String email : config.ignore_users ){
					ignoredSet.add( email );
				}

			}

		}

		return ignoredSet;
	}

	public static String getGroupName() {

		return config.group_name;

	}

	public static String getSuperAdmin(){
		return config.super_admin;
	}

	public static int getMinimumPendingMinuts(){
		return config.min_pending_minuts;
	}

	public static String getMailHost(){
		return config.mail_host;
	}
	
	public static String getDoNotModerateKey() {
		return config.do_not_moderate_key;
	}
	
	public static List<AddOnConfiguration> getAddOnConfigurations() {
		
		if( addOnConfigurations == null ) {
			
			addOnConfigurations = new ArrayList<Configurations.AddOnConfiguration>();
			
			if( config != null ) {
			
				for ( AddOnConfiguration addOnConfig : config.addons ) {
					addOnConfigurations.add( addOnConfig );
				}
			
			}
						
		}
		
		return addOnConfigurations;
		
	}


	private static class JsonConfig {

		private String[] ignore_users;
		private String group_name = "bruchin";
		private String super_admin = "meir@rotstein.co.il";
		private int min_pending_minuts = 3;
		private String mail_host = "mybruchin.appspotmail.com";
		private String do_not_moderate_key = "@";
		private AddOnConfiguration[] addons;

	}
	
	public static class AddOnConfiguration {
		private String addon_class;
		private String[] params;
		
		public String getAddOnClass() {
			return addon_class;
		}
		
		public String[] getParameters() {
			return params;
		}
	}

	private static class JsonConfigInstanceCreator implements InstanceCreator<JsonConfig> {

		@Override
		public JsonConfig createInstance(Type arg0) {
			return new JsonConfig();
		}

	}

}
