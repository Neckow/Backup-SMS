package com.supinfo.supsms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends Activity {

    int backButton = 0;
    public void onBackPressed()
    {
        if(backButton >= 1)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButton++;
        }
    }

    Button logout;
    Button smsBackup;
    Button contactBackup;
    Button about;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        logout = (Button) findViewById(R.id.buttonLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginAct = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(loginAct);
            }
        });

        smsBackup = (Button) findViewById(R.id.buttonSMS);
        smsBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connection anotherAsyncTask = new Connection();
                anotherAsyncTask.execute();
            }
        });

        contactBackup = (Button) findViewById(R.id.buttonContact);
        contactBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecondConnection anotherAsyncTask = new SecondConnection();
                anotherAsyncTask.execute();
            }
        });

        about = (Button) findViewById(R.id.buttonAbout);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutAct = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(aboutAct);
            }
        });
    }

    class Connection extends AsyncTask<Void,Void,Boolean> {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://91.121.105.200/API/");

        @Override
        protected Boolean doInBackground(Void... params) {

            Gson gson = new Gson(); // Instantiate Gson object

            String[] smsTab;
            smsTab = new String[2];
            smsTab[0] = "inbox";
            smsTab[1] = "sent";
            int i;

            for (i =0 ; i<smsTab.length;i++) {
                Log.d("debug :",smsTab[i]);

                List<Sms> smsArrayList = new ArrayList<>();
                Uri message = Uri.parse("content://sms/"+smsTab[i]);

                // Get the context and create cursor
                Cursor c = getContentResolver().query(message, null, null, null, null);

                if (c.getCount()<= 0) {
                    continue;
                }

                int totalSMS = c.getCount();
                // Get Sms wich put in Sms Object, which put in array list
                if (c.moveToFirst()) {
                    for (i = 0; i < totalSMS; i++) {

                        Sms smsObject = new Sms();

                        smsObject.setNumber(c.getString(c.getColumnIndexOrThrow("address")));
                        smsObject.setContent(c.getString(c.getColumnIndexOrThrow("body"))); // recupere le content du sms

                        smsArrayList.add(smsObject);
                        c.moveToNext();
                    }
                }
                c.close();

                List<NameValuePair> pairs = new ArrayList<>(5);
                pairs.add(new BasicNameValuePair("action","backupsms"));
                pairs.add(new BasicNameValuePair("login",LoginActivity.username));
                pairs.add(new BasicNameValuePair("password",LoginActivity.password));
                pairs.add(new BasicNameValuePair("box",smsTab[i]));
                pairs.add(new BasicNameValuePair("sms",gson.toJson(smsArrayList)));

                try {
                    post.setEntity(new UrlEncodedFormEntity(pairs)); // encoding url for request (array list in parameter)
                    HttpResponse response = client.execute(post);
                    // si la requete s'est bien passée
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                        String responseAsString = EntityUtils.toString(response.getEntity());
                        Log.d("debug", responseAsString);

                        Response result =  gson.fromJson(responseAsString,Response.class);

                        if (!result.getSuccess()){
                            return false;
                        }
                    }
                    else
                        return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                Toast.makeText(HomeActivity.this, "Backup réussi",Toast.LENGTH_LONG).show();
            }
            else  {
                Toast.makeText(HomeActivity.this, "Backup raté",Toast.LENGTH_LONG).show();
            }
        }
    }

    /* ________________________________________________________ */

    class SecondConnection extends AsyncTask<Void,Void,Boolean> {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://91.121.105.200/API/");

        @Override
        protected Boolean doInBackground(Void... params) {

            Gson gson = new Gson(); // Instantiate Gson object

            List<Contacts> contactsArrayList = new ArrayList<>();

            // Get the context and create cursor
            //ContentResolver ConResolver = getContentResolver();
            Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            // loop if there are contacts (get by cursor)
            if (c.getCount() > 0) {

                while (c.moveToNext()) {
                    Contacts myContact = new Contacts();
                    myContact.setName(c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

                    // Verify if contact has phone number
                    if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        // Get number thanks to ID
                        Cursor c2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);
                        // Get all phone number (if there are many phone number !)
                        while (c2.moveToNext()) {
                            myContact.addNumber(c2.getString(c2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            // Check
                            Log.d("Contact: ",myContact.getName()+" "+myContact.getNumber());
                        }
                        c2.close();
                    }
                    contactsArrayList.add(myContact);
                }
            }
            Log.d("Data contact : ", String.valueOf(contactsArrayList.size()));
            List<NameValuePair> pairs = new ArrayList<>(4);
            pairs.add(new BasicNameValuePair("action","backupcontacts"));
            pairs.add(new BasicNameValuePair("login",LoginActivity.username));
            pairs.add(new BasicNameValuePair("password",LoginActivity.password));
            pairs.add(new BasicNameValuePair("contacts",gson.toJson(contactsArrayList)));

            try {
                post.setEntity(new UrlEncodedFormEntity(pairs)); // à revoir
                HttpResponse response = client.execute(post);
                // si la requete s'est bien passée
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    String responseAsString = EntityUtils.toString(response.getEntity());
                    Log.d("debug", responseAsString);

                    Response result =  gson.fromJson(responseAsString,Response.class);

                    if (!result.getSuccess()){
                        return false;
                    }
                }
                else
                    return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                Toast.makeText(HomeActivity.this, "Backup réussi",Toast.LENGTH_LONG).show();
            }
            else  {
                Toast.makeText(HomeActivity.this, "Backup raté",Toast.LENGTH_LONG).show();
            }
        }
    }

}
