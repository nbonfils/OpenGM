package ch.epfl.sweng.opengm.events;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import ch.epfl.sweng.opengm.OpenGMApplication;
import ch.epfl.sweng.opengm.R;
import ch.epfl.sweng.opengm.identification.contacts.AppContactsActivity;
import ch.epfl.sweng.opengm.parse.PFEvent;
import ch.epfl.sweng.opengm.parse.PFException;
import ch.epfl.sweng.opengm.parse.PFGroup;
import ch.epfl.sweng.opengm.parse.PFUtils;
import ch.epfl.sweng.opengm.userProfile.MyProfileActivity;
import ch.epfl.sweng.opengm.utils.NetworkUtils;

public class EventListActivity extends AppCompatActivity {

    //TODO : Image is not optimal.
    public static final int EVENT_LIST_RESULT_CODE = 666;
    public static PFEvent currentEvent;
    private Map<String,PFEvent> eventMap;
    private PFGroup currentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        setTitle("List of your events");
        findViewById(R.id.Screen).setVisibility(View.GONE);
        findViewById(R.id.eventListAddButton).setVisibility(View.GONE);
        findViewById(R.id.EventListLoadingPanel).setVisibility(View.VISIBLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
               // Intent intent = getIntent();
                       /* new Intent(Utils.GROUP_INTENT_MESSAGE);


                    intent.putExtra(Utils.GROUP_INTENT_MESSAGE, "TXxysfyRqV");
               } catch (PFException e) {
                    e.printStackTrace();
                }*/
               // int currentGroupLocation = intent.getIntExtra(Utils.GROUP_INTENT_MESSAGE, -1);
                //currentGroup = OpenGMApplication.getCurrentUser().getGroups().get(currentGroupLocation);

                currentGroup = OpenGMApplication.getCurrentGroup();

                eventMap = new ArrayMap<>();
                for(PFEvent e : currentGroup.getEvents()){
                    eventMap.put(e.getId(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                findViewById(R.id.Screen).setVisibility(View.VISIBLE);
                findViewById(R.id.eventListAddButton).setVisibility(View.VISIBLE);
                displayEvents();
                findViewById(R.id.EventListLoadingPanel).setVisibility(View.GONE);
            }
        }.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_groups, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh_user:
                refresh();
                return true;
            case R.id.action_show_contacts:
                startActivity(new Intent(this, AppContactsActivity.class));
                return true;
            case R.id.action_show_user_profile:
                startActivity(new Intent(this, MyProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent eventIntent) {
        if (requestCode == EVENT_LIST_RESULT_CODE) {
            if(resultCode == Activity.RESULT_OK){
                PFEvent event = CreateEditEventActivity.newEvent;//eventIntent.getParcelableExtra(Utils.EVENT_INTENT_MESSAGE);
                boolean edited = eventIntent.getBooleanExtra(Utils.EDIT_INTENT_MESSAGE, false);

                if(NetworkUtils.haveInternet(getBaseContext())) {
                    try {
                        if(edited) {
                            currentGroup.updateEvent(event);
                            Toast.makeText(getApplicationContext(), "Event updated", Toast.LENGTH_SHORT).show();
                        }else{
                            currentGroup.addEvent(event);
                            Toast.makeText(getApplicationContext(), getString(R.string.EventListSuccessfullAdd), Toast.LENGTH_SHORT).show();
                        }
                    } catch (PFException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Couldn't add the event. Refresh later.",Toast.LENGTH_SHORT).show();
                }
                eventMap.put(event.getId(), event);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), getString(R.string.EventListFailToAdd), Toast.LENGTH_SHORT).show();
            }
            if(resultCode == Utils.DELETE_EVENT){
                PFEvent event = currentEvent;//eventIntent.getParcelableExtra(Utils.EVENT_INTENT_MESSAGE);

                if(NetworkUtils.haveInternet(getBaseContext())) {
                    try {
                       currentGroup.removeEvent(event);
                    } catch (PFException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Event deleted sucessfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Couldn't delete the event. Refresh later.",Toast.LENGTH_SHORT).show();
                }
                eventMap.remove(event.getId());
            }
            if(resultCode==Utils.SILENCE){
                //DO NOTHING
            }
            displayEvents();
        }
        currentEvent=null;
    }

    /**
     * When the button is click, it's supposed to open an other Activity (CreateEditEventActivity)
     * Then get the Activity created this way, add it to the calendar and then display the calendar again.
     * @param v The View.
     */
    public void clickOnAddButton(View v){
        if(currentGroup.hasUserPermission(OpenGMApplication.getCurrentUser().getId(), PFGroup.Permission.ADD_EVENT)) {
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            startActivityForResult(intent, EVENT_LIST_RESULT_CODE);
        }else{
            Toast.makeText(getApplicationContext(), "You don't have the permission to create a new event.", Toast.LENGTH_SHORT).show();
        }
    }
    public void clickOnCheckBoxForPastEvent(View v){
        displayEvents();
    }

    public void refresh(){
        findViewById(R.id.EventListLoadingPanel).setVisibility(View.VISIBLE);
        findViewById(R.id.scrollView4).setVisibility(View.GONE);
        findViewById(R.id.eventListAddButton).setClickable(false);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void[] params){
                try {
                    currentGroup.reload();
                    for (PFEvent e : currentGroup.getEvents()) {
                        if (!eventMap.containsKey(e.getId())) {
                            currentGroup.removeEvent(e);
                        }
                    }
                    for (PFEvent e : eventMap.values()) {
                        if (currentGroup.getEvents().contains(e)) {
                            currentGroup.updateEvent(e);
                        } else {
                            currentGroup.addEvent(e);
                        }
                    }
                    return true;
                }catch (PFException e){
                    return false;
                }

            }
            @Override
            protected void onPostExecute(Boolean result){
                findViewById(R.id.EventListLoadingPanel).setVisibility(View.GONE);
                findViewById(R.id.scrollView4).setVisibility(View.VISIBLE);
                findViewById(R.id.eventListAddButton).setClickable(true);
                if(!result) {
                    Toast.makeText(getApplicationContext(), "Unable to refresh.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private boolean compareDate(Date eventDate){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        Date currentDate = new Date(year, month, day, hour, min);
        return currentDate.before(eventDate);
    }

    /**
     * Call this method to refresh the calendar on the screen.
     */
    public void displayEvents(){
        LinearLayout linearLayoutListEvents = (LinearLayout) findViewById(R.id.line3);
        linearLayoutListEvents.removeAllViews();

        CheckBox checkboxForPastEvent = (CheckBox) findViewById(R.id.eventListCheckBoxForPastEvent);
        boolean displayPastEvents = checkboxForPastEvent.isChecked();

        ArrayList<PFEvent> eventList = new ArrayList<>(eventMap.values());
        Collections.sort(eventList, new Comparator<PFEvent>() {
            @Override
            public int compare(PFEvent lhs, PFEvent rhs) {
                return rhs.getDate().compareTo(lhs.getDate());
            }
        });

        for(PFEvent event :eventList) {
            if (displayPastEvents || compareDate(event.getDate())) {
                final Button b = new Button(this);
                GradientDrawable gd = new GradientDrawable();
                gd.setStroke(2, Color.WHITE);
                gd.setColor(getResources().getColor(R.color.bluegreen));
                b.setBackground(gd);
                b.setTextColor(Color.WHITE);
                SpannableString name = new SpannableString(String.format("%s \n %d/%02d/%04d, %d : %02d", event.getName(), event.getDay(), event.getMonth(), event.getYear(), event.getHours(), event.getMinutes()));
                name.setSpan(new RelativeSizeSpan(2f),0,event.getName().length(),0);
                b.setText(name);
                 b.setTag(event);
                b.setLayoutParams(linearLayoutListEvents.getLayoutParams());
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEvent((PFEvent) b.getTag());
                    }
                });
                linearLayoutListEvents.addView(b);
            }
        }
    }

    private void showEvent(PFEvent cliquedEvent) {
        if(cliquedEvent.getPicturePath().equals(PFUtils.pathNotSpecified)) {
            try {
                String imageName = cliquedEvent.getId() + "_event";
                cliquedEvent.setPicturePath(ch.epfl.sweng.opengm.utils.Utils.
                        saveToInternalStorage(cliquedEvent.getPicture(), getApplicationContext(), imageName));
                cliquedEvent.setPictureName(imageName);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Unable to write image or to retrieve image", Toast.LENGTH_SHORT).show();
                cliquedEvent.setPicturePath(PFUtils.pathNotSpecified);
                cliquedEvent.setPictureName(PFUtils.nameNotSpecified);
            }
        }

        Intent intent = new Intent(this, ShowEventActivity.class);
        currentEvent = cliquedEvent;
      //  intent.putExtra(Utils.EVENT_INTENT_MESSAGE, cliquedEvent);
        startActivityForResult(intent, EVENT_LIST_RESULT_CODE);
    }
}
