package zladnrms.defytech.forcoupon.member;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zladnrms.defytech.forcoupon.R;
import zladnrms.defytech.forcoupon.SHA256Util;

public class LoginActivity extends AppCompatActivity {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String URLlink = "http://115.71.238.61";
    private OkHttpClient okhttp = new OkHttpClient();

    private TextInputLayout llayout_login_id, llayout_login_password;
    private EditText et_login_id, et_login_password;
    private Button btn_login_submit;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String nickname;

    private String result_login;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_backarrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pref = getSharedPreferences("member", MODE_PRIVATE);
        editor = pref.edit();

        llayout_login_id = (TextInputLayout) findViewById(R.id.llayout_login_id);
        llayout_login_id.setErrorEnabled(true);
        llayout_login_password = (TextInputLayout) findViewById(R.id.llayout_login_password);
        llayout_login_password.setErrorEnabled(true);
        //editText.getBackground().setColorFilter(getResources().getColor(R.color.red_500_primary), PorterDuff.Mode.SRC_ATOP);
        //editText.getBackground().clearColorFilter;

        et_login_id = (EditText) findViewById(R.id.et_login_id);
        et_login_password = (EditText) findViewById(R.id.et_login_password);

        btn_login_submit = (Button) findViewById(R.id.btn_login_submit);
        btn_login_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_id = et_login_id.getText().toString();
                String input_password = et_login_password.getText().toString();

                // 아이디 패스워드 입력하였을 시
                if (!input_id.equals("") && !input_password.equals("")) {
                    loginSubmit(URLlink + "/forcoupon/member/login.php", input_id, input_password);
                } else if (input_id.equals("")) {
                    llayout_login_id.setError("아이디를 입력하세요");
                } else if (input_password.equals("")) {
                    llayout_login_password.setError("비밀번호를 입력하세요");
                }
            }
        });

        //추가한 라인
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseInstanceId.getInstance().getToken();

        //pushSubmit(URLlink + "/forcoupon/member/fcm/push.php");

    }

    private void loginSubmit(String url, String input_id, String input_password) {
        RequestBody body = new FormBody.Builder()
                .add("id", input_id)
                .add("password", input_password)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        okhttp.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handlerToast("네트워크 연결 상태를 확인해주세요");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        result_login = response.body().string();

                        try{
                            JSONObject jsonObj = new JSONObject(result_login);
                            JSONArray jsonArr = jsonObj.getJSONArray("result");

                            for(int i = 0; i < jsonArr.length(); i ++) {
                                JSONObject c = jsonArr.getJSONObject(i);
                                String js_error, js_result, js_id = "", js_nickname = "", js_sha256pw = "";

                                if(!c.isNull("error")) {
                                    js_error = c.getString("error");

                                    switch (js_error) {
                                        case "01":
                                            handlerToast("서버 상황이 좋지 않습니다. 잠시후 다시 시도해주세요");
                                            break;
                                    }
                                } else {
                                    if(!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        com.orhanobut.logger.Logger.t("RESULT-LOGIN").d(js_result);

                                        switch (js_result) {
                                            case "miss_id":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        llayout_login_id.setError("아이디가 틀렸습니다");
                                                    }
                                                });
                                                break;
                                            case "miss_password":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        llayout_login_password.setError("비밀번호가 틀렸습니다");
                                                    }
                                                });
                                                break;
                                            case "success":
                                                if(!c.isNull("id")){
                                                    js_id = c.getString("id");
                                                }
                                                if(!c.isNull("nickname")){
                                                    js_nickname = c.getString("nickname");
                                                }
                                                if(!c.isNull("sha256pw")){
                                                    js_sha256pw = c.getString("sha256pw");
                                                }
                                                editor.putString("id", js_id);
                                                editor.putString("nickname", js_nickname);
                                                editor.putString("sha256pw", js_sha256pw);
                                                editor.commit();
                                                handlerToast("로그인되었어요!");
                                                break;
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {

                        }
                    }
                });
    }

    /*
    private void pushSubmit(String url) {
        RequestBody body = new FormBody.Builder()
                .add("nickname", "a")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        okhttp.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handlerToast("네트워크 연결 상태를 확인해주세요");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        result_login = response.body().string();
                        System.out.println(result_login);

                        try{
                            JSONObject jsonObj = new JSONObject(result_login);
                            JSONArray jsonArr = jsonObj.getJSONArray("result");

                            for(int i = 0; i < jsonArr.length(); i ++) {
                                JSONObject c = jsonArr.getJSONObject(i);
                                String js_error, js_result, js_id = "", js_nickname = "", js_sha256pw = "";

                                if(!c.isNull("error")) {
                                    js_error = c.getString("error");

                                    switch (js_error) {
                                        case "01":
                                            handlerToast("서버 상황이 좋지 않습니다. 잠시후 다시 시도해주세요");
                                            break;
                                    }
                                } else {
                                    if(!c.isNull("result")) {
                                        js_result = c.getString("result");

                                        com.orhanobut.logger.Logger.t("RESULT-LOGIN").d(js_result);

                                    }
                                }
                            }
                        } catch (JSONException e) {

                        }
                    }
                });
    }
    */

    void handlerToast(final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                showCustomToast(msg, Toast.LENGTH_SHORT);
            }
        });
    }

    private void showCustomToast(String msg, int duration) {
        //Retrieve the layout inflator
        LayoutInflater inflater = getLayoutInflater();
        //Assign the custom layout to view
        //Parameter 1 - Custom layout XML
        //Parameter 2 - Custom layout ID present in linearlayout tag of XML
        View layout = inflater.inflate(R.layout.toast_custom, (ViewGroup) findViewById(R.id.llayout_custom_toast));
        TextView msgView = (TextView) layout.findViewById(R.id.tv_toast);
        msgView.setText(msg);
        //Return the application context
        Toast toast = new Toast(getApplicationContext());
        ////Set toast gravity to bottom
        //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        //Set toast duration
        toast.setDuration(duration);
        //Set the custom layout to Toast
        toast.setView(layout);
        //Display toast
        toast.show();
    }
}
