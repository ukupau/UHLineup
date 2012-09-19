package com.schock.android.uhlineup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class UHLineupActivity extends Activity implements OnClickListener, OnGestureListener {
    /** Called when the activity is first created. */

    ArrayList<Player> players;
    int currentIdx;
    int lastIdx;
    TextView playerNo;
    TextView playerPosition;
    TextView playerYear;
    TextView playerHeight;
    TextView playerWeight;
    TextView playerName;
    TextView playerHometown;
    EditText searchText;
    
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private GestureDetector gestureScanner;
   

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        playerNo = (TextView) findViewById(R.id.playerNo);
        playerPosition = (TextView) findViewById(R.id.playerPosition);
        playerYear = (TextView) findViewById(R.id.playerYear);
        playerHeight = (TextView) findViewById(R.id.playerHeight);
        playerWeight = (TextView) findViewById(R.id.playerWeight);
        playerName = (TextView) findViewById(R.id.playerName);
        playerHometown = (TextView) findViewById(R.id.hometown);
        
        currentIdx = 0;
        lastIdx = 0;
        
        gestureScanner = new GestureDetector(this);
        
        TextView title = (TextView) findViewById(R.id.playerName);
        //title.setText("testing");

        // Find the directory for the SD Card using the API
        File sdcard = Environment.getExternalStorageDirectory();
        // Get the text file
        // TODO: Test for missing file should go here
        File file = new File(sdcard, "uhlineup/uhlineup.json");

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

                players.add(player);
                //Log.v("UHLineup", player.name);
                //Log.v("UHLineup", Integer.toString(player.number));
            }

        } catch (Exception e) {
            Log.v("UHLineup", e.getMessage());
        } finally {
        }
        
        Button search = (Button)findViewById(R.id.search);
        Button next = (Button)findViewById(R.id.next);
        Button prev = (Button)findViewById(R.id.prev);
        searchText = (EditText)findViewById(R.id.searchText);
        
        search.setOnClickListener(this); 
        
//        search.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                int playerNumber;
//                try {
//                    playerNumber = Integer.parseInt(searchText.getText().toString());
//                }
//                catch (Exception e) {
//                    playerNumber = 0;
//                }
//                
//                for (int i = 0; i <= lastIdx; i++) {
//                    if (players.get(i).number == playerNumber) {
//                        currentIdx = i;
//                        DisplayPlayer(currentIdx);
//                    }
//                }
//                
//                // Blank search string when done.
//                searchText.setText("");
//            }
//          });        
        
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentIdx < lastIdx ) {
                    currentIdx++;
                    DisplayPlayer(currentIdx);
                }
                
            }
        });
        
        prev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (currentIdx > 0 ) {
                    currentIdx--;
                    DisplayPlayer(currentIdx);
                }
                
            }
        });

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
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        
        DisplayPlayer(currentIdx);

        // TODO: Display okinas
        // TODO: Remove slash in hometown and replace with line feed.
        // TODO: Keyboard slide out type
        // TODO: Work with different external storage locations
        
        // Note: Had problems with word wrap. Would not wrap. Don't know what caused the problem.
        //       Deleted and created a new text view which worked.
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


    @Override
    public void onClick(View arg0) {
        
        closeSoftKeyboard();
        
        int playerNumber;
        try {
            playerNumber = Integer.parseInt(searchText.getText().toString());
        }
        catch (Exception e) {
            playerNumber = 0;
        }
        
        for (int i = 0; i <= lastIdx; i++) {
            if (players.get(i).number == playerNumber) {
                currentIdx = i;
                DisplayPlayer(currentIdx);
            }
        }
       
        // Blank search string when done.
        searchText.setText("");
        
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
   }

    @Override
    public boolean onTouchEvent(MotionEvent me)
    {
        //Log.v("UHLineup","onTouchEvent");
    return gestureScanner.onTouchEvent(me);
    }


    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        //Log.v("UHLineup","onDown");
        return false;
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //Log.v("UHLineup","onFling");
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
            return false;
            // right to left swipe
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            //Toast.makeText(getApplicationContext(), "Left Swipe", Toast.LENGTH_SHORT).show();
            //Log.v("UHLineup","Left Swipe");
                if (currentIdx < lastIdx ) {
                    currentIdx++;
                    DisplayPlayer(currentIdx);
                }
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            //Toast.makeText(getApplicationContext(), "Right Swipe", Toast.LENGTH_SHORT).show();
            //Log.v("UHLineup","Right Swipe");
                if (currentIdx > 0 ) {
                    currentIdx--;
                    DisplayPlayer(currentIdx);
                }
            }
            else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            //Toast.makeText(getApplicationContext(), "Swipe up", Toast.LENGTH_SHORT).show();
            //Log.v("UHLineup","Swipe up");
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            //Toast.makeText(getApplicationContext(), "Swipe down", Toast.LENGTH_SHORT).show();
            //Log.v("UHLineup","Swipe down");
            }
            } catch (Exception e) {
            // nothing
            }
        return true;
        }


    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
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
            showDialog(1);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
 
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(id) {
        case 1:
            
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
        default:
            dialog = null;
        }
        return dialog;
    }

        
    
}