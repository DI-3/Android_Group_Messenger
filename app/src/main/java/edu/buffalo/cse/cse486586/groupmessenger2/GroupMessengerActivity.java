package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
class Message implements Comparable {

    String msg = null;
    int msgId;
    int process_id;
    int seq_number;
    int sugg_process;
    String port_num;
    String msg_type;
    boolean isDeliverable;

    Message(int msgid,int pid,int seqnum,String port,String msgs){

        this.process_id = pid;
        this.msgId = msgid;
        this.seq_number = seqnum;
        this.port_num = new String(port);
        this.msg = new String(msgs);
        this.isDeliverable = false;
        this.sugg_process = 0;
    }

    Message(int msgid,int pid,String port,String msgs,int propos_id,int currprocessId){

        this.msgId = msgid;
        this.process_id = pid;
        this.seq_number = propos_id;
        this.msg = new String(msgs);
        this.isDeliverable = false;
        this.sugg_process = currprocessId;
        this.port_num = new String(port);

    }


//    @Override
//    public int compareTo(Object another) {
//        Message other = (Message)another;
//
//        int i = this.seq_number - other.seq_number ;
//        if(i == 0)
//            return (this.sugg_process - other.sugg_process) ;
//        else return i ;
//    }


    @Override
    public int compareTo(Object arg0) {
        // TODO Auto-generated method stubis
        Message other = (Message)arg0;
        int i = this.seq_number - other.seq_number ;
        if(i==0){
            if(this.isDeliverable && other.isDeliverable || !this.isDeliverable && !other.isDeliverable)
                return (this.sugg_process - other.sugg_process);
            else{
                if(!this.isDeliverable) return -1 ;
                else if(this.isDeliverable) return 1;
            }

        }else{

        }
        return i;
    }
}

public class GroupMessengerActivity extends Activity {


    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final String[] PORTS = {"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT  = 10000;
    public static int sequenceNumber = 0;
    public static int seq_no  = 0;
    public static int sNo            = 0;
    public static int agreedNo       = 0;
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    public static ContentResolver mContentResolver ;
    private static Uri mUri ;
    static HashMap<String,Integer> hmProcessId = new HashMap<String,Integer>();
    //static HashMap<Integer,ArrayList<String>> hmMessages = new HashMap<Integer,ArrayList<String>>();
    public static ArrayList<Message> msgQueue = new ArrayList<Message>();
    public static HashMap<Integer,ArrayList<Integer[]>> hmProposals = new HashMap<Integer,ArrayList<Integer[]>>();
    public static String Iport = "null";
    public static boolean flag = false;
    Timer tm = null;
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);


        //initialise the hashmap of processids
        for(int i=0;i<5;i++)
            hmProcessId.put(PORTS[i],i+1);


        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */

        TelephonyManager telpmngr = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        String portStr = telpmngr.getLine1Number().substring(telpmngr.getLine1Number().length() - 4);
        final String myServerPort = String.valueOf(Integer.parseInt(portStr) * 2);
        Iport =  String.valueOf(Integer.parseInt(portStr) * 2);

        try{

            /*Create a server socket with the thread async task  that listens on the server port
             * This has to run asynchronously and concurrently with the other tasks
            */
            ServerSocket servSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,servSocket);

        }catch(IOException ae){

            Log.e(TAG, "Error creating a server socket");
        }

        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        final EditText editText = (EditText) findViewById(R.id.editText1);

        Button sendButton =  (Button)findViewById(R.id.button4);
        /* Setting the onClick listener for the send button */
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strMsg = editText.getText().toString()+"\n";
                editText.setText("");
                TextView textView = (TextView)findViewById(R.id.textView1);
                textView.append("\n"+strMsg);
                new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,strMsg,myServerPort);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in GroupMessengerActivity.
     *
     *
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            try {
                /* the server keeps on listening on the server socket */
                while (true) {
                    Socket socket = serverSocket.accept();
                    //Reference :http://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String msg = in.readLine();

                    //Get the data from the sender and save it onto the object
                    String[] data = msg.split("###");

                    /*  Send the proposal and the data to the queue  */
                    if (data[0].equals("MSG")) {

                        int propValue = Math.max(sNo, agreedNo) + 1;
                        sNo++;
                        Message objMsg = new Message(Integer.parseInt(data[2]), Integer.parseInt(data[1]), data[3], data[4], sNo, hmProcessId.get(Iport)); //change this to SNO.
                        StringBuilder sb = new StringBuilder();
                        sb.append("PRPSL").append("###").append(data[2]).append("###").append(propValue).append("###") //changed to data[2] from objMsg.seqnumber
                                .append(hmProcessId.get(Iport)).append("###").append(data[3]).append("###").append(data[4]);
                        msgQueue.add(objMsg);
                        Collections.sort(msgQueue);
                        sendMessage(sb.toString(), objMsg.port_num);
                    }
                    // Get the proposals and then get the highest proposals and mulicast
                    if (data[0].equals("PRPSL")) {

                        int imsgId = Integer.parseInt(data[1]);
                        int iproposal = Integer.parseInt(data[2]);
                        Integer[] lsPrpPair = new Integer[2];
                        lsPrpPair[0] = iproposal;
                        lsPrpPair[1] = Integer.parseInt(data[3]);

                        if (hmProposals.get(imsgId) == null) {
                            ArrayList<Integer[]> lsProposals = new ArrayList<Integer[]>();
                            lsProposals.add(lsPrpPair);
                            hmProposals.put(imsgId, lsProposals);
                        } else hmProposals.get(imsgId).add(lsPrpPair);

                        //Loop through all the msgId and get the maximum proposal if all nodes proposals have reached
                        int id = 0, proposeVal;

                        for (Iterator<Map.Entry<Integer, ArrayList<Integer[]>>> iterentry = hmProposals.entrySet().iterator(); iterentry.hasNext(); ) {
                            //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                            Map.Entry<Integer, ArrayList<Integer[]>> entry = iterentry.next();
                            id = entry.getKey();
                            if (entry.getValue().size() == 5) {

                                //proposeVal = Collections.max(entry.getValue());
                                int maxProposal = -1;
                                int maxProcess = -1;
                                for (Integer[] a : entry.getValue()) {
                                    if (a[0] > maxProposal) {
                                        maxProposal = a[0];
                                        maxProcess = a[1];
                                    }
                                    //if(a[0] == maxProposal){ if(a[1] < maxProcess) maxProcess = a[1]; }
                                }
                                StringBuilder sb = new StringBuilder();
                                sb.append("AGREE").append("###").append(id).append("###").append(String.valueOf(maxProposal)).append("###")
                                        .append(hmProcessId.get(Iport)).append("###").append(String.valueOf(maxProcess)).append("###").append(data[4])
                                        .append("###").append(data[5]);
                                bMultiCast(sb.toString());
                                System.out.println(sb.toString());
                                iterentry.remove();
                            }
                        }
                    }

                    if (data[0].equals("AGREE")) {

//                        if (!flag) {
//                            tm = new Timer();
//                            tm.schedule(delivers(),5000);
//                            flag = true;
//                        } else {
//                            if(tm!=null) tm.cancel();
//                             tm = new Timer();
//                             tm.schedule(delivers(),5000);
//                        }
                        // Loop through the message queue and select the particular message and set the agreed proposal
                        // and change undeliver to Deliver
                        System.out.println("Incoming message AG" + msg);
                        Log.i("AGREED", msg);
                        //sNo = Math.max(sNo,Integer.parseInt(data[2]));
                        agreedNo = Math.max(sNo, Integer.parseInt(data[2]));
                        Iterator iterator = msgQueue.iterator();
                        while (iterator.hasNext()) {
                            Message ms = (Message) iterator.next();
                            if (ms.msgId == Integer.parseInt(data[1]) && ms.process_id == Integer.parseInt(data[3])) {//&& ms.port_num.equals(data[5])
                                ms.isDeliverable = true;
                                ms.seq_number = Integer.parseInt(data[2]);
                                ms.sugg_process = Integer.parseInt(data[3]);  //changed data[4] to data[3]
                            }
                        }

//                        Collections.sort(msgQueue);
//
//                        Iterator delvIterator = msgQueue.iterator();
//                        while(delvIterator.hasNext()){
//                              //to do test thi and remove
//                             Message ms = (Message)delvIterator.next();
//                             if(ms.isDeliverable){
//                                /* Set the key value pair & store it in  the content provider*/
//                                 mContentResolver = getContentResolver();
//                                 mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
//                                 ContentValues content = new ContentValues();
//                                 content.put(KEY_FIELD,Integer.toString(seq_no++));
//                                 content.put(VALUE_FIELD,ms.msg);
//                                 mContentResolver.insert(mUri, content);
//                                 // sequenceNumber++;
//                                /* Calling the publish progress method                            */
//                                 delvIterator.remove();
//                             }else break;
//                        }
                    }
                    publishProgress(msg);
                    in.close();
                    socket.close();
                }
            } catch (IOException ex) {
                Log.e(TAG, "ClientTask socket IOException");
            }
            return null;
        }


        public synchronized TimerTask delivers() {

            TimerTask  timetk  = new TimerTask() {
                @Override
                public synchronized void run() {

                    Collections.sort(msgQueue);

                    Iterator delvIterator = msgQueue.iterator();
                    while (delvIterator.hasNext()) {
                        //to do test thi and remove
                        Message ms = (Message) delvIterator.next();
                        if (ms.isDeliverable) {
                                /* Set the key value pair & store it in  the content provider*/
                            mContentResolver = getContentResolver();
                            mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
                            ContentValues content = new ContentValues();
                            content.put(KEY_FIELD, Integer.toString(seq_no++));
                            content.put(VALUE_FIELD, ms.msg);
                            mContentResolver.insert(mUri, content);
                            // sequenceNumber++;
                                /* Calling the publish progress method                            */
                            delvIterator.remove();
                        } else break;
                    }
                }
            };
            flag = false;
            return timetk ;
        }

        protected synchronized void  bMultiCast(String msgs){
            for(int i=0;i<PORTS.length ;i++)
             sendMessage(msgs,PORTS[i]);
        }


        protected synchronized void sendMessage(String ms,String port){

            try{

                Socket send_socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(port));
                DataOutputStream out_stream = new DataOutputStream(send_socket.getOutputStream());
                out_stream.flush();
                out_stream.writeBytes(ms);
                out_stream.flush();
                out_stream.close();
                send_socket.close();

            }catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView extView = (TextView) findViewById(R.id.textView1);
            extView.append(strReceived + "\t\n");

            String filename = "GroupMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;
            if(string.contains("AGREE")) {
                try {
                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(string.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "File write failed");
                }
            }
            return;
        }
    }

    /***
     * Client Task extends Aynctask and its duty is to multicast the message to all the nodes in the network
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {


            String[] portNos = {REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4};
            sequenceNumber++;
            //sNo++;

            for(int i=0;i<portNos.length ;i++){

                try{
                    String remotePort = portNos[i];
                    //if(msgs[1].equals(remotePort)) continue;

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    String msgToSend = msgs[0];
                    int process_id = hmProcessId.get(msgs[1]);

                    StringBuilder sb = new StringBuilder();
                    sb.append("MSG").append("###").append(process_id).append("###").append(sequenceNumber).
                              append("###").append(msgs[1]).append("###").append(msgToSend);


                    /* Create a outputstream to write to the socket */
                    //Reference :http://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
                    DataOutputStream out_stream = new DataOutputStream(socket.getOutputStream());
                    OutputStream opStream = socket.getOutputStream();
                    out_stream.writeBytes(sb.toString());
                    out_stream.flush();
                    out_stream.close();
                    opStream.close();
                    socket.close();

                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                }
            }
            return null;
        }
    }
}
