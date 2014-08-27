package com.schock.android.uhlineup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class UHLineupActivity extends Activity { // implements OnClickListener {
    /** Called when the activity is first created. */

    // http://hawaiiathletics.com/roster.aspx?rp_id=12469&path=football
    private static final String UH_URL = "http://hawaiiathletics.com/roster.aspx?rp_id=";
    
    private static final int DIALOG_GRID = 1;
    private static final int DIALOG_ABOUT = 2;
    private static final int DIALOG_NO_DATA = 3;
    private static final int DIALOG_BAD_DATA = 4;
    private static final int DIALOG_LIST = 5;
    private static final int DIALOG_SETTINGS = 6;

    private String rosterDate = "";
    private String rosterGame = "";

    private ArrayList<Player> players;
    private ArrayList<Player> opponents;
    private ArrayList<Player>[] rosters = new ArrayList[2]; // Will get a compiler
                                                    // warning here.

    private int currentIdx;
    private int[] currentIndex = new int[2];
    private int lastIdx;
    private int[] lastIndex = new int[2];
    private int teamNumber = 0;
    private int[] assignedNumbers;
    private String currentPlayerFile;

    private TextView[] playerNo;
    private TextView[] playerPosition;
    private TextView[] playerYear;
    private TextView[] playerHeight;
    private TextView[] playerWeight;
    private TextView[] playerName;
    private TextView[] playerHometown;
    private ImageView[] playerImage;

    private EditText searchText;
    private ImageView imageView;
    private BitmapFactory.Options options;

    private Button buttonOK;
    private Button buttonClose;

    private EditText nameSearch;
    private ListView listView;
    private ViewFlipper viewFlipper;

    private int textlength = 0;

    private Map<String, Integer> playerNames;
    private String[] listview_array = new String[200];
    private ArrayList<String> array_sort = new ArrayList<String>();

    private Boolean isPortraitMode;
    private CheckBox lockPortraitMode;
    
    private File sdcard;

    private ImageButton useGrid;
    private ImageButton useList;
    
    private TextView textAboutHeader;

    private static String TAG = "UHLineup";
    private GestureDetector gestureDetector;

    private final Context context = this;
    
    private int currentView;
    private int lastView;
    
    private Drawable searchDrawable;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        useGrid = (ImageButton) findViewById(R.id.useGrid);
        useList = (ImageButton) findViewById(R.id.useList);

        // Select using player number grid.
        // Note: Font used on button is Allstar, Small from dafont.com.
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

        playerNo = new TextView[2];
        playerPosition = new TextView[2];
        playerYear = new TextView[2];
        playerHeight = new TextView[2];
        playerWeight = new TextView[2];
        playerName = new TextView[2];
        playerHometown = new TextView[2];
        playerImage = new ImageView[2];

        playerNo[0] = (TextView) findViewById(R.id.playerNo0);
        playerPosition[0] = (TextView) findViewById(R.id.playerPosition0);
        playerYear[0] = (TextView) findViewById(R.id.playerYear0);
        playerHeight[0] = (TextView) findViewById(R.id.playerHeight0);
        playerWeight[0] = (TextView) findViewById(R.id.playerWeight0);
        playerName[0] = (TextView) findViewById(R.id.playerName0);
        playerHometown[0] = (TextView) findViewById(R.id.hometown0);
        playerImage[0] = (ImageView) findViewById(R.id.imageView0);

        playerNo[1] = (TextView) findViewById(R.id.playerNo1);
        playerPosition[1] = (TextView) findViewById(R.id.playerPosition1);
        playerYear[1] = (TextView) findViewById(R.id.playerYear1);
        playerHeight[1] = (TextView) findViewById(R.id.playerHeight1);
        playerWeight[1] = (TextView) findViewById(R.id.playerWeight1);
        playerName[1] = (TextView) findViewById(R.id.playerName1);
        playerHometown[1] = (TextView) findViewById(R.id.hometown1);
        playerImage[1] = (ImageView) findViewById(R.id.imageView1);

        currentView = 0;
        lastView = 1;
        
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper1);
        
        teamNumber = 0;
        // currentIdx = 0;
        // Restore preferences
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        currentIdx = settings.getInt("currentIdx", 0);
        isPortraitMode = settings.getBoolean("portraitLock", true);
        
//        if (isPortraitMode && getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//            
//        }

        if (isPortraitMode) {
        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        currentIndex[teamNumber] = 0;
        lastIdx = -1;
        lastIndex[teamNumber] = 0;

        rosters[0] = new ArrayList<Player>();
        rosters[1] = new ArrayList<Player>();

        // Find the directory for the SD Card using the API
        sdcard = Environment.getExternalStorageDirectory();

        // Get the text file
        InputStream inputStream = getResources().openRawResource(R.raw.uhlineup);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ii;
        try {
            ii = inputStream.read();
            while (ii != -1) {
                byteArrayOutputStream.write(ii);
                ii = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {

        }

        String text = byteArrayOutputStream.toString();

        // File file = new File(sdcard, "uhlineup/uhlineup.json");
        // if (file.exists()) {
        //
        // // Read text from file
        // StringBuilder text = new StringBuilder();
        //
        // try {
        // BufferedReader br = new BufferedReader(new FileReader(file));
        // String line;
        //
        // while ((line = br.readLine()) != null) {
        // text.append(line);
        // text.append('\n');
        // }
        // br.close();
        // } catch (IOException e) {
        // // You'll need to add proper error handling here
        // }

        // title.setText(text.toString());

        assignedNumbers = new int[100];
        players = new ArrayList<Player>();
        playerNames = new HashMap<String, Integer>();

        try {
            JSONObject jsonObj = new JSONObject(text);

            JSONArray rosterObj = jsonObj.getJSONArray("players");
            rosterDate = jsonObj.getString("date");
            rosterDate = rosterDate.substring(4,6) + "/" + rosterDate.substring(6,8) + "/" + rosterDate.substring(0,4);
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
                player.file = playerObj.getString("file");
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

        playerImage[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayerPage(currentPlayerFile);
            }
        });
        
        playerImage[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayerPage(currentPlayerFile);
            }
        });
        
        playerNo[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayerPage(currentPlayerFile);
            }
        });
        
        playerNo[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayerPage(currentPlayerFile);
            }
        });
        
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

        viewFlipper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public void onLongPress(MotionEvent e) {
//                Log.d(TAG, "Long Press event");
//                showDialog(DIALOG_LIST);
//            }

//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                Log.d(TAG, "Double Tap event");
//                showDialog(DIALOG_GRID);
//                return true;
//            }

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
                    } else {
                        currentIdx = 0;
                        //Toast.makeText(context, "Top of roster", Toast.LENGTH_SHORT).show();
                    }
                    displayNextPlayerWithTransition(currentIdx);
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.d(TAG, "Left to right");
                    if (currentIdx > 0) {
                        currentIdx--;
                    } else {
                        currentIdx = lastIdx;
                        //Toast.makeText(context, "Bottom of roster", Toast.LENGTH_SHORT).show();
                    }
                    displayPreviousPlayerWithTransition(currentIdx);
                    return true;
                }
                return false;
            }
        });
        gestureDetector.setIsLongpressEnabled(true);

//        imageView = (ImageView) findViewById(R.id.imageView1);

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

        // } else {
        // showDialog(DIALOG_NO_DATA);
        // }

    }

    private void displayNextPlayerWithTransition(int index) {
        int tempView = currentView;
        currentView = lastView;
        lastView = tempView;
        DisplayPlayer(index);
        viewFlipper.setInAnimation(this, R.anim.in_from_right);
        viewFlipper.setOutAnimation(this, R.anim.out_to_left);
        viewFlipper.showNext();
    }
    
    private void displayPreviousPlayerWithTransition(int index) {
        int tempView = currentView;
        currentView = lastView;
        lastView = tempView;
        DisplayPlayer(index);
        viewFlipper.setInAnimation(this, R.anim.in_from_left);
        viewFlipper.setOutAnimation(this, R.anim.out_to_right);
        viewFlipper.showNext();
    }
    
    public void setLockPortraitMode() {
        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    public void unsetLockPortraitMode() {
        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
    
    
    public void DisplayPlayer(int idx) {
        if (idx <= lastIdx) {
            playerNo[currentView].setText(Integer.toString(players.get(idx).number));
            playerPosition[currentView].setText(players.get(idx).position);
            playerYear[currentView].setText(players.get(idx).year);
            playerHeight[currentView].setText(players.get(idx).height);
            playerWeight[currentView].setText(Integer.toString(players.get(idx).weight));
            playerName[currentView].setText(players.get(idx).name);
            playerHometown[currentView].setText(players.get(idx).hometown);
            currentPlayerFile = players.get(idx).file;
            Bitmap bm;

            // Order of image retrieval.
            // 1. sdcard
            // 2. assets
            // 3. default image
            // /mnt/sdcard vs /sdcard.
            // /sdcard is a symbolic link to /mnt/sdcard (at least on Samsung
            // Exhibit II)
            // File file = new File(sdcard, "/uhlineup/" +
            // players.get(idx).image);
            // if (file.exists()) {
            // bm = BitmapFactory.decodeFile(sdcard.getPath() + "/uhlineup/" +
            // players.get(idx).image, options);
            // imageView.setImageBitmap(bm);
            // }
            // else {
            try {
                InputStream istr = getAssets().open(players.get(idx).image);
                bm = BitmapFactory.decodeStream(istr);
                //imageView.setImageBitmap(bm);
                playerImage[currentView].setImageBitmap(bm);
            } catch (IOException e) {
                //imageView.setImageResource(R.drawable.uhwarriors1);
                playerImage[currentView].setImageResource(R.drawable.uhwarriors1);
            }
            // }
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

    // public void nextPlayer(View view) {
    // if (currentIdx < lastIdx) {
    // currentIdx++;
    // }
    // else {
    // currentIdx = 0;
    // Toast.makeText(context, "Top of roster", Toast.LENGTH_SHORT).show();
    // }
    // DisplayPlayer(currentIdx);
    // }
    //
    // public void prevPlayer(View view) {
    // if (currentIdx > 0) {
    // currentIdx--;
    // }
    // else {
    // currentIdx = lastIdx;
    // Toast.makeText(context, "Bottom of roster", Toast.LENGTH_SHORT).show();
    // }
    // DisplayPlayer(currentIdx);
    // }

    public void findPlayer(int playerNumber) {
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
    
    private void openPlayerPage(String id) {
        // Open web page.
        // Check data connectivity.
        if (id.length() > 0 && hasData()) {
            String url = UH_URL + id;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
    }
    
    private boolean hasData() {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.isConnectedOrConnecting();
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
            showDialog(DIALOG_ABOUT);
            return true;
        case R.id.settings:
            showDialog(DIALOG_SETTINGS);
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
        case DIALOG_ABOUT:

            // String msg = "Game date: " + rosterDate + "\n" +
            // "Game opponent: " + rosterGame;
            //
            // builder.setMessage(msg).setCancelable(true).setPositiveButton("OK",
            // new DialogInterface.OnClickListener() {
            // public void onClick(DialogInterface dialog, int id) {
            // dialog.cancel();
            // }
            // });
            // dialog = builder.create();

            dialog.setContentView(R.layout.about);
            buttonOK = (Button) dialog.findViewById(R.id.button1);

            buttonOK.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    dismissDialog(DIALOG_ABOUT);
                }
            });

            textAboutHeader = (TextView) dialog.findViewById(R.id.aboutApp);
            textAboutHeader.setText("UH Lineup\n\nGame Data:\n   Date: " + rosterDate + "\n   Opponent: " + rosterGame);
            break;

        case DIALOG_SETTINGS:

            dialog.setContentView(R.layout.settings);
            buttonOK = (Button) dialog.findViewById(R.id.button1);

            buttonOK.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    dismissDialog(DIALOG_SETTINGS);
                }
            });
            
            lockPortraitMode = (CheckBox) dialog.findViewById(R.id.lockPortraitMode);
            lockPortraitMode.setChecked(isPortraitMode);
            
            lockPortraitMode.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                  if (((CheckBox) v).isChecked() && !isPortraitMode) {
//                      Toast.makeText(UHLineupActivity.this,
//                         "Lock portrait mode", Toast.LENGTH_SHORT).show();
                      isPortraitMode = true;
                      setLockPortraitMode();
                      // Refresh screen?
                  }
                  
                  if (!((CheckBox) v).isChecked() && isPortraitMode) {
//                      Toast.makeText(UHLineupActivity.this,
//                         "unlock portrait mode", Toast.LENGTH_SHORT).show();
                      isPortraitMode = false;
                      unsetLockPortraitMode();
                  }
                }
              });
           
            

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
            buttonClose = (Button) dialog.findViewById(R.id.buttonClose);

            if (searchDrawable == null) {
                // Remember original search icon drawable.
                searchDrawable = nameSearch.getCompoundDrawables()[0];
            }

            listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listview_array));
            
            nameSearch.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Abstract Method of TextWatcher Interface.
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    textlength = nameSearch.getText().length();
                    if (textlength > 0)
                        nameSearch.setCompoundDrawables(null, null, null, null);
                    else
                        nameSearch.setCompoundDrawables(searchDrawable, null, null, null);
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
                }
            });

            buttonClose.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    dismissDialog(DIALOG_LIST);
                }
            });
            // Always force open keyboard.
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            // Force open keyboard on EditText focus.
            // This no work.
            // InputMethodManager imm = (InputMethodManager)
            // getSystemService(Context.INPUT_METHOD_SERVICE);
            // imm.showSoftInput(nameSearch, InputMethodManager.SHOW_FORCED);

            break;
        default:
            // dialog = null;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {

        case DIALOG_LIST:
            // Clear search criteria.
            nameSearch.setText("");
            break;
            
        default:
        }

        return;
    }

    @Override
    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("currentIdx", currentIdx);
        editor.putBoolean("portraitLock", isPortraitMode);

        // Commit the edits!
        editor.commit();
    }

}