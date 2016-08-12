package veera.smartmessager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by veera on 13/8/16.
 */
public class NewMessageActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.phone)
    EditText phone;

    @BindView(R.id.et_chat_msg)
    EditText editText;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.iv_send_msg)
    ImageView sendMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_new_message);
        ButterKnife.bind(this);
        toolbar.setTitle("New Message");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        sendMessage.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean hasTelephony() {

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return !(telephonyManager == null || telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send_msg:
                String message = editText.getText().toString().trim();
                String address = phone.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    return;
                }
                SmsManager smsManager = SmsManager.getDefault();
                if (hasTelephony()) {
                    smsManager.sendTextMessage(address, null, message, null, null);
                } else {
                    Toast.makeText(this, "Make sure you have sim ready to send a message", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
