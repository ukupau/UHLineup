package com.schock.android.uhlineup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class UHLineupActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */

    ArrayList<Player> players;
    ArrayList<Player> opponents;
    ArrayList<Player>[] rosters = new ArrayList[2];  // Will get a compiler warning here.
    int currentIdx;
    int[] currentIndex = new int[2];
    int lastIdx;
    int[] lastIndex = new int[2];
    int teamNumber = 0;
    private int[] assignedNumbers;
    
    TextView playerNo;
    TextView playerPosition;
    TextView playerYear;
    TextView playerHeight;
    TextView playerWeight;
    TextView playerName;
    TextView playerHometown;
    EditText searchText;
    ImageView imageView;
    BitmapFactory.Options options;
    
//    private static final int SWIPE_MIN_DISTANCE = 120;
//    private static final int SWIPE_MAX_OFF_PATH = 250;
//    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    static private String TAG = "UHLineup";
    private GestureDetector gestureDetector;
    
    final Context context = this;
   

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Get data from external files dir (e.g. /SDCARD)
        // If this doesn't exist, use the default data in the raw resource.
        
        playerNo = (TextView) findViewById(R.id.playerNo);
        playerPosition = (TextView) findViewById(R.id.playerPosition);
        playerYear = (TextView) findViewById(R.id.playerYear);
        playerHeight = (TextView) findViewById(R.id.playerHeight);
        playerWeight = (TextView) findViewById(R.id.playerWeight);
        playerName = (TextView) findViewById(R.id.playerName);
        playerHometown = (TextView) findViewById(R.id.hometown);

        teamNumber = 0;

        currentIdx = 0;
        currentIndex[teamNumber] = 0;
        lastIdx = 0;
        lastIndex[teamNumber] = 0;

        rosters[0] = new ArrayList<Player>();
        rosters[1] = new ArrayList<Player>();
        
        TextView title = (TextView) findViewById(R.id.playerName);
        //title.setText("testing");

        // Find the directory for the SD Card using the API
        File sdcard = Environment.getExternalStorageDirectory();
        // Get the text file
        // TODO: Test for missing file should go here
        File file = new File(sdcard, "uhlineup/uhlineup.json");
        if (file.exists()) {

        // Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            // You'll need to add proper error handling here
        }

        //title.setText(text.toString());

        assignedNumbers = new int[100];
        players = new ArrayList<Player>();

        try {
            JSONObject jsonObj = new JSONObject(text.toString());

            JSONArray rosterObj = jsonObj.getJSONArray("players");

            lastIdx = rosterObj.length() - 1;
            
            for (int i = 0; i < rosterObj.length(); i++) {
                JSONObject playerObj = rosterObj.getJSONObject(i);

                Player player = new Player();

                // these 2 are strings
                player.name = playerObj.getString("name");
                player.number = playerObj.getInt("number");
                player.weight = playerObj.getInt("weight");
                player.height = playerObj.getString("height");
                player.year = playerObj.getString("year");
                player.position = playerObj.getString("position");
                player.hometown = playerObj.getString("hometown");
                player.image = playerObj.getString("image");

                players.add(player);
                //Log.v("UHLineup", player.name);
                //Log.v("UHLineup", Integer.toString(player.number));
                assignedNumbers[player.number]= 1; 
            }

        } catch (Exception e) {
            Log.v("UHLineup", e.getMessage());
        } finally {
        }
        
        Button search = (Button)findViewById(R.id.search);
//        Button next = (Button)findViewById(R.id.next);
//        Button prev = (Button)findViewById(R.id.prev);
        searchText = (EditText)findViewById(R.id.searchText);
        
        search.setOnClickListener(this); 
        
//        next.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if (currentIdx < lastIdx ) {
//                    currentIdx++;
//                    DisplayPlayer(currentIdx);
//                }
//                
//            }
//        });
//        
//        prev.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if (currentIdx > 0 ) {
//                    currentIdx--;
//                    DisplayPlayer(currentIdx);
//                }
//                
//            }
//        });

        // This enables to GO button on the keyboard to invoke the search action.
        // android:imeOptions="actionGo" must be set on the EditText field.
        searchText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    onClick(view);
                    return true;
                } else {
                    return false;
                }
            }
        });
        
//        InputMethodManager imm = (InputMethodManager)getSystemService(
//                Context.INPUT_METHOD_SERVICE);
//          imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
        
        // TODO: Does this work? Can delete?
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        LinearLayout view = (LinearLayout)findViewById(R.id.LinearLayout0);
        
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
 
        gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "Long Press event");
                showDialog(1);
            }
 
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "Double Tap event");
                showDialog(1);
                return true;
            }
 
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            
            private static final int SWIPE_MIN_DISTANCE = 60;
            private static final int SWIPE_THRESHOLD_VELOCITY = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.d(TAG, "Right to left");
                    if (currentIdx < lastIdx ) {
                        currentIdx++;
                        DisplayPlayer(currentIdx);
                    }
                    return true;
                 } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                     Log.d(TAG, "Left to right");
                     if (currentIdx > 0 ) {
                         currentIdx--;
                         DisplayPlayer(currentIdx);
                     }
                     
                    return true;
                 }
                 return false;
            }            
        });
        gestureDetector.setIsLongpressEnabled(true);
        
        imageView = (ImageView)findViewById(R.id.imageView1);
        
        options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        // TODO: Remove slash in hometown and replace with line feed.
        // Keyboard slide out type. Manifest setting. No change noticed.
        // TODO: Work with different external storage locations
        // TODO: Help info should show date, source and location of data.
        // TODO: Add UH and opponent using tabs.
        // TODO: Character in Ne'quan
        // TODO: Sliding door animation
        
        // Note: Had problems with word wrap. Would not wrap. Don't know what caused the problem.
        //       Deleted and created a new text view which worked.

        DisplayPlayer(currentIdx);
        }
        else {
            showDialog(3);
        }

    }
    
    
    public void DisplayPlayer (int idx) {
        if (idx <= lastIdx) {
            playerNo.setText(Integer.toString(players.get(idx).number));
            playerPosition.setText(players.get(idx).position);
            playerYear.setText(players.get(idx).year);
            playerHeight.setText(players.get(idx).height);
            playerWeight.setText(Integer.toString(players.get(idx).weight));
            playerName.setText(players.get(idx).name);
            playerHometown.setText(players.get(idx).hometown);
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 2;
            Bitmap bm;
//            if (players.get(idx).number == 1) {
//                bm = BitmapFactory.decodeFile("/sdcard/uhlineup/Edwards_Mike12_7844.jpg", options);
//            }
//            else {
//                bm = BitmapFactory.decodeFile("/sdcard/uhlineup/UHWarriors1.png", options);
//            }
            bm = BitmapFactory.decodeFile("/sdcard/uhlineup/" + players.get(idx).image, options);
            imageView.setImageBitmap(bm);         
            
        }
    }
    
    
    
//    public void findPLayer() {
//        
//        int playerNumber;
//        try {
//            playerNumber = Integer.parseInt(searchText.getText().toString());
//        }
//        catch (Exception e) {
//            playerNumber = 0;
//        }
//        
//        for (int i = 0; i <= lastIdx; i++) {
//            if (players.get(i).number == playerNumber) {
//                currentIdx = i;
//                DisplayPlayer(currentIdx);
//            }
//        }
//        
//        
//    }
    
    
    private void closeSoftKeyboard() {
        // Close soft keyboard, if open.
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        //imm.toggleSoftInput(0, 0);  // This seems to be a toggle.
    }
    
    public void nextPlayer(View view) {
        if (currentIdx < lastIdx ) {
            currentIdx++;
            DisplayPlayer(currentIdx);
        }
    }
    
    public void prevPlayer(View view) {
        if (currentIdx > 0 ) {
            currentIdx--;
            DisplayPlayer(currentIdx);
        }
    }

    public void findPlayer(int playerNumber) {
        // TODO: Need to handle case where the number does not exist.
        // It is assumed the player numbers are in order, lowest to highest.
        for (int i = 0; i <= lastIdx; i++) {
            if (players.get(i).number == playerNumber) {
                currentIdx = i;
                DisplayPlayer(currentIdx);
                break;
            }
            else if (players.get(i).number > playerNumber) {
                Toast.makeText(context, "Player not found", Toast.LENGTH_SHORT).show();
                currentIdx = i;
                DisplayPlayer(currentIdx);
                break;
            }
        }
    }

    
    @Override
    public void onClick(View arg0) {
    showDialog(1);
    }

    //@Override
    public void onClick2(View arg0) {
        
        closeSoftKeyboard();
        
        int playerNumber;
        try {
            playerNumber = Integer.parseInt(searchText.getText().toString());
        }
        catch (Exception e) {
            playerNumber = 0;
        }
        
        // TODO: Need to handle case where the number does not exist.
        // It is assumed the player numbers are in order, lowest to highest.
        for (int i = 0; i <= lastIdx; i++) {
            if (players.get(i).number == playerNumber) {
                currentIdx = i;
                DisplayPlayer(currentIdx);
                break;
            }
            else if (players.get(i).number > playerNumber) {
                currentIdx = i;
                DisplayPlayer(currentIdx);
                break;
            }
        }
       
        // Blank search string when done.
        searchText.setText("");
        
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//        case R.id.settings:
//            Toast.makeText(this, "User settings", Toast.LENGTH_SHORT).show();
//            return true;
        case R.id.information:
            showDialog(2);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {
        case 1:
            
            // custom dialog
            dialog.setContentView(R.layout.number_grid);
            
            GridView gridview = (GridView) dialog.findViewById(R.id.gridview);
 
            ImageAdapter imageAdapter = new ImageAdapter(this);
            
//            int[] playerNumbers = new int[100];
//            for (int i = 0; i <= 99; i++) {
//                playerNumbers[i] = 0;
//            }
//            
//            for (int i = 0; i <= lastIdx; i++) {
//                playerNumbers[players.get(i).number] = 1;
//            }
            imageAdapter.playerNumbers = assignedNumbers; // playerNumbers;
            
            gridview.setAdapter(imageAdapter);   
            
            int iDisplayWidth = getResources().getDisplayMetrics().widthPixels - 30 ;

            int iImageWidth = (iDisplayWidth / 10 ); 
            Log.v("GridTest",Integer.toString(iDisplayWidth));
            Log.v("GridTest","padding: " + Integer.toString(gridview.getListPaddingLeft()));
            Log.v("GridTest","padding left: " + Integer.toString(gridview.getPaddingLeft()));
            Log.v("GridTest","padding right: " + Integer.toString(gridview.getPaddingRight()));
            gridview.setColumnWidth( iImageWidth );
            gridview.setStretchMode( GridView.NO_STRETCH ) ;   
            
            gridview.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    //Toast.makeText(context, "" + position, Toast.LENGTH_SHORT).show();
                    //TextView view = (TextView)v;
                    dismissDialog(1);
                    findPlayer(position);
                }
            });
           
            break;
        case 2:
            String msg =
            "Device information:\n"
            + "";
   
   builder.setMessage(msg)
   .setCancelable(true)
   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int id) {
           dialog.cancel();
       }
   });
   dialog = builder.create();
   break;
            
        case 3:
            String msg2 = "Player data not found.";
   
   builder.setMessage(msg2)
   .setCancelable(true)
   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int id) {
           dialog.cancel();
           finish();
           Log.v(TAG,"Attempting to close app");
      }
   });
   dialog = builder.create();
   Log.v(TAG,"Missing player data dialog");
   break;
        default:
            //dialog = null;
        }
        return dialog;
    }
    
    
}