package com.example.custos;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.custos.receivers.EventReceiver;
import com.example.custos.utils.Common;
import com.example.custos.utils.Event;
import com.example.custos.utils.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlusCode;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CreateEventActivity extends AppCompatActivity {
    TextView event_name_text_view;
    TextView event_description_text_view;
    TextView event_date_text_view;
    TextView event_time_text_view;
    ListView lv;
    String name;
    String description;
    ArrayList<String> uids = new ArrayList<String>();
    double lat;
    double lon;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    Place place;
    String location_name;
    ToolKit toolKit;
    Button back_button;
    Switch safetySwitch;
    Button event_detail_edit_guests;
    TextView create_event_invited_label;
    LinearLayout linear_layout_fragment;
    double homeLat;
    double homeLong;
    String homeAddress;
    EditText event_detail_end_date;
    EditText event_detail_end_time;
    String min_event_date;
    final long notification_time = 900000; //15 minutes

    //users that will be invited when creating event.
    ArrayList<User> selected;

    FirebaseUser firebaseUser;
    private DatabaseReference userReference;
    User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Handler handler = new Handler();
        final View decorView = getWindow().getDecorView();

        final int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    decorView.setSystemUiVisibility(uiOptions);
                                }
                            }, 2000);
                        } else {

                        }
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        event_detail_end_date = findViewById(R.id.event_detail_end_date);
        event_detail_end_time = findViewById(R.id.event_detail_end_time);

        back_button = findViewById(R.id.create_event_back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("User Information").child(firebaseUser.getUid());
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                current_user = new User();
                current_user.setUserName(dataSnapshot.child("userName").getValue().toString());
                current_user.setImageURL(dataSnapshot.child("imageURL").getValue().toString());
                current_user.setUserToken(dataSnapshot.child("userToken").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference user_info = FirebaseDatabase.getInstance().getReference("User Information").child(firebaseUser.getUid());
        user_info.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("User Address").exists()) {
                    homeAddress =  dataSnapshot.child("User Address").child("homeLocation").getValue().toString();
                    homeLat =  (double) dataSnapshot.child("User Address").child("userHomeLatitude").getValue();
                    homeLong =  (double) dataSnapshot.child("User Address").child("userHomeLongitude").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        toolKit = new ToolKit();
        lv = findViewById(R.id.event_detail_invite_list);

        event_date_text_view = findViewById(R.id.event_detail_date);
        event_date_text_view.setInputType(InputType.TYPE_NULL);
        event_detail_end_time.setInputType(InputType.TYPE_NULL);

        event_detail_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialogx
                datePickerDialog = new DatePickerDialog(CreateEventActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                event_detail_end_date.setText((monthOfYear+1) + "/" + dayOfMonth + "/" + year);
                            }
                        }, mYear, mMonth, mDay);
                long minEventDate = 0;
                try {
                    minEventDate = getMinEventDate(min_event_date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(min_event_date == null) {
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                } else if(minEventDate == 0){
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                } else {
                    datePickerDialog.getDatePicker().setMinDate(minEventDate+1000);

                }
                datePickerDialog.show();
            }
        });

        event_detail_end_time.setInputType(InputType.TYPE_NULL);

        event_detail_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String am_pm = "";
                        if(selectedHour > 11) {
                            if(selectedHour == 12) {
                                selectedHour = 12;
                            } else {
                                selectedHour = selectedHour - 12;
                            }
                            am_pm = "PM";
                        } else {
                            if(selectedHour == 0) {
                                selectedHour = 12;
                            }
                            am_pm = "AM";
                        }

                        if(selectedMinute < 10) {
                            event_detail_end_time.setText(selectedHour + ":0" + selectedMinute + " " + am_pm);
                        } else {
                            event_detail_end_time.setText(selectedHour + ":" + selectedMinute + " " + am_pm);
                        }

                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        event_date_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialogx
                datePickerDialog = new DatePickerDialog(CreateEventActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                String date = (monthOfYear+1) + "/" + dayOfMonth + "/" + year;
                                event_date_text_view.setText(date);
                                min_event_date = date;

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        event_time_text_view = findViewById(R.id.event_detail_time);
        event_time_text_view.setInputType(InputType.TYPE_NULL);

        event_time_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEventActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String am_pm = "";
                        if(selectedHour > 11) {
                            if(selectedHour == 12) {
                                selectedHour = 12;
                            } else {
                                selectedHour = selectedHour - 12;
                            }
                            am_pm = "PM";
                        } else {
                            if(selectedHour == 0) {
                                selectedHour = 12;
                            }
                            am_pm = "AM";
                        }

                        if(selectedMinute < 10) {
                            event_time_text_view.setText(selectedHour + ":0" + selectedMinute + " " + am_pm);
                        } else {
                            event_time_text_view.setText(selectedHour + ":" + selectedMinute + " " + am_pm);
                        }

                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        Places.initialize(getApplicationContext(),"AIzaSyCjncU-Fe5pQKOc85zuGoR9XEs61joNajc");
        //PlacesClient placesClient = Places.createClient(this);
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.event_location);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place pl) {
                final LatLng latLng = pl.getLatLng();
                place = pl;
                lat = latLng.latitude;
                lon = latLng.longitude;
                location_name = pl.getName();
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });

        //CREATE EVENT POST REQUEST
        findViewById(R.id.create_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createNotificationChannel();

                //POST DATA HERE
                event_name_text_view = findViewById(R.id.event_detail_name);
                event_description_text_view = findViewById(R.id.event_detail_description);
                event_date_text_view = findViewById(R.id.event_date);
                event_time_text_view = findViewById(R.id.event_detail_time);

                event_date_text_view = findViewById(R.id.event_detail_date);
                event_time_text_view = findViewById(R.id.event_detail_time);
                boolean isSafety = safetySwitch.isChecked();
                if(!isSafety) {
                    if(!isInputValidNoSafety(event_name_text_view, event_date_text_view,
                            event_time_text_view, event_detail_end_date, event_detail_end_time, place)) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid form", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        //Create event under root/user_event/uid/......
                        final String id = createEventID();
                        name = event_name_text_view.getText().toString();
                        description = event_description_text_view.getText().toString();
                        String start_date = event_date_text_view.getText().toString();
                        String start_time =  event_time_text_view.getText().toString();
                        String end_date = event_detail_end_date.getText().toString();
                        String end_time = event_detail_end_time.getText().toString();
                        boolean checkSafety = safetySwitch.isChecked();
                        final Event event = new Event(id, name, getLocationText(lat, lon),
                                start_date,
                                start_time,
                                end_date,
                                end_time,
                                description, location_name, selected);

                        DatabaseReference user_information = FirebaseDatabase.getInstance()
                                .getReference("user_event")
                                .child(firebaseUser.getUid())
                                .child(id);

                        user_information.child("name").setValue(event.getName());
                        user_information.child("start_date").setValue(event.getStartDate());
                        user_information.child("start_time").setValue(event.getStartTime());
                        user_information.child("end_date").setValue(event.getEndDate());
                        user_information.child("end_time").setValue(event.getEndTime());
                        user_information.child("description").setValue(event.getDescription());
                        user_information.child("area").setValue(event.getArea());
                        user_information.child("location").child("latitude").setValue(lat);
                        user_information.child("location").child("longitude").setValue(lon);
                        user_information.child("location_name").setValue(event.getLocation_name());
                        user_information.child("isSafetyEvent").setValue(checkSafety);
                        user_information.child("isOwner").setValue("true");
                        if(selected != null) {
                            for(User user : selected) {
                                user_information.child("invited_users").child(user.getUID()).child("name").setValue(user.getUserName());
                                user_information.child("invited_users").child(user.getUID()).child("status").setValue("invited");
                            }
                            user_information.child("invited_users").child(firebaseUser.getUid()).child("name").setValue(current_user.getUserName());

                        } else {
                            user_information.child("invited_users").setValue("NONE");
                        }



                        //Send notifications to invited users
                        DatabaseReference notifications = FirebaseDatabase.getInstance().getReference("Notifications");
                        DatabaseReference events = FirebaseDatabase.getInstance().getReference("user_event");

                        if(selected != null) {
                            for(User user : selected) {
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("friendName").setValue(current_user.getUserName());
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("imageURL").setValue(current_user.getImageURL());
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("request_time").setValue(getRequestTime());
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("request_type").setValue("invite_sent");
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("uid").setValue(firebaseUser.getUid());
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("eventId").setValue(event.getID());
                            }
                        }

                        try {
                            createBackgroundNotification(start_date + " " + start_time, name);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(v.getContext(), MainEventListActivity.class);
                        onActivityResult(1,1,intent);
                        setResult(1, intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                } else {
                    if(!isInputValidWithSafety(event_name_text_view, event_date_text_view,
                            event_time_text_view, event_detail_end_date, event_detail_end_time)) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid form", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        //Create event under root/user_event/uid/......
                        final String id = createEventID();
                        name = event_name_text_view.getText().toString();
                        description = event_description_text_view.getText().toString();
                        String start_date = event_date_text_view.getText().toString();
                        String start_time =  event_time_text_view.getText().toString();
                        String end_date = event_detail_end_date.getText().toString();
                        String end_time = event_detail_end_time.getText().toString();
                        boolean checkSafety = safetySwitch.isChecked();
                        final Event event = new Event(id, name, homeAddress,
                                start_date,
                                start_time,
                                end_date,
                                end_time,
                                description, location_name, selected);


                        DatabaseReference user_information = FirebaseDatabase.getInstance()
                                .getReference("user_event")
                                .child(firebaseUser.getUid())
                                .child(id);

                        user_information.child("name").setValue(event.getName());
                        user_information.child("start_date").setValue(event.getStartDate());
                        user_information.child("start_time").setValue(event.getStartTime());
                        user_information.child("end_date").setValue(event.getEndDate());
                        user_information.child("end_time").setValue(event.getEndTime());
                        user_information.child("description").setValue(event.getDescription());
                        user_information.child("area").setValue(event.getArea());
                        user_information.child("location").child("latitude").setValue(homeLat);
                        user_information.child("location").child("longitude").setValue(homeLong);
                        user_information.child("location_name").setValue(homeAddress);
                        user_information.child("isSafetyEvent").setValue(checkSafety);
                        user_information.child("isOwner").setValue("true");
                        if(selected != null) {
                            for(User user : selected) {
                                user_information.child("invited_users").child(user.getUID()).child("name").setValue(user.getUserName());
                                user_information.child("invited_users").child(user.getUID()).child("status").setValue("invited");
                            }
                            user_information.child("invited_users").child(firebaseUser.getUid()).child("name").setValue(current_user.getUserName());
                        } else {
                            user_information.child("invited_users").setValue("NONE");
                        }

                        //Send notifications to invited users
                        DatabaseReference notifications = FirebaseDatabase.getInstance().getReference("Notifications");
                        DatabaseReference events = FirebaseDatabase.getInstance().getReference("user_event");

                        if(selected != null) {
                            for(User user : selected) {
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("friendName").setValue(current_user.getUserName());
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("imageURL").setValue(current_user.getImageURL());
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("request_time").setValue(getRequestTime());
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("request_type").setValue("invite_sent");
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("uid").setValue(firebaseUser.getUid());
                                notifications.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("eventId").setValue(event.getID());
                            }
                        }
                        try {
                            createBackgroundNotification(start_date + " " + start_time, name);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(v.getContext(), MainEventListActivity.class);
                        onActivityResult(1,1,intent);
                        setResult(1, intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }

                }

            }
        });

        safetySwitch = findViewById(R.id.safety_switch);
        event_detail_edit_guests = findViewById(R.id.event_detail_edit_guests);
        linear_layout_fragment = findViewById(R.id.linear_layout_fragment);
        create_event_invited_label = findViewById(R.id.create_event_invited_label);
        safetySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(safetySwitch.isChecked()) {
                    event_detail_edit_guests.setVisibility(View.INVISIBLE);
                    linear_layout_fragment.setVisibility(View.GONE);
                    create_event_invited_label.setVisibility(View.INVISIBLE);
                    lv.setVisibility(View.INVISIBLE);
                } else {
                    event_detail_edit_guests.setVisibility(View.VISIBLE);
                    linear_layout_fragment.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.VISIBLE);
                }
            }
        });


        event_detail_edit_guests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), InviteGuestsActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", selected);
                intent.putExtra("BUNDLE", args);
                startActivityForResult(intent, 18);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 18) {
            if(data != null) {
                Bundle args = data.getBundleExtra("BUNDLE");
                selected = (ArrayList<User>) args.getSerializable("ARRAYLIST");
                String[] selectedNames = new String[selected.size()];
                for(int i = 0; i < selected.size(); i++) {
                    selectedNames[i] = selected.get(i).getUserName();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.invite_guest_list_item, R.id.aaaaaaaa, selectedNames);
                lv = findViewById(R.id.event_detail_invite_list);
                lv.setAdapter(adapter);
            } else {

            }

        }
    }

    private String getNameFromValue(String d) {
        int initialIndex = d.indexOf("name=") + 5;
        int lastIndex = d.indexOf("}");
        String name = d.substring(initialIndex, lastIndex);
        return name;
    }

    private String generateNumber() {
        String number = "";
        for(int i = 0; i < 4; i++) {
            number += String.valueOf(new Random().nextInt(10));
        }
        return number;
    }

    private String getLocationText(double latitude, double longitude) {
        String locationText = "";
        Geocoder geocoder;
        geocoder = new Geocoder(CreateEventActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude,1);
            locationText = addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return locationText;
    }

    private boolean isInputValidWithSafety(TextView name, TextView date, TextView time,
                                           TextView end_date, TextView end_time) {
        if(!name.getText().toString().equals("") &&
                !date.getText().toString().equals("") &&
                !time.getText().toString().equals("") &&
                !end_date.getText().toString().equals("") &&
                !end_time.getText().toString().equals("")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isInputValidNoSafety(TextView name, TextView date, TextView time,
                                         TextView end_date, TextView end_time, Place place) {

        if((!name.getText().toString().equals("")) &&
                (!date.getText().toString().equals("")) &&
                (!time.getText().toString().equals("")) &&
                (place != null) && (!end_date.getText().toString().equals("")) &&
                (!end_time.getText().toString().equals(""))) {
            return true;
        } else {
            return false;
        }
    }

    private String getCurrentTime() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }

    private String createEventID() {
        String id = "E" + generateNumber();
        return id;
    }

    private String getRequestTime() {
        Calendar calendarAccept = Calendar.getInstance();
        SimpleDateFormat acceptDate = new SimpleDateFormat("dd-MMMM-yyyy");
        Calendar timeAcceptFriend = Calendar.getInstance();
        SimpleDateFormat acceptTime = new SimpleDateFormat("hh:mm a");
        String dateAccept = acceptDate.format(calendarAccept.getTime());
        String timeAccept = acceptTime.format(timeAcceptFriend.getTime());

        return dateAccept + " at " + timeAccept;
    }

    private void createBackgroundNotification(String time, String event_name) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.ENGLISH);
        cal.setTime(sdf.parse(time));
        Intent intent = new Intent(getApplicationContext(), EventReceiver.class);
        intent.putExtra("name", event_name);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long event_time = cal.getTimeInMillis();

        if(System.currentTimeMillis() > event_time-notification_time) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,event_time-notification_time, pendingIntent);
        }
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification_Channel";
            String descriptoin = "Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("hello", name, importance);
            channel.setDescription(descriptoin);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private long getMinEventDate(String date) throws ParseException {
        if(date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
            Date d = sdf.parse(date);
            return d.getTime();
        } else {
            return 0;
        }

    }




}
