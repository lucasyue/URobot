package com;

import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import com.urule.URuleUtil;

@Component
public class UbEngine {
	private Logger logger = LoggerFactory.getLogger(UbEngine.class);
	private SmartQQClient client;
	private Map<Long, Date> remindTimer = new HashMap<Long, Date>();
	@Value("${lk.groupName}")
	private String watchedGroupName;
	@Value("${lk.cardRule}")
	private String cardRule = null;
	@Value("${lk.myNick}")
	private String myNick = null;
	@Value("${lk.welcomeMsg}")
	private String welcomeMsg = null;
	@Value("${lk.remindMsg}")
	private String remindMsg = null;
	@Value("${lk.ingoreNickList}")
	private String ingoreNickList = null;
	@Value("${lk.defaultBack1}")
	private String defaultBack1 = null;
	@Value("${lk.defaultBack2}")
	private String defaultBack2 = null;

	private Set<String> ignoreUsers = new HashSet<String>();
	private Group watchedGroup = null;
	private boolean hasGetWatchedGroupUsers = false;
	private int timeSpanRemind = 60 * 1000;// 提醒改名的时间间隔
	private Map<Long, GroupUser> watchGroupUsers = new HashMap<Long, GroupUser>();
	static Map<String, Integer> talkMap = new ConcurrentHashMap<String, Integer>();

	public void start(String[] args) {
		System.out.println(watchedGroupName + " " + cardRule);
		if (ingoreNickList != null) {
			String[] list = ingoreNickList.split(",");
			for (String n : list) {
				ignoreUsers.add(n);
			}
		}
		// watchedGroupName = "实力天团@天生闪耀";
		// 创建一个新对象时需要扫描二维码登录，并且传一个处理接收到消息的回调，如果你不需要接收消息，可以传null
		final Queue<GroupMessage> msgQueue = new ConcurrentLinkedQueue<GroupMessage>();// 接收消息队列
		MyTimer timer = new MyTimer();// 计时器
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
						try {
							boolean send = checkAndRemindRename(msg);
							if (!send) {
								talk(msg);
							}
						} catch (Exception e) {
							if (e instanceof SocketTimeoutException || e.getCause() instanceof SocketTimeoutException) {
								logger.error("SocketTimeoutException");
							} else {
								logger.error("error-checkAndRemindRename", e);
							}
						}
						msg = null;
						timer.restart();
					}
					if (timer.isTimeout()) {// 刷新用户列表，欢迎功能
						refreshWatchedGroupUsers();
					}
				}
			}
		}).start();
		// 使用后调用close方法关闭，你也可以使用try-with-resource创建该对象并自动关闭
		// client.close();
	}

	private void refreshWatchedGroupUsers() {
		if (watchedGroup == null) {
			List<Group> gList = null;
			try {
				client.pausePoll();
				gList = client.getGroupList();
			} catch (Exception e) {
				client.startPoll();
				if (e instanceof SocketTimeoutException || e.getCause() instanceof SocketTimeoutException) {
					logger.error("SocketTimeoutException");
				} else {
					logger.error("error-getGroupList", e);
				}
				return;
			}
			client.startPoll();
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
		if (watchedGroup == null) {
			return;
		}
		try {
			GroupInfo gInfo = client.getGroupInfo(watchedGroup.getCode());
			List<GroupUser> gUsers = gInfo.getUsers();
			for (GroupUser gu : gUsers) {
				if (ifSendWelcome(gu)) {
					sendWelcome(gu);
				}
				watchGroupUsers.put(gu.getUin(), gu);
			}
			hasGetWatchedGroupUsers = true;
		} catch (Exception e) {
			if (e instanceof SocketTimeoutException || e.getCause() instanceof SocketTimeoutException) {
				logger.error("SocketTimeoutException");
			} else {
				logger.error("error-getGroupInfo", e);
			}
		}
		System.out.println("目前群总人数" + watchGroupUsers.size());
	}

	public void sendWelcome(GroupUser gUser) {
		String welcomeMsg1 = "@" + gUser.getNick() + welcomeMsg;
		System.out.println(">>>>" + welcomeMsg1);
		logger.info(welcomeMsg1);
		RemindLogger.traceMessage(welcomeMsg1);
		client.sendMessageToGroup(watchedGroup.getId(), welcomeMsg1);
	}

	private Set<Long> hasSendWelcomeUsers = new HashSet<Long>();

	private boolean ifSendWelcome(GroupUser gUser) {
		if (!hasGetWatchedGroupUsers) {
			return false;
		}
		if (watchGroupUsers.containsKey(gUser.getUin())) {// 非新增成员
			return false;
		} else if (hasSendWelcomeUsers.contains(gUser.getUin())) {
			return false;
		} else {
			hasSendWelcomeUsers.add(gUser.getUin());
			return true;
		}
	}

	public boolean checkAndRemindRename(GroupMessage msg) {
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
			//should get nick and check again
			String remindMsg1 = "@" + (card == null ? nick : card) + "：" + remindMsg;
			client.sendMessageToGroup(msg.getGroupId(), remindMsg1);
			RemindLogger.traceMessage(remindMsg1);
			remindTimer.put(gUser.getUin(), new Date());
			return true;
		}
		return false;
	}

	public void talk(GroupMessage msg) {
		GroupUser gUser = watchGroupUsers.get(msg.getUserId());
		long uin = gUser.getUin();
		String content = msg.getContent();
		String nick = gUser.getNick();
		String card = gUser.getCard();

		if (content.contains("@" + myNick)) {
			Map<String, Object> params = new HashMap<String, Object>();

			Integer count = talkMap.get(nick);
			if (count == null) {
				count = 0;
			}
			params.put("content", content);
			params.put("count", count);
			Map<String, Object> rs = URuleUtil.getAnswer(params);
			String back = (String) rs.get("back");
			String talkBack = card == null ? nick : card;
			String talk = "@" + talkBack + "，";
			if (back == null) {
				back = defaultBack1;
			}
			if (count == 1) {
				talk = defaultBack2;
			}
			talk += back;
			count++;
			talkMap.put(talkBack, count);
			client.sendMessageToGroup(msg.getGroupId(), talk);
			TalkLogger.traceMessage(talk);
		}
		System.out.println(">>talk>>" + card + "," + nick + ">" + content + "," + msg.getUserId() + "," + uin);
	}

	public boolean ifNeedRemindTimer(Long uin) {
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
