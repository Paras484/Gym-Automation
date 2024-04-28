package com.paw.gymautomation;

import static com.paw.gymautomation.MainActivity.showToast;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomizedMessage extends AppCompatActivity {

    private EditText customMessageEditText;
    private Button sendButton;
    private List<User> users;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customized_message);

        // Initialize views
        customMessageEditText = findViewById(R.id.customMessageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference("Members");

        // Find the back button ImageView
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton == null) {
            throw new IllegalStateException("ImageView backButton not found in activity_customized_message.xml");
        }

        // Set click listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and return to the previous one
                finish();
            }
        });

        // Set click listener for send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the customized message from EditText
                String message = customMessageEditText.getText().toString().trim();

                // Check if message is not empty
                if (!message.isEmpty()) {
                    // Send the message to all gym members
                    sendCustomizedMessageToMembers(message);
                } else {
                    // Show a toast message indicating that the message is empty
                    Toast.makeText(CustomizedMessage.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Fetch user data from Firebase database
        fetchUserDataFromFirebase();
    }

    private void fetchUserDataFromFirebase() {
        users = new ArrayList<>();

        // Fetch user data from Firebase database
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate over the dataSnapshot to retrieve user data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get user data from the snapshot
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if data retrieval is cancelled
                Toast.makeText(CustomizedMessage.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendCustomizedMessageToMembers(String message) {
        // Iterate over the list of users and send the message to each one
        for (User user : users) {
            // Send the message to the user's phone number
            sendSMS(user.getPhoneNumber(), message);
        }

        // Show a toast message indicating that the message has been sent
        Toast.makeText(this, "Message sent to all gym members", Toast.LENGTH_SHORT).show();
    }

    // Method to send SMS message
    private void sendSMS(String phoneNumber, String message) {
        try {
            // Get the default instance of the SmsManager
            SmsManager smsManager = SmsManager.getDefault();

            // Divide the message into parts if it's too long
            ArrayList<String> parts = smsManager.divideMessage(message);

            // Send the SMS message
            for (String part : parts) {
                smsManager.sendTextMessage(phoneNumber, null, part, null, null);
            }

            // Show a log message indicating the SMS was sent successfully
            Log.d("SMS", "SMS sent successfully to " + phoneNumber);

            // Show a toast message indicating the SMS was sent successfully
           // showToast(CustomizedMessage.this, "SMS sent successfully to " + phoneNumber);

        } catch (Exception e) {
            e.printStackTrace(); // Log the exception stack trace
            // Show a log message indicating an error occurred
            Log.e("SMS", "Error sending SMS to " + phoneNumber + ": " + e.getMessage());
            // Show a toast message indicating an error occurred
            showToast(CustomizedMessage.this, "Error sending SMS to " + phoneNumber + ". Check logcat for details.");
        }
    }

}
