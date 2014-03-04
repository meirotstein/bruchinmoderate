package il.co.rotstein.server.addons;

import il.co.rotstein.server.config.Configurations.AddOnConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddOnsManager {
	
	
	private final static Logger log = Logger.getLogger( AddOnsManager.class.getName() );
	
	private static List<InAddOn> inAddOns = new ArrayList<InAddOn>();

	public static void loadAddOns(List<AddOnConfiguration> addOnConfigurations) {
		
		ClassLoader cLoader = AddOnsManager.class.getClassLoader();
		
		for ( AddOnConfiguration addOnConfig : addOnConfigurations ) {
			
			String addOnClass = addOnConfig.getAddOnClass();
			
			if ( addOnClass != null ){
				
				try {
					
					Class<?> aClass = cLoader.loadClass( addOnClass );
					AddOn addOn = ( AddOn ) aClass.newInstance();
					
					String[] params = addOnConfig.getParameters();
					addOn.init( params );
					
					if ( addOn instanceof InAddOn ){
						inAddOns.add( ( InAddOn ) addOn );
					} 
					
					log.log( Level.FINE , "Add On class loaded: " + addOnClass + " with parameters: " + Arrays.toString(params));
					//to be expanded as other types of addons will be added, inshaAlla. 
					
				} catch ( ClassNotFoundException e ) {
					
					log.log( Level.WARNING , "Fail to load Add On - class was not found " + e.getMessage() );
					
				} catch ( InstantiationException e ) {

					log.log( Level.WARNING , "Fail to load Add On" , e );
					
				} catch ( IllegalAccessException e ) {
					
					log.log( Level.WARNING , "Fail to load Add On" , e );
					
				} catch ( ClassCastException e ) {
					
					log.log( Level.WARNING , "Fail to load Add On - class is not an 'AddOn' class" , e );
					
				} catch ( Exception e ) {
					
					log.log( Level.WARNING , "Fail to load Add On" , e );
					
				}
				
			}
			
		}
		
	}

	public static List<InAddOn> getIncomingAddOns() {

		return inAddOns;
	}

}
