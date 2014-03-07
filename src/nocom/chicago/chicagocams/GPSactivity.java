package nocom.chicago.chicagocams;

import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;
import android.widget.Toast; 
import android.widget.ImageView;



public class GPSactivity extends Activity implements LocationListener, OnInitListener {
	public LocationManager lm;
	public double lat,lon;
	public float speed;
	
	public Model myModel;
	
	public TextToSpeech tts;
	public boolean said;
	
	public TextView locationLat, locationLon, warningTxt, speedTxt;
	public ImageView warningImg;
	
	public Canvas canvas;
	public float x,y,radius;
	
	//Lots of initialization. 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//init GPS
		super.onCreate(savedInstanceState);
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);	    
		
		//init layout.
		setContentView(R.layout.activity_gpsactivity);
		
		locationLat = (TextView) findViewById(R.id.locLat);
		locationLon = (TextView) findViewById(R.id.locLon);
		
		speedTxt = (TextView) findViewById(R.id.speedTxt);
		warningTxt = (TextView) findViewById(R.id.warnTxt);
		
		warningImg = (ImageView) findViewById(R.id.warnImg);
		
		//init status circle. 
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		warningImg.setImageBitmap(bitmap);

		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		x = width/2;
		y = height/2;
		radius = 200;
		
		canvas.drawCircle(x, y, radius, paint);
		
		//init model.
		myModel = new Model(this.getApplicationContext().getAssets());
		
		//init text to speech.
		tts = new TextToSpeech(this, this);
		said = false; 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gpsactivity, menu);
		return true;
	}
	

	 @Override
	 public void onLocationChanged(Location loc){	
		lat = loc.getLatitude();
		lon = loc.getLongitude();
		
		float msTomph = (float)2.237;
		speed = loc.getSpeed() * msTomph;
		speedTxt.setText(String.valueOf(speed) + " mph");
		
		locationLat.setText(String.valueOf(lat));
		locationLon.setText(String.valueOf(lon));
	     
	     
		Location currentLocation = new Location("Current");
		currentLocation.setLatitude(lat);
		currentLocation.setLongitude(lon);
	    
		myModel.sortByLocation(currentLocation);   
	     
	     
	    if(currentLocation.distanceTo(myModel.getNearest()) < 225){
            if(said != true){
	    		String seriousWarning = "You are approximately one block from a speed camera. You are traveling at "
	    	 		+ Math.round(speed) + " miles per hour.";
	    	
	    		tts.speak(seriousWarning, TextToSpeech.QUEUE_ADD, null);
	    		
	    		said = true;
	    	}
	    	
	    	warningTxt.setTextColor(Color.RED);
	    	
	    	Paint paintWarn = new Paint();
	    	paintWarn.setColor(Color.RED);
	 		
            canvas.drawCircle(x, y, radius, paintWarn);
	    }
	    
	    else{
            warningTxt.setTextColor(Color.WHITE);

            Paint paint = new Paint();
            paint.setColor(Color.GREEN);

            canvas.drawCircle(x, y, radius, paint);

            said = false;
	    }
	    
	    warningTxt.setText(myModel.getNearest().getProvider() + "\nTo nearest speed camera: " + String.valueOf(currentLocation.distanceTo(myModel.getNearest())));

	 }
	 
	@Override
	public void onInit(int code) {
		if(code==TextToSpeech.SUCCESS){
			tts.setLanguage(Locale.getDefault());
		}
		else{
			tts = null;
			Toast.makeText(this, "Failed to initialize TTS engine.", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onDestroy() {
		if(tts!=null){
			tts.stop();
			tts.shutdown();
		}
		if(lm!=null){
			lm.removeUpdates(this);
		}
		
		super.onDestroy();
	}

	@Override
	public void onProviderDisabled(String provider){
		Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT ).show();
	}

	 @Override
	 public void onProviderEnabled(String provider){
		Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
	 }

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras){

	}
	 
}
