package model;


import java.util.*;


public class Group {
    private static int counter = 1;

    private String groupId;
    private String groupName;
    private List<String> members;

    public Group(String name) {
        this.groupName = name;
        this.groupId = generateGroupId(name);
        this.members = new ArrayList<>();
    }

    private String generateGroupId(String name) {
        String prefix = name.replaceAll("\\s+", "").toUpperCase();
        if (prefix.length() > 5) {
            prefix = prefix.substring(0, 5);
        }
        return prefix + "-" + String.format("%03d", counter++);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getMembers() {
        return members;
    }

    public void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
        }
    }

    @Override
    public String toString() {
        return "\uD83D\uDC65 Group: " + groupName + " (" + groupId + ") - Members: " + members;
    }
}
