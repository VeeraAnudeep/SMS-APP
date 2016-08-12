package veera.smartmessager;

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
import android.util.Log;
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
import veera.smartmessager.marshmallowPermissions.ActivityManagePermission;
import veera.smartmessager.marshmallowPermissions.PermissionResult;
import veera.smartmessager.marshmallowPermissions.PermissionUtils;

public class MainActivity extends ActivityManagePermission implements MainView,
        SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final String TAG = "loo";

    public static final int CURSOR_ID = 1;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private static final int REQUEST_CODE = 101;
    public ArrayList<String> smsBuffer = new ArrayList<>();
    String smsFile = "SMS" + ".csv";
    File backUpSms;
    private GoogleApiClient googleApiClient;
    public static String drive_id;
    public static DriveId driveID;

    private MainAdapter mainAdapter;
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

        if (id == R.id.sync) {
            checkForStoragePermissions();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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

    @Override
    public void setItems(Cursor cursor) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String query;
        String selection;
        if (args != null) {
            query = args.getString("query");
            selection = Telephony.Sms.BODY + " LIKE ?";
            return new CursorLoader(this, CONTENT_URI, PROJECTION, selection, new String[]{"%" + query + "%"}, SORT_ORDER);
        } else {
            selection = Telephony.Sms.THREAD_ID + " NOT NULL) GROUP BY (" + Telephony.Sms.THREAD_ID;
            return new CursorLoader(this, CONTENT_URI, PROJECTION, selection, null, SORT_ORDER);
        }

    }

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
        queryText = newText;
        if (!TextUtils.isEmpty(newText)) {
            Bundle bundle = new Bundle();
            bundle.putString("query", newText);
            getSupportLoaderManager().restartLoader(CURSOR_ID, bundle, this);
        } else {
            getSupportLoaderManager().restartLoader(CURSOR_ID, null, this);
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }


    /*Handles onConnectionFailed callbacks*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            googleApiClient.connect();
        }
    }

    /* *//*handles connection callbacks*/
    @Override
    public void onConnected(Bundle bundle) {
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(driveContentsCallback);
    }

    /*handles suspended connection callbacks*/
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("v", "Error creating new file contents");
    }

    /*callback on getting the drive contents, contained in result*/
    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new
            ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Error creating new file contents");
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
                                    .setTitle("testFile")
                                    .setMimeType("text/plain")
                                    .setDescription("This is a text file uploaded from device")
                                    .setStarred(true).build();
                            Drive.DriveApi.getRootFolder(googleApiClient)
                                    .createFile(googleApiClient, changeSet, driveContents)
                                    .setResultCallback(fileCallback);
                        }

                    }.start();
                }
            };

    /*get input stream from text file, read it and put into the output stream*/
    private void addTextfileToOutputStream(OutputStream outputStream) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(
                    new FileInputStream(backUpSms));
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            Log.i(TAG, "problem converting input stream to output stream: " + e);
            e.printStackTrace();
        }
    }

    /*callback after creating the file, can get file info out of the result*/
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(@NonNull DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Error creating the file");
                        Toast.makeText(MainActivity.this,
                                "Error adding file to Drive", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i(TAG, "File added to Drive");
                    Log.i(TAG, "Created a file with content: "
                            + result.getDriveFile().getDriveId());
                    Toast.makeText(MainActivity.this,
                            "File successfully added to Drive", Toast.LENGTH_SHORT).show();
                    final PendingResult<DriveResource.MetadataResult> metadata
                            = result.getDriveFile().getMetadata(googleApiClient);
                    metadata.setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                        @Override
                        public void onResult(@NonNull DriveResource.MetadataResult metadataResult) {
                            Metadata data = metadataResult.getMetadata();
                            Log.i(TAG, "Title: " + data.getTitle());
                            drive_id = data.getDriveId().encodeToString();
                            Log.i(TAG, "DrivId: " + drive_id);
                            driveID = data.getDriveId();
                            Log.i(TAG, "Description: " + data.getDescription());
                            Log.i(TAG, "MimeType: " + data.getMimeType());
                            Log.i(TAG, "File size: " + String.valueOf(data.getFileSize()));
                        }
                    });
                }
            };

    /*callback when there there's an error connecting the client to the service.*/
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

    /*build the google api client*/
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

    private void backupSMS() {
        Uri mSmsinboxQueryUri = Uri.parse("content://sms");
        Cursor cursor1 = getContentResolver().query(
                mSmsinboxQueryUri,
                new String[]{"_id", "thread_id", "address", "person", "date",
                        "body", "type"}, null, null, null);
        //startManagingCursor(cursor1);
        String[] columns = new String[]{"_id", "thread_id", "address", "person", "date", "body",
                "type"};
        if (cursor1 != null) {
            if (cursor1.getCount() > 0) {
                String count = Integer.toString(cursor1.getCount());
                Log.d("Count", count);
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


    private void generateCSVFileForSMS(ArrayList<String> list) {

        try {
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
