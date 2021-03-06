package ch.epfl.sweng.opengm.groups;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import ch.epfl.sweng.opengm.R;
import ch.epfl.sweng.opengm.events.EventListActivity;
import ch.epfl.sweng.opengm.identification.InputUtils;
import ch.epfl.sweng.opengm.messages.ShowConversationsActivity;
import ch.epfl.sweng.opengm.parse.PFConstants;
import ch.epfl.sweng.opengm.parse.PFGroup;
import ch.epfl.sweng.opengm.polls.PollsListActivity;
import ch.epfl.sweng.opengm.userProfile.MyProfileActivity;

import static ch.epfl.sweng.opengm.OpenGMApplication.getCurrentGroup;
import static ch.epfl.sweng.opengm.OpenGMApplication.getCurrentUser;
import static ch.epfl.sweng.opengm.OpenGMApplication.setCurrentGroup;

public class GroupsHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawer;

    private PFGroup currentGroup;

    private AlertDialog addMember;

    private final int RESULT_EDIT = 1326;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_groups_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_user_profile:
                startActivity(new Intent(GroupsHomeActivity.this, MyProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_groups_home);

        currentGroup = getCurrentGroup();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navView = (NavigationView) drawer.findViewById(R.id.nav_view);

        View headerView = navView.getHeaderView(0);

        ImageView imageView = (ImageView) headerView.findViewById(R.id.imageUserView);

        if (getCurrentUser().getPicture() != null) {
            imageView.setImageBitmap(getCurrentUser().getPicture());
        }else{
            imageView.setImageResource(R.drawable.side_nav_bar);
        }

        TextView userText = (TextView) headerView.findViewById(R.id.usernameUserView);
        userText.setText(getCurrentUser().getUsername());
        TextView groupText = (TextView) headerView.findViewById(R.id.groupNameUserView);
        groupText.setText(currentGroup.getName());

        setTitle(currentGroup.getName());

        // set image of the group on the layout
        ImageView groupImage = (ImageView) findViewById(R.id.group_image);
        if (currentGroup.getPicture() != null) {
            groupImage.setImageBitmap(currentGroup.getPicture());
        }

        TextView descriptionView = (TextView) findViewById(R.id.textView_description);

        descriptionView.setText(currentGroup.getDescription());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create the dialog that add members which then only need to be shown
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        final EditText edit = (EditText) dialogLayout.findViewById(R.id.dialog_add_member_username);
        builder.setView(dialogLayout)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String usernameOrMail = String.valueOf(edit.getText());
                        addUser(usernameOrMail);
                        edit.getText().clear();
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        addMember = builder.create();

        // the floating button (+) that shows the dialog to add members
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAddMember);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMember.show();
            }
        });

        // show the floating button (+) only if user can add a member
        if (currentGroup.hasUserPermission(getCurrentUser().getId(), PFGroup.Permission.ADD_MEMBER)) {
            fab.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) descriptionView.getLayoutParams();
            params.setMargins(0, 0, 20, 0);
            descriptionView.setLayoutParams(params);
        } else {
            fab.setVisibility(View.GONE);
        }


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            setCurrentGroup(-1);
            finish();
        }
    }

    public void goToMessages(View view) {
        Intent intent2 = new Intent(GroupsHomeActivity.this, ShowConversationsActivity.class);
        startActivity(intent2);
    }

    public void goToPoll(View view) {
        Intent intent1 = new Intent(GroupsHomeActivity.this, PollsListActivity.class);
        startActivity(intent1);
    }

    public void goToEvents(View view) {
        Intent intent = new Intent(GroupsHomeActivity.this, EventListActivity.class);
        startActivity(intent);
    }

    public void onManageGroup(View v) {
        Intent intent = new Intent(this, CreateEditGroupActivity.class);
        startActivityForResult(intent, RESULT_EDIT);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_leave:
                LeaveGroupDialogFragment leaveGroupDialog = new LeaveGroupDialogFragment().setGroupToLeave(currentGroup);
                leaveGroupDialog.show(getFragmentManager(), "leaveGroupDialog");
                break;
            case R.id.nav_home:
                setCurrentGroup(-1);
                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.nav_members:
                startActivity(new Intent(GroupsHomeActivity.this, MembersActivity.class));
                break;
            case R.id.nav_events:
                Intent intent = new Intent(GroupsHomeActivity.this, EventListActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_messages:
                startActivity(new Intent(GroupsHomeActivity.this, ShowConversationsActivity.class));
                break;
            case R.id.nav_manage:
                onManageGroup(null);
                Intent intent3 = new Intent(this, CreateEditGroupActivity.class);
                startActivityForResult(intent3, RESULT_EDIT);
                break;
            case R.id.nav_polls:
                Intent intent1 = new Intent(GroupsHomeActivity.this, PollsListActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // handle back from edit
            case RESULT_EDIT:
                setTitle(currentGroup.getName());
                TextView descriptionView = (TextView) findViewById(R.id.textView_description);
                descriptionView.setText(currentGroup.getDescription());
                ImageView groupImage = (ImageView) findViewById(R.id.group_image);
                groupImage.setImageBitmap(currentGroup.getPicture());
                break;
            default:
        }
    }

    // almost same fucntion used in MembersActivity
    // add a user to the group and to the list that is displayed in background according to a username
    private void addUser(String usernameOrMail) {
        // select between username or mail
        String field;
        if (InputUtils.isEmailValid(usernameOrMail)) {
            field = PFConstants._USER_TABLE_EMAIL;
        } else {
            field = PFConstants._USER_TABLE_USERNAME;
        }

        // the actual query
        ParseQuery<ParseObject> query = ParseQuery.getQuery(PFConstants._USER_TABLE_NAME);
        query.whereEqualTo(field, usernameOrMail);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject != null) {
                    String userId = parseObject.getObjectId();
                    if (!currentGroup.containsMember(userId)) {
                        // add the user to the group and to the list
                        currentGroup.addUserWithId(userId);
                        Toast.makeText(getBaseContext(), "User correctly added to this group.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(), "User already belongs to this group.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Could not find this user", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
