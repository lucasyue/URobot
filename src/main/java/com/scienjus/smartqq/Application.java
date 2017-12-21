package com.scienjus.smartqq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.Category;
import com.scienjus.smartqq.model.DiscussMessage;
import com.scienjus.smartqq.model.Friend;
import com.scienjus.smartqq.model.Group;
import com.scienjus.smartqq.model.GroupInfo;
import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.GroupUser;
import com.scienjus.smartqq.model.Message;

/**
 * @author ScienJus
 * @date 2015/12/18.
 */
public class Application {

    public static void main(String[] args) {
        //创建一个新对象时需要扫描二维码登录，并且传一个处理接收到消息的回调，如果你不需要接收消息，可以传null
        Map<Long,String> gIdNames = new HashMap<Long,String>();
        Queue<GroupMessage> msgQueue = new ConcurrentLinkedQueue();
    	SmartQQClient client = new SmartQQClient(new MessageCallback() {
            @Override
            public void onMessage(Message message) {
            }

            @Override
            public void onGroupMessage(GroupMessage message) {
            	long gId = message.getGroupId();
            	String name = gIdNames.get(gId);
            	if("计算机职业技术认证".equals(name)){
            		System.out.println(message.getContent());
            		msgQueue.add(message);
            	} else if("计算机职业技术认证".equals(name)){
            		
            	}
            }

            @Override
            public void onDiscussMessage(DiscussMessage message) {
            }
        });
        //登录成功后便可以编写你自己的业务逻辑了
        List<Category> categories = client.getFriendListWithCategory();
        for (Category category : categories) {
            System.out.println(category.getName());
            for (Friend friend : category.getFriends()) {
                System.out.println("————" + friend.getNickname());
            }
        }
        Map<Long,String> userNames = new HashMap<Long,String>();

        List<Group> gList = client.getGroupList();
        for(Group g:gList){
        	String name = g.getName();
        	if("Techzero 博客交流2".equals(name)){
        	} else if("实力天团@天生闪耀".equals(name)){
        	} else if("淘宝优惠券总群".equals(name)){
        	} else if("计算机职业技术认证".equals(name)){
        		gIdNames.put(g.getId(), name);
        		System.out.println(g.getId()+","+g.getCode());
        		GroupInfo gInfo = client.getGroupInfo(g.getCode());
        		List<GroupUser> gUsers = gInfo.getUsers();
        		for(GroupUser gu : gUsers){
        			gu.getCard();
        			userNames.put(gu.getUin(), gu.getNick());
        		}
        	}
        }
        
       new Thread(new Runnable() {
		
		@Override
		public void run() {
			 while(true){
		        	GroupMessage msg = msgQueue.poll();
		        	if(msg != null){
		        		String content = msg.getContent();
		        		System.out.println(userNames.get(msg.getUserId()) + ">" + content);
		        		if(content != null && content.contains("大家好，我是")){
		        			String name = content.substring(content.indexOf("大家好，我是")+6, content.indexOf("。"));
		        	        client.sendMessageToGroup(msg.getGroupId(), "欢迎"+ name +"入圈，请修改群名片，否则小E会生气的，小七生气不要紧，关键是群主可能也会很生气哦，他生气了事情就严重了，他会不回答你问题的，快改名吧格式：xxx-xxx-xxx！");
		        		}
		        	}
		     }			
		}
	}).start();;
//        long gId = 0L;
//        long gId2 = 0L;
//        long gId3 = 0L;
      //  client.sendMessageToGroup(gId3, "哈哈");
        //for(int i=0;i<10;i++)
        //client.sendMessageToGroup(gId2, "ai"+i+"...");
        //使用后调用close方法关闭，你也可以使用try-with-resource创建该对象并自动关闭
//        try {
//            client.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
