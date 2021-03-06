package com.example.custos;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.custos.utils.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EventDetailsActivity extends AppCompatActivity {

    TextView event_detail_title;
    TextView event_detail_description;
    TextView event_detail_date;
    TextView event_detail_time;
    ListView event_detail_invite_list;
    Button edit_event_guests_button;
    Button back_button;
    EditText event_detail_title_input;
    EditText event_detail_description_input;
    EditText event_detail_date_input;
    EditText event_detail_time_input;
    DatePickerDialog datePickerDialog;
    Place place;
    AutocompleteSupportFragment autocompleteFragment;
    boolean isEditMode = false;
    FirebaseUser firebaseUser;
    double lat;
    double lon;
    ToolKit toolKit;
    String location_name_input;
    ArrayList<User> invited_users;
    TextView location_placeholder;
    ArrayList<User> updated;
    ArrayList<String> old_list = new ArrayList<String>();
    ArrayList<User> all_friends = new ArrayList<User>();
    ArrayList<String> new_list = new ArrayList<String>();
    DatabaseReference notification_root;
    DatabaseReference userReference;
    DatabaseReference user_event;
    User current_user;
    TextView event_details_end_date;
    TextView event_details_end_time;
    TextView event_details_end_date_input;
    TextView event_details_end_time_input;
    String min_event_date;
    long minEventDate;
    boolean isSafety;
    int clickCounter;



    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("User Information").child(firebaseUser.getUid());
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                current_user = new User();
                current_user.setUserName(dataSnapshot.child("userName").getValue().toString());
                current_user.setImageURL(dataSnapshot.child("imageURL").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        clickCounter = 0;
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
        setContentView(R.layout.event_details);
        //Textviews that show event details
        event_detail_title = findViewById(R.id.event_detail_title);
        event_detail_description = findViewById(R.id.event_detail_description);
        event_detail_date = findViewById(R.id.event_detail_date);
        event_detail_time = findViewById(R.id.event_detail_time);
        event_detail_invite_list = findViewById(R.id.event_detail_invite_list);
        final Button edit_event_button = findViewById(R.id.edit_event_button);
        edit_event_guests_button = findViewById(R.id.event_detail_invite_guests);
        location_placeholder = findViewById(R.id.location_placeholder);
        event_details_end_date = findViewById(R.id.event_details_end_date);
        event_details_end_time = findViewById(R.id.event_details_end_time);
        event_details_end_date_input = findViewById(R.id.event_details_end_date_input);
        event_details_end_time_input = findViewById(R.id.event_details_end_time_input);



        //EditText that is enabled when "EDIT" is clicked
        event_detail_title_input = findViewById(R.id.event_detail_title_input);
        event_detail_description_input = findViewById(R.id.event_detail_description_input);
        event_detail_date_input = findViewById(R.id.event_detail_date_input);
        event_detail_time_input = findViewById(R.id.event_detail_time_input);

        back_button = findViewById(R.id.event_detail_back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        final Intent reciever_intent = getIntent();

        final String id = reciever_intent.getStringExtra("event_id");
        isSafety = reciever_intent.getBooleanExtra("isSafety", false);
        String title = reciever_intent.getStringExtra("event_name");
        String description = reciever_intent.getStringExtra("event_desc");
        String date = reciever_intent.getStringExtra("event_date");
        String time = reciever_intent.getStringExtra("event_time");
        String end_date = reciever_intent.getStringExtra("event_end_date");
        String end_time = reciever_intent.getStringExtra("event_end_time");
        Bundle args = reciever_intent.getBundleExtra("BUNDLE");
        if(args != null) {
            invited_users = (ArrayList<User>) args.getSerializable("ARRAYLIST");
            String[] invited_users_adapter = new String[invited_users.size()];
            old_list = new ArrayList<String>();
            for(int i = 0; i < invited_users.size(); i++) {
                invited_users_adapter[i] = invited_users.get(i).getUserName();
                old_list.add(invited_users.get(i).getUID());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    R.layout.invite_guest_list_item, R.id.aaaaaaaa, invited_users_adapter);
            event_detail_invite_list.setAdapter(adapter);
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("user_event").child(firebaseUser.getUid()).child(id);

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("isOwner").exists()) {
                    if(dataSnapshot.child("isOwner").getValue().toString().equals("false")) {
                        edit_event_button.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String location_name = reciever_intent.getStringExtra("location_name");

        toolKit = new ToolKit();

        event_detail_title.setText(title);
        event_detail_description.setText(description);
        event_detail_date.setText(date);
        event_detail_time.setText(time);
        event_details_end_date.setText(end_date);
        event_details_end_time.setText(end_time);

        //Initial filling in data
        event_detail_title_input.setText(event_detail_title.getText().toString());
        event_detail_description_input.setText(event_detail_description.getText().toString());
        event_detail_date_input.setText(event_detail_date.getText().toString());
        event_detail_time_input.setText(event_detail_time.getText().toString());
        event_details_end_time_input.setText(end_time);
        event_details_end_date_input.setText(end_date);

        final LinearLayout linear_layout_details = findViewById(R.id.linear_layout_details);
        linear_layout_details.setVisibility(View.INVISIBLE);
        location_placeholder.setText(location_name);
        location_placeholder.setVisibility(View.VISIBLE);

        Places.initialize(getApplicationContext(),"AIzaSyCjncU-Fe5pQKOc85zuGoR9XEs61joNajc");
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.event_detail_location);
        autocompleteFragment.setText(location_name);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place pl) {
                final LatLng latLng = pl.getLatLng();
                place = pl;
                lat = latLng.latitude;
                lon = latLng.longitude;
                location_name_input = pl.getName();
            }
            @Override
            public void onError(@NonNull Status status) {
            }
        });

        if(isSafety) {
            edit_event_guests_button.setVisibility(View.INVISIBLE);
        }

        DatabaseReference friends_db = FirebaseDatabase.getInstance().getReference("Friends").child(firebaseUser.getUid());
        friends_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot element : dataSnapshot.getChildren()) {
                    User user = new User();
                    user.setUID(element.child("uid").getValue().toString());
                    user.setUserName(element.child("friendName").getValue().toString());
                    all_friends.add(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        final DatabaseReference event_root = FirebaseDatabase.getInstance().getReference("user_event").child(firebaseUser.getUid()).child(id).child("invited_users");
        event_root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot element : dataSnapshot.getChildren()) {
                    old_list.add(element.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


        edit_event_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEditMode) {
                    isEditMode = true;
                    back_button.setVisibility(View.GONE);
                    location_placeholder.setVisibility(View.INVISIBLE);
                    linear_layout_details.setVisibility(View.VISIBLE);
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    edit_event_button.setText(R.string.invite_guests_done_button);
                    event_detail_title_input.setVisibility(View.VISIBLE);
                    event_detail_description_input.setVisibility(View.VISIBLE);
                    event_detail_date_input.setVisibility(View.VISIBLE);
                    event_detail_time_input.setVisibility(View.VISIBLE);
                    event_details_end_date_input.setVisibility(View.VISIBLE);
                    event_details_end_time_input.setVisibility(View.VISIBLE);

                    event_detail_title.setVisibility(View.INVISIBLE);
                    event_detail_description.setVisibility(View.INVISIBLE);
                    event_detail_date.setVisibility(View.INVISIBLE);
                    event_detail_time.setVisibility(View.INVISIBLE);
                    event_details_end_date.setVisibility(View.INVISIBLE);
                    event_details_end_time.setVisibility(View.INVISIBLE);

                    event_detail_title_input.setText(event_detail_title_input.getText().toString());
                    event_detail_description_input.setText(event_detail_description_input.getText().toString());
                    event_details_end_time_input.setText(event_details_end_time_input.getText().toString());
                    event_details_end_date_input.setText(event_details_end_date_input.getText().toString());

                    edit_event_guests_button.setVisibility(View.VISIBLE);

                    if(isSafety) {
                        edit_event_guests_button.setVisibility(View.INVISIBLE);
                        linear_layout_details.setVisibility(View.INVISIBLE);
                    }

                    event_detail_title_input.requestFocus();
                } else {

                    if(!isInputValid(event_detail_title_input, event_detail_date_input, event_detail_time_input, event_details_end_date_input, event_details_end_time_input, place)) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid form", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        isEditMode = false;
                        back_button.setVisibility(View.VISIBLE);
                        linear_layout_details.setVisibility(View.INVISIBLE);
                        location_placeholder.setVisibility(View.VISIBLE);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                        edit_event_button.setText(R.string.event_edit_button_label);

                        event_detail_title_input.setVisibility(View.INVISIBLE);
                        event_detail_description_input.setVisibility(View.INVISIBLE);
                        event_detail_date_input.setVisibility(View.INVISIBLE);
                        event_detail_time_input.setVisibility(View.INVISIBLE);
                        event_details_end_date_input.setVisibility(View.INVISIBLE);
                        event_details_end_time_input.setVisibility(View.INVISIBLE);

                        event_detail_title.setVisibility(View.VISIBLE);
                        event_detail_description.setVisibility(View.VISIBLE);
                        event_detail_date.setVisibility(View.VISIBLE);
                        event_detail_time.setVisibility(View.VISIBLE);
                        event_details_end_time.setVisibility(View.VISIBLE);
                        event_details_end_date.setVisibility(View.VISIBLE);

                        DatabaseReference db_root = FirebaseDatabase.getInstance().getReference("user_event").child(firebaseUser.getUid()).child(id);

                        Map<String, Object> event_root_map = new HashMap<String, Object>();
                        Map<String, Object> event_location_map = new HashMap<String, Object>();
                        Map<String, Object> event_invited_users_map = new HashMap<String, Object>();
                        event_root_map.put("name", (event_detail_title_input.getText().toString()));
                        event_root_map.put("description", (event_detail_description_input.getText().toString()));
                        event_root_map.put("start_date", (event_detail_date_input.getText().toString()));
                        event_root_map.put("start_time", (event_detail_time_input.getText().toString()));
                        event_root_map.put("end_date", (event_details_end_date_input.getText().toString()));
                        event_root_map.put("end_time", (event_details_end_time_input.getText().toString()));
                        event_root_map.put("area", toolKit.getLocationText(lat, lon, EventDetailsActivity.this));
                        event_root_map.put("location_name", location_name_input);
                        event_location_map.put("latitude", lat);
                        event_location_map.put("longitude", lon);
                        event_invited_users_map.put("invited_users", updated);
                        db_root.updateChildren(event_root_map);
                        db_root.child("location").updateChildren(event_location_map);

                        if(clickCounter > 0) {
                            if (updated != null) {
                                for (User user : updated) {
                                    new_list.add(user.getUID());
                                }
                            }

                            Set<String> set = new HashSet<>(old_list);
                            old_list.clear();
                            old_list.addAll(set);

                            set = new HashSet<>(new_list);
                            new_list.clear();
                            new_list.addAll(set);


                            ArrayList<User> users_to_add = new ArrayList<User>();
                            ArrayList<User> users_to_remove = new ArrayList<User>();

                            for (String uid : new_list) {
                                if (!old_list.contains(uid)) {
                                    //add `user`
                                    for (User user : all_friends) {
                                        if (user.getUID().equals(uid)) {
                                            users_to_add.add(user);
                                        }
                                    }
                                }
                            }
                            for (String uid : old_list) {
                                if (!new_list.contains(uid)) {
                                    //remove `user`
                                    for (User user : all_friends) {
                                        if (user.getUID().equals(uid)) {
                                            users_to_remove.add(user);
                                        }
                                    }
                                }
                            }

                            System.out.println(all_friends);
                            System.out.println(old_list);

                            //add new users to event
                            for (User user : users_to_add) {
                                event_root.child(user.getUID()).child("name").setValue(user.getUserName());
                                event_root.child(user.getUID()).child("status").setValue("invited");


                            }

                            //remove old users from event
                            for (User user : users_to_remove) {
                                event_root.child(user.getUID()).removeValue();
                            }

                            //Send notification to users recently added to event
                            notification_root = FirebaseDatabase.getInstance().getReference("Notifications");
                            for(User user : users_to_add) {
                                notification_root.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("friendName").setValue(current_user.getUserName());
                                notification_root.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("imageURL").setValue(current_user.getImageURL());
                                notification_root.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("request_time").setValue(getRequestTime());
                                notification_root.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("request_type").setValue("invite_sent");
                                notification_root.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("uid").setValue(firebaseUser.getUid());
                                notification_root.child(user.getUID()).child("friend_request_notifications").child(id)
                                        .child("eventId").setValue(id);

                            }

                            //Remove event and notification from recently removed users.
                            user_event = FirebaseDatabase.getInstance().getReference("user_event");
                            for(final User user : users_to_remove) {
                                notification_root.child(user.getUID()).child("friend_request_notifications").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            //Toast.makeText(getApplicationContext(), "sdlfkjsk", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                user_event.child(user.getUID()).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        System.out.println("DELETING EVENT FROM CURRENT ID " + id + " FROM " + user.getUID());
                                    }
                                });
                            }

                            event_detail_title.setText(event_detail_title_input.getText().toString());
                            event_detail_description.setText(event_detail_description_input.getText().toString());
                            event_detail_date.setText(event_detail_date_input.getText().toString());
                            event_detail_time.setText(event_detail_time_input.getText().toString());
                            location_placeholder.setText(location_name_input);
                            event_details_end_time.setText(event_details_end_time_input.getText().toString());
                            event_details_end_date.setText(event_details_end_date_input.getText().toString());

                            edit_event_guests_button.setVisibility(View.INVISIBLE);
                            if(isSafety) {
                                edit_event_guests_button.setVisibility(View.INVISIBLE);
                                linear_layout_details.setVisibility(View.INVISIBLE);
                            }
                        }
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }

                }

            }
        });

        event_detail_date_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(EventDetailsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                String date = (monthOfYear+1) + "/" + dayOfMonth + "/" + year;
                                event_detail_date_input.setText(date);
                                event_detail_date.setText(date);
                                min_event_date = date;

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        event_detail_time_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(EventDetailsActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
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
                            event_detail_time_input.setText(selectedHour + ":0" + selectedMinute + " " + am_pm);
                            event_detail_time.setText(selectedHour + ":0" + selectedMinute + " " + am_pm);
                        } else {
                            event_detail_time_input.setText(selectedHour + ":" + selectedMinute + " " + am_pm);
                            event_detail_time.setText(selectedHour + ":" + selectedMinute + " " + am_pm);
                        }

                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        event_details_end_date_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(EventDetailsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                String date = (monthOfYear+1) + "/" + dayOfMonth + "/" + year;
                                min_event_date = date;
                                event_details_end_date_input.setText(date);
                                event_details_end_date.setText(date);

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

        event_details_end_time_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(EventDetailsActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
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
                            event_details_end_time_input.setText(selectedHour + ":0" + selectedMinute + " " + am_pm);
                            event_details_end_time.setText(selectedHour + ":0" + selectedMinute + " " + am_pm);
                        } else {
                            event_details_end_time_input.setText(selectedHour + ":" + selectedMinute + " " + am_pm);
                            event_details_end_time.setText(selectedHour + ":" + selectedMinute + " " + am_pm);
                        }

                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        Places.initialize(getApplicationContext(),"AIzaSyCjncU-Fe5pQKOc85zuGoR9XEs61joNajc");
        //PlacesClient placesClient = Places.createClient(this);

        edit_event_guests_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCounter++;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                ArrayList<String> selected = new ArrayList<String>();
                for(int i = 0; i < event_detail_invite_list.getCount(); i++) {
                    selected.add(event_detail_invite_list.getItemAtPosition(i).toString());
                }
                Intent intent = new Intent(v.getContext(), InviteGuestsActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("BUNDLE", invited_users);
                intent.putStringArrayListExtra("selected", selected);
                intent.putExtra("ARRAYLIST", args);
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
                ArrayList<User> selected = (ArrayList<User>) args.getSerializable("ARRAYLIST");
                updated = (ArrayList<User>) args.getSerializable("ARRAYLIST");
                String[] selected_adapter = new String[selected.size()];
                for(int i = 0; i < selected.size(); i++) {
                    selected_adapter[i] = selected.get(i).getUserName();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.invite_guest_list_item, R.id.aaaaaaaa, selected_adapter);
                event_detail_invite_list = findViewById(R.id.event_detail_invite_list);
                event_detail_invite_list.setAdapter(adapter);
            } else {

            }

        }
    }

    private boolean isInputValid(TextView name, TextView date, TextView time, TextView end_date,
                                 TextView end_time, Place place) {
        if((!name.getText().toString().equals("")) &&  (!date.getText().toString().equals("")) &&
                (!time.getText().toString().equals("")) && (place != null) &&
                (!end_date.getText().toString().equals("")) &&
                (!end_time.getText().toString().equals(""))) {
            return true;
        } else {
            return false;
        }
    }

    private String getLocationText(double latitude, double longitude) {
        String locationText = "";
        Geocoder geocoder;
        geocoder = new Geocoder(EventDetailsActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude,1);
            locationText = addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea();
            Log.d("mylog","complete address: " + addresses.toString());
            Log.d("mylog","address: " + locationText);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return locationText;
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
