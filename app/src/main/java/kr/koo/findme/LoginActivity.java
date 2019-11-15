package kr.koo.findme;

import androidx.appcompat.app.AppCompatActivity;
import kr.koo.findme.type.CustomType;
import okhttp3.OkHttpClient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;


public class LoginActivity extends AppCompatActivity {

    private  static ApolloClient myApollo;
    public List<UsersQuery.User> userList;
    private  boolean getUserList = false;

    public  static ApolloClient getMyApollo(){
        myApollo = ApolloClient.builder()
                .serverUrl("https://us1.prisma.sh/tlfkthsl42-b639da/findme/dev")
                .okHttpClient(
                        new OkHttpClient.Builder()
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .build()
                )
                .build();

        return myApollo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getMyApollo().query(UsersQuery.builder().build())
                .enqueue(new ApolloCall.Callback<UsersQuery.Data>() {
                    @Override public void onResponse(@Nonnull Response<UsersQuery.Data> dataResponse) {
                        userList =  dataResponse.data().users();
                        getUserList = true;
                    }
                    @Override
                    public void onFailure(@Nonnull ApolloException e) {

                    }
                });


        Intent intent = getIntent();
        try {
            if(intent.getExtras().getBoolean("result")){
                Toast.makeText(getApplicationContext(), "회원가입에 성공, 로그인해주세요.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }




        final EditText edit_id = (EditText) findViewById(R.id.id);
        final EditText edit_passwd = (EditText) findViewById(R.id.passwd);

        TextView btn_signup = (TextView) findViewById(R.id.login_signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });

        TextView btn_login = (TextView) findViewById(R.id.login_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!getUserList){
                    Toast.makeText(getApplicationContext(),"유저정보를 얻고 있습니다. 잠시후 시도해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(edit_id.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"아이디를 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(edit_passwd.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"패스워드를 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }

                for(UsersQuery.User object : userList) {
                    if (edit_id.getText().toString().equals(object.userid)) {
                        if (edit_passwd.getText().toString().equals(object.password)) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("user", object.id);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "패스워드가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                }

                Toast.makeText(getApplicationContext(), "가입정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        });

    }
}
