package ch.epfl.sweng.opengm.events;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ch.epfl.sweng.opengm.R;
import ch.epfl.sweng.opengm.parse.PFConstants;
import ch.epfl.sweng.opengm.parse.PFEvent;
import ch.epfl.sweng.opengm.parse.PFException;
import ch.epfl.sweng.opengm.parse.PFGroup;
import ch.epfl.sweng.opengm.parse.PFMember;
import ch.epfl.sweng.opengm.utils.NetworkUtils;

import static ch.epfl.sweng.opengm.events.Utils.dateToString;

public class CreateEditEventActivity extends AppCompatActivity {

    public static final int CREATE_EDIT_EVENT_RESULT_CODE_BROWSEFORBITMAP = 69;
    public static final int CREATE_EDIT_EVENT_RESULT_CODE = 42;
    private PFEvent editedEvent;
    private boolean editing;
    private HashMap<String, PFMember> participants;
    private PFGroup currentGroup;
    private Uri outputFileUri;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_event);

        Intent intent = getIntent();
        currentGroup = intent.getParcelableExtra(Utils.GROUP_INTENT_MESSAGE);
        PFEvent event = intent.getParcelableExtra(Utils.EVENT_INTENT_MESSAGE);
        Log.v("group members", Integer.toString(currentGroup.getMembers().size()));
        if (event == null) {
            editing = false;
            participants = new HashMap<>();
        } else {
            editedEvent = event;
            editing = true;
            participants = editedEvent.getParticipants();
            fillTexts(event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_EDIT_EVENT_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<PFMember> members = data.getParcelableArrayListExtra(AddRemoveParticipantsActivity.PARTICIPANTS_LIST_RESULT);
                participants.clear();
                for(PFMember member : members) {
                    participants.put(member.getId(), member);
                }
                Toast.makeText(this, getString(R.string.CreateEditSuccessfullAddParticipants), Toast.LENGTH_SHORT).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.CreateEditFailToAddParticipants), Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == CREATE_EDIT_EVENT_RESULT_CODE_BROWSEFORBITMAP) {
            if (resultCode == RESULT_OK) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }
                TextView nText= (TextView) findViewById(R.id.CreateEditEventBitmapNameText);
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                    nText.setText("File From Camera");
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                    nText.setText(selectedImageUri.toString());
                }

            }
        }
    }
    public void onDeleteButtonClick(View v)  {
        currentGroup.removeEvent(editedEvent);
        Intent intent = new Intent(this, EventListActivity.class);
        try {
            editedEvent.delete();
            setResult(Utils.DELETE_COMPLETED, intent);
        } catch (PFException e) {
            setResult(Utils.DELETE_FAILED, intent);
        }
        finish();
    }
    public void onArchiveButtonClick(View v) {
        //TODO : coder cette methode
    }

    public void onOkButtonClick(View v) {
        if (legalArguments()) {
            if (participants != null) {
                Intent intent = new Intent();
                intent.putExtra(Utils.EVENT_INTENT_MESSAGE, createEditEvent());
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "You must specify participants", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onParticipantsButtonClick(View v) {
        Intent intent = new Intent(this, AddRemoveParticipantsActivity.class);
        intent.putExtra(Utils.GROUP_INTENT_MESSAGE, currentGroup);
        if (editing) {
            intent.putExtra(Utils.EVENT_INTENT_MESSAGE, createEditEvent());
        }
        startActivityForResult(intent, CREATE_EDIT_EVENT_RESULT_CODE);
    }

    public void onBrowseButtonClick(View v){
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, CREATE_EDIT_EVENT_RESULT_CODE_BROWSEFORBITMAP);
    }

    private void fillTexts(PFEvent event) {
        ((EditText) findViewById(R.id.CreateEditEventNameText)).setText(event.getName());
        ((EditText) findViewById(R.id.CreateEditEventPlaceText)).setText(event.getPlace());
        ((MultiAutoCompleteTextView) findViewById(R.id.CreateEditEventDescriptionText)).setText(event.getDescription());
        String timeString = String.format("%d : %02d", event.getHours(), event.getMinutes());
        ((Button) findViewById(R.id.CreateEditEventTimeText)).setText(timeString);
        String dateString = String.format("%d/%02d/%04d", event.getDay(), event.getMonth(), event.getYear());
        ((Button) findViewById(R.id.CreateEditEventDateText)).setText(dateString);
        TextView participantsList = ((TextView) findViewById(R.id.CreateEditEventParticipantsTextView));
        String participantsStringList = "";
        for(PFMember member : event.getParticipants().values()) {
            participantsStringList += member.getName() + "; ";
        }
        participantsList.setText(participantsStringList.substring(0, participantsStringList.length() - 2));
    }

    private PFEvent createEditEvent() {
        if (editing) {
            return editEvent();
        } else {
            return createEvent();
        }
    }

    private PFEvent createEvent() {

        Date date = getDateFromText();
        String name = ((EditText) findViewById(R.id.CreateEditEventNameText)).getText().toString();
        String description = ((MultiAutoCompleteTextView) findViewById(R.id.CreateEditEventDescriptionText)).getText().toString();
        String place = ((EditText)findViewById(R.id.CreateEditEventPlaceText)).getText().toString();
        //TODO : get new id for creating event maybe asynchronously in onCreate
        ParseObject parseObject = new ParseObject(PFConstants.EVENT_TABLE_NAME);
        Bitmap b = null;
        try {
            parseObject.save();
            if(selectedImageUri!=null) {
                b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return PFEvent.createEvent(currentGroup, name, place, date, new ArrayList<>(participants.values()), description, b);
        } catch (PFException e) {
            // TODO toast ?
            return null;
        }
    }

    private PFEvent editEvent() {
        Date date = getDateFromText();

        String name = ((EditText) findViewById(R.id.CreateEditEventNameText)).getText().toString();
        String description = ((MultiAutoCompleteTextView) findViewById(R.id.CreateEditEventDescriptionText)).getText().toString();
        String place = ((EditText)findViewById(R.id.CreateEditEventPlaceText)).getText().toString();
        Bitmap b = null;
        try {
            b = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        editedEvent.setName(name);
        editedEvent.setDate(date);
        editedEvent.setDescription(description);
        for(PFMember member : participants.values()) {
            editedEvent.removeParticipant(member.getId());
            editedEvent.addParticipant(member.getId(), member);
        }
        editedEvent.setPlace(place);
        editedEvent.setPicture(b);
        return editedEvent;
    }

    /**
     * @return an array of int with year at index 0, month at index 1 and day at index 2
     */
    public Date getDateFromText() {
        String[] dateString = ((Button) findViewById(R.id.CreateEditEventDateText)).getText().toString().split("/");
        String[] timeString = ((Button) findViewById(R.id.CreateEditEventTimeText)).getText().toString().split(" : ");
        if (dateString.length != 3 || timeString.length != 2) {
            return null;
        }
        int year = Integer.parseInt(dateString[2]);
        int month = Integer.parseInt(dateString[1]) - 1;
        int day = Integer.parseInt(dateString[0]);
        int hours = Integer.parseInt(timeString[0]);
        int minutes = Integer.parseInt(timeString[1]);
        return new Date(year, month, day, hours, minutes);
    }

    /**
     * @return true if all arguments except the list of participants are legal for building an event
     * display a toast while it's not.
     */
    private boolean legalArguments() {
        String name = ((EditText) findViewById(R.id.CreateEditEventNameText)).getText().toString();
        if (name.isEmpty()) {
            ((EditText) findViewById(R.id.CreateEditEventNameText)).setError(getString(R.string.CreateEditEmptyNameErrorMessage));
            return false;
        }
        Date date = getDateFromText();

        if (date == null) {
            ((Button) findViewById(R.id.CreateEditEventTimeText)).setError("");
            ((Button) findViewById(R.id.CreateEditEventDateText)).setError("");
            Toast.makeText(this, getString(R.string.CreateEditEmptyTimeDateErrorMessage), Toast.LENGTH_SHORT).show();
            return false;
        }

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        Date currentDate = new Date(year, month, day, hour, min);
        if (date.before(currentDate)) {
            if (year == date.getYear() && month == date.getMonth() && day == date.getDate()) {
                ((Button) findViewById(R.id.CreateEditEventTimeText)).setError("");
            } else {
                ((Button) findViewById(R.id.CreateEditEventDateText)).setError("");
            }
            Toast.makeText(this, getString(R.string.CreateEditEarlyDate), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (participants.isEmpty()) {
            Toast.makeText(this, getString(R.string.CreateEditNoParticipants), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void showTimePickerDialog(View view) {
        DialogFragment dialogFragment = new TimePickerFragment();
        Date date = getDateFromText();
        if(date != null) {
            dialogFragment.show(getFragmentManager(), dateToString(date));
        } else {
            dialogFragment.show(getFragmentManager(), "");
        }
    }

    public void showDatePickerDialog(View view) {
        DialogFragment dialogFragment = new DatePickerFragment();
        Date date = getDateFromText();
        if(date != null) {
            dialogFragment.show(getFragmentManager(), dateToString(date));
        } else {
            dialogFragment.show(getFragmentManager(), "");
        }
    }
}
