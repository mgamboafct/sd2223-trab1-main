package sd2223.trab1.clients.rest;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;

import java.net.URI;
import java.util.List;

public class RestFeedsClient extends RestClient implements Feeds {

    final WebTarget target;

    public RestFeedsClient( URI serverURI ) {
        super( serverURI );
        target = client.target( serverURI ).path( FeedsService.PATH );
    }

    public Result<Long> clt_postMessage(String user, String pwd, Message msg) {
        Response r = target.path(user)
                .queryParam(UsersService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(msg, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, Long.class);
    }

    public Result<Void> clt_removeFromPersonalFeed(String user, long mid, String pwd) {
        Response r = target.path(user).path(Long.toString(mid))
                .queryParam(UsersService.PWD, pwd).request()
                .delete();

        return super.toJavaResult(r, Void.class);
    }

    public Result<Message> clt_getMessage(String user, long mid) {
        Response r = target.path(user).path(Long.toString(mid)).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, Message.class);
    }

    public Result<List<Message>> clt_getMessages(String user, long time) {
        Response r = target.path(user)
                .queryParam(FeedsService.TIME, Long.toString(time)).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<List<Message>>() {});
    }

    public Result<Void> clt_subUser(String user, String userSub, String pwd) {
        Response r = target.path("sub").path(user).path(userSub)
                .queryParam(FeedsService.PWD, pwd).request()
                .post(null);

        return super.toJavaResult(r, Void.class);
    }

    public Result<Void> clt_unsubscribeUser(String user, String userSub, String pwd) {
        Response r = target.path("sub").path(user).path(userSub)
                .queryParam(FeedsService.PWD, pwd).request()
                .delete();

        return super.toJavaResult(r, Void.class);
    }

    public Result<List<String>> clt_listSubs(String user) {
        Response r = target.path("sub").path("list").path(user).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<List<String>>() {});
    }

    public Result<Message> clt_getUserMessage(String user, long mid) {
        Response r = target.path(user).path("own").path(Long.toString(mid)).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, Message.class);
    }

    public Result<List<Message>> clt_getUserMessages(String user, long time) {
        Response r = target.path(user).path("own")
                .queryParam(FeedsService.TIME, Long.toString(time)).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, new GenericType<List<Message>>() {});
    }

    public Result<Void> clt_createFeed(String user) {
        Response r = target.request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, Void.class);
    }

    public Result<Void> clt_deleteFeed(String user) {
        Response r = target.path(user).request()
                .delete();

        return super.toJavaResult(r, Void.class);
    }

    public Result<Long> clt_userLastUpdate(String user) {
        Response r = target.path(user).path("updatetime").request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, Long.class);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        return super.reTry(() -> clt_postMessage(user, pwd, msg));
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return super.reTry(() -> clt_removeFromPersonalFeed(user, mid, pwd));
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return super.reTry(() -> clt_getMessage(user, mid));
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        return super.reTry(() -> clt_getMessages(user, time));
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return super.reTry(() -> clt_subUser(user, userSub, pwd));
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return super.reTry(() -> clt_unsubscribeUser(user, userSub, pwd));
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return super.reTry(() -> clt_listSubs(user));
    }

    @Override
    public Result<Message> getUserMessage(String user, long mid) {
        return super.reTry(() -> clt_getUserMessage(user, mid));
    }

    @Override
    public Result<List<Message>> getUserMessages(String user, long time) {
        return super.reTry(() -> clt_getUserMessages(user, time));
    }

    @Override
    public Result<Void> createFeed(String user) {
        return super.reTry(() -> clt_createFeed(user));
    }

    @Override
    public Result<Void> deleteFeed(String user) {
        return super.reTry(() -> clt_deleteFeed(user));
    }

    @Override
    public Result<Long> userLastUpdate(String user) {
        return super.reTry(() -> clt_userLastUpdate(user));
    }
}
