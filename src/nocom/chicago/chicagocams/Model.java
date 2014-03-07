package nocom.chicago.chicagocams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.res.AssetManager;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Model {
    
    private Location nearest;
    private ArrayList<Location> cameras;
    
    private AssetManager asset;

    public Model(AssetManager appAssets) {
        asset = appAssets;
        
        cameras = new ArrayList<Location>();
        
        try {
            this.loadLocationsFromJSON();
        }
        catch (JSONException e) {
             e.printStackTrace();
        }
        
    }
    
    public Location getNearest(){
        nearest = cameras.get(0);
        return nearest;
    }
    
    //Sort by location nearest to your current location.
    public void sortByLocation(Location currentLocation){
        boolean swapped = true;
        
        while(swapped){
            swapped = false;
            
            for(int i = 1; i < cameras.size(); i++){
                if(cameras.get(i-1).distanceTo(currentLocation) > cameras.get(i).distanceTo(currentLocation)){
                    Location tmp = cameras.get(i);
                    cameras.set(i, cameras.get(i-1));
                    cameras.set(i-1, tmp);
                    swapped = true;
                }
            }
        }
    }
    
    //Returns JSON in String format.
    private String loadJSONFromAsset(){
        String json = null;
        
        try{
            InputStream is = asset.open("speedcams");
            int size = is.available();
            byte[] buffer = new byte[size];
            
            is.read(buffer);
            is.close();
            json = new String(buffer);
        }
        catch(IOException ex){
            ex.printStackTrace();
            return null;
        }
        
        return json;
    }
    
    //Loads all locations into an ArrayList. See block comment for bizare behavior.
    private void loadLocationsFromJSON() throws JSONException{
        JSONArray toParse = new JSONArray(this.loadJSONFromAsset());
        JSONObject current = null;
        
        for(int i = 0; i < toParse.length(); i++){
            Location currentLoc;
            String camera; 
            double lat,lon;
            
            try{
                current = toParse.getJSONObject(i);
                
                camera = current.getString("fullname");
                lat = current.getDouble("lat");
                lon = current.getDouble("lon");
                
                currentLoc = new Location(camera);
                /*
                 * This is super bizarre. Either the city of Chicago got it mixed up 
                 * or the Android API has latitude and longitude mixed up.
                 */
                currentLoc.setLatitude(lon);
                currentLoc.setLongitude(lat);
                
                cameras.add(currentLoc);

            } 
            catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
    
     

}
