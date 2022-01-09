package com.app.managebr.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//Firebase cloud messaging Instance built in service class for sending fcm notifications..
public class MyFirebaseInstanceService extends FirebaseInstanceIdService {
    private Context context;
    private static final String TAG="Genretaed Token";

    public void sendMessageSingle(Context context, final String recipient, final String title, final String body, final Map<String, String> dataMap) {
        this.context = context;
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("body", body);
        notificationMap.put("title", title);

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("notification", notificationMap);
        rootMap.put("to", recipient);
        if (dataMap != null)
            rootMap.put("data", dataMap);

        new SendFCM().setFcm(rootMap).execute();
    }
    public void sendMessageMulti(Context context, final JSONArray recipients, final String title, final String body, final Map<String, String> dataMap) {
        this.context = context;
        if(recipients.length() > 0){
            Map<String, Object> notificationMap = new HashMap<>();
            notificationMap.put("body", body);
            notificationMap.put("title", title);
//        notificationMap.put("icon" , R.drawable.common_google_signin_btn_icon_dark);
            Map<String, Object> rootMap = new HashMap<>();
            rootMap.put("notification", notificationMap);
            rootMap.put("registration_ids", recipients);
            if (dataMap != null)
                rootMap.put("data", dataMap);

            new SendFCM().setFcm(rootMap).execute();
        }

    }


    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        try {
            String refreshedToken = FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken();
            // FirebaseMessaging.getInstance().subscribeToTopic("all");


            Log.d(TAG, "Refreshed token: " + refreshedToken);
            SharedPreferences.Editor e = getSharedPreferences("token",MODE_PRIVATE).edit();
            e.putString("id",refreshedToken);
            e.apply();
            /* If you want to send messages to this application instance or manage this apps subscriptions on the server side, send the Instance ID token to your app server.*/

            //sendRegistrationToServer(refreshedToken);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void sendRegistrationToServer(String refreshedToken) {
        Log.d("TOKENNNN ", refreshedToken);
        SharedPreferences.Editor e = getSharedPreferences("token",MODE_PRIVATE).edit();
        e.putString("id",refreshedToken);
        e.apply();
    }

    @SuppressLint("StaticFieldLeak")
    class SendFCM extends AsyncTask<String, String, String> {

        private String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
        private Map<String, Object> fcm;

        SendFCM setFcm(Map<String, Object> fcm) {
            this.fcm = fcm;
            return this;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, new JSONObject(fcm).toString());
                Request request = new Request.Builder()
                        .url(FCM_MESSAGE_URL)
                        .post(body)
                        .addHeader("Authorization", "key=AAAAjjjHd3g:APA91bFG0g-WlYrGPxq4ohF5mRqEyZ_pp8cPpIoAw5KUxdhThATejQEcIZiJIZ43Rk1PHuayYmWfzlZaaRnNv3TdLyZRYDoxTlZVBcZQ21lW5Zx2M72Gq1O5xs1_kIPxSjzjuWXMCgls")
                        .build();
                Response response = new OkHttpClient().newCall(request).execute();
                return response.body().string();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject resultJson = new JSONObject(result);
                int success, failure;
                success = resultJson.getInt("success");
                failure = resultJson.getInt("failure");
             //   Toast.makeText(context, "Sent: " + success + "/" + (success + failure), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
//                Toast.makeText(context, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
            }
        }
    }
}