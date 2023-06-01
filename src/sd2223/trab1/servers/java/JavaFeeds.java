package sd2223.trab1.servers.java;

import sd2223.trab1.api.Feed;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.FeedsClientFactory;
import sd2223.trab1.clients.UsersClientFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class JavaFeeds implements Feeds{
    private final ConcurrentHashMap<String, Feed> feeds = new ConcurrentHashMap<>();

    private static Logger Log = Logger.getLogger(JavaFeeds.class.getName());

    private final String domain;
    private final long base;

    private final AtomicLong msgNum = new AtomicLong(0);

    public JavaFeeds(String domain, long base) {
        this.domain = domain;
        this.base = base;
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        Log.info("postMessage to user feed : " + user);

        if(user == null || pwd == null || msg == null) {
            Log.info("Invalid parameter");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        String name = user.split("@")[0];
        String domain = user.split("@")[1];

        //Check if user domain equals feeds domain
        if(!domain.equals(this.domain)) {
            Log.info("User domain does not match FeedsService domain");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Users usersService = UsersClientFactory.get(domain);
        Result<User> result = usersService.getUser(name, pwd);

        // Check if user exists and password is correct
        if(!result.isOK()) {
            Log.info("User not found or incorrect password");
            return Result.error(result.error());
        }

        // Assign a unique ID to the message
        msg.setId(msgNum.getAndIncrement() + base * 1024);
        // Post message to user feed
        Feed feed = feeds.get(name);
        feed.post(msg);
        feeds.put(name, feed);

        return Result.ok(msg.getId());
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        Log.info("removeFromPersonalFeed : " + mid + " in users feed : " + user);

        String name = user.split("@")[0];

        Feed feed = feeds.get(name);

        // Check if feed exists (which also means user exists)
        if (feed == null) {
            Log.info("User not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Attempt to remove message from user feed
        if (!feed.removeMessage(mid)) {
            Log.info("Message not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok();
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Log.info("getMessage : " + mid + " in users feed : " + user);

        String name = user.split("@")[0];
        String domain = user.split("@")[1];

        Feeds feedsService;
        Feed feed = feeds.get(name);
        Result<Message> result = Result.error(Result.ErrorCode.NOT_FOUND);

        // Feed is remote
        if(feed == null && !domain.equals(this.domain)) {
            feedsService = FeedsClientFactory.get(domain);
            result = feedsService.getUserMessage(user, mid);
        }
        // Feed is local
        else if(feed != null) {
            Message message = feed.getMessage(mid);
            // Message is from users own messages
            if (message != null) {
                Log.info("Message found in user feed");
                return Result.ok(message);
            }
            // Message is from another users feed
            else {
                message = feed.getCacheMessage(mid);
                // Message exists in users cache
                if( message != null) {
                    String nameSub = message.getUser();
                    String domainSub = message.getDomain();
                    feedsService = FeedsClientFactory.get(domainSub);
                    Result<Long> received = feedsService.userLastUpdate(nameSub + "@" + domainSub);
                    // If message owner no longer exists
                    if(!received.isOK()) {
                        feed.unsubscribe(nameSub);
                        return Result.error(Result.ErrorCode.NOT_FOUND);
                    }
                    // Check if cache is up-to-date
                    else if (received.value() <= feed.getCacheTime()) {
                        return Result.ok(message);
                    }
                }
                // Search for message is subscribed users feed
                List<String> subscribed = feed.listSubscribed();
                for (String sub : subscribed) {
                    String domainSub = sub.split("@")[1];
                    // Search in local user feed
                    if (domainSub.equals(this.domain)) {
                        result = getUserMessage(sub, mid);
                    }
                    // Search in remote users feed
                    else {
                        feedsService = FeedsClientFactory.get(domainSub);
                        result = feedsService.getUserMessage(sub, mid);
                    }
                    // Message is from subscribed users feed
                    if (result.isOK()) {
                        Log.info("Message found in subscribed user feed");
                        return result;
                    }
                }
            }
        }

        Log.info("Message not found");
        return result;
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        Log.info("getMessages with time : " + time + " in users feed : " + user);

        String name = user.split("@")[0];
        String domain = user.split("@")[1];

        Feeds feedsService;
        List<Message> messages;
        Feed feed = feeds.get(name);

        // Feed is remote
        if(feed == null && !domain.equals(this.domain)) {
            feedsService = FeedsClientFactory.get(domain);
            messages = feedsService.getMessages(user, time).value();
        }
        // Feed is local
        else if(feed != null) {
            // Get users feed
            messages = feed.getMessages(time);
            // Get subscribed users feed
            List<String> subscribed = feed.listSubscribed();
            for (String sub : subscribed) {
                String nameSub = sub.split("@")[0];
                String domainSub = sub.split("@")[1];
                // Get local user feed
                if (domainSub.equals(this.domain)) {
                    messages.addAll(getUserMessages(sub, time).value());
                }
                // Get remote user feed
                else {
                    feedsService = FeedsClientFactory.get(domainSub);
                    Result<Long> received = feedsService.userLastUpdate(sub);
                    // If subscribed user no longer exists
                    if(!received.isOK()) {
                        feed.unsubscribe(nameSub);
                    }
                    // Update cache if it's not up-to-date
                    else if (received.value() > feed.getCacheTime()) {
                        List<Message> update = feedsService.getUserMessages(sub, 0).value();
                        feed.updateCache(nameSub, update);
                    }
                    // Add subscribed users cached messages to messages retrieved list
                    messages.addAll(feed.getCacheMessages(nameSub, time));
                }
            }
        } else {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok(messages);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        Log.info("subUser user : " + user + " to : " + userSub);

        String name = user.split("@")[0];
        String domain = user.split("@")[1];

        Users usersService = UsersClientFactory.get(domain);
        Result<User> result = usersService.getUser(name, pwd);

        // Check if user exists and password ir correct
        if(!result.isOK()) {
            Log.info("User could not be found or password incorrect");
            return Result.error(result.error());
        }

        // Subscribe user to userSub
        Feed feed = feeds.get(name);
        feed.subscribe(userSub);
        feeds.put(name, feed);

        Log.info("User : " + user + " subscribed to user : " + userSub);
        return Result.ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        Log.info("unsubscribeUser : " + user + " from userSub : " + userSub);

        String name = user.split("@")[0];
        String domain = user.split("@")[1];

        Users usersService = UsersClientFactory.get(domain);
        Result<User> result = usersService.getUser(name, pwd);

        // Check if user exists and password ir correct
        if(!result.isOK()) {
            Log.info("User could not be found or password incorrect");
            return Result.error(result.error());
        }

        // Unsubscribe user from userSub
        Feed feed = feeds.get(name);
        feed.unsubscribe(userSub);
        feeds.put(name,feed);

        Log.info("User : " + user + " unsubscribed from user : " + userSub );
        return Result.ok();
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        Log.info("listSubs from user : " + user);

        String name = user.split("@")[0];

        // Check if user exists
        Feed feed = feeds.get(name);
        if(feed == null) {
            Log.info("User not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok(feed.listSubscribed());
    }

    @Override
    public Result<Message> getUserMessage(String user, long mid) {
        Log.info("getUserMessage : " + mid + " in users feed : " + user);

        String name = user.split("@")[0];

        // Check if user exists
        Feed feed = feeds.get(name);
        if(feed == null) {
            Log.info("User not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Attempt to retrieve message from users own messages
        Message message = feed.getMessage(mid);
        if(message == null) {
            Log.info("Message not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        return Result.ok(message);
    }

    @Override
    public Result<List<Message>> getUserMessages(String user, long time) {
        Log.info("getUserMessages with time : " + time + " in users feed : " + user);

        String name = user.split("@")[0];

        // Check if user exists
        Feed feed = feeds.get(name);
        if(feed == null) {
            Log.info("User not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Get users own messages
        List<Message> messages = feed.getMessages(time);

        return Result.ok(messages);
    }

    @Override
    public Result<Void> createFeed(String user) {
        Log.info("createFeed for user : " + user);

        String name = user.split("@")[0];

        synchronized (feeds) {
            Feed feed = new Feed();
            feeds.put(name, feed);

            return Result.ok();
        }
    }

    @Override
    public Result<Void> deleteFeed(String user) {
        Log.info("deleteFeed from user : " + user);

        String name = user.split("@")[0];

        synchronized (feeds) {
            feeds.remove(name);

            return Result.ok();
        }
    }

    @Override
    public Result<Long> userLastUpdate(String user) {
        Log.info("userLastUpdate : " + user);

        String name = user.split("@")[0];

        Feed feed = feeds.get(name);

        return Result.ok(feed.getUpdateTime());
    }


}
