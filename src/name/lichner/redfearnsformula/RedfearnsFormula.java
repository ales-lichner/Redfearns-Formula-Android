package name.lichner.redfearnsformula;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class RedfearnsFormula extends Activity {

	private EditText txtLat;
	private EditText txtLon;
	private Button btnCalculate;
	private TextView txtResult;
	
//	private String result;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initControls(); 
        
        
//        EditText et1 = new EditText(this);
//        et1.setHint("latitude e.g. -33.243577");
//        et1.setHeight();
//        et1.setMaxLines(1);
//        setContentView(et1);   
      
/*   
        TextView tv1 = new TextView(this);
        tv1.setText("Latitude: \nLongitude: ");
        setContentView(tv1);    
*/
    }

    private void initControls(){
    	txtLat = (EditText)findViewById(R.id.txtLat);
    	txtLon = (EditText)findViewById(R.id.txtLon);
    	btnCalculate = (Button)findViewById(R.id.btnCalculate);
    	btnCalculate.setOnClickListener(new Button.OnClickListener() { public void onClick(View v){ calculate(); }});
    	txtResult = (TextView)findViewById(R.id.txtResult);

 /*
        txtLon.setOnEditorActionListener(
                new OnEditorActionListener(){
                	@Override
                	public boolean onEditorAction(EditText v, int actionId, KeyEvent event){
                		if(event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                			InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                			in.hideSoftInputFromWindow(searchBar.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                			userValidateEntry();
                		}
                		return false;
                	}
                }
            );
 */   
   

		txtLon.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
/*
					mCurrentChars = mEditText.length();
					if (mCurrentChars == 0) {
						mReplyId = 0;
					}
*/
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
//						sendMessage();
						return true;
					case KeyEvent.KEYCODE_ENTER:
/*
	        			InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	        			in.hideSoftInputFromWindow(searchBar.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
*/						
						calculate();

						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(txtLon.getWindowToken(), 0);

						
						return true;
/*
						if (event.isAltPressed()) {						
							return true;
						}
*/
					default:
/*
						if (keyCode != KeyEvent.KEYCODE_DEL && mCurrentChars > mLimitChars) {
							return true;
						}
						mCharsLeftText.setText(String.valueOf(mLimitChars - mCurrentChars));
*/
						break;
					}
				}
				return false;
			}
		});

   	
    	
    	
    }

    private void calculate(){
    	
//        GridPoint gp = LLtoGrid(-37.0, 143.0);
    	
    	double dblLat;
    	double dblLon;
    	
    	try{
    		dblLat = Double.parseDouble(txtLat.getText().toString());
    		dblLon = Double.parseDouble(txtLon.getText().toString());
        } catch(NumberFormatException e) {  
        	txtResult.setText("Please enter latitude and longitude in decimal format."); 
            return;  
        }  
    	
    	GridPoint gp = LLtoGrid(dblLat, dblLon);
    	String result = "\nLatitude: " + txtLat.getText() + "\n";
    	result += "Longitude: " + txtLon.getText() + "\n\n";
    	result += "GDA-MGA\n";
    	result += "Zone: " + Integer.toString(gp.getZ()) + "\n";
    	result += "Easting: " + Double.toString(gp.getE()) + "\n";
    	result += "Northing: " + Double.toString(gp.getN()) + "\n";
    	txtResult.setText(result);
    }

    
    
    class DMS{
        int d;
        int m;
        int s;

        public DMS(int nD, int nM, int nS){
			d = nD;
			m = nM;
			s = nS;
        }

        public int getD(){ return d; }
        public int getM(){ return m; }
        public int getS(){ return s; }

    }

    class Point{
        double lat;
        double lon;

        public Point(int nLat, int nLon){
        	lat = nLat;
        	lon = nLon;
        }
    }

    class GridPoint{
        int z;
        double e;
        double n;

        public GridPoint(int nz, double ne, double nn){
			z = nz;
			e = ne;
			n = nn;
        }

        public int getZ(){ return z; }
        public double getE(){ return e; }
        public double getN(){ return n; }

      }


    private double DMStoDecimalDegrees(int d, int m, int s){
        return  ( d < 0 ? -1 : 1 ) * ( Math.abs(d) + Math.abs(m) / 60.0 + Math.abs(s) / 3600.0 );
    }

    private DMS DecimalDegreesToDMS(double dec){
		int sgn = (int) dec < 0 ? -1 : 1;
		dec = Math.abs(dec);
		int d = (int) dec;
		double f = dec - d;
		int m = (int) ( f * 60.0 );
		int s = (int) ( ( f * 60.0 - m ) * 60.0 );
		DMS dms = new DMS(sgn * d, m, s);
		return dms;
    }

    public GridPoint LLtoGrid(double lat, double lon){
        // GDA Specifications
		double a = 6378137.0;                       // Semi major axis
		double inverse_flattening = 298.257222101;  // 1/f
		double K0 = 0.9996;                         // Central scale factor
		int zone_width = 6;                         // Degrees
		
		int longitude_of_central_meridian_zone0 = -183;
		int longitude_of_western_edge_zone0 = -186;
		int false_easting = 500000;
		int false_northing = lat < 0 ? 10000000 : 0;  // Southern : Northern hemisphere
		
		//Derived constants
		double f = 1.0 / inverse_flattening;
		double b = a * ( 1 - f );           // Semi minor axis
		double e2 = 2 * f - f * f;          // = f*(2-f) = (a^2-b^2/a^2   //Eccentricity
		double e = Math.sqrt(e2);
		double e2_ = e2 / ( 1 - e2 );       // = (a^2-b^2)/b^2            //Second eccentricity
		double e_ = Math.sqrt(e2_);
		double e4 = e2 * e2;
		double e6 = e2 * e4;
		
		// Foot point latitude
		double n = (a - b) / (a + b);       // Same as e2 - why ?
		double n2 = n * n;
		double n3 = n * n2;
		double n4 = n2 * n2;
		
		double G = a * ( 1 - n ) * ( 1 - n2 ) * ( 1 + 9 * n2 / 4 + 225 * n4 / 64 ) * Math.PI / 180;
		
		double phi = lat * Math.PI / 180;     // Convert latitude to radians
		
		double sinphi = Math.sin(phi);
		double sin2phi = Math.sin(2 * phi);
		double sin4phi = Math.sin(4 * phi);
		double sin6phi = Math.sin(6 * phi);
		
		double cosphi = Math.cos(phi);
		double cosphi2 = cosphi * cosphi;
		double cosphi3 = cosphi * cosphi2;
		double cosphi4 = cosphi2 * cosphi2;
		double cosphi5 = cosphi * cosphi4;
		double cosphi6 = cosphi2 * cosphi4;
		double cosphi7 = cosphi * cosphi6;
		double cosphi8 = cosphi4 * cosphi4;
		
		double t = Math.tan(phi);
		double t2 = t * t;
		double t4 = t2 * t2;
		double t6 = t2 * t4;
		
		//Radius of Curvature
		double rho = a * ( 1 - e2 ) / Math.pow( 1 - e2 * sinphi * sinphi, 1.5 );
		double nu = a / Math.pow( 1 - e2 * sinphi * sinphi, 0.5 );
		double psi = nu / rho;
		double psi2 = psi * psi;
		double psi3 = psi * psi2;
		double psi4 = psi2 * psi2;
		
		//Meridian distance
		double A0 = 1 - e2 / 4 - 3 * e4 / 64 - 5 * e6 / 256;
		double A2 = 3.0 / 8 * ( e2 + e4 / 4 + 15 * e6 / 128 );
		double A4 = 15.0 / 256 * ( e4 + 3 * e6 / 4 );
		double A6 = 35 * e6 / 3072;
		
		double term1 = a * A0 * phi;
		double term2 = -a * A2 * sin2phi;
		double term3 = a * A4 * sin4phi;
		double term4 = -a * A6 * sin6phi;
		
		double m = term1 + term2 + term3 + term4;
		
		//Zone
		int zone = (int) ( ( lon - longitude_of_western_edge_zone0 ) / zone_width );
		
		double central_meridian = zone * zone_width + longitude_of_central_meridian_zone0;
		
		double omega = ( lon - central_meridian ) * Math.PI / 180; // Relative longitude (radians)
		double omega2 = omega * omega;
		double omega3 = omega * omega2;
		double omega4 = omega2 * omega2;
		double omega5 = omega * omega4;
		double omega6 = omega3 * omega3;
		double omega7 = omega * omega6;
		double omega8 = omega4 * omega4;
		
		//Northing
		term1 = nu * sinphi * cosphi * omega2 / 2;
		term2 = nu * sinphi * cosphi3 * ( 4 * psi2 + psi - t2 ) * omega4 / 24;
		term3 = nu * sinphi * cosphi5 * ( 8 * psi4 * ( 11 - 24 * t2 ) - 28 * psi3 * ( 1 - 6 * t2 ) + psi2 * ( 1 - 32 * t2 ) - psi * 2 * t2 + t4 - t2 ) * omega6 / 720;
		term4 = nu * sinphi * cosphi7 * ( 1385 - 3111 * t2 + 543 * t4 - t6 ) * omega8 / 40320;
		double northing = false_northing + K0 * ( m + term1 + term2 + term3 + term4 );
		
	    //Easting
	    term1 = nu * omega * cosphi;
	    term2 = nu * cosphi3 * ( psi - t2 ) * omega3 / 6;
	    term3 = nu * cosphi5 * ( 4 * psi3 * ( 1 - 6 * t2 ) + psi2 * ( 1 + 8 * t2 ) - 2 * psi * t2 + t4 ) * omega5 / 120;
	    term4 = nu * cosphi7 * ( 61 - 479 * t2 + 179 * t4 - t6 ) * omega7 / 5040;
	    double easting = false_easting + K0 * ( term1 + term2 + term3 + term4 );
	
	
	    GridPoint gp = new GridPoint(zone, easting, northing);
	    return gp;
	}


	public Point GridToLL(int e, int n, int z){
		Point p = new Point(1,1);
		return p;
	}
}
