package com.example.syncano;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.syncano.android.lib.Syncano;
import com.syncano.android.lib.SyncanoBase;
import com.syncano.android.lib.modules.Response;
import com.syncano.android.lib.modules.data.ParamsDataDelete;
import com.syncano.android.lib.modules.data.ParamsDataGet;
import com.syncano.android.lib.modules.data.ParamsDataNew;
import com.syncano.android.lib.modules.data.ResponseDataGet;
import com.syncano.android.lib.modules.data.ResponseDataNew;
import com.syncano.android.lib.modules.notification.ParamsNotificationSend;
import com.syncano.android.lib.objects.Data;
import com.syncano.android.lib.syncserver.DataChanges;
import com.syncano.android.lib.syncserver.SyncServerConnection;

import java.util.ArrayList;


public class MyActivity extends Activity {

    // Set your Api Key and instance name here
    public static final String API_KEY = "";
    public static final String INSTANCE_NAME = "";

    // Create Project and Collection on your instance
    public static final String PROJECT_ID = "";
    public static final String COLLECTION_ID = "";

    private static final String PARAM_NOTIFICATION_TEXT = "text";

    private SyncServerConnection connection;
    private Syncano syncano;

    private TextView outputTextView;
    private Button sendDataButton;
    private Button getDataButton;
    private Button deleteDataButton;
    private Button sendNotificationButton;
    private EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        outputTextView = (TextView) findViewById(R.id.textView_output);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());
        sendDataButton = (Button) findViewById(R.id.button_send_data);
        getDataButton = (Button) findViewById(R.id.button_get_data);
        deleteDataButton = (Button) findViewById(R.id.button_delete_data);
        sendNotificationButton = (Button) findViewById(R.id.button_send_notification);
        messageEditText = (EditText) findViewById(R.id.editText_message);

        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });

        getDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });

        deleteDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData(new DataGetCallback() {
                    @Override
                    public void finished(Data data) {
                        if (data != null) {
                            deleteData(data.getId());
                        } else {
                            logLine("Nothing was deleted.");
                        }
                    }
                });
            }
        });

        sendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification();
            }
        });

        syncano = new Syncano(this, INSTANCE_NAME, API_KEY);

        connection = new SyncServerConnection(this, INSTANCE_NAME, API_KEY,
                new SyncServerConnection.SyncServerListener() {
                    @Override
                    public void message(String object, JsonObject message) {
                        outputTextView.append("<< Notification: " + message + "\n");
                    }

                    @Override
                    public void error(String why) {

                    }

                    @Override
                    public void disconnected() {

                    }

                    @Override
                    public void connected() {

                    }
                }, new SyncServerConnection.SubscriptionListener() {
            @Override
            public void deleted(int[] ids) {

            }

            @Override
            public void changed(ArrayList<DataChanges> changes) {

            }

            @Override
            public void added(Data data) {

            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        connection.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (connection.isConnected()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    connection.stop();
                }
            });
            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void logLine(CharSequence text) {
        outputTextView.append(text + "\n");
    }

    private void sendData() {
        final ParamsDataNew paramsDataNew = new ParamsDataNew(PROJECT_ID, COLLECTION_ID, null, Data.PENDING);
        logLine(">> Method: " + paramsDataNew.getMethodName());
        syncano.sendAsyncRequest(paramsDataNew, new SyncanoBase.Callback() {
            @Override
            public void finished(Response response) {
                Data data = ((ResponseDataNew) response).getData();

                if (response.getResultCode() != Response.CODE_SUCCESS) {
                    logLine("<< Error: " + response.getError());
                } else {
                    logLine("<< Response: " + response.getResult() + " Created data id: " + data.getId());
                }
            }
        });
    }

    private void getData() {
        getData(null);
    }

    private void getData(final DataGetCallback callback) {
        final ParamsDataGet paramsDataGet = new ParamsDataGet(PROJECT_ID, COLLECTION_ID, null);
        paramsDataGet.setLimit(1); // Get only one data object
        paramsDataGet.setOrder("DESC");
        logLine(">> Method: " + paramsDataGet.getMethodName());
        syncano.sendAsyncRequest(paramsDataGet, new SyncanoBase.Callback() {
            @Override
            public void finished(Response response) {
                Data[] data = ((ResponseDataGet) response).getData();

                if (response.getResultCode() != Response.CODE_SUCCESS) {
                    logLine("<< Error: " + response.getError());
                    if (callback != null) callback.finished(null);
                } else if(data.length == 0) {
                    logLine("<< Response: No data to get");
                    if (callback != null) callback.finished(null);
                } else {
                    logLine("<< Response: " + response.getResult() + " Received data id: " + data[0].getId());
                    if (callback != null) callback.finished(data[0]);
                }
            }
        });
    }

    private void deleteData(final String dataId) {
        final ParamsDataDelete paramsDataDelete = new ParamsDataDelete(PROJECT_ID, COLLECTION_ID, null);
        paramsDataDelete.setDataIds(new String[]{dataId});

        logLine(">> Method: " + paramsDataDelete.getMethodName());
        syncano.sendAsyncRequest(paramsDataDelete, new SyncanoBase.Callback() {
            @Override
            public void finished(Response response) {

                if (response.getResultCode() != Response.CODE_SUCCESS) {
                    logLine("<< Error: " + response.getError());
                }  else {
                    logLine("<< Response: " + response.getResult() + " Deleted data with id: " + dataId);
                }
            }
        });
    }

    private  void sendNotification() {
        ParamsNotificationSend paramsNotificationSend = new ParamsNotificationSend(null);
        paramsNotificationSend.addParam(PARAM_NOTIFICATION_TEXT, messageEditText.getText().toString());

        messageEditText.setText("");

        logLine(">> Method: " + paramsNotificationSend.getMethodName());
        connection.call(paramsNotificationSend, new SyncServerConnection.SyncServerCallback() {
            @Override
            public void result(Response response) {
                if (response.getResultCode() != Response.CODE_SUCCESS) {
                    logLine("<< Error: " + response.getError());
                } else {
                    logLine("<< Response: " + response.getResult());
                }
            }
        });
    }

    private interface DataGetCallback
    {
        public void finished(Data data);
    }
}
