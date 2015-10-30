package ch.epfl.sweng.opengm.identification;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.opengm.OpenGMApplication;
import ch.epfl.sweng.opengm.R;
import ch.epfl.sweng.opengm.parse.PFException;
import ch.epfl.sweng.opengm.parse.PFGroup;
import ch.epfl.sweng.opengm.parse.PFUser;

public class MyGroupsActivity extends AppCompatActivity {

    public static final String COMING_FROM_KEY = "ch.epfl.ch.opengm.connexion.signup.groupsActivity.coming";
    public static final String RELOAD_USER_KEY = "ch.epfl.ch.opengm.connexion.signup.groupsActivity.reloadUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);

        RecyclerView groupsRecyclerView = (RecyclerView) findViewById(R.id.groups_recycler_view);
//        LinearLayoutManager llm = new LinearLayoutManager(this);
//        rv.setLayoutManager(llm);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        groupsRecyclerView.setLayoutManager(gridLayoutManager);
        groupsRecyclerView.setHasFixedSize(true);

        //List<PFGroup> groups = new ArrayList<>(OpenGMApplication.getCurrentUser().getGroups());
        List<PFGroup> groups = new ArrayList<>();
        try {
            PFGroup group1 = PFGroup.fetchExistingGroup("6YdYsS59yo");
            Log.v("ORNYTHO", group1.getName() + " | " + group1.getDescription());
            groups.add(group1);
            PFGroup group2 = PFGroup.fetchExistingGroup("YQ7Ehgac93");
            Log.v("ORNYTHO", group2.getName() + " | " + group2.getDescription());
            groups.add(group2);
            PFGroup group3 = PFGroup.fetchExistingGroup("bFstqir7Qf");
            Log.v("ORNYTHO", group3.getName() + " | " + group3.getDescription());
            groups.add(group3);
            PFGroup group4 = PFGroup.fetchExistingGroup("OXsMb6BrSa");
            Log.v("ORNYTHO", group4.getName() + " | " + group4.getDescription());
            groups.add(group4);
        } catch (PFException e) {
            e.printStackTrace();
        }

        GroupCardViewAdapter groupCardViewAdapter = new GroupCardViewAdapter(groups);
        groupsRecyclerView.setAdapter(groupCardViewAdapter);
    }

    public void gotoGroup(View view) {
        String tag = (String) view.getTag();
        view.setBackgroundColor(0xBA1027);  // FIXME: change color
        Log.v("ORNYTHO", "datTag = ["+tag+"]");
    }
}
