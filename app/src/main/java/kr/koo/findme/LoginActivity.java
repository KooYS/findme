package kr.koo.findme;

import androidx.appcompat.app.AppCompatActivity;
import kr.koo.findme.type.CustomType;
import okhttp3.OkHttpClient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;


public class LoginActivity extends AppCompatActivity {

    private  static ApolloClient myApollo;


    public  static ApolloClient getMyApollo(){
        myApollo = ApolloClient.builder()
                .serverUrl("http://106.10.45.184:4000/")
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


        TextView btn_signup = (TextView) findViewById(R.id.signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });

        TextView btn_login = (TextView) findViewById(R.id.login);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        getMyApollo().query(InfoQuery.builder().build())
        .enqueue(new ApolloCall.Callback<InfoQuery.Data>() {
            @Override public void onResponse(@Nonnull Response<InfoQuery.Data> dataResponse) {
                Log.v("graphql", dataResponse.data().info());
            }
            @Override
            public void onFailure(@Nonnull ApolloException e) {
    
            }
        });
    }
}
