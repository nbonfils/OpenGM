package ch.epfl.sweng.opengm.events;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import ch.epfl.sweng.opengm.R;
import ch.epfl.sweng.opengm.parse.PFEvent;
import ch.epfl.sweng.opengm.parse.PFGroup;
import ch.epfl.sweng.opengm.parse.PFMember;

public class ShowEventActivity extends AppCompatActivity {
    public final static String SHOW_EVENT_MESSAGE_EVENT = "ch.epfl.sweng.opengm.events.SHOW_EVENT_EVENT";

    private PFEvent event;
    private PFGroup currentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_event);

        Intent intent = getIntent();
        event = intent.getParcelableExtra(EventListActivity.EVENT_LIST_MESSAGE_EVENT);
        currentGroup = intent.getParcelableExtra(EventListActivity.EVENT_LIST_INTENT_GROUP);
        displayEventInformation();
    }

    private void displayEventInformation() {
        fillEventName();
        fillEventPlace();
        fillEventDate();
        fillEventDescription();
        fillEventParticipants();
    }

    private void fillEventName() {
        ((TextView) findViewById(R.id.ShowEventNameText)).setText(event.getName());
    }

    private void fillEventPlace() {
        TextView textView = (TextView) findViewById(R.id.ShowEventPlaceText);
        if (event.getPlace().isEmpty()) {
            textView.setHeight(0);
        } else {
            textView.setText(event.getPlace());
        }
    }

    private void fillEventDate() {
        String hourString = String.format("%d : %02d", event.getHours(), event.getMinutes());
        ((TextView)findViewById(R.id.ShowEventHourText)).setText(hourString);
        String dateString = String.format("%d/%02d/%04d", event.getDay(), event.getMonth(), event.getYear());
        ((TextView)findViewById(R.id.ShowEventDateText)).setText(dateString);
    }

    private void fillEventDescription() {
        TextView textView = (TextView) findViewById(R.id.ShowEventDescriptionText);
        if (event.getDescription().isEmpty()) {
            textView.setHeight(0);
        } else {
            String description = "Description:\n" + event.getDescription();
            textView.setText(description);
        }
    }

    private void fillEventParticipants() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Participants:");
        for (PFMember participant : event.getParticipants().values()) {
            stringBuilder.append('\n');

            stringBuilder.append(participant.getUsername());
        }
        ((TextView) findViewById(R.id.ShowEventParticipants)).setText(stringBuilder.toString());
    }

    public void onEditButtonClick(View view) {
        Intent intent = new Intent(this, CreateEditEventActivity.class);
        intent.putExtra(SHOW_EVENT_MESSAGE_EVENT, event);
        intent.putExtra(EventListActivity.EVENT_LIST_INTENT_GROUP, currentGroup);
        startActivity(intent);
    }
}
