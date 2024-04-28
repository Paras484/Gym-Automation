package com.paw.gymautomation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;

import java.util.List;

public class UserListAdapter extends BaseAdapter {

    private Context context;
    private List<User> userList;

    public UserListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.table_row_member, parent, false);
            holder = new ViewHolder();
            holder.nameEditText = convertView.findViewById(R.id.nameEditText);
            holder.phoneNumberEditText = convertView.findViewById(R.id.phoneNumberEditText);
            holder.startDateEditText = convertView.findViewById(R.id.startDateEditText);
            holder.expiryDateEditText = convertView.findViewById(R.id.expiryDateEditText);
            holder.deleteButton = convertView.findViewById(R.id.deleteButton);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = userList.get(position);

        holder.nameEditText.setText(user.getName());
        holder.phoneNumberEditText.setText(user.getPhoneNumber());
        holder.startDateEditText.setText(user.getStartDate());
        holder.expiryDateEditText.setText(user.getExpiryDate());

        // Handle delete button click for this row
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the user from the list
                userList.remove(position);
                // Notify the adapter that the data set has changed
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    static class ViewHolder {
        EditText nameEditText;
        EditText phoneNumberEditText;
        EditText startDateEditText;
        EditText expiryDateEditText;
        Button deleteButton;
    }
}
