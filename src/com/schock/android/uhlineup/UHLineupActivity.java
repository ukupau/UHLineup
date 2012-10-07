package com.schock.android.uhlineup;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.text.Editable;
import android.text.TextWatcher;
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

public class UHLineupActivity extends Activity { // implements OnClickListener {
    /** Called when the activity is first created. */

    private static final int DIALOG_GRID = 1;
    private static final int DIALOG_INFO = 2;
    private static final int DIALOG_NO_DATA = 3;
    private static final int DIALOG_BAD_DATA = 4;
    private static final int DIALOG_LIST = 5;

    String rosterDate = "";
    String rosterGame = "";

    ArrayList<Player> players;
    ArrayList<Player> opponents;
    ArrayList<Player>[] rosters = new ArrayList[2]; // Will get a compiler
                                                    // warning here.

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

    EditText nameSearch;
    ListView listView;
    int textlength = 0;

    Map<String, Integer> playerNames;
    String[] listview_array = new String[200];
    ArrayList<String> array_sort = new ArrayList<String>();

    private File sdcard;

    ImageButton useGrid;
    ImageButton useList;

    static private String TAG = "UHLineup";
    private GestureDetector gestureDetector;

    final Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        useGrid = (ImageButton) findViewById(R.id.useGrid);
        useList = (ImageButton) findViewById(R.id.useList);

        useGrid.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDialog(DIALOG_GRID);
            }
        });

        useList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showDialog(DIALOG_LIST);
            }
        });

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
        lastIdx = -1;
        lastIndex[teamNumber] = 0;

        rosters[0] = new ArrayList<Player>();
        rosters[1] = new ArrayList<Player>();

        TextView title = (TextView) findViewById(R.id.playerName);
        // title.setText("testing");

        // Find the directory for the SD Card using the API
        sdcard = Environment.getExternalStorageDirectory();
        
        // Get the text file
        InputStream inputStream = getResources().openRawResource(R.raw.uhlineup);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ii;
         try {
          ii = inputStream.read();
          while (ii != -1)
             {
              byteArrayOutputStream.write(ii);
              ii = inputStream.read();
             }
             inputStream.close();
         } catch (IOException e) {
             
         }   
         
         String text = byteArrayOutputStream.toString();
        
//        File file = new File(sdcard, "uhlineup/uhlineup.json");
//        if (file.exists()) {
//
//            // Read text from file
//            StringBuilder text = new StringBuilder();
//
//            try {
//                BufferedReader br = new BufferedReader(new FileReader(file));
//                String line;
//
//                while ((line = br.readLine()) != null) {
//                    text.append(line);
//                    text.append('\n');
//                }
//                br.close();
//            } catch (IOException e) {
//                // You'll need to add proper error handling here
//            }

            // title.setText(text.toString());

            assignedNumbers = new int[100];
            players = new ArrayList<Player>();
            playerNames = new HashMap<String, Integer>();

            try {
                JSONObject jsonObj = new JSONObject(text);

                JSONArray rosterObj = jsonObj.getJSONArray("players");
                rosterDate = jsonObj.getString("date");
                rosterGame = jsonObj.getString("opponent");

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
                    // TODO: Remove leading and trailing spaces around "/".
                    player.hometown = playerObj.getString("hometown").replace("/", "\n");
                    player.image = playerObj.getString("image");
                    playerNames.put(playerObj.getString("lastname") + ", " + playerObj.getString("firstname"), i);

                    players.add(player);
                    // Log.v("UHLineup", player.name);
                    // Log.v("UHLineup", Integer.toString(player.number));
                    assignedNumbers[player.number] = 1;
                }

            } catch (Exception e) {
                Log.v("UHLineup", e.getMessage());
            } finally {
            }

            listview_array = new String[playerNames.size()];
            int idx = 0;
            for (String s : playerNames.keySet()) {
                listview_array[idx] = s;
                idx++;
            }
            Arrays.sort(listview_array);

            // Button search = (Button) findViewById(R.id.search);
            // Button next = (Button)findViewById(R.id.next);
            // Button prev = (Button)findViewById(R.id.prev);
            // searchText = (EditText) findViewById(R.id.searchText);

            // search.setOnClickListener(this);

            // next.setOnClickListener(new View.OnClickListener() {
            // public void onClick(View view) {
            // if (currentIdx < lastIdx ) {
            // currentIdx++;
            // DisplayPlayer(currentIdx);
            // }
            //
            // }
            // });
            //
            // prev.setOnClickListener(new View.OnClickListener() {
            // public void onClick(View view) {
            // if (currentIdx > 0 ) {
            // currentIdx--;
            // DisplayPlayer(currentIdx);
            // }
            //
            // }
            // });

            // This enables to GO button on the keyboard to invoke the search
            // action.
            // android:imeOptions="actionGo" must be set on the EditText field.
            // searchText.setOnKeyListener(new OnKeyListener() {
            // public boolean onKey(View view, int keyCode, KeyEvent event) {
            // if (keyCode == KeyEvent.KEYCODE_ENTER) {
            // onClick(view);
            // return true;
            // } else {
            // return false;
            // }
            // }
            // });

            // InputMethodManager imm = (InputMethodManager)getSystemService(
            // Context.INPUT_METHOD_SERVICE);
            // imm.hideSoftInputFromWindow(search.getWindowToken(), 0);

            // TODO: Does this work? Can delete?
            // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            LinearLayout view = (LinearLayout) findViewById(R.id.LinearLayout0);

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
                    showDialog(DIALOG_LIST);
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d(TAG, "Double Tap event");
                    showDialog(DIALOG_GRID);
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
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        Log.d(TAG, "Right to left");
                        if (currentIdx < lastIdx) {
                            currentIdx++;
                            DisplayPlayer(currentIdx);
                        }
                        return true;
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        Log.d(TAG, "Left to right");
                        if (currentIdx > 0) {
                            currentIdx--;
                            DisplayPlayer(currentIdx);
                        }

                        return true;
                    }
                    return false;
                }
            });
            gestureDetector.setIsLongpressEnabled(true);

            imageView = (ImageView) findViewById(R.id.imageView1);

            options = new BitmapFactory.Options();
            options.inSampleSize = 1;

            // Keyboard slide out type. Manifest setting. No change noticed.
            // TODO: Add UH and opponent using tabs.
            // TODO: Sliding door animation

            // Note: Had problems with word wrap. Would not wrap. Don't know
            // what caused the problem.
            // Deleted and created a new text view which worked.

            if (lastIdx < 0) {
                showDialog(DIALOG_BAD_DATA);
            } else {
                DisplayPlayer(currentIdx);
            }

//        } else {
//            showDialog(DIALOG_NO_DATA);
//        }

    }

    public void DisplayPlayer(int idx) {
        if (idx <= lastIdx) {
            playerNo.setText(Integer.toString(players.get(idx).number));
            playerPosition.setText(players.get(idx).position);
            playerYear.setText(players.get(idx).year);
            playerHeight.setText(players.get(idx).height);
            playerWeight.setText(Integer.toString(players.get(idx).weight));
            playerName.setText(players.get(idx).name);
            playerHometown.setText(players.get(idx).hometown);
            Bitmap bm;

            // Order of image retrieval.
            // 1. sdcard
            // 2. assets
            // 3. default image
            // TODO: /mnt/sdcard vs /sdcard.
            // /sdcard is a symbolic link to /mnt/sdcard (at least on Samsung
            // Exhibit II)
//            File file = new File(sdcard, "/uhlineup/" + players.get(idx).image);
//            if (file.exists()) {
//                bm = BitmapFactory.decodeFile(sdcard.getPath() + "/uhlineup/" + players.get(idx).image, options);
//                imageView.setImageBitmap(bm);
//            }
//            else {
                try {
                    InputStream istr = getAssets().open(players.get(idx).image);
                    bm = BitmapFactory.decodeStream(istr);
                    imageView.setImageBitmap(bm);
                } catch (IOException e) {
                    imageView.setImageResource(R.drawable.uhwarriors1);
                }
//            } 
            currentIdx = idx;

        }
    }

    // public void findPLayer() {
    //
    // int playerNumber;
    // try {
    // playerNumber = Integer.parseInt(searchText.getText().toString());
    // }
    // catch (Exception e) {
    // playerNumber = 0;
    // }
    //
    // for (int i = 0; i <= lastIdx; i++) {
    // if (players.get(i).number == playerNumber) {
    // currentIdx = i;
    // DisplayPlayer(currentIdx);
    // }
    // }
    //
    //
    // }

    private void closeSoftKeyboard() {
        // Close soft keyboard, if open.
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        // imm.toggleSoftInput(0, 0); // This seems to be a toggle.
    }

    public void nextPlayer(View view) {
        if (currentIdx < lastIdx) {
            currentIdx++;
            DisplayPlayer(currentIdx);
        }
    }

    public void prevPlayer(View view) {
        if (currentIdx > 0) {
            currentIdx--;
            DisplayPlayer(currentIdx);
        }
    }

    public void findPlayer(int playerNumber) {
        // TODO: Need to handle case where the number does not exist.
        // It is assumed the player numbers are in order, lowest to highest.
        for (int i = 0; i <= lastIdx; i++) {
            if (players.get(i).number == playerNumber) {
                // currentIdx = i;
                DisplayPlayer(i);
                break;
            } else if (players.get(i).number > playerNumber) {
                Toast.makeText(context, "Player not found: " + Integer.toString(playerNumber), Toast.LENGTH_SHORT).show();
                // currentIdx = i;
                DisplayPlayer(i);
                break;
            }
        }
    }

    // @Override
    // public void onClick(View arg0) {
    // showDialog(1);
    // }

    // @Override
    public void onClick2(View arg0) {

        closeSoftKeyboard();

        int playerNumber;
        try {
            playerNumber = Integer.parseInt(searchText.getText().toString());
        } catch (Exception e) {
            playerNumber = 0;
        }

        // TODO: Need to handle case where the number does not exist.
        // It is assumed the player numbers are in order, lowest to highest.
        for (int i = 0; i <= lastIdx; i++) {
            if (players.get(i).number == playerNumber) {
                currentIdx = i;
                DisplayPlayer(currentIdx);
                break;
            } else if (players.get(i).number > playerNumber) {
                currentIdx = i;
                DisplayPlayer(currentIdx);
                break;
            }
        }

        // Blank search string when done.
        searchText.setText("");

        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
        // case R.id.settings:
        // Toast.makeText(this, "User settings", Toast.LENGTH_SHORT).show();
        // return true;
        case R.id.information:
            showDialog(DIALOG_INFO);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
        case DIALOG_GRID:

            // custom dialog
            dialog.setContentView(R.layout.number_grid);

            GridView gridview = (GridView) dialog.findViewById(R.id.gridview);

            ImageAdapter imageAdapter = new ImageAdapter(this);

            // int[] playerNumbers = new int[100];
            // for (int i = 0; i <= 99; i++) {
            // playerNumbers[i] = 0;
            // }
            //
            // for (int i = 0; i <= lastIdx; i++) {
            // playerNumbers[players.get(i).number] = 1;
            // }
            imageAdapter.playerNumbers = assignedNumbers; // playerNumbers;

            gridview.setAdapter(imageAdapter);

            int iDisplayWidth = getResources().getDisplayMetrics().widthPixels - 30;

            int iImageWidth = (iDisplayWidth / 10);
            Log.v("GridTest", Integer.toString(iDisplayWidth));
            Log.v("GridTest", "padding: " + Integer.toString(gridview.getListPaddingLeft()));
            Log.v("GridTest", "padding left: " + Integer.toString(gridview.getPaddingLeft()));
            Log.v("GridTest", "padding right: " + Integer.toString(gridview.getPaddingRight()));
            gridview.setColumnWidth(iImageWidth);
            gridview.setStretchMode(GridView.NO_STRETCH);

            gridview.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // Toast.makeText(context, "" + position,
                    // Toast.LENGTH_SHORT).show();
                    // TextView view = (TextView)v;
                    dismissDialog(DIALOG_GRID);
                    findPlayer(position);
                }
            });

            break;
        case DIALOG_INFO:
            String msg = "Game date: " + rosterDate + "\n" + "Game opponent: " + rosterGame;

            builder.setMessage(msg).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            dialog = builder.create();
            break;

        case DIALOG_NO_DATA:
            String msg2 = "Player data not found.";

            builder.setMessage(msg2).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    finish();
                    Log.v(TAG, "Attempting to close app");
                }
            });
            dialog = builder.create();
            Log.v(TAG, "Missing player data dialog");
            break;
        case DIALOG_BAD_DATA:
            String msg3 = "Error reading player data.";

            builder.setMessage(msg3).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    finish();
                    Log.v(TAG, "Attempting to close app");
                }
            });
            dialog = builder.create();
            Log.v(TAG, "Error reading player data dialog");
            break;
        case DIALOG_LIST:

            // custom dialog
            dialog.setContentView(R.layout.name_search);

            nameSearch = (EditText) dialog.findViewById(R.id.nameSearch);
            listView = (ListView) dialog.findViewById(R.id.listView1);

            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listview_array));

            nameSearch.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    // Abstract Method of TextWatcher Interface.
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Abstract Method of TextWatcher Interface.
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    textlength = nameSearch.getText().length();
                    array_sort.clear();
                    for (int i = 0; i < listview_array.length; i++) {
                        if (textlength <= listview_array[i].length()) {
                            if (nameSearch.getText().toString().equalsIgnoreCase((String) listview_array[i].subSequence(0, textlength))) {
                                array_sort.add(listview_array[i]);
                            }
                        }
                    }
                    listView.setAdapter(new ArrayAdapter<String>(UHLineupActivity.this, android.R.layout.simple_list_item_1, array_sort));
                }
            });

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String text = ((TextView) view).getText().toString();
                    // Toast.makeText(getApplicationContext(),
                    // "name: " + text + ", index: " +
                    // playerNames.get(text).toString(), Toast.LENGTH_SHORT)
                    // .show();
                    dismissDialog(DIALOG_LIST);
                    DisplayPlayer(playerNames.get(text));
                    nameSearch.setText("");
                }
            });

            // Always force open keyboard.
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            break;
        default:
            // dialog = null;
        }
        return dialog;
    }

}