package sd2223.trab1.servers.rest;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.servers.java.JavaFeeds;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class RestFeedsResource extends RestResource implements FeedsService {
    final Feeds impl;
    public RestFeedsResource(String domain, long base) {
        this.impl = new JavaFeeds(domain, base);
    }

    @Override
    public long postMessage(String user, String pwd, Message msg) {
        return super.fromJavaResult( impl.postMessage(user, pwd, msg));
    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {
        super.fromJavaResult( impl.removeFromPersonalFeed(user, mid, pwd));
    }

    @Override
    public Message getMessage(String user, long mid) {
        return super.fromJavaResult( impl.getMessage(user, mid));
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        return super.fromJavaResult( impl.getMessages(user, time));
    }

    @Override
    public void subUser(String user, String userSub, String pwd) {
        super.fromJavaResult( impl.subUser(user, userSub, pwd));
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {
        super.fromJavaResult( impl.unsubscribeUser(user, userSub, pwd));
    }

    @Override
    public List<String> listSubs(String user) {
        return super.fromJavaResult( impl.listSubs(user));
    }

    @Override
    public Message getUserMessage(String user, long mid) {
        return super.fromJavaResult( impl.getUserMessage(user, mid));
    }

    @Override
    public List<Message> getUserMessages(String user, long time) {
        return super.fromJavaResult( impl.getUserMessages(user, time));
    }

    @Override
    public void createFeed(String user) {
        super.fromJavaResult( impl.createFeed(user));
    }

    @Override
    public void deleteFeed(String user) {
        super.fromJavaResult( impl.deleteFeed(user));
    }

    @Override
    public long userLastUpdate(String user) {
        return super.fromJavaResult( impl.userLastUpdate(user));
    }
}
