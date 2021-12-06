package reolina.MineFinancial.AControl;

public interface IClanListener {
    void onClanInviteAccept(int senderID);
    void onClanInviteDeny(int senderID);
    void onClanJoinAccept(int senderID);
    void onClanJoinDeny(int senderID);
}
