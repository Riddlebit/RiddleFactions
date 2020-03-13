package net.riddlebit.mc.data;

public class Invite {

    public Invite(FactionData factionData, PlayerData inviter, PlayerData invitee) {
        this.factionData = factionData;
        this.inviter = inviter;
        this.invitee = invitee;
    }

    public FactionData factionData;
    public PlayerData inviter;
    public PlayerData invitee;
}
