package veera.smartmessager;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.provider.Telephony;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private TypedArray ids;
    private ClickListener listener;

    public MainAdapter(Context context) {
        this.context = context;
        listener = (ClickListener) context;
        inflater = LayoutInflater.from(context);
        ids = context.getResources().obtainTypedArray(R.array.random_background);
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
        holder.root.setTag(R.id.address, cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS)));
        holder.root.setTag(R.id.position, position);
        holder.contactImage.setBackgroundResource(ids.getResourceId(position % 3, 0));
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
                String address = (String) v.getTag(R.id.address);
                int position = (Integer) v.getTag(R.id.position);
                listener.onClick(position, address);
                break;
        }
    }

    public class SmsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.rootView)
        LinearLayout root;

        @BindView(R.id.contactImage)
        ImageView contactImage;

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
