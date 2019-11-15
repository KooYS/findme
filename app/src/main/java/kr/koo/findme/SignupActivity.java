package kr.koo.findme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kr.koo.findme.type.UserCreateInput;
import okhttp3.OkHttpClient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloCallback;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class SignupActivity extends AppCompatActivity {

    private  static ApolloClient myApollo;
    String token;

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

    //                FirebaseInstanceId.getInstance().getInstanceId()
//                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//@Override
//public void onComplete(@NonNull Task<InstanceIdResult> task) {
//        if (!task.isSuccessful()) {
//        Log.w(TAG, "getInstanceId failed", task.getException());
//        return;
//        }
//
//        // Get new Instance ID token
//        String token = task.getResult().getToken();
//
//        // Log and toast
//        String msg = getString(R.string.msg_token_fmt, token);
//        Log.d(TAG, msg);
//        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//        }
//        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("getInstanceId", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        token = task.getResult().getToken();
                    }
                });


        final EditText edit_id = (EditText) findViewById(R.id.id);
        final EditText edit_passwd = (EditText) findViewById(R.id.passwd);
        final EditText edit_addr = (EditText) findViewById(R.id.addr);
        final EditText edit_birth = (EditText) findViewById(R.id.birth);
        final EditText edit_name = (EditText) findViewById(R.id.name);
        final EditText edit_repasswd = (EditText) findViewById(R.id.repasswd);
        final EditText edit_phone = (EditText) findViewById(R.id.phone);
        final Context getApplicationContext = getApplicationContext();
        TextView btn_join = (TextView) findViewById(R.id.join);

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit_id.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"아이디를 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(edit_passwd.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"패스워드를 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(edit_addr.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"주소를 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(edit_birth.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"생년월일을 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(edit_name.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"이름을 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(edit_phone.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"연락처 입력해주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(!edit_passwd.getText().toString().equals(edit_repasswd.getText().toString())){
                    Toast.makeText(getApplicationContext(),"패스워드가 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                CreateUserMutation createUsermutation = CreateUserMutation.builder()
                        .data(UserCreateInput.builder().userid(edit_id.getText().toString())
                                .password(edit_passwd.getText().toString())
                                .name(edit_name.getText().toString())
                                .birthday(edit_birth.getText().toString())
                                .address(edit_addr.getText().toString())
                                .phonenumber(edit_phone.getText().toString())
                                .token(token).build()
                        )
                        .build();

                getMyApollo()
                        .mutate(createUsermutation)
                        .enqueue(
                                (new ApolloCall.Callback<CreateUserMutation.Data>() {
                                    @Override public void onResponse(@Nonnull Response<CreateUserMutation.Data> response) {
                                        Log.i("mutation", response.toString());
                                        Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
                                        intent.putExtra("result",true);
                                        startActivity(intent);
                                    }

                                    @Override public void onFailure(@Nonnull ApolloException e) {
                                        Log.e("mutation", e.getMessage(), e);

                                    }
                                }));
            }
        });

        TextView btn_login = (TextView) findViewById(R.id.login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
                 startActivity(intent);
            }
        });

    }
}
