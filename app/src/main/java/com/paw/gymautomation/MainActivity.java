package com.paw.gymautomation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String AUTOMATE_SWITCH_STATE = "automateSwitchState";

    private boolean isAutomateOn = false;

    private Button customizedMessageButton, databaseButton;
    private Switch automateSwitch;
    private SharedPreferences sharedPreferences;

    private static DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Request SMS permission
        requestSendSMSPermission();

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference("Members");

        // Find the switch by its ID
        automateSwitch = findViewById(R.id.automateSwitch);

        // Find the buttons by their IDs
        customizedMessageButton = findViewById(R.id.customizedMessage);
        databaseButton = findViewById(R.id.ViewDB);

        // Set saved state of switch
        isAutomateOn = sharedPreferences.getBoolean(AUTOMATE_SWITCH_STATE, false);
        automateSwitch.setChecked(isAutomateOn);

        // Set OnCheckedChangeListener for the switch
        automateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the flag to track if message sending automation is on
                isAutomateOn = isChecked;

                // Save the state of the switch in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(AUTOMATE_SWITCH_STATE, isChecked);
                editor.apply();

                // If automation is turned on, start sending messages
                if (isChecked) {
                    sendMembershipExpirationSMS(MainActivity.this);
                }
            }
        });

        // Set click listener for the customized message button
        customizedMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the CustomizedMessage activity
                startActivity(new Intent(MainActivity.this, CustomizedMessage.class));
            }
        });

        // Set click listener for the database button
        databaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the DatabaseActivity activity
                startActivity(new Intent(MainActivity.this, DatabaseActivity.class));
            }
        });
    }


    // Method to request SEND_SMS permission
    private void requestSendSMSPermission() {
        // Check if permission is granted, if not, request it
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with sending SMS
            if (isAutomateOn) {
                sendMembershipExpirationSMS(MainActivity.this);
            }
        } else {
            // Permission denied, handle accordingly (show message, disable functionality, etc.)
            // showToast("Permission denied to send SMS");
        }
    }

    // Method to send SMS messages to users whose gym membership is about to expire or has expired
    // Method to send SMS messages to users whose gym membership is about to expire or has expired
    // Method to send SMS messages to users whose gym membership is about to expire or has expired
    public void sendMembershipExpirationSMS(Context context) {
        // Initialize Firebase database reference
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Members");

        // Fetch user data from Firebase database
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate over the dataSnapshot to retrieve user data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get user data from the snapshot
                    User user = snapshot.getValue(User.class);

                    // Log user data
                    Log.d("User Data", "Name: " + user.getName() + ", Phone Number: " + user.getPhoneNumber() + ", Start Date: " + user.getStartDate() + ", Expiry Date: " + user.getExpiryDate());

                    // Check membership expiration status and send SMS accordingly
                    if (user != null && isMembershipExpiringSoon(user)) {
                        // Adjust message content based on membership status
                        String message;
                        if (isMembershipExpired(user)) {
                            message = "Your gym membership has expired.";
                        } else {
                            message = "Your gym membership will expire on " + user.getExpiryDate();
                        }

                        // Include start date in the message
                        message += " (Expiry Date: " + user.getExpiryDate() + ")";

                        // Send SMS
                        sendSMS(context, user.getPhoneNumber(), message);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if data retrieval is cancelled
                Log.e("Firebase Error", "Error fetching user data: " + databaseError.getMessage());
            }
        });
    }




    // Method to check if a user's gym membership is about to expire
    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isMembershipExpiringSoon(User user) {
        try {
            // Get the current date
            LocalDate currentDate = LocalDate.now();

            // Parse the membership expiration date from the user object
            LocalDate expiryDate = LocalDate.parse(user.getExpiryDate());

            // Check if the current date is after the membership expiration date
            // You can adjust the threshold as needed, for example, within 30 days from today
            return currentDate.isAfter(expiryDate);

        } catch (Exception e) {
            // Handle the case where the membership expiry date is not in the correct format
            // Log the error or handle it based on your application's requirements
            e.printStackTrace();
            return false; // Return false if unable to parse the date
        }
    }

    // Method to check if a user's gym membership has expired
    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isMembershipExpired(User user) {
        try {
            // Get the current date
            LocalDate currentDate = LocalDate.now();

            // Parse the membership expiration date from the user object
            LocalDate expiryDate = LocalDate.parse(user.getExpiryDate());

            // Check if the current date is after the membership expiration date
            return currentDate.isAfter(expiryDate);

        } catch (Exception e) {
            // Handle the case where the membership expiry date is not in the correct format
            // Log the error or handle it based on your application's requirements
            e.printStackTrace();
            return false; // Return false if unable to parse the date
        }
    }

    // Method to send SMS message
    // Method to send SMS message
    private static void sendSMS(Context context, String phoneNumber, String message) {
        try {
            // Get the default instance of the SmsManager
            SmsManager smsManager = SmsManager.getDefault();

            // Send the SMS message
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            // Show a log message indicating the SMS was sent successfully
            Log.d("SMS", "SMS sent successfully to " + phoneNumber);

            // Show a toast message indicating the SMS was sent successfully
            showToast(context, "SMS sent successfully to " + phoneNumber);

        } catch (Exception e) {
            e.printStackTrace(); // Log the exception stack trace
            // Show a log message indicating an error occurred
            Log.e("SMS", "Error sending SMS to " + phoneNumber + ": " + e.getMessage());
        }
    }


    // Method to show a toast message
    static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
