package kr.koo.findme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;
import kr.koo.findme.card.CardItem;
import kr.koo.findme.card.CardPagerAdapter;
import kr.koo.findme.card.ShadowTransformer;
import kr.koo.findme.lib.MyBitmap;
import kr.koo.findme.message.ChatActivity;
import kr.koo.findme.type.ItemCreateInput;
import kr.koo.findme.type.ItemWhereInput;
import kr.koo.findme.type.ItemWhereUniqueInput;
import kr.koo.findme.type.UserWhereUniqueInput;
import kr.koo.findme.ui.acquisition.AcquisitionFragment;
import kr.koo.findme.ui.list.RegListFragment;
import kr.koo.findme.ui.loss.LossFragment;
import kr.koo.findme.ui.message.MessageFragment;
import kr.koo.findme.ui.profile.ProfileFragment;
import kr.koo.findme.ui.read.ReadFragment;
import kr.koo.findme.ui.reg.RegistrationFragment;
import okhttp3.OkHttpClient;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    private static final int PICK_IMAGE_REQUEST = 100;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;




    TextView readItemName;
    TextView readUserInfo;
    TextView userNameText;
    ImageView readImage;


    TextView message;
    Button btnWrite;
    Button btnAddItem;
    Button readForAlert;

    String imgString;
    AlertDialog alertDialog;

    LinearLayout nfc_registration;
    LinearLayout nfc_read;
    LinearLayout nfc_message;
    LinearLayout nfc_home;
    ConstraintLayout nfc_profile;
    ConstraintLayout nfc_acquisition_report;
    ConstraintLayout nfc_lost_report;


    // nfc_message 사용 변수
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> arr_roomList = new ArrayList<>();
    private DatabaseReference reference = FirebaseDatabase.getInstance()
            .getReference().getRoot();
    Map<String, Object> map = new HashMap<String, Object>();
    Map<String, String> set = new HashMap<String,String>();




    private ImageView imgPreview;
    private  static ApolloClient myApollo;
    public UserQuery.User user;
    private ViewPager mViewPager;
    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;
    private AppBarConfiguration mAppBarConfiguration;



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



    public void pick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case PICK_IMAGE_REQUEST:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    Bitmap bitmap = MyBitmap.resize(context, selectedImage,300);
                    String myBase64Image = MyBitmap.encodeToBase64(bitmap, Bitmap.CompressFormat.JPEG, 100);

                    imgString = myBase64Image;
                    Bitmap myBitmapAgain = MyBitmap.decodeBase64(myBase64Image);

                    imgPreview.setImageBitmap(myBitmapAgain);

                }
                break;
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        nfc_profile.setVisibility(View.GONE);
        nfc_registration.setVisibility(View.GONE);
        nfc_read.setVisibility(View.GONE);
        nfc_message.setVisibility(View.GONE);
        nfc_home.setVisibility(View.GONE);
        nfc_acquisition_report.setVisibility(View.GONE);
        nfc_lost_report.setVisibility(View.GONE);


        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if (id == R.id.nav_profile) {
            fragment = new ProfileFragment();
            title = "PROFILE";
            nfc_profile.setVisibility(View.VISIBLE);
        }
        else if (id == R.id.nav_nfc_registration) {
            // Handle the camera action
            fragment = new RegistrationFragment();
            title = "NFC Registration";
            nfc_registration.setVisibility(View.VISIBLE);
        }
        else if (id == R.id.nav_nfc_read) {
            // Handle the camera action
            fragment = new ReadFragment();
            title = "NFC READ";
            nfc_read.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_registered_list) {
            fragment = new RegListFragment();
            title = "REGISTERED LIST";
        } else if (id == R.id.nav_loss_report) {
            fragment = new LossFragment();
            title = "THE LOSS REPORT";
            nfc_lost_report.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_acquisition_report) {
            fragment = new AcquisitionFragment();
            title = "THE ACQUISITION REPORT";
            nfc_acquisition_report.setVisibility(View.VISIBLE);

        } else if (id == R.id.nav_message) {
            fragment = new MessageFragment();
            title = "MESSAGE";
            nfc_message.setVisibility(View.VISIBLE);
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, fragment);
            ft.commit();

            // set the toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

        }

        return true;
    }

    public void xmlSet(){
        nfc_registration = (LinearLayout) findViewById(R.id.nfc_registration); // nfc 등록 xml
        nfc_read = (LinearLayout) findViewById(R.id.nfc_read);                 // nfc 등록 xml
        nfc_message = (LinearLayout) findViewById(R.id.nfc_message);           // nfc 등록 xml
        nfc_home = (LinearLayout) findViewById(R.id.nfc_home);                 // nfc 등록 xml
        nfc_acquisition_report = (ConstraintLayout) findViewById(R.id.acquisition_report);                 // nfc 등록 xml
        nfc_lost_report = (ConstraintLayout) findViewById(R.id.lost_report);                 // nfc 등록 xml
        nfc_profile = (ConstraintLayout) findViewById(R.id.nfc_profile);
    }


    public void navigationSet(){
        Toolbar toolbar = findViewById(R.id.toolbar); // 위 상단 툴바
        setSupportActionBar(toolbar);                 // 위 상단 툴바

        DrawerLayout drawer = findViewById(R.id.drawer_layout);                       // 네비게이션 바
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view); // 네비게이션 바
        navigationView.setNavigationItemSelectedListener(this);                       // 네비게이션 바

        mAppBarConfiguration = new AppBarConfiguration.Builder(                       // 네비게이션 바 버튼 구성
                R.id.nav_nfc_registration, R.id.nav_nfc_read, R.id.nav_registered_list,
                R.id.nav_loss_report, R.id.nav_acquisition_report, R.id.nav_message)
                .setDrawerLayout(drawer)
                .build();

        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main2);
        userNameText = headerLayout.findViewById(R.id.usernameText);                    // 네비게이션 바 사용자이름
    }
    public void nfcMessageSet(){
        listView = (ListView) findViewById(R.id.list);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr_roomList);
        listView.setAdapter(arrayAdapter);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


//                Map<String, String> setMap = new HashMap<String, String>();
                Iterator i = dataSnapshot.getChildren().iterator();

                while (i.hasNext()) {
                    String key = ((DataSnapshot) i.next()).getKey();
                    String[] keySplit = key.split("&");
                    String itemName = keySplit[3];
                    String userId = keySplit[0];
                    String itemId = keySplit[2];
                    String itemUser = keySplit[1];

                    if(user.id.equals(userId)){
                        set.put("내가쓴글/"+itemName ,key);
                    }
                    else{
                        if(key.contains(user.id))
                            set.put(itemName,key);
                    }
                }
                arr_roomList.clear();
                arr_roomList.addAll(set.keySet());

                arrayAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                String room_name = set.get(((TextView) view).getText().toString());
                intent.putExtra("room_name", room_name);
                intent.putExtra("user_name", user.id+"/"+user.name);
                intent.putExtra("item_user", room_name.split("&")[1]);
                startActivity(intent);
            }
        });


    }
    public void nfcRegSet(){
        mViewPager = (ViewPager)  findViewById(R.id.viewPager);
        mCardAdapter = new CardPagerAdapter();
        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);

        mViewPager.setAdapter(mCardAdapter);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                mCardShadowTransformer.enableScaling(false);
            }

            @Override
            public void onPageSelected(int position)
            {
                mCardShadowTransformer.enableScaling(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mCardShadowTransformer.enableScaling(false);
            }
        });

        btnAddItem = (Button) findViewById(R.id.addItem);
        btnAddItem.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                // 아이템 추가시 Alert 생성
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setCancelable(false);

                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View popupInputDialogView = layoutInflater.inflate(R.layout.popup_add_item, null);

                // alert element 생성
                final EditText itemNameEditText = (EditText) popupInputDialogView.findViewById(R.id.itemName);
                final EditText itemPasswdEditText = (EditText) popupInputDialogView.findViewById(R.id.itemPasswd);
                final Button saveButton = popupInputDialogView.findViewById(R.id.btn_add);
                final Button cancelButton = popupInputDialogView.findViewById(R.id.btn_cancel);
                imgPreview = (ImageView) popupInputDialogView.findViewById(R.id.imgPreview);
                // alert element 생성

                alertDialogBuilder.setView(popupInputDialogView);
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                // Registration 버튼
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {

                        // ProgressDialog 생성
                        final ProgressDialog dialog = ProgressDialog.show(view.getContext(), "",
                                "Loading. Please wait...", true);



                        //  DB 저장
                        final String itemName = itemNameEditText.getText().toString();
                        final String itemPasswd = itemPasswdEditText.getText().toString();

                        CreateItemMutation createItemmutation = CreateItemMutation.builder()
                                .data(ItemCreateInput.builder()
                                        .userid(user.id)
                                        .itemname(itemName)
                                        .itempasswd(itemPasswd)
                                        .img(imgString).build())
                                .build();

                        getMyApollo()
                                .mutate(createItemmutation)
                                .enqueue(
                                        (new ApolloCall.Callback<CreateItemMutation.Data>() {
                                            @Override public void onResponse(@Nonnull Response<CreateItemMutation.Data> response) {
                                                Log.i("mutation", response.toString());
                                                if(response == null){
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            Toast.makeText(context, "다른 이미지를 사용해주세요. 용량이 너무 큽니다.", Toast.LENGTH_LONG).show();
                                                        }});
                                                }
                                                final String res = response.data().createItem().id();
                                                NewData(res,itemName);

                                                dialog.cancel();
                                                alertDialog.cancel();
                                            }

                                            @Override public void onFailure(@Nonnull ApolloException e) {
                                                Log.e("mutation", e.getMessage(), e);
                                            }
                                        }));

                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });

            }
        });

    }

    public void getUserInfo(){
        Intent intent = getIntent();

        try {
            UserQuery userQuery = UserQuery.builder()
                    .where(UserWhereUniqueInput.builder().id(intent.getExtras().getString("user")).build())
                    .build();

            getMyApollo()
                    .query(userQuery)
                    .enqueue(
                            (new ApolloCall.Callback<UserQuery.Data>() {
                                @Override
                                public void onResponse(@Nonnull Response<UserQuery.Data> response) {
                                    user = response.data().user();
                                    ItemListUp(user.id);
                                    nfcRegSet();        // nfc_registration 안에 뷰페이저 및 기본 설정
                                    nfcReadSet();       // nfc_read 설정
                                    nfcMessageSet();    // nfc_message 설정
                                }

                                @Override
                                public void onFailure(@Nonnull ApolloException e) {
                                    Log.e("query", e.getMessage(), e);

                                }
                            }));
        } catch (Exception e) {

        }
    }

    public void nfcReadSet(){
        readItemName = (TextView) findViewById(R.id.readItemName);
        readUserInfo = (TextView) findViewById(R.id.readUserInfo);
        readImage = (ImageView) findViewById(R.id.readImage);
        readForAlert = (Button) findViewById(R.id.readForAlert);
        readForAlert.setVisibility(View.GONE);
    }

    public void nfcSet(){
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
//        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //firebase ㅊㅐ팅
        /////
        context = this;

        xmlSet();           //xml 설정
        navigationSet();    // navigation 설정

        getUserInfo();      // 로그인시 넘어오는 Intent에서 로그인 정보 조회
        nfcSet();           // nfc 설정

    }
    public void ItemListUp(final String id){
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    userNameText.setText(user.name);
                    final ProgressDialog progressDialog;
                    progressDialog= new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();

                    ItemsQuery itemsQuery = ItemsQuery.builder()
                            .where(ItemWhereInput.builder().userid(id).build())
                            .build();

                    getMyApollo()
                            .query(itemsQuery)
                            .enqueue(
                                    (new ApolloCall.Callback<ItemsQuery.Data>() {

                                        @Override
                                        public void onResponse(@Nonnull Response<ItemsQuery.Data> response) {

                                            for(ItemsQuery.Item object : response.data().items()) {
                                                OnNewSensorData(object);

                                            }
                                            progressDialog.cancel();

                                        }

                                        @Override
                                        public void onFailure(@Nonnull ApolloException e) {
                                            Log.e("query", e.getMessage(), e);

                                        }
                                    }));


                } catch (Exception e) {

                }
            }
        });
    }
    public void NewData(final  String res, final String itemName){
        runOnUiThread(new Runnable() {
            public void run() {
                mCardAdapter.addCardItem(new CardItem(itemName, imgString,res,
                        new NFCButtonOnClickListener(res, new Callable<Void>() {
                            public Void call() {
                                try {
                                    if(myTag ==null) {
                                        Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                                    } else {
                                        //write(message.getText().toString(), myTag);
                                        write("itemname="+ itemName + "&" + "id="+ res + "&userid="+ user.id , myTag);
                                        Toast.makeText(context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show();
                                    }
                                } catch (IOException e) {
                                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                                    e.printStackTrace();
                                } catch (FormatException e) {
                                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        })));
                mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
                mViewPager.setAdapter(mCardAdapter);
                mViewPager.setPageTransformer(false, mCardShadowTransformer);
                mViewPager.setOffscreenPageLimit(3);
            }
        });

    }
    public void OnNewSensorData(final ItemsQuery.Item data) {
        runOnUiThread(new Runnable() {
            public void run() {

                mCardAdapter.addCardItem(new CardItem( data.itemname, data.img,data.id, new NFCButtonOnClickListener(data.id, new Callable<Void>() {
                    public Void call() {
                        try {
                            if(myTag ==null) {
                                Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                            } else {
                                //write(message.getText().toString(), myTag);
                                write("itemname="+ data.itemname + "&" + "id="+ data.id + "&userid="+ user.id + "&itempasswd="+ data.itempasswd , myTag);
                                Toast.makeText(context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show();
                            }
                        } catch (IOException e) {
                            Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                            e.printStackTrace();
                        } catch (FormatException e) {
                            Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                            e.printStackTrace();
                        }
                        return null;
                    }
                })));
                mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
                mViewPager.setAdapter(mCardAdapter);
                mViewPager.setPageTransformer(false, mCardShadowTransformer);
                mViewPager.setOffscreenPageLimit(3);
            }
        });
    }
    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }




        final String[] split = text.split("&");

        if(nfc_read.getVisibility() == View.VISIBLE)
        try {
            ItemQuery itemQuery = ItemQuery.builder()
                    .where(ItemWhereUniqueInput.builder().id(split[1].split("=")[1]).build())
                    .build();

            getMyApollo()
                    .query(itemQuery)
                    .enqueue(
                            (new ApolloCall.Callback<ItemQuery.Data>() {
                                @Override
                                public void onResponse(@Nonnull Response<ItemQuery.Data> response) {
                                    final ItemQuery.Item read_item = response.data().item();
                                    runOnUiThread(new Runnable() {
                                        public void run() {



                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                            alertDialogBuilder.setCancelable(false);


                                            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                                            View popupInputDialogView = layoutInflater.inflate(R.layout.popup_check_passwd, null);

                                            final EditText itemPasswdEditText = (EditText) popupInputDialogView.findViewById(R.id.check_itemPasswd);
                                            final Button saveButton = popupInputDialogView.findViewById(R.id.check_btn_add);

                                            // Set the inflated layout view object to the AlertDialog builder.
                                            alertDialogBuilder.setView(popupInputDialogView);

                                            // Create AlertDialog and show.
                                            alertDialog = alertDialogBuilder.create();
                                            alertDialog.show();

                                            saveButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(final View view) {
                                                    final ProgressDialog dialog = ProgressDialog.show(view.getContext(), "",
                                                            "Loading. Please wait...", true);

                                                    final String itemPasswd = itemPasswdEditText.getText().toString();

                                                    if(read_item.itempasswd.equals(itemPasswd)){

                                                        readItemName.setText("Item Name : " + split[0].split("=")[1]);
                                                        Bitmap myBitmapAgain = MyBitmap.decodeBase64(read_item.img);
                                                        readImage.setImageBitmap(myBitmapAgain);

                                                        try {
                                                            UserQuery userQuery = UserQuery.builder()
                                                                    .where(UserWhereUniqueInput.builder().id(split[2].split("=")[1]).build())
                                                                    .build();

                                                            getMyApollo()
                                                                    .query(userQuery)
                                                                    .enqueue(
                                                                            (new ApolloCall.Callback<UserQuery.Data>() {
                                                                                @Override
                                                                                public void onResponse(@Nonnull Response<UserQuery.Data> response) {
                                                                                    final UserQuery.User read_user = response.data().user();
                                                                                    runOnUiThread(new Runnable() {
                                                                                        public void run() {
                                                                                            readUserInfo.setText("Address : " + read_user.address + "\n" +
                                                                                                    "Name : " + read_user.name + "\n" +
                                                                                                    "H.P. : " + read_user.phonenumber + "\n" +
                                                                                                    "Birthday : " + read_user.birthday + "\n");
                                                                                            readForAlert.setVisibility(View.VISIBLE);
                                                                                            setOnClick(readForAlert,user.id + "&" + read_user.id + "&" + read_item.id + "&" + read_item.itemname);
                                                                                        }});

                                                                                }

                                                                                @Override
                                                                                public void onFailure(@Nonnull ApolloException e) {
                                                                                    Log.e("query", e.getMessage(), e);

                                                                                }
                                                                            }));
                                                        } catch (Exception e) {

                                                        }

                                                        dialog.cancel();
                                                        alertDialog.cancel();
                                                    }
                                                    else
                                                    {
                                                        new AlertDialog.Builder(context)
                                                                .setTitle("error")
                                                                .setMessage("패스워드가 일치하지않습니다.")

                                                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                                                // The dialog is automatically dismissed when a dialog button is clicked.
                                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        // Continue with delete operation
                                                                    }
                                                                })
                                                                .setNegativeButton(android.R.string.no, null)
                                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                                .show();

                                                        dialog.cancel();
                                                        alertDialog.cancel();
                                                    }

                                                }
                                            });




                                        }});
                                }

                                @Override
                                public void onFailure(@Nonnull ApolloException e) {
                                    Log.e("query", e.getMessage(), e);

                                }
                            }));
        } catch (Exception e) {

        }









//        Bitmap myBitmapAgain = decodeBase64(text);
//        imgPreview.setImageBitmap(myBitmapAgain);

    }
    private void setOnClick(final Button btn, final String str){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.put(str, "");
                reference.updateChildren(map);
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("room_name", str);
                intent.putExtra("user_name", user.id+"/"+user.name);
                intent.putExtra("item_user", str.split("&")[1]);
                startActivity(intent);
                readForAlert.setVisibility(View.GONE);
            }
        });
    }

    /******************************************************************************
     **********************************Write to NFC Tag****************************
     ******************************************************************************/
    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
//        Toast.makeText(context, tag.getId().toString(), Toast.LENGTH_LONG).show();
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }
    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }



    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }
    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

    public void onClickMenu(View view) {
        Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_SHORT).show();
    }

}


// [START retrieve_current_token]
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
// [END retrieve_current_token]