package veera.smartmessager.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import veera.smartmessager.ClickListener;
import veera.smartmessager.R;
import veera.smartmessager.adapters.MainAdapter;
import veera.smartmessager.marshmallowPermissions.ActivityManagePermission;
import veera.smartmessager.marshmallowPermissions.PermissionResult;
import veera.smartmessager.marshmallowPermissions.PermissionUtils;

public class MainActivity extends ActivityManagePermission implements ClickListener,
        SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int CURSOR_ID = 1;
    public static final String QUERY = "query";
    public static final String ADDRESS = "address";
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private static final int REQUEST_CODE = 101;
    public ArrayList<String> smsBuffer = new ArrayList<>();
    private File backUpSms;
    private GoogleApiClient googleApiClient;
    public String drive_id;
    public DriveId driveID;

    private MainAdapter mainAdapter;
    final Uri CONTENT_URI = Telephony.Sms.CONTENT_URI;
    final String SORT_ORDER = Telephony.Sms.DEFAULT_SORT_ORDER;
    final String[] PROJECTION = {
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.PERSON,
            Telephony.Sms.DATE,
            Telephony.Sms.BODY,
            Telephony.Sms.TYPE
    };
    private String queryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkForPermissions();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainAdapter = new MainAdapter(this);
        recyclerView.setAdapter(mainAdapter);
        fab.setOnClickListener(this);
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

        if (id == R.id.sync) {
            checkForStoragePermissions();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Marshmallow Runtime Permission Handling
     * #READ and WRITE to STORAGE
     */
    private void checkForStoragePermissions() {
        askCompactPermissions(new String[]{PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE}, new PermissionResult() {
            @Override
            public void permissionGranted() {
                buildGoogleApiClient();
            }

            @Override
            public void permissionDenied() {
                finish();
            }
        });
    }

    /**
     * Marshmallow Runtime Permission Handling
     * #READ_SMS & #WRITE_SMS
     */
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

    /**
     * Depending on the requirement returns the cursor loader in case of
     *
     * @param args Bundle when not null returns the search results loader
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String query;
        String selection;
        if (args != null) {
            query = args.getString(QUERY);
            selection = Telephony.Sms.BODY + " LIKE ?";
            return new CursorLoader(this, CONTENT_URI, PROJECTION, selection, new String[]{"%" + query + "%"}, SORT_ORDER);
        } else {
            selection = Telephony.Sms.THREAD_ID + " NOT NULL) GROUP BY (" + Telephony.Sms.THREAD_ID;
            return new CursorLoader(this, CONTENT_URI, PROJECTION, selection, null, SORT_ORDER);
        }

    }

    /**
     * @param data returns the cursor upon load finish which is set to the adapter
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!TextUtils.isEmpty(queryText)) {
            mainAdapter.setCursor(data, queryText);
        } else {
            mainAdapter.setCursor(data, "");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Starts the activity #MessagesActivity
     * which is the complete messages thread of corresponding sender
     *
     * @param address is the contact or Number of the sender
     */
    @Override
    public void onClick(String address) {
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra(ADDRESS, address);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    /**
     * Restarts the Cursor Loader on search
     *
     * @param newText is the search query
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        queryText = newText;
        Bundle bundle = null;
        if (!TextUtils.isEmpty(newText)) {
            bundle = new Bundle();
            bundle.putString(QUERY, newText);
        }
        getSupportLoaderManager().restartLoader(CURSOR_ID, bundle, this);
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        /**Disconnecting Google API client*/
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }


    /**
     * Handles onConnectionFailed callbacks
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            googleApiClient.connect();
        }
    }

    /**
     * handles connection callbacks
     */
    @Override
    public void onConnected(Bundle bundle) {
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(driveContentsCallback);
    }

    /**
     * handles suspended connection callbacks
     */
    @Override
    public void onConnectionSuspended(int cause) {
    }

    /**
     * callback when there there's an error connecting the client to the service.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!result.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    /**
     * callback on getting the drive contents, contained in result
     */
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    final DriveContents driveContents = result.getDriveContents();
                    new Thread() {
                        @Override
                        public void run() {
                            OutputStream outputStream = driveContents.getOutputStream();
                            backupSMS();
                            addTextfileToOutputStream(outputStream);
                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle("SMS-CSV")
                                    .setMimeType("text/plain")
                                    .setDescription("This is your sms backup")
                                    .setStarred(true).build();
                            Drive.DriveApi.getRootFolder(googleApiClient)
                                    .createFile(googleApiClient, changeSet, driveContents)
                                    .setResultCallback(fileCallback);
                        }

                    }.start();
                }
            };

    /**
     * get input stream from text file, read it and put into the output stream
     */
    private void addTextfileToOutputStream(OutputStream outputStream) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(
                    new FileInputStream(backUpSms));
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * callback after creating the file, can get file info out of the result
     */
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(@NonNull DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Toast.makeText(MainActivity.this,
                                "Error Syncing to Drive", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(MainActivity.this,
                            "Messages Synced to Drive", Toast.LENGTH_SHORT).show();
                    final PendingResult<DriveResource.MetadataResult> metadata
                            = result.getDriveFile().getMetadata(googleApiClient);
                    metadata.setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                        @Override
                        public void onResult(@NonNull DriveResource.MetadataResult metadataResult) {
                            Metadata data = metadataResult.getMetadata();
                            drive_id = data.getDriveId().encodeToString();
                            driveID = data.getDriveId();
                        }
                    });
                }
            };


    /**
     * build the google api client
     */
    private void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();
        }
    }

    /**
     * Backing Up SMS
     */
    private void backupSMS() {
        Toast.makeText(this, "Syncing..", Toast.LENGTH_SHORT).show();
        Cursor cursor1 = getContentResolver().query(
                CONTENT_URI,
                PROJECTION, null, null, null);
        //startManagingCursor(cursor1);
        String[] columns = PROJECTION;
        if (cursor1 != null) {
            if (cursor1.getCount() > 0) {
                while (cursor1.moveToNext()) {

                    String messageId = cursor1.getString(cursor1
                            .getColumnIndex(columns[0]));

                    String threadId = cursor1.getString(cursor1
                            .getColumnIndex(columns[1]));

                    String address = cursor1.getString(cursor1
                            .getColumnIndex(columns[2]));
                    String name = cursor1.getString(cursor1
                            .getColumnIndex(columns[3]));
                    String date = cursor1.getString(cursor1
                            .getColumnIndex(columns[4]));
                    String msg = cursor1.getString(cursor1
                            .getColumnIndex(columns[5]));
                    String type = cursor1.getString(cursor1
                            .getColumnIndex(columns[6]));


                    smsBuffer.add(messageId + "," + threadId + "," + address + "," + name + "," + date + " ," + msg + " ,"
                            + type);

                }
                cursor1.close();
                generateCSVFileForSMS(smsBuffer);
            }
        }
    }

    /**
     * Writing in to a file
     */
    private void generateCSVFileForSMS(ArrayList<String> list) {

        try {
            String smsFile = "SMS" + ".csv";
            backUpSms = new File(Environment.getExternalStorageDirectory()
                    + File.separator + smsFile);
            String storage_path = Environment.getExternalStorageDirectory().toString() + File.separator + smsFile;
            FileWriter write = new FileWriter(storage_path);

            write.append("messageId, threadId, Address, Name, Date, msg, type");
            write.append('\n');

            for (String s : list) {
                write.append(s);
                write.append('\n');
            }
            write.flush();
            write.close();
            smsBuffer.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                startActivity(new Intent(this, NewMessageActivity.class));
                break;
        }
    }
}
