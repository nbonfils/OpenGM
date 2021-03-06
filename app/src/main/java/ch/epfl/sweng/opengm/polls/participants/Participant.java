package ch.epfl.sweng.opengm.polls.participants;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.opengm.parse.PFMember;

public class Participant implements Comparable<Participant> {

    private final List<PFMember> participants;
    private final String prefix;
    private final boolean isGroup;
    private final String info;
    private final String name;

    public Participant(PFMember member) {
        this.participants = new ArrayList<>();
        this.participants.add(member);
        this.isGroup = false;
        this.prefix = member.getFirstName().substring(0, 1).concat(member.getLastName().substring(0, 1)).toUpperCase();
        this.info = member.getUsername();
        this.name = member.getDisplayedName();
    }

    public Participant(String name, List<PFMember> members) {
        participants = members;
        isGroup = true;
        this.prefix = name.substring(0, 2).toUpperCase();
        this.name = name;
        this.info = members.size() + " member" + (members.size() > 1 ? "s" : "");
    }

    public List<PFMember> getParticipants() {
        return participants;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Participant that = (Participant) o;

        return !(info != null ? !info.equals(that.info) : that.info != null) && !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        int result = info != null ? info.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name.concat(info);
    }

    @Override
    public int compareTo(@NonNull Participant another) {
        if (name.equals(ListParticipantActivity.EVERY_BODY)) {
            return -1;
        } else if (another.name.equals(ListParticipantActivity.EVERY_BODY)) {
            return 1;
        } else if (isGroup && !another.isGroup) {
            return -1;
        } else if (!isGroup && another.isGroup) {
            return 1;
        } else {
            return prefix.compareTo(another.prefix);
        }
    }
}
