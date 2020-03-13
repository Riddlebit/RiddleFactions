package net.riddlebit.mc.data;

import java.util.Objects;

public class Invite {

    public Invite(FactionData factionData, PlayerData inviter, PlayerData invitee) {
        this.factionData = factionData;
        this.inviter = inviter;
        this.invitee = invitee;
    }

    public FactionData factionData;
    public PlayerData inviter;
    public PlayerData invitee;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invite invite = (Invite) o;
        return factionData.equals(invite.factionData) &&
                inviter.equals(invite.inviter) &&
                invitee.equals(invite.invitee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factionData, inviter, invitee);
    }
}
