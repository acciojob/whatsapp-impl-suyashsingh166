package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository() {
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        //User newUser = new User(name, mobile);
        if (!userMobile.contains(mobile)) {
            User user = new User(name, mobile);
            userMobile.add(mobile);
            return "SUCCESS";
        }
        return null;
    }

    public Group createGroup(List<User> users) {
        if (users.size() < 2) {
           return null;
        }
        if (users.size() == 2) {
            Group group = new Group( users.get(1).getName(),2);
            // 1st person will be the admin
            adminMap.put(group,users.get(0));

            // stores list of users in hashmap
            groupUserMap.put(group,users);
            groupMessageMap.put(group,new ArrayList<Message>());
            return group;
        }
        if(users.size() > 2) {
            customGroupCount += 1;
            Group group = new Group(new String("Group " + customGroupCount), users.size());
            adminMap.put(group,users.get(0));
            groupUserMap.put(group,users);
            return group;
        }
        return null;
    }

    public int createMessage(String content) {
        messageId++;
        Message message = new Message(messageId, content);
        //senderMap.put(message, null);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if (!groupUserMap.containsKey(group)) {
           return -1;
        }
        List<User> userList = groupUserMap.get(group);
        if (!userList.contains(sender)) {
           return -2;
        }
        if(groupUserMap.containsKey(group) && userList.contains(sender)){
            if(groupMessageMap.containsKey(group)){
                groupMessageMap.get(group).add(message);
            }
            else{
                List<Message> messageList = new ArrayList<>();
                messageList.add(message);
                groupMessageMap.put(group,messageList);
            }
        }
        senderMap.put(message, sender);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if (!groupUserMap.containsKey(group)) {
            return "Group does not exist.";
        }
        if (!adminMap.get(group).equals(approver)) {
            return "not the current admin";
        }
        List<User> userList = groupUserMap.get(group);
        if (!userList.contains(user)) {
             return "user is not a part of the group";
        }
        else
        adminMap.put(group, user);

        return "SUCCESS";
    }


    public int removeUser(User user) throws Exception {
          boolean flag = false;
          Group group = null;

          for(Group g : groupUserMap.keySet()){
              if(groupUserMap.get(g).contains(user)){
                  group = g;
                  flag = true;
                  break;
              }
          }
          if(flag == false){
              return -1;
          }
          if(flag == true){
            if( adminMap.containsKey(user)){
                 return -2;
              }
          }
          groupUserMap.get(group).remove(user);
          List<Message> msgList = new ArrayList<>();
          for(Message msg : senderMap.keySet()){
              if(senderMap.get(msg).equals(user)){
                  msgList.add(msg);
              }
          }
          for(Message msg : msgList){
              groupMessageMap.get(group).remove(msg);
              senderMap.remove(msg);
          }
          return group.getNumberOfParticipants() + groupMessageMap.get(group).size() + senderMap.size();
        }

    public  String findMessage(Date start, Date end, int K) throws Exception{
        int cnt = 0;
        List<Message> messages = new ArrayList<>();
        for (Message msg : senderMap.keySet()) {
            if (msg.getTimestamp().compareTo(start) > 0 && msg.getTimestamp().compareTo(end) < 0) {
                messages.add(msg);
                cnt++;
            }
        }

      if (messages.size() < K) {
            return "K is greater than the number of messages";
       }

      return messages.get(cnt - K).getContent();
    }
}
