package com.example.sarthak.navigationdrawer;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.sarthak.navigationdrawer.Backend.Backend.Config;
import com.example.sarthak.navigationdrawer.Backend.Backend.LocationService;
import com.example.sarthak.navigationdrawer.Backend.Backend.LoginRegister;
import com.example.sarthak.navigationdrawer.Backend.Backend.Reports;
import com.example.sarthak.navigationdrawer.Backend.Backend.ServerRequest;
import com.example.sarthak.navigationdrawer.ContactDisplay.MainActivity_Contacts;
import com.example.sarthak.navigationdrawer.FriendsNearMe.FriendsNearMe;
import com.example.sarthak.navigationdrawer.History.MainActivity_History;
import com.example.sarthak.navigationdrawer.LeaderBoard.MainActivity_Leaderboard;
import com.example.sarthak.navigationdrawer.ProfilePage.MainActivity_ProfilePage;
import com.example.sarthak.navigationdrawer.ReportPanel.DurationPickerDialog;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import info.hoang8f.widget.FButton;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private final int INTENT_GPS_PROVIDER = 1;
    private final int PERMISSION_CHECK = 2;
    public Marker sourceMarker = null;
    public Marker destinationMarker = null;
    SharedPreferences pref;
    String phone_number;
    Reports report;
    ArrayList<Reports> reports;
    private GoogleMap mMap;
    private CardView cardView_Source;
    private CardView cardView_Destination;
    private LocationManager locationManager;
    private PlaceAutocompleteFragment autocompleteFragmentSource;
    private PlaceAutocompleteFragment autocompleteFragmentDestination;
    private Location lastKnowncurrentLocation;
    private String lastSelectedLocationName;
    private LatLng lastSelectedLatLng;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MenuItem cancel_source_and_destination;
    private Menu menu;
    private FloatingActionsMenu fab_menu_report_panel;
    private String typeOfReport = "";
    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST = 3;
    private int places_id = 0;
    private final int PICK_A_RANDOM_PLACE_ON_MAP = 4;
    private LatLng randomPickedPlaceLatLng;
    private EditText location_report;
    private EditText description;
    private String descriptionReport;
    private int hours;
    private int minutes;
    private int days;
    private FButton hour_minute_selector;
    private FButton day_selector;
    private double lat_report;
    private double long_report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Setting up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_MapsActivity_Main);
        if (toolbar != null) {
            toolbar.setTitle("TRAVEY");
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //getting shared preferrences
        pref = this.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = pref.edit();
        phone_number = pref.getString(Config.phone_number, "");


        //Setting up drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_MapsActivity_Main);

        /*Creating instance of place Picker*/
        mGoogleApiClient = new GoogleApiClient
                .Builder(MapsActivity.this)
                .enableAutoManage(MapsActivity.this, places_id, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Toast.makeText(MapsActivity.this, "Connection Error!\nPlease try again after some time", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.e("Connection failed", connectionResult.getErrorMessage());
                    }
                })
                .build();


        //Setting up Navigation Drawer
        navigationView = (NavigationView) findViewById(R.id.navigation_view_MapsActivity_Main);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                if (menuItem.getTitle().equals("Profile")) {
                    Intent intent = new Intent(MapsActivity.this, MainActivity_ProfilePage.class);
                    startActivity(intent);
                } else if (menuItem.getTitle().equals("Places")) {
                    if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
                        if (mGoogleApiClient == null)
                            Toast.makeText(MapsActivity.this, "Could not create Google Api Client Instance", Toast.LENGTH_SHORT).show();
                        else if (!mGoogleApiClient.isConnected()) {
                            Toast.makeText(MapsActivity.this, "Could not connect to the Google Apis", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                        try {
                            startActivityForResult(builder.build(MapsActivity.this), PLACE_PICKER_REQUEST);
                        } catch (GooglePlayServicesRepairableException e) {
                            Log.d("PlacesAPI Demo", "GooglePlayServicesRepairableException thrown");
                        } catch (GooglePlayServicesNotAvailableException e) {
                            Log.d("PlacesAPI Demo", "GooglePlayServicesNotAvailableException thrown");
                        }
                    }
                } else if (menuItem.getTitle().equals("Leaderboard")) {
                    Intent intent = new Intent(MapsActivity.this, MainActivity_Leaderboard.class);
                    startActivity(intent);
                } else if (menuItem.getTitle().equals("History")) {
                    Intent intent = new Intent(MapsActivity.this, MainActivity_History.class);
                    startActivity(intent);
                } else if (menuItem.getTitle().equals("Track a Friend")) {
                    Intent intent = new Intent(MapsActivity.this, MainActivity_Contacts.class);
                    startActivity(intent);
                } else if (menuItem.getTitle().equals("Friends Near Me")) {
                    Intent intent = new Intent(MapsActivity.this, FriendsNearMe.class);
                    startActivity(intent);
                } else if (menuItem.getTitle().equals("Logout")) {
                    edit.clear();
                    edit.commit();
                    startActivity(new Intent(MapsActivity.this, LoginRegister.class));
                    Toast.makeText(MapsActivity.this, "Logged Out !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        /*To do enable permission code*/
        enablePermissions();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //enablePermissions();
            Toast.makeText(MapsActivity.this, "Enable Permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER





        /*Autocomplete Source and Destination Box*/

        cardView_Destination = (CardView) findViewById(R.id.card_view_Destination_search);
        cardView_Source = (CardView) findViewById(R.id.card_view_Source_search);
        autocompleteFragmentSource = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_source);
        autocompleteFragmentSource.setHint("Select Source");
        autocompleteFragmentSource.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                Log.i("Selected", "Place: " + place.getName());
                lastSelectedLocationName = String.valueOf(place.getName());
                lastSelectedLatLng = place.getLatLng();
                LatLng sourceLatLng = place.getLatLng();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(sourceLatLng, 15);
                mMap.animateCamera(cameraUpdate);
                if (sourceMarker != null) {
                    sourceMarker.remove();
                }
                //sourceMarker = new MarkerOptions().position(sourceLatLng).draggable(true);
                //mMap.addMarker(sourceMarker);
                sourceMarker = mMap.addMarker(new MarkerOptions().position(sourceLatLng).draggable(true));


                if (destinationMarker != null) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(sourceMarker.getPosition());
                    builder.include(destinationMarker.getPosition());
                    LatLngBounds bounds = builder.build();
                    int padding = 310; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom));

                }
                cardView_Destination.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Error", "An error occurred: " + status);
            }

        });
        autocompleteFragmentDestination = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destination);
        autocompleteFragmentDestination.setHint("Select Destination");
        cardView_Destination.setVisibility(View.GONE);
        autocompleteFragmentDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i("Selected", "Place: " + place.getName());
                LatLng destinationLatLng = place.getLatLng();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15);
                mMap.animateCamera(cameraUpdate);
                if (destinationMarker != null) {
                    destinationMarker.remove();
                }

                destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).draggable(true));


                if (sourceMarker != null) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(sourceMarker.getPosition());
                    builder.include(destinationMarker.getPosition());
                    LatLngBounds bounds = builder.build();
                    int padding = 310; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);
                    cardView_Destination.setVisibility(View.GONE);
                    cardView_Source.setVisibility(View.GONE);
                    if (cancel_source_and_destination != null)
                        cancel_source_and_destination.setVisible(true);
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 0.5f));

                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Error", "An error occurred: " + status);
            }
        });


        /*Floating action buttons inside the menu*/
        fab_menu_report_panel = (FloatingActionsMenu) findViewById(R.id.fab_report_panel);
        com.getbase.floatingactionbutton.FloatingActionButton fab_accident = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_report_accident);
        fab_accident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_menu_report_panel.collapse();
                typeOfReport = "Accident";
                getDialogReportAdd();
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton fab_roadblock = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_report_roadblock);
        fab_roadblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_menu_report_panel.collapse();
                typeOfReport = "Roadblock";
                getDialogReportAdd();
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton fab_event = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_report_event);
        fab_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_menu_report_panel.collapse();
                typeOfReport = "Event";
                getDialogReportAdd();
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton fab_traffic = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_report_traffic);
        fab_traffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_menu_report_panel.collapse();
                typeOfReport = "Traffic";
                getDialogReportAdd();
            }
        });
        startService(new Intent(this, LocationService.class));//Start the location refresh service


    }

    private void addLabelsForAllReports() {

        //clearing the map before adding all the other reports.
        //mMap.clear();

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(Config.phone_number, phone_number));
        ServerRequest sr = new ServerRequest(MapsActivity.this);
        reports = new ArrayList<Reports>();
        Log.d("here", "params sent");
        //JSONObject json = sr.getJSON("http://127.0.0.1:8080/register",params);
        JSONArray json = sr.getJSONArray(Config.ip + "/allReports", params);
        Log.d("here", "json received");
        if (json != null) {
            try {
                Log.d("JsonAllReports", "" + json);
                for (int i = 0; i < json.length(); i++) {
                    Gson gson = new Gson();
                    report = gson.fromJson(json.getString(i), new TypeToken<Reports>() {
                    }.getType());
                    reports.add(report);

                    double[] ar = report.getLocation();
                    LatLng place = new LatLng(ar[0], ar[1]);

                    if (report.getTag().equals("Traffic")) {
                        mMap.addMarker(new MarkerOptions()
                                .position(place).flat(true)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    } else if (report.getTag().equals("Roadblock")) {
                        mMap.addMarker(new MarkerOptions()
                                .position(place).flat(true)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                    } else if (report.getTag().equals("Accident")) {
                        mMap.addMarker(new MarkerOptions()
                                .position(place).flat(true)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    } else if (report.getTag().equals("Event")) {
                        mMap.addMarker(new MarkerOptions()
                                .position(place).flat(true)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    } else {
                        mMap.addMarker(new MarkerOptions()
                                .position(place).flat(true)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            for (int j = 0; j < reports.size(); j++) {
                                double loc[] = reports.get(j).getLocation();
                                LatLng loc1 = new LatLng(loc[0], loc[1]);
                                if (loc1.latitude == marker.getPosition().latitude && loc1.longitude == marker.getPosition().longitude) {

                                    getDetailsForReport(reports.get(j).getDetail(), reports.get(j).getTag(), (reports.get(j).getUpvotes()), reports.get(j).getDownvotes(), reports.get(j).get_id(), j);
                                    Log.d("checkVote", "After one iteration" + reports.size());
                                    break;
                                }
                            }

                            return false;
                        }
                    });


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    private void getDialogReportAdd() {
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Enter Report Parameters")
                .customView(R.layout.activity_enter_report_parameters, wrapInScrollView)
                .positiveText("Add")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //add to database and dismiss dialog
                        if(lat_report!=0 && long_report!=0){
                            saveDetailsToDataBase();
                            lat_report=0;
                            long_report=0;
                            addLabelsForAllReports();
                        }else{
                            Toast.makeText(MapsActivity.this, "Please enter a location", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .negativeText("Dismiss")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
        description=(EditText)dialog.getCustomView().findViewById(R.id.descriptionReportEditText);
        descriptionReport=description.getText().toString();
        location_report=(EditText)dialog.getCustomView().findViewById(R.id.locationReportEditText);
        location_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MapsActivity.this), PICK_A_RANDOM_PLACE_ON_MAP);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.d("PlacesAPI Demo", "GooglePlayServicesRepairableException thrown");
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.d("PlacesAPI Demo", "GooglePlayServicesNotAvailableException thrown");
                }
            }
        });
        hour_minute_selector = (FButton) dialog.getCustomView().findViewById(R.id.hour_minute_selectorButton_ReportAdd);
        hour_minute_selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DurationPickerDialog durationPickerDialog = new DurationPickerDialog(MapsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        hours = hourOfDay;
                        minutes = minute;
                    }
                }, 0, 0);
                durationPickerDialog.show();
            }
        });

    }

    private void saveDetailsToDataBase() {
        ArrayList<NameValuePair> parametersDatabase=new ArrayList<NameValuePair>();

        /*To do*/
        parametersDatabase.add(new BasicNameValuePair(Config.phone_number, "8758964908"));

        /*Time format*/
        /*July 22, 2013 14:00:00*/

        Calendar calendar=Calendar.getInstance();
        int month=calendar.get(Calendar.MONTH);
        int year=calendar.get(Calendar.YEAR);
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        int hours=calendar.get(Calendar.HOUR_OF_DAY);
        int minutes=calendar.get(Calendar.MINUTE);
        String monthString=getMonthFormatted(month);
        int seconds=0;
        String finalStartDate=getFormattedDate(monthString,day,year,hours,minutes,seconds);
        int dayNew = 0, hourNew = 0, minuteNew = 0;
        if (minutes + this.minutes >= 60) {
            minuteNew += (minutes + this.minutes % 60);
            hourNew++;
        }
        if (hourNew + hours + this.hours >= 24) {
            hourNew += (hours + this.hours) % 24;
            dayNew++;
        }
        dayNew += day;
        String finalEndDate = getFormattedDate(monthString, dayNew, year, hourNew, minuteNew, seconds);
        Log.d("enddate",finalEndDate);
        parametersDatabase.add(new BasicNameValuePair(Config.detail, descriptionReport));
        parametersDatabase.add(new BasicNameValuePair(Config.tag,typeOfReport));
        parametersDatabase.add(new BasicNameValuePair(Config.start_time, finalStartDate));
        parametersDatabase.add(new BasicNameValuePair(Config.end_time, finalEndDate));
        parametersDatabase.add(new BasicNameValuePair(Config.latitude, ""+lat_report));
        parametersDatabase.add(new BasicNameValuePair(Config.longitude, ""+long_report));

        ServerRequest sr = new ServerRequest(MapsActivity.this);
        Log.d("here", "params sent");
        //JSONObject json = sr.getJSON("http://127.0.0.1:8080/register",params);
        JSONObject json = sr.getJSON(Config.ip+"/reportAdd",parametersDatabase);
        Log.d("here", "json received");
        if(json != null){
            try{
                String jsonstr = json.getString("response");
                //String sue = json.getString("use");

                //Toast.makeText(getContext(),jsonstr+ "     " + sue ,Toast.LENGTH_LONG).show();

                Log.d("Hello", jsonstr);
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }

    private String getFormattedDate(String monthString, int day, int year, int hours, int minutes, int seconds) {
        String formattedDate= monthString+" "+day+", "+year+" "+hours+":"+minutes+":"+seconds;
        Log.d("FormattedDate",formattedDate);
        return formattedDate;
    }

    private String getMonthFormatted(int month){
        String monthString[]={
                "January","February","March","April","May","June","July","August","September","October","November","December"
        };
        return monthString[month];
    }


    private void getDetailsForReport(String detail, String title, int upvote, int downvote, final String id, final int pos) {
        CharSequence methodsToTakeSource[] = new CharSequence[]{"" + detail, "Upvotes : " + upvote, "Downvotes : " + downvote};

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(methodsToTakeSource, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:

                        return;
                    case 1:
                        ArrayList<NameValuePair> params1 = new ArrayList<>();
                        params1.add(new BasicNameValuePair(Config.reportId, id));
                        ServerRequest sr1 = new ServerRequest(MapsActivity.this);
                        Log.d("here", "params sent" + id);
                        //JSONObject json = sr.getJSON("http://127.0.0.1:8080/register",params);
                        JSONObject json1 = sr1.getJSON(Config.ip + "/reportUpVote", params1);
                        Log.d("here", "json received");
                        Toast.makeText(MapsActivity.this, "Upvoted!", Toast.LENGTH_SHORT).show();
                        Reports rep = reports.get(pos);
                        rep.setUpvotes(rep.getUpvotes() + 1);
                        reports.set(pos, rep);


                        return;
                    case 2:
                        ArrayList<NameValuePair> params2 = new ArrayList<>();
                        params2.add(new BasicNameValuePair(Config.reportId, id));
                        ServerRequest sr2 = new ServerRequest(MapsActivity.this);
                        Log.d("here", "params sent");
                        //JSONObject json = sr.getJSON("http://127.0.0.1:8080/register",params);
                        JSONArray json2 = sr2.getJSONArray(Config.ip + "/reportDownVote", params2);
                        Log.d("here", "json received");
                        Toast.makeText(MapsActivity.this, "Downvoted!", Toast.LENGTH_SHORT).show();
                        Reports rep1 = reports.get(pos);
                        rep1.setDownvotes(rep1.getDownvotes() + 1);
                        reports.set(pos, rep1);
                        return;
                }
            }
        });
        builder.show();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //enablePermissions();
            Toast.makeText(MapsActivity.this, "Enable Permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.setMyLocationEnabled(true);

        //disabled my location button
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //adding labels for map
        addLabelsForAllReports();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastKnowncurrentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.animateCamera(cameraUpdate);

        //locationManager.removeUpdates(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //enablePermissions();
            Toast.makeText(MapsActivity.this, "Enable Permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_GPS_PROVIDER) {
            if (checkGPSEnabled() == false) {
                Toast.makeText(MapsActivity.this, "Please Enable GPS", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == PICK_A_RANDOM_PLACE_ON_MAP) {
            if (resultCode == RESULT_OK) {
                randomPickedPlaceLatLng = getRandomPickedPlaceOnMap(PlacePicker.getPlace(data, this));
                //to set the place name in location edit text
                Place place=PlacePicker.getPlace(data,this);
                if(place!=null){
                    location_report.setText(place.getName());
                }
            }
        }


    }

    private LatLng getRandomPickedPlaceOnMap(Place place) {
        if (place == null)
            return null;
        else {
            LatLng latLng = place.getLatLng();
            lat_report=latLng.latitude;
            long_report=latLng.longitude;
            return latLng;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MapsActivity.this, "Enable Provider", Toast.LENGTH_SHORT).show();
        createGPSEnableDialog();

    }

    private boolean checkGPSEnabled() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

    private void createGPSEnableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setMessage("Please Enable the GPS")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent gpsOptionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                        if (checkGPSEnabled() == true) {
                            Toast.makeText(MapsActivity.this, "GPS Enabled", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MapsActivity.this, "Please Enable GPS", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        cancel_source_and_destination = (MenuItem) menu.findItem(R.id.action_cancel_souce_and_destination);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_cancel_souce_and_destination:
                cancel_source_and_destination.setVisible(false);
                cardView_Source.setVisibility(View.VISIBLE);
                cardView_Destination.setVisibility(View.VISIBLE);
                return true;
            case R.id.action_get_currentLocation:
                goToCurrentLocation();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToCurrentLocation() {
        if (checkGPSEnabled() == false) {
            Toast.makeText(MapsActivity.this, "Please Enable GPS", Toast.LENGTH_LONG).show();
            createGPSEnableDialog();
        }
        if (lastKnowncurrentLocation != null) {
            LatLng latLng = new LatLng(lastKnowncurrentLocation.getLatitude(), lastKnowncurrentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(cameraUpdate);
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //enablePermissions();
                Toast.makeText(MapsActivity.this, "Enable Permissions", Toast.LENGTH_SHORT).show();
                return;
            }
            mMap.setMyLocationEnabled(true);
            locationManager.removeUpdates(MapsActivity.this);
        }
    }

    /*Enable Permissions if not granted*/
    private void enablePermissions() {
        //To see this code many threads running simultaneously requesting permissions
        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(MapsActivity.this, "Error while requesting permissions", Toast.LENGTH_SHORT).show();
                return;
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CHECK);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(MapsActivity.this, "Error while requesting permissions", Toast.LENGTH_SHORT).show();
                return;
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CHECK);
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(MapsActivity.this, "Error while requesting permissions", Toast.LENGTH_SHORT).show();
                return;
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CHECK);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(MapsActivity.this, "Error while requesting permissions", Toast.LENGTH_SHORT).show();
                return;
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CHECK);
            }
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        /*
        switch (requestCode) {
            case PERMISSION_CHECK: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;

                } else {
                    enablePermissions();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
        */
    }
}
