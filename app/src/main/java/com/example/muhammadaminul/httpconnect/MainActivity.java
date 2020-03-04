package com.example.muhammadaminul.httpconnect;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private EditText _urlText;
    private TextView _textView;
    private Button _btSubmit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _urlText = (EditText) findViewById(R.id._textURL);
        _textView = (TextView) findViewById(R.id._txtResult);
        _btSubmit = findViewById(R.id._btSubmit);
        _btSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String stringUrl = _urlText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageText().execute(stringUrl);
        } else {
            _textView.setText("No network connection available.");
        }
    }


    private class DownloadWebpageText extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid."+urls[0];
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            jsonParsing(result);
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                //Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }

    private void jsonParsing(String in){
        try{
            JSONObject toParse = new JSONObject(in);

            _textView.setText("Nama Kota: "+toParse.getString("name")+"\n");

            JSONObject cuaca = new JSONObject(new JSONArray(toParse.getString("weather")).getString(0));
            _textView.append("Cuaca: "+cuaca.getString("description")+"\n");

            JSONObject koordinat = new JSONObject(toParse.getString("coord"));
            _textView.append("Koordinat (Longitude): "+koordinat.getString("lon")+"\n");
            _textView.append("Koordinat (Latitude): "+koordinat.getString("lat")+"\n");

        } catch (Exception e){
            _textView.setText("PARSING FAILURE");
            _textView.append("\n"+in);
        }
    }

}
