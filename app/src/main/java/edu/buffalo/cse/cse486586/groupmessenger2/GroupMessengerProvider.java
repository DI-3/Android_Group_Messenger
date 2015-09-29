package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    static final String TAG = GroupMessengerProvider.class.getSimpleName();
    private static Uri mUri ;
    private static int seq_no = 0;
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */

        String filename = values.getAsString("key");
        String value = values.getAsString("value");
        try{
            FileOutputStream fpOutput = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            fpOutput.write(value.getBytes());
            fpOutput.close();

        }catch(IOException ae){

            Log.e(TAG, "Filewrite  error - IOException");
        }


        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the  formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */

        Collections.sort(GroupMessengerActivity.msgQueue);

        Iterator delvIterator = GroupMessengerActivity.msgQueue.iterator();
        while (delvIterator.hasNext()) {
            //to do test thi and remove
            Message ms = (Message) delvIterator.next();
            if (ms.isDeliverable) {
                                /* Set the key value pair & store it in  the content provider*/
                GroupMessengerActivity.mContentResolver = getContext().getContentResolver();
                mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                ContentValues content = new ContentValues();
                content.put(KEY_FIELD, Integer.toString(seq_no++));
                content.put(VALUE_FIELD, ms.msg);
                GroupMessengerActivity.mContentResolver.insert(mUri, content);
                // sequenceNumber++;
                                /* Calling the publish progress method                            */
                delvIterator.remove();
            } else break;
        }

        String filename = selection ;
        String[] coulmnNames = {"key","value"};
        MatrixCursor a = new MatrixCursor(coulmnNames);

        try{
            // Reference http://www.mkyong.com/java/how-to-convert-inputstream-to-string-in-java/
            //byte[] buffer = new byte[300];
            StringBuilder sb =  new StringBuilder();
            FileInputStream fpInput = getContext().openFileInput(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fpInput));
            //int readSize = fpInput.read(buffer);
            sb.append(br.readLine());
            Object[] colVals = {filename,sb.toString()};
            a.addRow(colVals);
            fpInput.close();
        }catch(IOException e){

        }


        Log.v("query", selection);
        return a;
    }
}
