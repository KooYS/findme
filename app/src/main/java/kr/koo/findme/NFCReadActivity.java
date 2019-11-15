package kr.koo.findme;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;
import kr.koo.findme.CreateItemMutation;
import kr.koo.findme.ItemsQuery;
import kr.koo.findme.MainActivity;
import kr.koo.findme.NFCButtonOnClickListener;
import kr.koo.findme.R;
import kr.koo.findme.UserQuery;
import kr.koo.findme.card.CardItem;
import kr.koo.findme.card.CardPagerAdapter;
import kr.koo.findme.card.ShadowTransformer;
import kr.koo.findme.type.ItemCreateInput;
import kr.koo.findme.type.ItemWhereInput;
import kr.koo.findme.type.ItemWhereUniqueInput;
import kr.koo.findme.type.UserWhereUniqueInput;
import kr.koo.findme.ui.acquisition.AcquisitionFragment;
import kr.koo.findme.ui.list.RegListFragment;
import kr.koo.findme.ui.loss.LossFragment;
import kr.koo.findme.ui.message.MessageFragment;
import kr.koo.findme.ui.read.ReadFragment;
import okhttp3.OkHttpClient;

public class NFCReadActivity extends AppCompatActivity {
        public static final String ERROR_DETECTED = "No NFC tag detected!";
        public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
        public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";

    TextView readItemName;
    TextView readUserInfo;
    ImageView readImage;

        private static final int PICK_IMAGE_REQUEST = 100;
        NfcAdapter nfcAdapter;
        PendingIntent pendingIntent;
        IntentFilter writeTagFilters[];
        boolean writeMode;
        Tag myTag;
        Context context;

        TextView tvNFCContent;
        TextView message;
        Button btnWrite;
        Button btnAddItem;

        String imgString;
        AlertDialog alertDialog;



        private  static ApolloClient myApollo;
        public UserQuery.User user;


        private ViewPager mViewPager;

        private CardPagerAdapter mCardAdapter;
        private ShadowTransformer mCardShadowTransformer;


        private AppBarConfiguration mAppBarConfiguration;

        public static float dpToPixels(int dp, Context context) {
            return dp * (context.getResources().getDisplayMetrics().density);
        }


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

        public String resizeBase64Image(String base64image){
            byte [] encodeByte= Base64.decode(base64image.getBytes(),Base64.DEFAULT);
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inSampleSize = 4;
            Bitmap image = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length,options);


            if(image.getHeight() <= 100 && image.getWidth() <= 100){
                return base64image;
            }
            image = Bitmap.createScaledBitmap(image, 100, 100, false);

            ByteArrayOutputStream baos=new  ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG,100, baos);

            byte [] b=baos.toByteArray();
            System.gc();
            return Base64.encodeToString(b, Base64.NO_WRAP);

        }

        public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
        {
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            image.compress(compressFormat, quality, byteArrayOS);
            return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
        }

        public static Bitmap decodeBase64(String input)
        {
            byte[] decodedBytes = Base64.decode(input, 0);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }

        private Bitmap resize(Context context, Uri uri, int resize){
            Bitmap resizeBitmap=null;

            BitmapFactory.Options options = new BitmapFactory.Options();
            try {
                BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

                int width = options.outWidth;
                int height = options.outHeight;
                int samplesize = 1;

                while (true) {//2번
                    if (width / 2 < resize || height / 2 < resize)
                        break;
                    width /= 2;
                    height /= 2;
                    samplesize *= 2;
                }

                options.inSampleSize = samplesize;
                Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
                resizeBitmap=bitmap;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return resizeBitmap;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.nfc_read);
            context = this;

            readItemName = (TextView) findViewById(R.id.readItemName);
            readUserInfo = (TextView) findViewById(R.id.readUserInfo);
            readImage = (ImageView) findViewById(R.id.readImage);


            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter == null) {
                // Stop here, we definitely need NFC
                Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
                finish();
            }
            readFromIntent(getIntent());

            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
            writeTagFilters = new IntentFilter[] { tagDetected };
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

            String[] split = text.split("&");
            readItemName.setText("Item Name : " + split[0].split("=")[1]);

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
                                            }});

                                    }

                                    @Override
                                    public void onFailure(@Nonnull ApolloException e) {
                                        Log.e("query", e.getMessage(), e);

                                    }
                                }));
            } catch (Exception e) {

            }

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
                                                Bitmap myBitmapAgain = decodeBase64(read_item.img);
                                                readImage.setImageBitmap(myBitmapAgain);
                                            }});
                                    }

                                    @Override
                                    public void onFailure(@Nonnull ApolloException e) {
                                        Log.e("query", e.getMessage(), e);

                                    }
                                }));
            } catch (Exception e) {

            }


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


    }