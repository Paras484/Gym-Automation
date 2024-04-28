package com.paw.gymautomation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TableLayout membersTable;
    private List<User> usersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference("Members");

        // Find the TableLayout
        membersTable = findViewById(R.id.membersTable);
        if (membersTable == null) {
            throw new IllegalStateException("TableLayout membersTable not found in activity_database.xml");
        }

        // Load existing users data from Firebase
        loadUsersData();

        // Find the Add Member button
        Button addMemberButton = findViewById(R.id.addMemberButton);
        if (addMemberButton == null) {
            throw new IllegalStateException("Button addMemberButton not found in activity_database.xml");
        }

        // Find the Save button
        Button saveButton = findViewById(R.id.saveButton);
        if (saveButton == null) {
            throw new IllegalStateException("Button saveButton not found in activity_database.xml");
        }

        // Set OnClickListener for Save Button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save data to Firebase
                saveData();
            }
        });


        // Set OnClickListener for Add Member Button
        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a new user row
                addNewUserRow();
            }
        });
    }

    // Saving Data
    private void saveData() {
        // Clear the existing data in the database
        mDatabase.removeValue();

        // Iterate over the TableLayout to retrieve user data
        for (int i = 1; i < membersTable.getChildCount(); i++) {
            TableRow row = (TableRow) membersTable.getChildAt(i);
            EditText nameEditText = row.findViewById(R.id.nameEditText);
            EditText phoneNumberEditText = row.findViewById(R.id.phoneNumberEditText);
            EditText startDateEditText = row.findViewById(R.id.startDateEditText);
            EditText expiryDateEditText = row.findViewById(R.id.expiryDateEditText);

            // Extract data from EditText fields
            String name = nameEditText.getText().toString().trim();
            String phoneNumber = phoneNumberEditText.getText().toString().trim();
            String startDate = startDateEditText.getText().toString().trim();
            String expiryDate = expiryDateEditText.getText().toString().trim();

            // Validate input fields
            if (isValidInput(name, phoneNumber, startDate, expiryDate)) {
                // Create a new HashMap to store the user data
                HashMap<String, String> userData = new HashMap<>();
                userData.put("name", name);
                userData.put("phoneNumber", phoneNumber);
                userData.put("startDate", startDate);
                userData.put("expiryDate", expiryDate);

                // Push the new user data to Firebase
                String userId = mDatabase.push().getKey(); // Generate a unique key for the user
                mDatabase.child(userId).setValue(userData);

                // Log the saved user data
                Log.d("DatabaseActivity", "User saved: " + name + ", " + phoneNumber + ", " + startDate + ", " + expiryDate);
            } else {
                // Show a toast message if input is invalid
                Toast.makeText(DatabaseActivity.this, "Invalid input. Please check and try again.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Show a toast message
        Toast.makeText(DatabaseActivity.this, "Data saved successfully", Toast.LENGTH_SHORT).show();
    }

    // Loading Data
    private void loadUsersData() {
        // Read data from Firebase database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing list
                usersList.clear();

                // Iterate over the dataSnapshot to retrieve user data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the user ID (Firebase key)
                    String userId = snapshot.getKey();

                    // Get the value as a HashMap
                    HashMap<String, String> userData = (HashMap<String, String>) snapshot.getValue();

                    // Extract user data from the HashMap
                    String name = userData.get("name");
                    String phoneNumber = userData.get("phoneNumber");
                    String startDate = userData.get("startDate");
                    String expiryDate = userData.get("expiryDate");

                    // Create a new User object with userId
                    User user = new User(userId, name, phoneNumber, startDate, expiryDate);

                    // Add the user to the list
                    usersList.add(user);
                }

                // Display users in the table
                displayUsers();

                // Log statement to check if data is loaded from Firebase
                Log.d("DatabaseActivity", "Data loaded from Firebase");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("DatabaseActivity", "Failed to read value.", error.toException());
            }
        });
    }


    // Display users in the TableLayout
    private void displayUsers() {
        // Clear existing rows
        membersTable.removeAllViews();

        // Add header row
        TableRow headerRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.table_row_member_header, null);
        membersTable.addView(headerRow);

        // Add rows for each user
        for (final User user : usersList) {
            if (user != null) {
                TableRow newRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.table_row_member, null);
                EditText nameEditText = newRow.findViewById(R.id.nameEditText);
                EditText phoneNumberEditText = newRow.findViewById(R.id.phoneNumberEditText);
                EditText startDateEditText = newRow.findViewById(R.id.startDateEditText);
                EditText expiryDateEditText = newRow.findViewById(R.id.expiryDateEditText);
                Button deleteButton = newRow.findViewById(R.id.deleteButton); // Find delete button

                // Set data to EditText fields
                nameEditText.setText(user.getName());
                phoneNumberEditText.setText(user.getPhoneNumber());
                startDateEditText.setText(user.getStartDate());
                expiryDateEditText.setText(user.getExpiryDate());

                // Set onClickListener for delete button
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteUser(user);
                    }
                });

                // Add the new row to the TableLayout
                membersTable.addView(newRow);
            } else {
                Log.e("DatabaseActivity", "User object is null");
            }
        }
    }


    // Add a new row to the TableLayout with data
    private void addRowWithData(String name, String phoneNumber, String startDate, String expiryDate) {
        TableRow newRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.table_row_member, null);
        EditText nameEditText = newRow.findViewById(R.id.nameEditText);
        EditText phoneNumberEditText = newRow.findViewById(R.id.phoneNumberEditText);
        EditText startDateEditText = newRow.findViewById(R.id.startDateEditText);
        EditText expiryDateEditText = newRow.findViewById(R.id.expiryDateEditText);

        // Set data to EditText fields
        nameEditText.setText(name);
        phoneNumberEditText.setText(phoneNumber);
        startDateEditText.setText(startDate);
        expiryDateEditText.setText(expiryDate);

        // Add the new row to the TableLayout
        membersTable.addView(newRow);
    }

    // Delete a user from the TableLayout and Firebase database
    private void deleteUser(User user) {
        if (user != null && !TextUtils.isEmpty(user.getUserId())) {
            // Remove the user from the local list
            usersList.remove(user);

            // Remove the user row from the TableLayout
            for (int i = 1; i < membersTable.getChildCount(); i++) {
                TableRow row = (TableRow) membersTable.getChildAt(i);
                EditText nameEditText = row.findViewById(R.id.nameEditText);
                if (nameEditText.getText().toString().equals(user.getName())) {
                    membersTable.removeView(row);
                    break;
                }
            }

            // Remove the user from the Firebase database
            mDatabase.child(user.getUserId()).removeValue();
        }
    }

    // Add a new row to the TableLayout for entering user data
    private void addNewUserRow() {
        // Inflate a new table row with EditText fields for user details
        TableRow newRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.table_row_member, null);

        // Add the new row to the TableLayout
        membersTable.addView(newRow);
    }

    // Validate input fields
    private boolean isValidInput(String name, String phoneNumber, String startDate, String expiryDate) {
        // Check if any field is empty
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(expiryDate)) {
            return false;
        }

        // Validate name (alphabets only)
        if (!isValidName(name)) {
            return false;
        }

        // Validate phone number (10-digit number)
        if (!isValidPhoneNumber(phoneNumber)) {
            return false;
        }

        // Validate date format (yyyy-mm-dd)
        if (!isValidDateFormat(startDate) || !isValidDateFormat(expiryDate)) {
            return false;
        }

        return true;
    }

    // Validate name (alphabets only)
    private boolean isValidName(String name) {
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    // Validate phone number (10-digit number)
    private boolean isValidPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile("^\\d{10}$");
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    // Validate date format (yyyy-mm-dd)
    private boolean isValidDateFormat(String date) {
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }
}
