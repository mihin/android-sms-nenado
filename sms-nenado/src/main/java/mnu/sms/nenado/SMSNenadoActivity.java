package mnu.sms.nenado;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.URI;

public class SMSNenadoActivity extends Activity {

    private ProgressDialog pd;
    private View tv_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        tv_date = findViewById(R.id.tv_sms_date);

//        startActivity(new Intent(getApplicationContext(), NotifySMSReceived.class));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String smsText = extras.getString(Constants.EXTRA_TEXT);
            final String smsAddres = extras.getString(Constants.EXTRA_ADRESS);
            final String smsDate = extras.getString(Constants.EXTRA_DATE);

            Toast.makeText(getApplicationContext(), "SMS received: " + smsText, Toast.LENGTH_SHORT).show();

            startRequest(smsText, smsAddres,smsDate);
        } else {
            Log.v(Constants.TAG, "SMSNenadoActivity bundle is null");
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRequest("test text", "123456", "12/12/1234");
            }
        });
    }

    private void startRequest(String smsText, String smsAddres, String smsDate) {
        new NetworkRequest().execute(new String[]{smsAddres, smsText,smsDate});
    }

    class NetworkRequest extends AsyncTask<String, Void, Integer> {


        private HttpClient mHttpClient;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(SMSNenadoActivity.this);
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            final String smsAddres = params[0];
            final String smsText = params[1];
            final String smsDate = params[2];

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
            String phone = sharedPreferences.getString(Constants.PREFS_PHONE, null);
            String email = sharedPreferences.getString(Constants.PREFS_EMAIL,null);

            return request() ? 1 : 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {

            pd.cancel();

            Toast.makeText(getApplicationContext(), "Network request: " + integer, Toast.LENGTH_SHORT).show();

            super.onPostExecute(integer);
        }

        private boolean request() {

            HttpPost httpPost = new HttpPost(URI.create("http://google.ru"));
            HttpGet httpGet = new HttpGet(URI.create("http://google.ru"));
            HttpClient httpClient = getHttpClient();

            synchronized (mHttpClient) {
                try {
//                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//                    nameValuePairs.add(new BasicNameValuePair("id", "12345"));
//                    nameValuePairs.add(new BasicNameValuePair("stringdata", "Hi"));
//                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                    HttpResponse response = httpClient.execute(httpPost);

                    HttpResponse response = httpClient.execute(httpGet);

                    String content = null;
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        return true;

                    }
                    //Header xsign = response.getFirstHeader("X-Sign");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        private HttpClient getHttpClient() {
            if (mHttpClient == null) {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setIntParameter(HttpConnectionParams.SO_TIMEOUT, 5000);
                httpParams.setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
                mHttpClient = new DefaultHttpClient();
            }
            return mHttpClient;
        }
    }

}
