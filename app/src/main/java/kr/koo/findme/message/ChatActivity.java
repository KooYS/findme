package kr.koo.findme.message;

import androidx.appcompat.app.AppCompatActivity;
import kr.koo.findme.R;
import kr.koo.findme.UserQuery;
import kr.koo.findme.type.UserWhereUniqueInput;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.Bundle;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.exception.ApolloException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

public class ChatActivity extends AppCompatActivity {

    private ListView lv_chating;
    private EditText et_send;
    private Button btn_send;

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> arr_room = new ArrayList<>();

    private String str_room_name;
    private String str_user_name;

    private DatabaseReference reference;
    private String key;
    private String chat_user;
    private String chat_message;
    private  static ApolloClient myApollo;
    private  String token;

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
        setContentView(R.layout.activity_chat);
        try {
            UserQuery userQuery = UserQuery.builder()
                    .where(UserWhereUniqueInput.builder().id(getIntent().getExtras().get("item_user").toString()).build())
                    .build();

            getMyApollo()
                    .query(userQuery)
                    .enqueue(
                            (new ApolloCall.Callback<UserQuery.Data>() {
                                @Override
                                public void onResponse(@Nonnull com.apollographql.apollo.api.Response<UserQuery.Data> response) {
                                    token= response.data().user().token();
                                }

                                @Override
                                public void onFailure(@Nonnull ApolloException e) {
                                    Log.e("query", e.getMessage(), e);

                                }
                            }));
        } catch (Exception e) {

        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        et_send = (EditText) findViewById(R.id.et_send);
        lv_chating = (ListView) findViewById(R.id.lv_chating);
        btn_send = (Button) findViewById(R.id.btn_send);

        str_room_name = getIntent().getExtras().get("room_name").toString();
        str_user_name = getIntent().getExtras().get("user_name").toString().split("/")[1];
        reference = FirebaseDatabase.getInstance().getReference().child(str_room_name);

        setTitle(str_room_name + " 채팅방");

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr_room);
        lv_chating.setAdapter(arrayAdapter);
        // 리스트뷰가 갱신될때 하단으로 자동 스크롤
        lv_chating.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                // map을 사용해 name과 메시지를 가져오고, key에 값 요청
//                Map<String, Object> map = new HashMap<String, Object>();
                key = reference.push().getKey();
//                reference.updateChildren(map);

                DatabaseReference root = reference.child(key);

                // updateChildren를 호출하여 database 최종 업데이트
                Map<String, Object> objectMap = new HashMap<String, Object>();
                objectMap.put("name", str_user_name);
                objectMap.put("message", et_send.getText().toString());

                root.updateChildren(objectMap);

                et_send.setText("");


                new Thread() {
                        public void run() {

                            JsonObject jsonObj = new JsonObject();

                            Gson gson = new Gson();
                            JsonElement jsonElement = gson.toJsonTree(token);
                            jsonObj.add("to", jsonElement);
                            JsonObject notification = new JsonObject();

                            notification.addProperty("title", str_user_name);
                            notification.addProperty("body", "A message arrived from " + str_user_name);
                            jsonObj.add("notification", notification);

                            /*발송*/
                            final MediaType mediaType = MediaType.parse("application/json");
                            OkHttpClient httpClient = new OkHttpClient();
                            Logger logger;
                            try {
                                Request request = new Request.Builder().url("https://fcm.googleapis.com/fcm/send")
                                        .addHeader("Content-Type", "application/json; UTF-8")
                                        .addHeader("Authorization", "key=" + "AAAAgeZvgw8:APA91bGAhbXRW5GEkNEKdJW5Q5obo9MB31Y9pczI0QExihO0I4eN10PrYi2zw0Q1Fw7ozM4XD6yWtYYk6dbtJzcH7MUs3A87mYdsJqMkM_ObRCgEmazU20NsUNJPyrpHikB3rPyKkydR")
                                        .post(RequestBody.create(mediaType, jsonObj.toString())).build();

                                Response response = httpClient.newCall(request).execute();
                                String res = response.body().string();
                                Log.v("message","notification response " + res);
                            } catch (IOException e) {
                                Log.v("message","Error in sending message to FCM server " + e);
                            }
                        }
                    }.start();


            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                chatConversation(dataSnapshot);
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                chatConversation(dataSnapshot);
            }

            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // addChildEventListener를 통해 실제 데이터베이스에 변경된 값이 있으면,
    // 화면에 보여지고 있는 Listview의 값을 갱신함
    private void chatConversation(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext()) {
            chat_message = (String) ((DataSnapshot) i.next()).getValue();
            chat_user = (String) ((DataSnapshot) i.next()).getValue();

            arrayAdapter.add(chat_user + " : " + chat_message);
        }

        arrayAdapter.notifyDataSetChanged();
    }
}


