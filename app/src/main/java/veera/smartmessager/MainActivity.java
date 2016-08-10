package veera.smartmessager;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainView, LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    MainPresenter mainPresenter;
    MainAdapter mainAdapter;
    final String[] PROJECTION = {
            Telephony.Sms.DATE,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms._ID,
            Telephony.Sms.PERSON,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainPresenter = new MainPresenterImpl(this);
        mainAdapter = new MainAdapter(this);
        recyclerView.setAdapter(mainAdapter);
        getSupportLoaderManager().initLoader(1, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setItems(Cursor cursor) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri CONTENT_URI = Telephony.Sms.CONTENT_URI;

        final String SORT_ORDER = Telephony.Sms.DEFAULT_SORT_ORDER;
        String selection = Telephony.Sms.THREAD_ID + " NOT NULL) GROUP BY (" + Telephony.Sms.THREAD_ID;
        return new CursorLoader(this, CONTENT_URI, PROJECTION, selection, null, SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mainAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
