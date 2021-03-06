package com.scienjus.smartqq;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.DiscussMessage;
import com.scienjus.smartqq.model.Group;
import com.scienjus.smartqq.model.GroupInfo;
import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.GroupUser;
import com.scienjus.smartqq.model.Message;

public class Application {
	static SmartQQClient client;
	static Map<Long, Date> remindTimer = new HashMap<Long, Date>();
	static String watchedGroupName = "规则报表工作流交流群";//"计算机职业技术认证";
	static Group watchedGroup = null;
	static Map<Long, GroupUser> watchGroupUsers = new HashMap<Long, GroupUser>();
	static String rule1 = "所在地-中文公司名-呢称如[上海-锐道-小U]";
	static int timeSpanRemind = 10 * 1000;
    static Set<String> ignoreUsers = new HashSet<String>(); 
	public static void main(String[] args) {
//		ignoreUsers.add("开创未来");
		//ignoreUsers.add("QQ小冰");
		ignoreUsers.add("Jacky");
		//watchedGroupName = "实力天团@天生闪耀";
		// 创建一个新对象时需要扫描二维码登录，并且传一个处理接收到消息的回调，如果你不需要接收消息，可以传null
		final Queue<GroupMessage> msgQueue = new ConcurrentLinkedQueue<GroupMessage>();
		client = new SmartQQClient(new MessageCallback() {
			@Override
			public void onMessage(Message message) {
			}

			@Override
			public void onGroupMessage(GroupMessage message) {
				if(watchedGroup == null){
					return;
				}
				long gId = message.getGroupId();
				long watchId = watchedGroup.getId();
				if (gId == watchId) {
					System.out.println(message.getContent());
					msgQueue.add(message);
				}
			}

			@Override
			public void onDiscussMessage(DiscussMessage message) {
			}
		});

		// 登录成功后便可以编写你自己的业务逻辑了
		refreshWatchedGroupUsers();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					GroupMessage msg = msgQueue.poll();
					if (msg != null) {
						boolean send = checkAndSendWelcome(msg);
						if (!send) {
							refreshWatchedGroupUsers();
							checkAndRemindRename(msg);
						}
						msg = null;
					}
				}
			}
		}).start();
		// for(int i=0;i<10;i++)
		// client.sendMessageToGroup(gId2, "ai"+i+"...");
		// 使用后调用close方法关闭，你也可以使用try-with-resource创建该对象并自动关闭
		// try {
		// client.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	private static void refreshWatchedGroupUsers() {
		if(watchedGroup == null){
			List<Group> gList = null;
			try {
				gList = client.getGroupList();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (gList == null) {
				return;
			}
			for (Group g : gList) {
				String name = g.getName();
				if (watchedGroupName.equals(name)) {
					watchedGroup = g;
					break;
				}
			}
		}
		GroupInfo gInfo = client.getGroupInfo(watchedGroup.getCode());
		List<GroupUser> gUsers = gInfo.getUsers();
		for (GroupUser gu : gUsers) {
			watchGroupUsers.put(gu.getUin(), gu);
		}
	}

	public static boolean checkAndSendWelcome(GroupMessage msg) {
		String content = msg.getContent();
		if (content != null && content.contains("大家好，我是")) {
			String name = content.substring(content.indexOf("大家好，我是") + 6, content.indexOf("。"));
			String welcomeMsg = "@" + name + "欢迎入圈，请修改群名片，否则小U会生气的，小U生气不要紧，关键是群主可能也会很生气哦，那样事情就严重了，他会不回答你问题的，快改名吧格式：" + rule1 + "！";
			System.out.println(">>>>>>>>>"+welcomeMsg);
			//client.sendMessageToGroup(msg.getGroupId(), welcomeMsg);
			return true;
		}
		return false;
	}

	public static void checkAndRemindRename(GroupMessage msg) {
		GroupUser gUser = watchGroupUsers.get(msg.getUserId());
		long uin = gUser.getUin();
		if (!ifNeedRemindTimer(uin)) {
			return;
		}
		String content = msg.getContent();
		String nick = gUser.getNick();
		String card = gUser.getCard();
		if(ignoreUsers.contains(nick)){
			return;
		}
		System.out.println(card+"," +nick + ">" + content + "," + msg.getUserId() + ","+ uin);
		boolean right = false;
		if (card != null) {
			String array[] = card.split("-");
			if (array.length >= 3) {
				right = true;
			}
		}
		if (!right) {
			String array[] = nick.split("-");
			if (array.length >= 3) {
				right = true;
			}
		}
		if (!right) {
			String remindMsg = "@"+(card == null? nick : card) + "：小U发现您的群名片不合规，请修改一下吧，格式为：" + rule1 + "！";
			//client.sendMessageToGroup(msg.getGroupId(), remindMsg);
			System.out.println(">>>>>>>>>>>"+remindMsg);
			remindTimer.put(gUser.getUin(), new Date());
		}
	}

	public static boolean ifNeedRemindTimer(Long uin) {
		remindTimer.get(uin);
		if (!remindTimer.containsKey(uin)) {
			return true;
		} else {
			Date last = remindTimer.get(uin);
			Date now = new Date();
			long span = now.getTime() - last.getTime();
			if (span >= timeSpanRemind) {
				return true;
			}
		}
		return false;
	}
}
