package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GroupsHelper {

    static boolean createGroup(User user, HashMap<String, Group> groups, String name)
    {
        String currentGroupName = getGroupName(groups);
        if (!currentGroupName.equals("") || name.equals("")) {
            // We are currently in a group, so can't create another
            return false;
        }

        Group g = new Group();
        Category c = new Category();

        g.name = name;
        g.adminUid = user.uid;

        // Member info
        g.members = new HashMap<String, Member>();
        g.members.put(user.uid, new Member(user.uid, user.displayName));

        // Category info
        g.categories = new HashMap<String, Category>();
        g.categories.put(user.uid, new Category(user.uid, user.displayName, -1.0));
        g.categories.put("Groceries", new Category("Groceries", "Groceries", -1.0));
        g.categories.put("School", new Category(user.uid, "School", -1.0));
        g.categories.put("Automobile", new Category(user.uid, "Automobile", -1.0));
        g.categories.put("Home Improvement", new Category(user.uid, "Home Improvement", -1.0));
        g.categories.put("Dining", new Category(user.uid, "Dining", -1.0));
        g.categories.put("Entertainment", new Category(user.uid, "Entertainment", -1.0));
        g.categories.put("Gifts", new Category(user.uid, "Gifts", -1.0));

        // Create child reference; i.e. group node
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();

        DatabaseReference groupsRef =  mRootReference.child("groups");
        Random rand = new Random();
        g.token = Integer.toString(rand.nextInt(1000000));
        groupsRef.child(g.token).setValue(g);

        DatabaseReference uidRef = mRootReference.child("users").child(user.uid);
        uidRef.child("group").setValue(g.token);

        return true;
    }

    static String getGroupToken(HashMap<String, Group> groups)
    {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        // Only admin can see token; verbally conveyed to users
        if (groups != null) {
            for (Map.Entry  g : groups.entrySet()) {
                if ( ((Group)g.getValue()).adminUid.equals(mAuth.getCurrentUser().getUid()) ) {
                    return ((Group)g.getValue()).token;
                }
            }
        }

        return "";
    }

    // FIXME: Hacky and redundant with getGroupToken, but this one doesn't check for admin.
    // FIXME: Consolidate and use this function instead.
    static String getGroupUserToken(HashMap<String, Group> groups)
    {
        String myGroupName = getGroupName(groups);

        // Only admin can see token; verbally conveyed to users
        if (groups != null) {
            for (Map.Entry  g : groups.entrySet()) {
                if ( ((Group)g.getValue()).name.equals(myGroupName) ) {
                    return ((Group)g.getValue()).token;
                }
            }
        }

        return "";
    }

    static String getGroupName(HashMap<String, Group> groups)
    {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        // If admin of a group, return that
        if (groups != null) {
            for (Map.Entry  g : groups.entrySet()) {
                if (((Group)g.getValue()).adminUid.equals(mAuth.getCurrentUser().getUid()) ) {
                    return ((Group)g.getValue()).name;
                }
            }
        }

        // Not an admin? see if member of a group
        if (groups != null) {
            for (Map.Entry g : groups.entrySet()) {
                for (Map.Entry m : ((Group)g.getValue()).members.entrySet()) {
                    if (m.getKey().equals(mAuth.getCurrentUser().getUid())) {
                        return ((Group)g.getValue()).name;
                    }
                }
            }
        }

        // Not an admin or member, so not part of a group
        return "";
    }



    static String getGroupAdmin(HashMap<String, Group> groups)
    {
        String myGroupName = getGroupName(groups);

        if (groups != null) {
            for (Map.Entry  g : groups.entrySet()) {
                if ( ((Group)g.getValue()).name.equals(myGroupName) ) {
                    return ((Group)g.getValue()).adminUid;
                }
            }
        }

        return "";
    }

    static boolean isAdmin(HashMap<String, Group> groups)
    {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        String groupAdmin = getGroupAdmin(groups);

        if (groupAdmin.equals(mAuth.getCurrentUser().getUid())) {
            return true;
        }

        return false;
    }

    static boolean updateGroupName(HashMap<String, Group> groups, String name)
    {
        String myGroupName = getGroupName(groups);

        if (groups != null) {
            for (Map.Entry g : groups.entrySet()) {
                if ( ((Group)g.getValue()).name.equals(myGroupName) ) {

                    // Update group name
                    ((Group)g.getValue()).name = name;

                    // Add groups node
                    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference groupsRef =  mRootReference.child("groups");
                    groupsRef.setValue(groups);
                    return true;
                }
            }
        }

        return false;
    }

    static boolean removeGroup(HashMap<String, Group> groups)
    {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();

        // If admin of a group, return that
        if (groups != null) {
            for (Map.Entry  g : groups.entrySet()) {
                Group databaseGroup = (Group)g.getValue();
                if (databaseGroup.adminUid.equals(mAuth.getCurrentUser().getUid()) ) {
                    if (databaseGroup.members != null) {
                        for (Map.Entry m : databaseGroup.members.entrySet()) {
                            Member databaseMember = (Member)m.getValue();
                            databaseMember.uid = (String)m.getKey();

                            DatabaseReference groupRef =  mRootReference.child("users").child(databaseMember.uid).child("group");
                            groupRef.removeValue();
                        }
                    }

                    groups.remove(g.getKey());

                    DatabaseReference groupsRef =  mRootReference.child("groups");
                    groupsRef.setValue(groups);

                    return true;
                }
            }
        }

        return false;
    }

    static boolean updateCategory(HashMap<String, Group> groups, String name, Category category) {
        String myGroupName = getGroupName(groups);

        if (groups != null) {
            for (Map.Entry g : groups.entrySet()) {
                if (((Group) g.getValue()).name.equals(myGroupName)) {
                    if (((Group) g.getValue()).categories != null) {
                        for (Map.Entry m : ((Group) g.getValue()).categories.entrySet()) {
                            if (m.getKey().equals(name)) {

                                //((Category) m.getValue()).name = category.name;
                                ((Category)m.getValue()).limit = category.limit;
                                //((Category)m.getValue()).displayName= category.displayName;

                                // Add groups node
                                DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
                                DatabaseReference groupsRef = mRootReference.child("groups");
                                groupsRef.setValue(groups);
                                return true;

                            }
                        }
                    }
                }
            }
        }

        return false;
    }


    static Map<String, Member> getMembers(HashMap<String, Group> groups)
    {
        String myGroupName = GroupsHelper.getGroupName(groups);

        if (groups != null) {
            for (Map.Entry g : groups.entrySet()) {
                if (((Group)g.getValue()).name.equals(myGroupName) ) {
                    return ((Group)g.getValue()).members;
                }
            }
        }

        return new HashMap<String, Member>();
    }

    static boolean removeMember(HashMap<String, Group> groups, String uid)
    {
        Group groupContainingMember = null;
        String memberToRemove = null;

        // we don't want to remove member who is also the admin
        if (getGroupAdmin(groups).equals(uid) )
        {
            return false;
        }

        if (groups != null) {
            for (Map.Entry g : groups.entrySet())
            {
                if (((Group)g.getValue()).members != null) {
                    for (Map.Entry m : ((Group)g.getValue()).members.entrySet())
                    {
                        if (m.getKey().equals(uid)) {
                            groupContainingMember = ((Group)g.getValue());
                            memberToRemove = m.getKey().toString();
                        }
                    }
                }
            }
        }

        if (groupContainingMember != null && memberToRemove != null)
        {
            groupContainingMember.members.remove(memberToRemove);

            // Add groups node
            DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference groupsRef =  mRootReference.child("groups");
            groupsRef.setValue(groups);

            DatabaseReference groupRef = mRootReference.child("users").child(uid).child("group");
            groupRef.removeValue();

            DatabaseReference categoryRef = mRootReference.child("groups").child(groupContainingMember.token).child("categories").child(uid);
            categoryRef.removeValue();

            return true;
        }

        return false;
    }


    static Map<String, Category> getCategories(HashMap<String, Group> groups)
    {
        String myGroupName = GroupsHelper.getGroupName(groups);

        if (groups != null) {
            for (Map.Entry g : groups.entrySet()) {
                if (((Group)g.getValue()).name.equals(myGroupName) ) {
                    return ((Group)g.getValue()).categories;
                }
            }
        }

        return new HashMap<String, Category>();
    }

    static Category getCategory(HashMap<String, Group> groups, String name)
    {
        String myGroupName = GroupsHelper.getGroupName(groups);

        if (groups != null) {
            for (Map.Entry g : groups.entrySet()) {
                if (((Group)g.getValue()).name.equals(myGroupName) ) {
                    if(((Group) g.getValue()).categories != null) {
                        for (Map.Entry m : ((Group) g.getValue()).categories.entrySet()) {
                            //if (m.getKey().equals(name)) {
                            if ( ((Category)m.getValue()).displayName.equals(name) ) {
                                return ((Category) m.getValue());
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    static void addCategory(HashMap<String, Group> groups, String name)
    {
        String myGroupName = GroupsHelper.getGroupName(groups);

        if (groups != null) {
            for (Map.Entry g : groups.entrySet()) {
                if (((Group)g.getValue()).name.equals(myGroupName) ) {
                    ((Group) g.getValue()).categories.put(name, new Category(name, name, -1.0));

                    // Add groups node
                    DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference groupsRef =  mRootReference.child("groups");
                    groupsRef.setValue(groups);
                }
            }
        }
    }

    static boolean removeCategory(HashMap<String, Group> groups, String category)
    {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser().getUid().equals(category)) {
            // Don't delete category for current user
            return false;
        }

        String myGroupName = GroupsHelper.getGroupName(groups);

        // Not an admin? see if member of a group
        Group groupContainingMember = null;
        String memberToRemove = null;
        if (groups != null) {
            for (Map.Entry g : groups.entrySet()) {
                if (((Group)g.getValue()).name.equals(myGroupName) ) {
                    if (((Group) g.getValue()).categories != null) {
                        for (Map.Entry m : ((Group) g.getValue()).categories.entrySet()) {
                            if (m.getKey().equals(category)) {
                                groupContainingMember = ((Group) g.getValue());
                                memberToRemove = m.getKey().toString();
                            }
                        }
                    }
                }
            }
        }

        if (groupContainingMember != null && memberToRemove != null) {
            groupContainingMember.categories.remove(memberToRemove);

            // Add groups node
            DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference groupsRef =  mRootReference.child("groups");
            groupsRef.setValue(groups);

            return true;
        }

        return false;
    }
}
