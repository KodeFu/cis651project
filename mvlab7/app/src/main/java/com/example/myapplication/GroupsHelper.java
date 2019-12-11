package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GroupsHelper {

    static public void createGroup(HashMap<String, Group> groups, String name)
    {
        String currentGroupName = getGroupName(groups);
        if (!currentGroupName.equals("")) {
            // We are currently in a group, so can't create another
            //Toast.makeText(getApplicationContext(), "Already in a group. Leave group before creating a new group.",
            //        Toast.LENGTH_SHORT).show();
            return;
        }

        Group g = new Group();
        Category c = new Category();

        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser.getUid();
        String currentName = currentUser.getDisplayName();

        // Group info
        g.name = name;
        g.adminUid = currentUid;

        // Member info
        g.members = new HashMap<String, Member>();
        g.members.put(currentUid, new Member(currentUid, currentName));

        // Category info
        g.categories = new HashMap<String, Category>();
        g.categories.put(currentUid, new Category(currentUid, currentName, -1));
        g.categories.put("Groceries", new Category("Groceries", "Groceries", -1));
        g.categories.put("School", new Category(currentUid, "School", -1));
        g.categories.put("Automobile", new Category(currentUid, "Automobile", -1));
        g.categories.put("Home Improvement", new Category(currentUid, "Home Improvement", -1));
        g.categories.put("Dining", new Category(currentUid, "Dining", -1));
        g.categories.put("Entertainment", new Category(currentUid, "Entertainment", -1));
        g.categories.put("Gifts", new Category(currentUid, "Gifts", -1));

        // Create child reference; i.e. group node
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef =  mRootReference.child("groups");
        Random rand = new Random();
        g.token = Integer.toString(rand.nextInt(1000000));
        groupsRef.child(g.token).setValue(g);
    }

    static String getGroupToken(HashMap<String, Group> groups)
    {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        // Only admin can see token; verbally conveyed to users
        for (Map.Entry  g : groups.entrySet()) {
            if ( ((Group)g.getValue()).adminUid.equals(mAuth.getCurrentUser().getUid()) ) {
                return ((Group)g.getValue()).token;
            }
        }

        return "";
    }

    static String getGroupName(HashMap<String, Group> groups)
    {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        // If admin of a group, return that
        for (Map.Entry  g : groups.entrySet()) {
            if (((Group)g.getValue()).adminUid.equals(mAuth.getCurrentUser().getUid()) ) {
                return ((Group)g.getValue()).name;
            }
        }

        // Not an admin? see if member of a group
        for (Map.Entry g : groups.entrySet()) {
            for (Map.Entry m : ((Group)g.getValue()).members.entrySet()) {
                if (m.getKey().equals(mAuth.getCurrentUser().getUid())) {
                    return ((Group)g.getValue()).name;
                }
            }
        }

        // Not an admin or member, so not part of a group
        return "";
    }

    static String getGroupAdmin(HashMap<String, Group> groups)
    {
        String myGroupName = getGroupName(groups);

        for (Map.Entry  g : groups.entrySet()) {
            if ( ((Group)g.getValue()).name.equals(myGroupName) ) {
                return ((Group)g.getValue()).adminUid;
            }
        }

        return "";
    }

    static Map<String, Member> getMembers(HashMap<String, Group> groups)
    {
        String myGroupName = GroupsHelper.getGroupName(groups);

        for (Map.Entry g : groups.entrySet()) {
            if (((Group)g.getValue()).name.equals(myGroupName) ) {
                return ((Group)g.getValue()).members;
            }
        }

        return new HashMap<String, Member>();
    }

    static void removeMember(HashMap<String, Group> groups, String uid)
    {
        // Not an admin? see if member of a group
        Group groupContainingMember = null;
        String memberToRemove = null;
        for (Map.Entry g : groups.entrySet()) {
            for (Map.Entry m : ((Group)g.getValue()).members.entrySet()) {
                if (m.getKey().equals(uid)) {
                    groupContainingMember = ((Group)g.getValue());
                    memberToRemove = m.getKey().toString();
                }
            }
        }

        if (groupContainingMember != null && memberToRemove != null) {
            groupContainingMember.members.remove(memberToRemove);

            // Add groups node
            DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference groupsRef =  mRootReference.child("groups");
            groupsRef.setValue(groups);
        } else {
            //Toast.makeText(getApplicationContext(), "Unable to remove member",
            //        Toast.LENGTH_SHORT).show();
        }
    }


    static Map<String, Category> getCategories(HashMap<String, Group> groups)
    {
        String myGroupName = GroupsHelper.getGroupName(groups);

        for (Map.Entry g : groups.entrySet()) {
            if (((Group)g.getValue()).name.equals(myGroupName) ) {
                return ((Group)g.getValue()).categories;
            }
        }

        return new HashMap<String, Category>();
    }

    static void removeCategory(HashMap<String, Group> groups, String category)
    {
        // Not an admin? see if member of a group
        Group groupContainingMember = null;
        String memberToRemove = null;
        for (Map.Entry g : groups.entrySet()) {
            for (Map.Entry m : ((Group)g.getValue()).categories.entrySet()) {
                if (m.getKey().equals(category)) {
                    groupContainingMember = ((Group)g.getValue());
                    memberToRemove = m.getKey().toString();
                }
            }
        }

        if (groupContainingMember != null && memberToRemove != null) {
            groupContainingMember.categories.remove(memberToRemove);

            // Add groups node
            DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference groupsRef =  mRootReference.child("groups");
            groupsRef.setValue(groups);
        } else {
            //Toast.makeText(getApplicationContext(), "Unable to remove member",
            //        Toast.LENGTH_SHORT).show();
        }
    }
}
