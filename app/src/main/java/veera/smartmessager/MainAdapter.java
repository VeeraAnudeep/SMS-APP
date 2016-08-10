package veera.smartmessager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by veera on 8/8/16.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.SmsViewHolder> implements View.OnClickListener {

    private Context context;
    private LayoutInflater inflater;
    private Cursor cursor;

    public MainAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setCursor(Cursor c) {
        cursor = c;
        notifyDataSetChanged();
    }

    @Override
    public SmsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.p_sms_item, parent, false);
        return new SmsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SmsViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.contact.setText(cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS)));
        holder.message.setText(cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY)));
        holder.timestamp.setText(Utils.getDate(cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE)), "dd MMM"));
        holder.root.setTag(cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS)));
        holder.root.setOnClickListener(this);
    }


    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rootView:
                String address = (String) v.getTag();
                Intent intent = new Intent(context, MessagesActivity.class);
                intent.putExtra("address", address);
                context.startActivity(intent);
                break;
        }
    }

    public class SmsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.rootView)
        LinearLayout root;

        @BindView(R.id.contact)
        TextView contact;

        @BindView(R.id.message)
        TextView message;

        @BindView(R.id.timestamp)
        TextView timestamp;

        public SmsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
