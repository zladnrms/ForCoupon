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

public class JoinActivity extends AppCompatActivity {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String URLlink = "http://115.71.238.61";
    private OkHttpClient okhttp = new OkHttpClient();

    private TextInputLayout llayout_join_id, llayout_join_password, llayout_join_nickname;
    private EditText et_join_id, et_join_password, et_join_nickname;
    private Button btn_join_submit;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String nickname;

    private String result_join;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

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

        llayout_join_id = (TextInputLayout) findViewById(R.id.llayout_join_id);
        llayout_join_id.setErrorEnabled(true);
        llayout_join_password = (TextInputLayout) findViewById(R.id.llayout_join_password);
        llayout_join_password.setErrorEnabled(true);
        llayout_join_nickname = (TextInputLayout) findViewById(R.id.llayout_join_nickname);
        llayout_join_nickname.setErrorEnabled(true);
        //editText.getBackground().setColorFilter(getResources().getColor(R.color.red_500_primary), PorterDuff.Mode.SRC_ATOP);
        //editText.getBackground().clearColorFilter;

        et_join_id = (EditText) findViewById(R.id.et_join_id);
        et_join_password = (EditText) findViewById(R.id.et_join_password);
        et_join_nickname = (EditText) findViewById(R.id.et_join_nickname);

        btn_join_submit = (Button) findViewById(R.id.btn_join_submit);
        btn_join_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_id = et_join_id.getText().toString();
                String input_password = et_join_password.getText().toString();
                String input_nickname = et_join_nickname.getText().toString();

                // 아이디 패스워드 입력하였을 시
                if(!input_id.equals("") && !input_password.equals("")) {
                    joinSubmit(URLlink + "/forcoupon/member/join.php", input_id, input_password, input_nickname);
                } else if(input_id.equals("")) {
                    llayout_join_id.setError("아이디를 입력하세요");
                } else if(input_password.equals("")) {
                    llayout_join_password.setError("비밀번호를 입력하세요");
                } else if(input_nickname.equals("")) {
                    llayout_join_nickname.setError("별명을 입력하세요");
                }
            }
        });
    }

    private void joinSubmit(String url, String input_id, String input_password, String input_nickname) {
        RequestBody body = new FormBody.Builder()
                .add("id", input_id)
                .add("password", input_password)
                .add("nickname", input_nickname)
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
                        result_join = response.body().string();

                        try{
                            JSONObject jsonObj = new JSONObject(result_join);
                            JSONArray jsonArr = jsonObj.getJSONArray("result");

                            for(int i = 0; i < jsonArr.length(); i ++) {
                                JSONObject c = jsonArr.getJSONObject(i);
                                String js_error, js_result, js_id, js_nickname, js_sha256pw;

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

                                        com.orhanobut.logger.Logger.t("RESULT-JOIN").d(js_result);

                                        switch (js_result) {
                                            case "already_id":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        llayout_join_id.setError("중복된 아이디입니다");
                                                    }
                                                });
                                                break;
                                            case "already_nickname":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        llayout_join_nickname.setError("중복된 별명입니다");
                                                    }
                                                });
                                                break;
                                            case "success":
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        showCustomToast("가입에 성공했어요!", Toast.LENGTH_SHORT);
                                                    }
                                                });
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
