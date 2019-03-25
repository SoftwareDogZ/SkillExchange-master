package com.example.aoge.skillexchange;

import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class EditUserIfoActivity extends BaseActivity {

    private CircleImageButton circleButton;
    private RadioGroup gender;
    private Button ConfirmButton;
    private String headPicture = null;
    private Bitmap bitmap = null;
    private EditText edtCan,edtWant;
    private String gd = "male";

    private ImageUtils imageUtils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_ifo);

//        Intent intent = getIntent();
//        email = intent.getStringExtra("email");

        circleButton = (CircleImageButton) findViewById(R.id.img_e_headportrait);
        edtCan = (EditText)findViewById(R.id.edt_e_can);
        edtWant = (EditText)findViewById(R.id.edt_e_want);

        // 实例化控件
        gender = (RadioGroup) findViewById(R.id.e_sex);

        // 方法一监听事件,通过获取点击的id来实例化并获取选中状态的RadioButton控件
        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 获取选中的RadioButton的id
                int id = group.getCheckedRadioButtonId();
                // 通过id实例化选中的这个RadioButton
                RadioButton choise = (RadioButton) findViewById(id);
                // 获取这个RadioButton的text内容
                gd = choise.getText().toString();
//                Toast.makeText(FirstLoginActivity.this, "你的性别为：" + output, Toast.LENGTH_SHORT).show();
            }
        });

        ConfirmButton = (Button) findViewById(R.id.btnconfirm);
    }

    /**
     * Press the button Login, go to Login form
     *
     * @param view from the activity_login.xml
     */
    public void btnConfirm(View view) {
        String cando = edtCan.getText().toString().trim();
        String wantdo = edtWant.getText().toString().trim();
        ConfirmButton.setClickable(false);
        if(cando.isEmpty() || wantdo.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                    R.string.enter_credentials, Toast.LENGTH_LONG)
                    .show();
        }else{
            if(bitmap != null){
                edit_headPicture(bitmap);
            }
            EditIfoRequest(UserInformation.userinformation,gd,cando,wantdo,headPicture);
        }
    }

    private void edit_headPicture(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//将Bitmap转成Byte[]
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);//压缩
        headPicture = Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);//加密转换成String
    }

    /**
     * link to server to depend whether the username and password are right.
     * @param email
     * @param
     */
    public void EditIfoRequest(final String email, final String gender,final String can, final String want, final String headpicture) {
        //request url
        String url = "http://106.14.117.91:8080/SkillsExchangeServer/FirstLoginServlet";    //注①
        String tag = "FirstLogin";    //注②

        //get the request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //cancel the request queue that marked by "Register" in order to not request again.
        requestQueue.cancelAll(tag);

        //build StringRequest and set the request method "POST"(default "GET")
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");  //注③
                            String result = jsonObject.getString("Result");
                            if (result.equals("success")) {
                                Toast.makeText(getApplicationContext(),
                                        "Success!", Toast.LENGTH_LONG)
                                        .show();
                                UserInformation.firstShow = 3;
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Something wrong! Please try again.", Toast.LENGTH_LONG)
                                        .show();
                                ConfirmButton.setClickable(true);
                            }
                        } catch (JSONException e) {
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "No internet!", Toast.LENGTH_LONG)
                        .show();
                Log.e("TAG", error.getMessage(), error);
                ConfirmButton.setClickable(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("AccountNumber", UserInformation.userinformation);  //set the parameter.
                params.put("Gender", gender);
                params.put("Can", can);
                params.put("Want", want);
//                params.put("HeadPicture", headPicture);
                return params;
            }
        };

        //set the tag.
        request.setTag(tag);

        //add the request to queue.
        requestQueue.add(request);
    }









    /**
     * 打开本地相册选择图片
     */
    public void selectPic(View view){
        //intent可以应用于广播和发起意图，其中属性有：ComponentName,action,data等
        Intent intent=new Intent();
        intent.setType("image/*");
        //action表示intent的类型，可以是查看、删除、发布或其他情况；我们选择ACTION_GET_CONTENT，系统可以根据Type类型来调用系统程序选择Type
        //类型的内容给你选择
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //如果第二个参数大于或等于0，那么当用户操作完成后会返回到本程序的onActivityResult方法
        startActivityForResult(intent, 1);
    }
    /**
     *把用户选择的图片显示在imageview中
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //用户操作完成，结果码返回是-1，即RESULT_OK
        if(resultCode==RESULT_OK){
            //获取选中文件的定位符
            Uri uri = data.getData();
            Log.e("uri", uri.toString());
            //使用content的接口
            ContentResolver cr = this.getContentResolver();
            try {
                //获取图片
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
//                bitmap = BitmapFactory.decodeFile("C:\\Users\\dell\\Pictures\\lovewallpaper\\woman.bmp");
                circleButton.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(),e);
            }
        }else{
            //操作错误或没有选择图片
            Log.i("FirstLoginActivtiy", "operation error");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
