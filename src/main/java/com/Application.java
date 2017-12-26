package com;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.log.RemindLogger;
import com.log.TalkLogger;
import com.log.TraceLogger;
import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.DiscussMessage;
import com.scienjus.smartqq.model.Group;
import com.scienjus.smartqq.model.GroupInfo;
import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.GroupUser;
import com.scienjus.smartqq.model.Message;

@Component
public class Application {
	static Logger logger = LoggerFactory.getLogger(Application.class);
	static SmartQQClient client;
	static Map<Long, Date> remindTimer = new HashMap<Long, Date>();
	static String watchedGroupName;
	static String cardRule = null;
	static String myNick = null;
	static Group watchedGroup = null;
	static Map<Long, GroupUser> watchGroupUsers = new HashMap<Long, GroupUser>();
	static int timeSpanRemind = 60 * 1000;
	static Set<String> ignoreUsers = new HashSet<String>();

	public static void main(String[] args) {
		ClassPathResource yF = new ClassPathResource("application.properties");
		Properties p = new Properties();
		try {
			p.load(yF.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		watchedGroupName = p.getProperty("lk.groupName");
		cardRule = p.getProperty("lk.cardRule");
		myNick = p.getProperty("lk.myNick");
		System.out.println(cardRule);
		// ignoreUsers.add("开创未来");
		// ignoreUsers.add("QQ小冰");
		ignoreUsers.add("Jacky");
		// watchedGroupName = "实力天团@天生闪耀";
		// 创建一个新对象时需要扫描二维码登录，并且传一个处理接收到消息的回调，如果你不需要接收消息，可以传null
		final Queue<GroupMessage> msgQueue = new ConcurrentLinkedQueue<GroupMessage>();
		client = new SmartQQClient(new MessageCallback() {
			@Override
			public void onMessage(Message message) {
			}

			@Override
			public void onGroupMessage(GroupMessage message) {
				if (watchedGroup == null) {
					return;
				}
				long gId = message.getGroupId();
				long watchId = watchedGroup.getId();
				if (gId == watchId) {
					GroupUser gUser = watchGroupUsers.get(message.getUserId());
					TraceLogger.traceMessage(gUser, message);
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
							send = checkAndRemindRename(msg);
						}
						if (!send) {
							talk(msg);
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
		if (watchedGroup == null) {
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
			String welcomeMsg = "@" + name + "欢迎入圈，请修改群名片，否则小U会生气的，小U生气不要紧，关键是群主可能也会很生气哦，那样事情就严重了，他会不回答你问题的，快改名吧格式："
					+ cardRule + "！";
			System.out.println(">>>>>>>>>" + welcomeMsg);
			RemindLogger.traceMessage(welcomeMsg);
			client.sendMessageToGroup(msg.getGroupId(), welcomeMsg);
			return true;
		}
		return false;
	}

	public static boolean checkAndRemindRename(GroupMessage msg) {
		GroupUser gUser = watchGroupUsers.get(msg.getUserId());
		long uin = gUser.getUin();
		if (!ifNeedRemindTimer(uin)) {
			return false;
		}
		String content = msg.getContent();
		String nick = gUser.getNick();
		String card = gUser.getCard();
		if (ignoreUsers.contains(nick)) {
			return false;
		}
		System.out.println(card + "," + nick + ">" + content + "," + msg.getUserId() + "," + uin);
		boolean right = false;
		if (card != null) {
			String array[] = card.split("-");
			if (array.length >= 3 || (array.length == 2 && card.endsWith("-"))) {
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
			String remindMsg = "@" + (card == null ? nick : card) + "：小U发现您的群名片不合规，请修改一下吧，格式为：" + cardRule + "！";
			client.sendMessageToGroup(msg.getGroupId(), remindMsg);
			RemindLogger.traceMessage(remindMsg);
			remindTimer.put(gUser.getUin(), new Date());
			return true;
		}
		return false;
	}

	static Map<String, Integer> talkMap = new ConcurrentHashMap<String, Integer>();

	public static void talk(GroupMessage msg) {
		GroupUser gUser = watchGroupUsers.get(msg.getUserId());
		long uin = gUser.getUin();
		String content = msg.getContent();
		String nick = gUser.getNick();
		String card = gUser.getCard();
		if (content.startsWith("@"+myNick)) {
			String talk = "@" + card + "，你好呀，你可以撩我，但我不能撩你哦，不过我会记住你的，我有空回撩你，瓦哈哈！";
			Integer count = talkMap.get(nick);
			if (count != null) {
				if (count == 1) {
					talk = "@" + card + "，已经回复你了，不要撩了，好好上班干活吧，否则老板会神气的哦。";
				}
				count++;
			} else {
				count = 1;
			}
			talkMap.put(nick, count);
			if(count < 3){
				client.sendMessageToGroup(msg.getGroupId(), talk);
			}
			TalkLogger.traceMessage(talk);
		}
		System.out.println(card + "," + nick + ">" + content + "," + msg.getUserId() + "," + uin);
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
