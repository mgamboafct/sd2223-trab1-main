package sd2223.trab1.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Feed {
    private final List<Message> messages = new CopyOnWriteArrayList<>(); // List of users feed
    private final List<String> subscribed = new CopyOnWriteArrayList<>(); // List of subscribed users
    private final List<Message> cache = new CopyOnWriteArrayList<>(); // Cached feeds from remote subscribed users
    private long cacheTime = 0; // Cache last update time
    private long updateTime = 0; // User feed last update time

    public Feed() { }

    public synchronized void post(Message msg) {
        messages.add(msg);
        updateTime = System.currentTimeMillis();
    }

    public Message getMessage(long mid) {
        for (Message message : messages) {
            if (message.getId() == mid) {
                return message;
            }
        }
        return null;
    }

    public List<Message> getMessages(long time) {
        List<Message> result = new ArrayList<>();
        for (Message message : messages) {
            if (message.getCreationTime() > time) {
                result.add(message);
            }
        }
        return result;
    }

    public synchronized boolean removeMessage(long mid) {
        updateTime = System.currentTimeMillis();
        return messages.removeIf(message -> message.getId() == mid);
    }

    public synchronized void subscribe(String user) {
        if(!subscribed.contains(user)) {
            subscribed.add(user);
        }
        // Invalidate cache
        cacheTime = 0;
    }

    public synchronized void unsubscribe(String user) {
        subscribed.remove(user);
        cache.removeIf(message -> message.getUser().equals(user));
    }
    public List<String> listSubscribed() {
        return subscribed;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public Message getCacheMessage(long mid) {
        for (Message message : cache) {
            if (message.getId() == mid) {
                return message;
            }
        }
        return null;
    }

    public List<Message> getCacheMessages(String user, long time) {
        List<Message> result = new ArrayList<>();
        for (Message message : cache) {
            if (message.getUser().equals(user) && message.getCreationTime() > time) {
                result.add(message);
            }
        }
        return result;
    }

    public synchronized void updateCache(String user, List<Message> update) {
        // Remove all messages from user from cache
        cache.removeIf(message -> message.getUser().equals(user));
        // Add update to cache
        cache.addAll(update);
        // Update caches last update time
        cacheTime = System.currentTimeMillis();
    }

    public List<Message> getCache() {
        return cache;
    }


}
