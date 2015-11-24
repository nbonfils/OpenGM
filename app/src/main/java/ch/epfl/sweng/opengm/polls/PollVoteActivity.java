package ch.epfl.sweng.opengm.polls;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.opengm.R;
import ch.epfl.sweng.opengm.parse.PFException;
import ch.epfl.sweng.opengm.parse.PFPoll;

import static ch.epfl.sweng.opengm.OpenGMApplication.getCurrentUser;
import static ch.epfl.sweng.opengm.parse.PFPoll.Answer;

public class PollVoteActivity extends AppCompatActivity {

    private PFPoll mPoll;

    private boolean userCanVote;

    private LinearLayout scrollViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_vote);

        setTitle("Reply to this poll");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPoll = getIntent().getParcelableExtra(CreatePollActivity.POLL_INTENT);

        userCanVote = !mPoll.hasUserAlreadyVoted(getCurrentUser().getId());

        scrollViewLayout = (LinearLayout) findViewById(R.id.container_scrollView_answers);

        TextView mName = (TextView) findViewById(R.id.nameVotePollText);
        mName.setText(mPoll.getName());

        TextView mDescription = (TextView) findViewById(R.id.descriptionVotePollText);
        mDescription.setText(mPoll.getDescription());

        TextView mPossibleAnswers = (TextView) findViewById(R.id.possiblePollVoteText);
        mPossibleAnswers.setText(mPossibleAnswers.getText().toString().concat(" " + mPoll.getNOfAnswers()));

        fillWithAnswers(mPoll.getNOfAnswers() != 1);

    }


    private void fillWithAnswers(boolean areCheckbox) {
        if (areCheckbox) {
            for (Answer answer : mPoll.getAnswers()) {
                CheckBox button = new CheckBox(this);
                button.setText(answer.getAnswer());
                button.setEnabled(userCanVote);
                button.setTextColor(getResources().getColor(R.color.bluegreen));
                button.setTag(answer);
                scrollViewLayout.addView(button);
            }
        } else {
            RadioGroup rg = new RadioGroup(getBaseContext());
            for (Answer answer : mPoll.getAnswers()) {
                RadioButton button = new RadioButton(this);
                button.setText(answer.getAnswer());
                button.setEnabled(userCanVote);
                button.setTextColor(getResources().getColor(R.color.bluegreen));
                button.setTag(answer);
                rg.addView(button);
            }
            scrollViewLayout.addView(rg);
        }

    }

    public void validateVote(View view) {
        if (userCanVote) {
            List<Answer> checkedAnswers = new ArrayList<>();
            ViewGroup container = mPoll.getNOfAnswers() != 1 ? scrollViewLayout : (RadioGroup) scrollViewLayout.getChildAt(0);
            for (int i = 0; i < container.getChildCount(); i++) {
                CompoundButton child = (CompoundButton) container.getChildAt(i);
                if (child.isChecked()) {
                    checkedAnswers.add((Answer) child.getTag());
                }
            }
            if (checkedAnswers.size() > mPoll.getNOfAnswers()) {
                Toast.makeText(this, String.format("You can't choose more than %d answers!", mPoll.getNOfAnswers()),
                        Toast.LENGTH_LONG).show();
            } else {
                for (Answer answer : checkedAnswers) {
                    mPoll.increaseVoteForAnswer(answer);
                }
                try {
                    mPoll.updateAnswers(getCurrentUser().getId());
                    Toast.makeText(this, "Success! Your vote has been taken into account! Thanks for your participation",
                            Toast.LENGTH_LONG).show();
                    finish();
                } catch (PFException e) {
                    Toast.makeText(this, "Error while updating your answer : your vote has not been taken into account",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else

        {
            Toast.makeText(this, "You have already vote, you can't vote twice!",
                    Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return true;
        }
    }
}
