package mnu.sms.nenado;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSNenadoActivity extends Activity {

    private ProgressDialog pd;
    private TextView tv_sms_date, tv_sms_from, tv_sms_text;
    private EditText et_email, et_phone;
    private CheckBox cbPermission;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        startActivity(new Intent(getApplicationContext(), NotifySMSReceived.class));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String smsText = extras.getString(Constants.EXTRA_TEXT);
            final String smsAddres = extras.getString(Constants.EXTRA_ADRESS);
            final String smsDate = extras.getString(Constants.EXTRA_DATE);

            Toast.makeText(getApplicationContext(), "SMS received: " + smsText, Toast.LENGTH_SHORT).show();

            prepareFillForm(smsText, smsAddres, smsDate);
        } else {
            Log.v(Constants.TAG, "SMSNenadoActivity bundle is null");
        }

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                String date = dateFormat.format(new Date(System.currentTimeMillis()));
                prepareFillForm("test text", "ALPHA", date);
            }
        });
    }

    private void prepareFillForm(final String smsText, final String smsAddres, final String smsDate) {
        tv_sms_date = (TextView) findViewById(R.id.tv_sms_date);
        tv_sms_from = (TextView) findViewById(R.id.tv_sms_from);
        tv_sms_text = (TextView) findViewById(R.id.tv_sms_text);

        et_email = (EditText) findViewById(R.id.et_email);
        et_phone = (EditText) findViewById(R.id.et_phone);

        cbPermission = (CheckBox) findViewById(R.id.checkBox);

        sharedPreferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        String phone = sharedPreferences.getString(Constants.PREFS_PHONE, null);
        String email = sharedPreferences.getString(Constants.PREFS_EMAIL, null);

        tv_sms_date.setText(smsDate);
        tv_sms_from.setText(smsAddres);
        tv_sms_text.setText(smsText);

        if (!TextUtils.isEmpty(phone))
            et_email.setText(phone);
        if (!TextUtils.isEmpty(email))
            et_phone.setText(email);

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasPermission = cbPermission.isChecked();
                String phone, email;

                phone = et_phone.getText().toString();
                email = et_email.getText().toString();

                try {
                    PostRequestEntity postRequestEntity = new PostRequestEntity(smsText, smsAddres, smsDate, phone, email, hasPermission);
                    startRequest(postRequestEntity);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private String validatePhoneNumber(String phone){
        String phoneValidater = "^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$";
        Pattern pattern = Pattern.compile("phoneValidater");
        Matcher matcher = pattern.matcher(phone);

        if (matcher.matches()){
            //format phone +7 (960) 109-43-22
            return phone;
        } else {
            return null;
        }
    }

    private void startRequest(PostRequestEntity postRequestEntity) {
        new NetworkRequest().execute(postRequestEntity);
    }

    class NetworkRequest extends AsyncTask<PostRequestEntity, Void, Integer> {


        private HttpClient mHttpClient;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(SMSNenadoActivity.this);
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(PostRequestEntity... params) {
            return request(params[0]) ? 1 : 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {

            pd.cancel();

            Toast.makeText(getApplicationContext(), "Network request: " + integer, Toast.LENGTH_SHORT).show();

            super.onPostExecute(integer);
        }

        private boolean request(PostRequestEntity postRequestEntity) {
            final String url = Constants.URL + postRequestEntity.getUrl();

            HttpPost httpPost = new HttpPost(URI.create(url));
            HttpGet httpGet = new HttpGet(URI.create(url));
            HttpClient httpClient = getHttpClient();

            httpPost.setHeader("Origin", "http://smsnenado.ru");
            httpPost.setHeader("Referer", "http://smsnenado.ru/");
            httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
//            Origin:http://smsnenado.ru
//            Referer:http://smsnenado.ru/
//            X-Requested-With:XMLHttpRequest

            synchronized (mHttpClient) {
                try {
                    httpPost.setEntity(postRequestEntity.getEntity());
                    HttpResponse response = httpClient.execute(httpPost);

                    Log.d(Constants.TAG, "request: " + httpPost.getURI() + "data = " + httpPost.getEntity() + "\nresponse = " + response.getEntity());

//                    HttpResponse response = httpClient.execute(httpGet);

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

    class PostRequestEntity {
        private final String URL = "/sms.php";
        private final AbstractHttpEntity entity;

        public PostRequestEntity(String smsText, String smsAddres, String smsDate, String phone, String email, boolean hasPermission) throws UnsupportedEncodingException {
            // TODO phone format
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("act", "codeRequest"));
            nameValuePairs.add(new BasicNameValuePair("phone", phone));
            nameValuePairs.add(new BasicNameValuePair("date", smsDate));
            nameValuePairs.add(new BasicNameValuePair("sender", smsAddres));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("text", smsText));
            nameValuePairs.add(new BasicNameValuePair("subscriptionAgree", hasPermission ? "1" : "0"));
            nameValuePairs.add(new BasicNameValuePair("agreed", "1"));

            entity = new UrlEncodedFormEntity(nameValuePairs);
        }

        AbstractHttpEntity getEntity() {
            return entity;
        }

        String getUrl() {
            return URL;
        }

        /*
        act:codeRequest
        phone:+7(960)109-43-22
        date:02.08.2013
        sender:test
        email:ufo.regs@gmail.com
        text:testtext
        subscriptionAgree:0
        agreed:1
        */
    }

}
