package veera.smartmessager.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by veera on 13/8/16.
 */
public class MessagesBaseActivity extends AppCompatActivity {
    protected String address;


    /**
     * Handles the on #backpress of toolbar back Button
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sends the SMS if message is not empty and if there is a SIM READY
     *
     * @param address Phone no or address to which SMS to be sent
     * @param message Message to be sent
     */
    public void sendSms(String address, String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        if (hasTelephony()) {
            smsManager.sendTextMessage(address, null, message, null, null);
        } else {
            Toast.makeText(this, "Make sure you have sim ready to send a message", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if the phone has a SIM or Network ready
     */
    public boolean hasTelephony() {

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return !(telephonyManager == null || telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY);
    }
}
