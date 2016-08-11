package veera.smartmessager;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import veera.smartmessager.marshmallowPermissions.ActivityManagePermission;
import veera.smartmessager.marshmallowPermissions.PermissionResult;
import veera.smartmessager.marshmallowPermissions.PermissionUtils;

public class MainActivity extends ActivityManagePermission implements MainView, SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int CURSOR_ID = 1;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    MainPresenter mainPresenter;
    MainAdapter mainAdapter;
    final Uri CONTENT_URI = Telephony.Sms.CONTENT_URI;
    final String SORT_ORDER = Telephony.Sms.DEFAULT_SORT_ORDER;
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
        checkForPermissions();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainPresenter = new MainPresenterImpl(this);
        mainAdapter = new MainAdapter(this);
        recyclerView.setAdapter(mainAdapter);
    }

    private void checkForPermissions() {
        askCompactPermissions(new String[]{PermissionUtils.Manifest_READ_SMS, PermissionUtils.Manifest_RECEIVE_SMS}, new PermissionResult() {
            @Override
            public void permissionGranted() {
                getSupportLoaderManager().initLoader(CURSOR_ID, null, MainActivity.this);
            }

            @Override
            public void permissionDenied() {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
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
        if(args!=null){
            String query = args.getString("query");
        }
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

    @Override
    public void onClick(int position, String address) {
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra("address", address);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Bundle bundle = new Bundle();
        bundle.putString("query", newText);
        getSupportLoaderManager().restartLoader(CURSOR_ID, bundle, this);
        return false;
    }
}
