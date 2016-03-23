package com.supinfo.supsms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {

    public static String username;
    public static String password;


    /* * Back button should be pressed 2 time to close the app * */
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

    public String my_username;
    public String my_password;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        final EditText editText = (EditText) findViewById(R.id.editTextUsername);
        final EditText editText1 = (EditText) findViewById(R.id.editTextPassword);
        Button login = (Button) findViewById(R.id.buttonLogin);

/* * Called when button is pressed and execute task in background * */
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                my_username = editText.getText().toString();
                my_password = editText1.getText().toString();

                Connection asyncTask = new Connection();
                asyncTask.execute();
            }
        });
    }

    class Connection extends AsyncTask<Void,Void,Boolean> {

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://91.121.105.200/API/");

        @Override
        protected Boolean doInBackground(Void... params) {

            List<NameValuePair> pairs = new ArrayList<>(3);
            pairs.add(new BasicNameValuePair("action","login"));
            pairs.add(new BasicNameValuePair("login",my_username));
            pairs.add(new BasicNameValuePair("password",my_password));

            try {
                post.setEntity(new UrlEncodedFormEntity(pairs)); // encode le tab pour créer une requete
                HttpResponse response = client.execute(post);
                    // si la requete s'est bien passée
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                    // Convert httpresponse to String
                    String responseAsString = EntityUtils.toString(response.getEntity());
                    Log.d("debug",responseAsString);

                    // Convert String to Json thanks to Gson library
                    Gson gson = new Gson();
                    Response result =  gson.fromJson(responseAsString,Response.class);

                    if (result.getSuccess()) {
                        LoginActivity.username = my_username;
                        LoginActivity.password = my_password;
                    }

                    return result.getSuccess(); // renvoie le resultat de la réponse httpey
                }
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);

                startActivity(intent);
            }
            else  {
                Toast.makeText(LoginActivity.this, "Retry it",Toast.LENGTH_LONG).show();
            }
        }
    }
}
